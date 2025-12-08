package com.codehows.ksisbe.history.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.history.dto.CrawlResultItemHistory;
import com.codehows.ksisbe.history.dto.CrawlWorkHistory;
import com.codehows.ksisbe.history.repository.CrawlResultItemHistoryRepository;
import com.codehows.ksisbe.history.repository.CrawlWorkHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CrawlWorkHistoryRepository crawlWorkHistoryRepository;
    private final CrawlResultItemHistoryRepository crawlResultItemHistoryRepository;

    public List<CrawlWorkHistory> getAllCrawlWorkHistories() {
        List<CrawlWork> crawlWorks = crawlWorkHistoryRepository.findAll();

        // CrawlWork 엔티티 리스트를 CrawlWorkHistory DTO 리스트로 변환
        return crawlWorks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CrawlWorkHistory toDto(CrawlWork entity) {
        return CrawlWorkHistory.builder()
                .workId(entity.getWorkId())
                .settingId(entity.getSetting() != null ? entity.getSetting().getSettingId() : null)
                .settingName(entity.getSetting() != null ? entity.getSetting().getSettingName() : null)
                .userId(entity.getSetting() != null ? entity.getSetting().getUser().getId() : null)
                .username(entity.getSetting() != null ? entity.getSetting().getUser().getUsername() : null)
                .scheduleId(entity.getScheduler() != null ? entity.getScheduler().getScheduleId() : null)
                .startedBy(entity.getStartedBy() != null ? entity.getStartedBy().getId() : null)
                .failCount(entity.getFailCount() != null ? entity.getFailCount().longValue() : 0L)
                .state(entity.getState())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .build();
    }

    public List<CrawlResultItemHistory> getSelectedWorkResults(Long workId) {
        List<CrawlResultItem> crawlResults = crawlResultItemHistoryRepository.findByCrawlWork_WorkId(workId);

        return crawlResults.stream()
                .map(this::toResultDto)
                .collect(Collectors.toList());
    }

    private CrawlResultItemHistory toResultDto(CrawlResultItem entity) {
        return CrawlResultItemHistory.builder()
                .itemId(entity.getItemId())
                .workId(entity.getCrawlWork() != null ? entity.getCrawlWork().getWorkId() : null)
                .pageUrl(entity.getPageUrl())
                .resultValue(entity.getResultValue())
                .seq(entity.getSeq())
                .state(entity.getState())
                .build();
    }
}