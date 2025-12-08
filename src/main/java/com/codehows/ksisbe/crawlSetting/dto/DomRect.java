package com.codehows.ksisbe.crawlSetting.dto;

import lombok.Data;

@Data
public class DomRect {
    private String selector;
    private int x;
    private int y;
    private int width;
    private int height;
}