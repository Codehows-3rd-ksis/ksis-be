package com.codehows.ksisbe.setting.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingUpdateDto {
    private String settingName;
    private String url;
    private String type;  // 단일/다중
    private String userAgent;
    private Integer rate;
    private String listArea;
    private String pagingType;
    private String pagingArea;
    private String pagingNextbtn;
    private Integer maxPage;
    private String linkArea;
    private List<ConditionsUpdateDto> conditions;
}
