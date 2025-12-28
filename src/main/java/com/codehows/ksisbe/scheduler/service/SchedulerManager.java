package com.codehows.ksisbe.scheduler.service;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Component
public class SchedulerManager {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final JobLauncher jobLauncher;
    private final Job crawlingJob;

    public SchedulerManager(
            @Qualifier("schedulerTaskScheduler")
            ThreadPoolTaskScheduler taskScheduler,
            JobLauncher jobLauncher,
            Job crawlingJob
    ) {
        this.taskScheduler = taskScheduler;
        this.jobLauncher = jobLauncher;
        this.crawlingJob = crawlingJob;
    }

    public void schedule(Scheduler scheduler) {

        log.info("스케줄 등록됨 schedulerId={}, cron={}",
                scheduler.getScheduleId(),
                scheduler.getCronExpression());

        if (!ScheduleValidator.isExecutable(scheduler)) {
            log.info("스케줄 실행 조건 불일치 - schedulerId={}",
                    scheduler.getScheduleId());
            return;
        }
        Runnable task = () -> {
            try {
                JobParameters params = new JobParametersBuilder()
                        .addLong("schedulerId", scheduler.getScheduleId())
                        .addLong("settingId", scheduler.getSetting().getSettingId())
                        .addLong("runTime", System.currentTimeMillis()) // 중복 방지
                        .toJobParameters();

                jobLauncher.run(crawlingJob, params);

            } catch (Exception e) {
                log.error("Batch job failed", e);
            }
        };

        taskScheduler.schedule(
                task,
                new CronTrigger(scheduler.getCronExpression())
        );
    }
}