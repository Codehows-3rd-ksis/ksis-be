package com.codehows.ksisbe.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCondition {
    private String type;
    private String keyword;

    private String startDate; // yyyy-MM-dd
    private String endDate;   // yyyy-MM-dd
}
