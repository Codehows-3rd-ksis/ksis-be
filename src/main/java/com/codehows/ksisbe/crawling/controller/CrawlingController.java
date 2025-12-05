package com.codehows.ksisbe.crawling.controller;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;

    @PostMapping("/start/{settingId}")
    public ResponseEntity<?> startSetting(@PathVariable Long settingId, Authentication authentication) {
        String username = authentication.getName();
        crawlingService.startCrawling(settingId, username);
        return  ResponseEntity.ok("수집시작");
    }
}
