package com.codehows.ksisbe.history.controller;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.history.dto.CrawlResultItemHistory;
import com.codehows.ksisbe.history.dto.CrawlWorkHistory;
import com.codehows.ksisbe.history.repository.CrawlWorkHistoryRepository;
import com.codehows.ksisbe.history.repository.CrawlWorkHistoryRepositoryImpl;
import com.codehows.ksisbe.history.service.HistoryService;
import com.codehows.ksisbe.query.dto.CustomPageResponse;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.user.dto.UserShowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
//    private final CrawlWorkHistoryRepository crawlWorkHistoryRepository;
    private final CrawlWorkHistoryRepositoryImpl crawlWorkHistoryRepositoryImpl;

//    @GetMapping("/history")
//    public ResponseEntity<?> getCrawlWorkHistories(Authentication authentication) {
//        try {
//            String username = authentication.getName();
//            List<CrawlWorkHistory> list = historyService.findCrawlWorkHistories(username);
//            return ResponseEntity.ok(list);
//        } catch (UsernameNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("message", e.getMessage()));
//        }
//    }

    @GetMapping("/history")
    public ResponseEntity<?> findCrawlWorks(Authentication authentication,
                                            @ModelAttribute SearchCondition condition, Pageable pageable ) {
        try {
            String username = authentication.getName();
            Page<CrawlWorkHistory> result = historyService.findCrawlWorkHistories(username, condition, pageable);
            return ResponseEntity.ok(new CustomPageResponse<>(
                    result.getContent(),
                    result.getNumber(),
                    result.getSize(),
                    result.getTotalElements(),
                    result.getTotalPages()
            ));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history/result/{workId}")
    public List<CrawlResultItemHistory> getResultsByWorkId(@PathVariable Long workId) {
        return historyService.getSelectedWorkResults(workId);
    }

//  유저관리 - 사용자 로그 확인용
    @GetMapping("/history/{userId}")
    public List<CrawlWorkHistory> getCrawlWorksByUserId(@PathVariable Long userId) {
        return historyService.findByUserId(userId);
    }

}