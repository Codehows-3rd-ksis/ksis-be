package com.codehows.ksisbe.history.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlWorkHistory {
    private Long workId;
    private Long settingId;
    private Long userId;
    private String username;
    private String settingName;
    private Long scheduleId;
    private Long startedBy;
    private Integer failCount;
    private String state;
    private String type;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private String period;
}
