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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class SchedulerManager {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final CrawlingService crawlingService;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public SchedulerManager(
            @Qualifier("schedulerTaskScheduler")
            ThreadPoolTaskScheduler taskScheduler,
            CrawlingService crawlingService
    ) {
        this.taskScheduler = taskScheduler;
        this.crawlingService = crawlingService;
    }

    public void schedule(Scheduler scheduler) {

        Long schedulerId = scheduler.getScheduleId();
        Long settingId   = scheduler.getSetting().getSettingId();

        Runnable task = () -> {


            if (!ScheduleValidator.isExecutable(scheduler)) {
                log.info("실행 조건 불일치 - schedulerId={}", schedulerId);
                return;
            }

            log.info("크롤링 실행 schedulerId={}, settingId={}", schedulerId, settingId);
            crawlingService.startCrawlingBySchedule(schedulerId, settingId);
        };

        ScheduledFuture<?> future =
                taskScheduler.schedule(task, new CronTrigger(scheduler.getCronExpression()));

        scheduledTasks.put(schedulerId, future);
    }
    public void cancel(Long schedulerId) {
        ScheduledFuture<?> future = scheduledTasks.remove(schedulerId);
        if (future != null) {
            future.cancel(false);
            log.info("스케줄 취소 - schedulerId={}", schedulerId);
        }
    }
}