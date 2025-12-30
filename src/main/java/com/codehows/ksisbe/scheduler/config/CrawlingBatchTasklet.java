package com.codehows.ksisbe.scheduler.config;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class CrawlingBatchTasklet implements Tasklet {

    private final CrawlingService crawlingService;

    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        var executionContext = contribution
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        Long schedulerId = executionContext.getLong("schedulerId");
        Long settingId   = executionContext.getLong("settingId");

        log.info("배치 크롤링 시작: schedulerId={}, settingId={}",
                schedulerId, settingId);

        crawlingService.startCrawlingBySchedule(schedulerId, settingId);
        return RepeatStatus.FINISHED;
    }
}