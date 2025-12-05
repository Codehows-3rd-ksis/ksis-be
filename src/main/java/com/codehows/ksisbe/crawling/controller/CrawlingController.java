package com.codehows.ksisbe.crawling.controller;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final TaskExecutor taskExecutor; // 비동기 실행을 위한 TaskExecutor 주입

    @PostMapping("/start/{settingId}")
    public ResponseEntity<?> startSetting(@PathVariable Long settingId, Authentication authentication) {
        String username = authentication.getName();
//        crawlingService.startCrawling(settingId, username);
//        return  ResponseEntity.ok("수집시작");

        // ⭐ 크롤링 작업을 별도 스레드에서 실행 (비동기)
        taskExecutor.execute(() -> {
            crawlingService.startCrawling(settingId, username);
        });

        // ⭐ 프론트에는 즉시 반환
        return ResponseEntity.ok("수집 시작됨 (백엔드에서 비동기 실행 중)");
    }
}
