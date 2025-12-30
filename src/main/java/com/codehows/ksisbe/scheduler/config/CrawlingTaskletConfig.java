package com.codehows.ksisbe.scheduler.config;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlingTaskletConfig {

    @Bean
    public CrawlingBatchTasklet crawlingBatchTasklet(
            CrawlingService crawlingService) {
        return new CrawlingBatchTasklet(crawlingService);
    }
}