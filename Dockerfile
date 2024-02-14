# GPAC Builder Stage
FROM alpine:3.6 AS gpac_builder
 
WORKDIR /app
RUN apk update && \
    apk add --no-cache \
        wget \
        g++ \
        make \
        && \
    wget --no-check-certificate https://codeload.github.com/gpac/gpac/zip/master -O gpac-master.zip && \
    unzip gpac-master.zip
 
WORKDIR gpac-master
RUN ./configure --use-zlib=no && \
    make && \
    mkdir -p install/bin && \
    cp -R ./bin/gcc ./install/lib && \
  #  rm ./install/lib/gm_* ./install/lib/*.a && \
    rm -Rf ./install/lib/temp && \
    mv ./install/lib/MP4* ./install/bin
 
# Build Stage
FROM maven:3.8.4-openjdk-17 AS build
 
# Copy project files
COPY src /home/app/src
COPY pom.xml /home/app
 
# Build the application
RUN mvn -f /home/app/pom.xml clean package
 
# Package Stage
#FROM alpine:3.6
#FROM alpine:latest
FROM openjdk:17-alpine
 
WORKDIR /app
COPY --from=gpac_builder /app/gpac-master/install/lib /usr/local/lib
COPY --from=gpac_builder /app/gpac-master/install/bin /usr/local/bin
COPY --from=build /home/app/target/*.jar /app/lms.jar
 
# Install FFmpeg (already available in Alpine)
RUN apk add ffmpeg
 
# Expose port (adjust if needed)
EXPOSE 8080
 
# Start the application
#ENTRYPOINT ["java","-jar","/usr/local/lib/lms.jar"]
ENTRYPOINT ["java", "-jar", "/app/lms.jar"]