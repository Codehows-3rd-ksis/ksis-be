package com.codehows.ksisbe.status.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class StatusShowDto {
    private Long workId;
    private String settingName;
    private String userId;
    private String type;
    private LocalDateTime startAt;
//    private LocalDate startDate;
//    private LocalDate endDate;
    private String period;
    private String searchText;
    private Double progress;
}
