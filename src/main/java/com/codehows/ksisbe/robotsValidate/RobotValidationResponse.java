package com.codehows.ksisbe.robotsValidate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RobotValidationResponse {

    private boolean allow;
    private String robotsTxt;
    private String message;

}
