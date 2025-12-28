package com.codehows.ksisbe.scheduler.config;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingBatchTasklet implements Tasklet {

    private final CrawlingService crawlingService;

    @Override
    public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        JobParameters param =
                contribution.getStepExecution()
                            .getJobExecution()
                            .getJobParameters();

        Long schedulerId =  param.getLong("schedulerId");
        Long settingId = param.getLong("settingId");
//        String username =  param.getString("username");

        log.info("배치 크롤링 시작: schedulerId={}, settingId={}", schedulerId, settingId);

        if (schedulerId == null) {
            throw new IllegalStateException("schedulerId가 파라미터로 넘어오지 않음");
        }
        if (settingId == null) {
            throw new IllegalStateException("settingId가 파라미터로 넘어오지 않음");
        }
        crawlingService.startCrawlingBySchedule(schedulerId, settingId);
        return RepeatStatus.FINISHED;
    }
}
