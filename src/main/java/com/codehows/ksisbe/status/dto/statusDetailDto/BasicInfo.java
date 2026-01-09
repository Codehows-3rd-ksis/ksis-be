package com.codehows.ksisbe.status.dto.statusDetailDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class BasicInfo {
    private Long workId;
    private String settingName;
    private String type;
    private String userId;

    private LocalDate startDate;
    private LocalDate endDate;
    private String cycle;
    private String period;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private String state;
    private Double progressRate;
}
