package com.codehows.ksisbe.crawlSetting.dto;

import lombok.Data;
import java.util.List;

@Data
public class PreviewResponse {
    private String html;
    private String imageBase64;
    private List<DomRect> domRects;
}