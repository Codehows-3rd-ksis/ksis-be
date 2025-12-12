package com.codehows.ksisbe.setting.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionsRequestDto {
    private Long conditionsId;
    private String conditionsKey;
    private String conditionsValue;
    private String attr;
}
