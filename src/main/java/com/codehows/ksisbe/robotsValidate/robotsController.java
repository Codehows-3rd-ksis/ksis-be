package com.codehows.ksisbe.robotsValidate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class robotsController {

    private final robotsService robotsService;

    @PostMapping("/robots")
    public RobotValidationResponse validate(@RequestBody Map<String, String> body) {
        String pageUrl = body.get("url");
        String useragent = body.get("userAgent");
        return robotsService.validate(pageUrl, useragent);
    }
}
