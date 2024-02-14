package com.terralogic.hros.lms.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.backblaze.b2.client.exceptions.B2Exception;
import com.terralogic.hros.lms.exceptionHandling.NoResourceFound;
import com.terralogic.hros.lms.exceptionHandling.TranscodingException;

@Service
public class VideoTranscodingService {

	@Value("${ffmpeg.path}")
	private String ffmpegPath;
	private final Logger logger = LoggerFactory.getLogger(VideoTranscodingService.class);

	@Autowired
	private VideoService videoService;

	public void transcodeAndStoreVideos(byte[] videoData, String videoName, String bucketName)
			throws IOException, InterruptedException, InvalidKeyException, NoSuchAlgorithmException,
			B2Exception, NoResourceFound, TranscodingException {
		// Create a temporary directory to store the generated files
		Path tempDir = null;
		try {

			tempDir = Files.createTempDirectory("video_transcoding");
			// Define the temporary video file path
			Path tempVideoFile = tempDir.resolve("temp_video.mp4");

			// Save the video data to the temporary video file
			Files.write(tempVideoFile, videoData);

			// Create an executor service with a fixed number of threads
			int numThreads = 10; // Adjust this based on your system's capabilities
			ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

			String[] ffmpegCommands = {
					ffmpegPath + " -i " + tempVideoFile +
					" -vf \"scale=256:144\" -c:v libx264 -b:v 400k -c:a aac -b:a 128k -profile:v baseline -level 3 -preset ultrafast -g 48 -keyint_min 48 -sc_threshold 0 -use_template 1 -use_timeline 1 -init_seg_name init_144p.mp4 -media_seg_name chunk_144p_$Number$.m4s -single_file_name video_144p.mp4 " +
					tempDir.resolve("output_144p.mp4"),

					ffmpegPath + " -i " + tempVideoFile +
					" -vf \"scale=640:360\" -c:v libx264 -b:v 800k -c:a aac -b:a 128k -profile:v baseline -level 3 -preset ultrafast -g 48 -keyint_min 48 -sc_threshold 0 -use_template 1 -use_timeline 1 -init_seg_name init_360p.mp4 -media_seg_name chunk_360p_$Number$.m4s -single_file_name video_360p.mp4 " +
					tempDir.resolve("output_360p.mp4"),

					ffmpegPath + " -i " + tempVideoFile +
					" -vf \"scale=1280:720\" -c:v libx264 -b:v 1500k -c:a aac -b:a 192k -profile:v main -level 3.1 -preset ultrafast -g 48 -keyint_min 48 -sc_threshold 0 -use_template 1 -use_timeline 1 -init_seg_name init_720p.mp4 -media_seg_name chunk_720p_$Number$.m4s -single_file_name video_720p.mp4 " +
					tempDir.resolve("output_720p.mp4"),

					ffmpegPath + " -i " + tempVideoFile +
					" -vf \"scale=1920:1080\" -c:v libx264 -b:v 3000k -c:a aac -b:a 192k -profile:v high -level 4.0 -preset ultrafast -g 48 -keyint_min 48 -sc_threshold 0 -use_template 1 -use_timeline 1 -init_seg_name init_1080p.mp4 -media_seg_name chunk_1080p_$Number$.m4s -single_file_name video_1080p.mp4 " +
					tempDir.resolve("output_1080p.mp4")


			};
			

			// Execute FFmpeg commands concurrently
			for (String command : ffmpegCommands) {
				executorService.execute(() -> {
					try {
						videoService.executeFFmpegCommand(command);
						System.out.println("");
					} catch (IOException | InterruptedException |  TranscodingException e) {
                  //      throw new Exception("");
						//e.printStackTrace();


					}
				});
			}

			// Shutdown the executor and wait for all threads to complete
			executorService.shutdown();
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			// Execute the MP4Box command
            videoService.executeMP4BoxCommand("MP4Box -dash 4000 -rap -frag-rap -profile \"dashavc264:live\" -out " +
                    tempDir.resolve("video.mpd") + " " +
                    tempDir.resolve("output_144p.mp4") + " " +
                    tempDir.resolve("output_360p.mp4") + " " +
                    tempDir.resolve("output_720p.mp4") + " " +
                    tempDir.resolve("output_1080p.mp4"));
			logger.info("the ffmpeg command for ffmpeg started ");
			

			// Upload the transcoded video files with content type set
			// String bucketName = "videos2";
			String objectNamePrefix = "videos/" + videoName;
			logger.info("uploading started for dash");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_144p.mp4").getFileName(), tempDir.resolve("output_144p.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_360p.mp4").getFileName(), tempDir.resolve("output_360p.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_720p.mp4").getFileName(), tempDir.resolve("output_720p.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_1080p.mp4").getFileName(), tempDir.resolve("output_1080p.mp4").toFile(), "video/mp4");

			// Upload the MPD file with content type set
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("video.mpd").getFileName(), tempDir.resolve("video.mpd").toFile(), "application/dash+xml");

			// Upload the transcoded video files with content type set
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_144p_dashinit.mp4").getFileName(), tempDir.resolve("output_144p_dashinit.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_360p_dashinit.mp4").getFileName(), tempDir.resolve("output_360p_dashinit.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_720p_dashinit.mp4").getFileName(), tempDir.resolve("output_720p_dashinit.mp4").toFile(), "video/mp4");
			uploadFileWithContentType(bucketName, objectNamePrefix + "/" + tempDir.resolve("output_1080p_dashinit.mp4").getFileName(), tempDir.resolve("output_1080p_dashinit.mp4").toFile(), "video/mp4");

			// Check for chunk files in the temporary directory
			Files.list(tempDir)
			.filter(file -> Files.isRegularFile(file) && file.getFileName().toString().endsWith(".m4s"))
			.forEach(chunkFile -> {
				try {
					// Upload the chunk file with content type set to the "videos" folder
					uploadFileWithContentType(bucketName, objectNamePrefix + "/" + chunkFile.getFileName().toString(), chunkFile.toFile(), "video/mp4");


				} 
				catch (Exception e) {
					e.printStackTrace();
				}

			}) ;


			logger.info("Transcoding and backblaze upload completed successfully.");
		} catch (Exception e) {
			//	throw new TranscodingException("Transcoding failed due to " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			try {
				Files.walk(tempDir.toAbsolutePath())
				.sorted(Comparator.reverseOrder())  // Delete in reverse order (subdirectories first)
				.map(Path::toFile)
				.forEach(File::delete);
			} catch (IOException e) {
				logger.error("Error cleaning up temporary files:", e);
			}
		}

	}

	private void uploadFileWithContentType(String bucketName, String objectName, File file, String contentType)
			throws IOException,  InvalidKeyException, NoSuchAlgorithmException, B2Exception, NoResourceFound {
		// Set the content type for the object before uploading
		videoService.uploadVideo(bucketName, objectName, file, contentType);
	}


}

