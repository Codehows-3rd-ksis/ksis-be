package com.codehows.ksisbe.scheduler.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SearchCondition {

    private String Type;   // all, settingName, cycle, time
    private String keyword;      // 검색어

    private LocalDate startDate; // 검색 시작일
    private LocalDate endDate;   // 검색 종료일
}
