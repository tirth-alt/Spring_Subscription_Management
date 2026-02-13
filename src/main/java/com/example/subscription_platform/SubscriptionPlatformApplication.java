package com.example.subscription_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SubscriptionPlatformApplication {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionPlatformApplication.class);
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(SubscriptionPlatformApplication.class, args);
		String mongoUri = context.getEnvironment().getProperty("spring.data.mongodb.uri");
        log.info("MongoDB URI in use: {}", mongoUri);
	}

}
