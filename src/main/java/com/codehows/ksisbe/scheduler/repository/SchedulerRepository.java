package com.codehows.ksisbe.scheduler.repository;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerRepository extends JpaRepository<Scheduler, Long> {
    
}
