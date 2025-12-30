package com.codehows.ksisbe.scheduler.config;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.scheduler.repository.SchedulerRepository;
import com.codehows.ksisbe.scheduler.service.SchedulerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerStartupLoader {

    private final SchedulerRepository schedulerRepository;
    private final SchedulerManager schedulerManager;

    @EventListener(ApplicationReadyEvent.class)
    public void loadSchedules() {

        List<Scheduler> schedulers =
                schedulerRepository.findAllByIsDelete("N");

        for (Scheduler scheduler : schedulers) {
            schedulerManager.schedule(scheduler);
        }
    }
}