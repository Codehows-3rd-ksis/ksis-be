package com.codehows.ksisbe.scheduler.service;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchedulerManager {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final CrawlingService crawlingService;

    public SchedulerManager(
            @Qualifier("schedulerTaskScheduler")
            ThreadPoolTaskScheduler taskScheduler,
            CrawlingService crawlingService
    ) {
        this.taskScheduler = taskScheduler;
        this.crawlingService = crawlingService;
    }

    public void schedule(Scheduler scheduler) {

        if (!ScheduleValidator.isExecutable(scheduler)) {
            log.info("스케줄 실행 조건 불일치 - schedulerId={}",
                    scheduler.getScheduleId());
            return;
        }

        Runnable task = () -> {
            Long schedulerId = scheduler.getScheduleId();
            Long settingId   = scheduler.getSetting().getSettingId();

            log.info("크롤링 실행 schedulerId={}, settingId={}",
                    schedulerId, settingId);

            crawlingService.startCrawlingBySchedule(
                    schedulerId,
                    settingId
            );
        };

        taskScheduler.schedule(
                task,
                new CronTrigger(scheduler.getCronExpression())
        );
    }
}