package com.codehows.ksisbe.crawlSetting.dto;

import lombok.Data;

@Data
public class HighlightRequest {
    private String url;
    private String cssSelector;
}
