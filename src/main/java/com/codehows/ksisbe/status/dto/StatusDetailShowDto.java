package com.codehows.ksisbe.status.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class StatusDetailShowDto {
    private Long workId;
    private String settingName;
    private String userId;
    private String type;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String cycle;
    private Double progress;
    private String state;
    private Integer failCount;
    private Integer collectCount;
    private Integer totalCount;
    private LocalDateTime expectEndAt;
    private List<StatusDetailItemShowDto> statusDetailShowDto;
}
