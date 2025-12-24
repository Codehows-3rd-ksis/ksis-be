package com.codehows.ksisbe.scheduler.dto;

import com.codehows.ksisbe.scheduler.entity.Scheduler;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class SchedulerResponseDto {

    private Long scheduleId;
    private Long settingId;
    private String settingName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String cronExpression;
    private String daysOfWeek;
    private String weekOfMonth;

    public static SchedulerResponseDto from(Scheduler scheduler) {
        return SchedulerResponseDto.builder()
                .scheduleId(scheduler.getScheduleId())
                .settingId(scheduler.getSetting().getSettingId())
                .settingName(scheduler.getSetting().getSettingName())
                .startDate(scheduler.getStartDate())
                .endDate(scheduler.getEndDate())
                .cronExpression(scheduler.getCronExpression())
                .daysOfWeek(scheduler.getDaysOfWeek())
                .weekOfMonth(scheduler.getWeekOfMonth())
                .build();
    }
}
