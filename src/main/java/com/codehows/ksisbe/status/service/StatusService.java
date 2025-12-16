package com.codehows.ksisbe.status.service;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;

import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.status.dto.StatusShowDto;

import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final UserRepository userRepository;
    private final CrawlWorkRepository crawlWorkRepository;

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
        dto.setWorkId(work.getWorkId());
        dto.setUserId(work.getStartedBy().getUsername());
        dto.setType(work.getType());
        dto.setProgress(work.getProgress());
        return dto;
    }
}
