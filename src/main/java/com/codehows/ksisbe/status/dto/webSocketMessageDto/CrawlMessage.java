package com.codehows.ksisbe.status.dto.webSocketMessageDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CrawlMessage {
    private String type;   // "COLLECT_UPDATE"
    private Long workId;
    private CrawlProgressDto data;
    private CrawlResultItemDto crawlResultItem;
}
