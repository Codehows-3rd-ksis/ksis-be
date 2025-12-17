package com.codehows.ksisbe.status.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;

import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.status.dto.StatusDetailItemShowDto;
import com.codehows.ksisbe.status.dto.StatusDetailShowDto;
import com.codehows.ksisbe.status.dto.StatusShowDto;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final UserRepository userRepository;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;

    @Transactional(readOnly = true)
    public List<StatusShowDto> findStatus(String username) {

        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username)
                );

        List<CrawlWork> works;

        // 관리자 → 전체 조회
        if ("ROLE_ADMIN".equals(user.getRole())) {
            works = crawlWorkRepository.findAllByIsDeleteAndState("N","RUNNING");
        }
        // 일반 사용자 → 본인 작업만
        else {
            works = crawlWorkRepository.findAllByStartedByAndIsDeleteAndState(user, "N", "RUNNING");
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
        return dto;
    }

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

        List<StatusDetailItemShowDto> itemDtos = new ArrayList<>();

        for (CrawlResultItem item : items) {
            StatusDetailItemShowDto dto = new StatusDetailItemShowDto();
            dto.setItemId(item.getItemId());
            dto.setSeq(item.getSeq());
            dto.setResultValue(item.getResultValue());

            itemDtos.add(dto);
        }
        StatusDetailShowDto dto = new StatusDetailShowDto();
        dto.setWorkId(work.getWorkId());
        dto.setSettingName(work.getSetting().getSettingName());
        dto.setUserId(work.getStartedBy().getUsername());
        dto.setType(work.getType());
        dto.setStartAt(work.getStartAt());
        dto.setEndAt(work.getEndAt());
//        dto.setStartDate(work.getStartDate());
//        dto.setEndDate(work.getEndDate());
//        dto.setCycle(work.getCycle());
        dto.setProgress(work.getProgress());
        dto.setState(work.getState());
        dto.setFailCount(work.getFailCount());
        dto.setCollectCount(work.getCollectCount());
        dto.setTotalCount(work.getTotalCount());
        dto.setExpectEndAt(work.getExpectEndAt());
        dto.setStatusDetailShowDto(itemDtos);

        return dto;
    }
}
