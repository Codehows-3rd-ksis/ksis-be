package com.codehows.ksisbe.scheduler.repository;

import com.codehows.ksisbe.scheduler.dto.SearchCondition;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends JpaRepository<Scheduler, Long> {
    Optional<Scheduler> findByScheduleIdAndIsDelete(Long scheduleId, String isDelete);
}
