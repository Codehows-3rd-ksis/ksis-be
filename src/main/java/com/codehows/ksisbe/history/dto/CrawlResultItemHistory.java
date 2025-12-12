package com.codehows.ksisbe.history.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResultItemHistory {
    private Long itemId;
    private Long workId;
    private String pageUrl;
    private String resultValue;
    private Long seq;
    private String state;
}
