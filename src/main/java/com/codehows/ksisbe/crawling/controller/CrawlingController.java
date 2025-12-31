package com.codehows.ksisbe.crawling.controller;

import com.codehows.ksisbe.crawling.service.CrawlingService;
import com.codehows.ksisbe.user.dto.UserAccountUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final TaskExecutor taskExecutor; // 비동기 실행을 위한 TaskExecutor 주입

    @PostMapping("/start/{settingId}")
    public ResponseEntity<?> startSetting(@PathVariable Long settingId, Authentication authentication) {
        String username = authentication.getName();

        // 크롤링 작업을 별도 스레드에서 실행 (비동기)
        taskExecutor.execute(() -> {
            try {
                crawlingService.startCrawling(settingId, username);
            } catch (AccessDeniedException e) {
                // 로그 기록 또는 상태 저장
                log.warn("접근 권한 없음: " + e.getMessage());
                // 필요한 추가 작업
            } catch (Exception e) {
                log.error("크롤링 실패: ", e);
            }
        });

        // 프론트에는 즉시 반환
        return ResponseEntity.ok("수집 시작됨 (백엔드에서 비동기 실행 중)");
    }

    @PutMapping("/crawl/stop/{workId}")
    public ResponseEntity<?> stopRequestCrawling(@PathVariable Long workId, Authentication authentication) {
        crawlingService.requestStop(workId);
        return ResponseEntity.ok("수집 중지 요청");
    }
}
