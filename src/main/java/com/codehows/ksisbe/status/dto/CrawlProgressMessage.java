package com.codehows.ksisbe.status.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CrawlProgressMessage {
    private Long workId;
    private Double progress;
}
