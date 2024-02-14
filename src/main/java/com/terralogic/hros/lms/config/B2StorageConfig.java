package com.terralogic.hros.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;

@Configuration
public class B2StorageConfig {

	@Value("${backblaze.b2.api.key}")
	private String APP_KEY_ID ;
	@Value("${backblaze.b2.application.key}")
	private String APP_KEY;

	@Value("${b2.userAgent}")
	private String userAgent;

	@Bean
	public B2StorageClient b2StorageClient() {

		return B2StorageClientFactory
				.createDefaultFactory()
				.create(APP_KEY_ID, APP_KEY, userAgent);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
