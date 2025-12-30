package com.codehows.ksisbe.history.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.history.dto.CrawlResultItemHistory;
import com.codehows.ksisbe.history.dto.CrawlWorkHistory;
import com.codehows.ksisbe.history.repository.CrawlResultItemHistoryRepository;
import com.codehows.ksisbe.history.repository.CrawlWorkHistoryRepository;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.status.dto.statusDetailDto.*;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CrawlWorkHistoryRepository crawlWorkHistoryRepository;
    private final CrawlResultItemHistoryRepository crawlResultItemHistoryRepository;
    private final UserRepository userRepository;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;

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
                .period(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getStartDate() + " ~ " + crawlWork.getScheduler().getEndDate() : null)
//                .startDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getStartDate() : null)
//                .endDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getEndDate() : null)
                .searchText(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getSearchText() : null)
                .failCount(crawlWork.getFailCount())
                .state(crawlWork.getState())
                .type(crawlWork.getType())
                .startAt(crawlWork.getStartAt())
                .endAt(crawlWork.getEndAt())
                .build()
        );
    }

    //상세조회
    @Transactional
    public StatusDetailShowDto findHistoryDetail(Long workId, String username) {
        User user =  userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new RuntimeException("유효하지 않은 사용자"));

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());

        CrawlWork work;
        if (isAdmin) {
            work = crawlWorkRepository.findByWorkId(workId)
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 작업"));
        } else {
            work = crawlWorkRepository.findByWorkIdAndStartedByUsername(workId, username)
                    .orElseThrow(() -> new RuntimeException("유효하지 않은 작업 또는 권한이 없습니다."));
        }

        List<CrawlResultItem> items = crawlResultItemRepository.findByCrawlWorkOrderBySeqAsc(work);

        List<FailureItem> failureList = new ArrayList<>();
        List<CollectionRow> collectionRows = new ArrayList<>();

        for (CrawlResultItem item : items) {
            if ("FAILED".equals(item.getState())) {
                FailureItem fail = new FailureItem();
                fail.setItemId(item.getItemId());
                fail.setSeq(item.getSeq());
                fail.setUrl(item.getPageUrl());
                failureList.add(fail);

            }


            CollectionRow row = new CollectionRow();
            row.setItemId(item.getItemId());
            row.setSeq(item.getSeq());
            row.setResultValue(item.getResultValue());
            row.setState(item.getState());
            collectionRows.add(row);

        }
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setWorkId(work.getWorkId());
        basicInfo.setSettingName(work.getSetting().getSettingName());
        basicInfo.setType(work.getType());
        basicInfo.setUserId(work.getStartedBy().getUsername());
//            basicInfo.setStartDate(work.getStartDate());
//            basicInfo.setEndDate(work.getEndDate());
//            basicInfo.setCycle(work.getCycle());
        basicInfo.setStartAt(work.getStartAt());
        basicInfo.setEndAt(work.getEndAt());
        basicInfo.setState(work.getState());
        basicInfo.setProgress(work.getProgress());

        ProgressInfo progressInfo = new ProgressInfo();
        progressInfo.setTotalCount(work.getTotalCount());
        progressInfo.setCollectCount(work.getCollectCount());
        progressInfo.setFailCount(work.getFailCount());
        progressInfo.setExpectEndAt(work.getExpectEndAt());

        CollectionData collectionData = new CollectionData();
        collectionData.setRows(collectionRows);

        StatusDetailShowDto response = new StatusDetailShowDto();
        response.setBasicInfo(basicInfo);
        response.setFailureList(failureList);
        response.setCollectionData(collectionData);
        response.setProgress(progressInfo);

        return response;
    }


    // setting_id 안의 user_id가 input과 같은 값들을 조회 : 사용자 이력조회로 사용
    @Transactional
    public Page<CrawlWorkHistory> findCrawlWorkHistoriesByUserId(Long userId, SearchCondition condition, Pageable pageable) {

        Page<CrawlWork> works = crawlWorkHistoryRepository.searchByUserLog(
                userId,
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
                .period(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getStartDate() + " ~ " + crawlWork.getScheduler().getEndDate() : null)
//                .startDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getStartDate() : null)
//                .endDate(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getEndDate() : null)
                .searchText(crawlWork.getScheduler() != null ? crawlWork.getScheduler().getSearchText() : null)
                .failCount(crawlWork.getFailCount())
                .state(crawlWork.getState())
                .type(crawlWork.getType())
                .startAt(crawlWork.getStartAt())
                .endAt(crawlWork.getEndAt())
                .build()
        );
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
}