package com.learntv.api.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async processing.
 * Enables @Async annotation and configures thread pool for lesson generation.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {

    /**
     * Thread pool for async lesson generation.
     * Configured to handle multiple concurrent generation jobs.
     */
    @Bean(name = "lessonGenerationExecutor")
    public Executor lessonGenerationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("lesson-gen-");
        executor.initialize();
        return executor;
    }
}
