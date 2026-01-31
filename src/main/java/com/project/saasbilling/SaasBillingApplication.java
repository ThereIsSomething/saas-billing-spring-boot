package com.project.saasbilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the SaaS Billing Platform.
 * 
 * Features enabled:
 * - MongoDB Auditing for automatic timestamp management
 * - Caching for performance optimization
 * - Async processing for email notifications
 * - Scheduling for recurring billing tasks
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class SaasBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasBillingApplication.class, args);
    }
}
