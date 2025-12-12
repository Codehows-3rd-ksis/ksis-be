package com.codehows.ksisbe.crawling.asyncConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 기본 스레드 개수
        executor.setMaxPoolSize(10);  // 최대 스레드 개수
        executor.setQueueCapacity(25); // 대기 큐 크기
        executor.setThreadNamePrefix("CrawlingExecutor-");
        executor.initialize();
        return executor;
    }
}
