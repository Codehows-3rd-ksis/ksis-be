package com.codehows.ksisbe.setting.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionsShowDto {
    private Long settingId;
    private Long conditionsId;
    private String conditionsKey;
    private String conditionsValue;
    private String attr;
}
