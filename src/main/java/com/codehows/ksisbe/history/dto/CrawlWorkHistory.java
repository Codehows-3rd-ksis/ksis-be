package com.codehows.ksisbe.history.dto;

import lombok.*;

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
    private Long failCount;
    private String state;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
