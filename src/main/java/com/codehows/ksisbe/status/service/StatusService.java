package com.codehows.ksisbe.status.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;

import com.codehows.ksisbe.status.dto.statusDetailDto.*;
import com.codehows.ksisbe.status.dto.StatusShowDto;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final UserRepository userRepository;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;

    //현황조회
    @Transactional(readOnly = true)
    public List<StatusShowDto> findStatus(String username) {

        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)
                );

        List<CrawlWork> works;

        // 관리자 → 전체 조회
        if ("ROLE_ADMIN".equals(user.getRole())) {
            works = crawlWorkRepository.findAllByIsDeleteAndStateOrderByCreateAtDesc("N","RUNNING");
        }
        // 일반 사용자 → 본인 작업만
        else {
            works = crawlWorkRepository.findAllByStartedByAndIsDeleteAndStateOrderByCreateAtDesc(user, "N", "RUNNING");
        }

        // Entity → DTO 변환
        return works.stream()
                .map(this::toDto)
                .toList();
    }

    private StatusShowDto toDto(CrawlWork work) {
        StatusShowDto dto = new StatusShowDto();
        dto.setSettingName(work.getSetting().getSettingName());
        dto.setStartAt(work.getStartAt());
        dto.setWorkId(work.getWorkId());
        dto.setUserId(work.getStartedBy().getUsername());
        dto.setType(work.getType());
        dto.setProgress(work.getProgress());
        dto.setSearchText(work.getScheduler() != null ? work.getScheduler().getSearchText() : null);
        dto.setPeriod(work.getScheduler() != null ? work.getScheduler().getStartDate() + " ~ " + work.getScheduler().getEndDate() : null);
        return dto;
    }

    //상세조회
    @Transactional(readOnly = true)
    public StatusDetailShowDto findStatusDetail(Long workId, String username) {
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
        basicInfo.setStartDate(work.getScheduler() != null ? work.getScheduler().getStartDate() : null);
        basicInfo.setEndDate(work.getScheduler() != null ? work.getScheduler().getEndDate() : null);
        basicInfo.setPeriod(work.getScheduler() != null ? work.getScheduler().getStartDate() + " ~ " + work.getScheduler().getEndDate() : null);
        basicInfo.setCycle(work.getScheduler() != null ? work.getScheduler().getSearchText() : null);
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
}
