//package com.codehows.ksisbe.scheduler.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.job.Job;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.Step;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class CrawlingJobConfig {
//
//    private final JobRepository jobRepository;
//    private final CrawlingBatchTasklet crawlingBatchTasklet;
//
//    @Bean
//    public Step crawlingStep() {
//        return new StepBuilder("crawlingStep", jobRepository)
//                .tasklet(crawlingBatchTasklet)
//                .build();
//    }
//
//    @Bean
//    public Job crawlingJob() {
//        return new JobBuilder("crawlingJob", jobRepository)
//                .start(crawlingStep())
//                .build();
//    }
//}