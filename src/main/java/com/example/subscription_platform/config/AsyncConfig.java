package com.example.subscription_platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration to enable async method execution.
 * Required for @Async annotations in EmailService and NotificationService.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
