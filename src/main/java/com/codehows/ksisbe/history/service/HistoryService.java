package com.codehows.ksisbe.history.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.history.dto.CrawlResultItemHistory;
import com.codehows.ksisbe.history.dto.CrawlWorkHistory;
import com.codehows.ksisbe.history.repository.CrawlResultItemHistoryRepository;
import com.codehows.ksisbe.history.repository.CrawlWorkHistoryRepository;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CrawlWorkHistoryRepository crawlWorkHistoryRepository;
    private final CrawlResultItemHistoryRepository crawlResultItemHistoryRepository;
    private final UserRepository userRepository;

    // 관리자일 때 필터없이 모든이력 조회, 유저일 때는 자신의 것만 조회
    @Transactional
    public Page<CrawlWorkHistory> findCrawlWorkHistories(String username, SearchCondition condition, Pageable pageable) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Page<CrawlWork> works = crawlWorkHistoryRepository.search(
                user.getId(),
                user.getRole(),
                condition,
                pageable
        );

        return works.map(crawlWork -> CrawlWorkHistory.builder()
                .workId(crawlWork.getWorkId())
                .settingId(crawlWork.getSetting().getSettingId())
                .settingName(crawlWork.getSetting().getSettingName())
                .startedBy(crawlWork.getStartedBy() != null ? crawlWork.getStartedBy().getId() : null)
                .username(crawlWork.getStartedBy() != null ? crawlWork.getStartedBy().getUsername() : null)
                .scheduleId(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getScheduleId() : null)
                .startDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getStartDate() : null)
                .endDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getEndDate() : null)
                .period(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getCronExpression() : null)
                .failCount(crawlWork.getFailCount())
                .state(crawlWork.getState())
                .type(crawlWork.getType())
                .startAt(crawlWork.getStartAt())
                .endAt(crawlWork.getEndAt())
                .build()
        );
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
                .failCount(entity.getFailCount() != null ? entity.getFailCount().intValue() : 0)
                .state(entity.getState())
                .type(entity.getType())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .build();
    }

    // input(workId)를 물고있는 resultItem을 조회
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

    // setting_id 안의 user_id가 input과 같은 값들을 조회 : 사용자 이력조회로 사용
    public List<CrawlWorkHistory> findByUserId(Long userId) {
        List<CrawlWork> works = crawlWorkHistoryRepository.findByUserId(userId);

        return works.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}