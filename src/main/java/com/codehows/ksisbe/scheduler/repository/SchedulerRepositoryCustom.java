package com.codehows.ksisbe.scheduler.repository;

import com.codehows.ksisbe.scheduler.dto.SearchCondition;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SchedulerRepositoryCustom {
    Page<Scheduler> search(Long id, String role, SearchCondition request, Pageable pageable);
}
