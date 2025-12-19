package com.codehows.ksisbe.status.dto.statusDetailDto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProgressInfo {
    private Integer totalCount;
    private Integer collectCount;
    private Integer failCount;
    private LocalDateTime expectEndAt;
}
