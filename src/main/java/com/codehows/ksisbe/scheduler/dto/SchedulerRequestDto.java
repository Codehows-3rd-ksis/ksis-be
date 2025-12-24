package com.codehows.ksisbe.scheduler.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerRequestDto {
    private Long settingId;
    private String cronExpression;
    private List<String> daysOfWeek;
    private String weekOfMonth;
    private LocalDate startDate;
    private LocalDate endDate;
}
