package com.codehows.ksisbe.scheduler.config;

import com.codehows.ksisbe.scheduler.repository.SchedulerRepository;
import com.codehows.ksisbe.scheduler.service.SchedulerManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchedulerInitializer {

    private final SchedulerRepository schedulerRepository;
    private final SchedulerManager schedulerManager;

    @PostConstruct
    public void init() {
        schedulerRepository.findAllByIsDelete("N")
                .forEach(schedulerManager::schedule);
    }
}
