package com.codehows.ksisbe.status.dto.webSocketMessageDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CrawlResultItemDto {
    private Long id;
    private Long seq;
    private Object resultValue;
    private String state;
    private String url;
}
