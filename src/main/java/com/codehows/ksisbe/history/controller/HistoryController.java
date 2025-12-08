package com.codehows.ksisbe.history.controller;

import com.codehows.ksisbe.history.dto.CrawlResultItemHistory;
import com.codehows.ksisbe.history.dto.CrawlWorkHistory;
import com.codehows.ksisbe.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/history")
    public List<CrawlWorkHistory> getCrawlWorkHistories() {
        return historyService.getAllCrawlWorkHistories();
    }

    @GetMapping("/history/{workId}")
    public List<CrawlResultItemHistory> getResultsByWorkId(@PathVariable Long workId) {
        return historyService.getSelectedWorkResults(workId);
    }
}