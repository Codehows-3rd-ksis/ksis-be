package com.codehows.ksisbe.scheduler.entity;

import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_id", nullable = false)
    private Setting setting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;

    @Column(name = "job_name", nullable = false, length = 20)
    private String jobName;

    @Column(name = "job_parameters", nullable = false, columnDefinition = "TEXT")
    private String jobParameters;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "is_delete", nullable = false, length = 1)
    private String isDelete = "N";

}
