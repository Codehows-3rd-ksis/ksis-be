package com.codehows.ksisbe.status.dto.webSocketMessageDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CrawlProgressDto {
    private Integer totalCount;
    private Integer collectCount;
    private Integer failCount;
    private Double progress;
    private LocalDateTime expectEndAt;
}
