package com.codehows.ksisbe.setting.service;

import com.codehows.ksisbe.setting.dto.ConditionsRequestDto;
import com.codehows.ksisbe.setting.dto.ConditionsShowDto;
import com.codehows.ksisbe.setting.dto.SettingRequestDto;
import com.codehows.ksisbe.setting.dto.SettingShowDto;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.ConditionsRepository;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;
    private final ConditionsRepository conditionsRepository;

    //설정등록
    @Transactional
    public void createSetting(Long userId, SettingRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저"));

        // DTO → 엔티티 변환
        Setting setting = Setting.builder()
                .settingName(dto.getSettingName())
                .url(dto.getUrl())
                .type(dto.getType())
                .userAgent(dto.getUserAgent())
                .rate(dto.getRate())
                .listArea(dto.getListArea())
                .pagingType(dto.getPagingType())
                .pagingArea(dto.getPagingArea())
                .pagingNextbtn(dto.getPagingNextbtn())
                .maxPage(dto.getMaxPage())
                .linkArea(dto.getLinkArea())
                .createAt(LocalDateTime.now())
                .isDelete("N")
                .user(user)
                .build();

        // Conditions 변환 및 부모 세팅 연결
        if (dto.getConditions() != null) {

            List<Conditions> conditions = new ArrayList<>();

            for (ConditionsRequestDto cDto : dto.getConditions()) {
                Conditions c = new Conditions();
                c.setConditionsKey(cDto.getConditionsKey());
                c.setConditionsValue(cDto.getConditionsValue());
                c.setAttr(cDto.getAttr());
                c.setSetting(setting); // 연관관계 설정 (ManyToOne)
                conditions.add(c);
            }

            setting.setConditions(conditions);
        }

        // 단일 타입일 경우 필드 초기화
        if ("단일".equalsIgnoreCase(setting.getType())) {
            setting.setListArea(null);
            setting.setPagingType(null);
            setting.setPagingArea(null);
            setting.setPagingNextbtn(null);
            setting.setMaxPage(null);
            setting.setLinkArea(null);
        } else if (!"다중".equalsIgnoreCase(setting.getType())) {
            throw new IllegalArgumentException("없는 세팅타입: " + setting.getType());
        }

        settingRepository.save(setting);
    }

    //설정조회
    @Transactional(readOnly = true)
    public List<SettingShowDto> findSetting(String username) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<Setting> settings;

        if ("ROLE_ADMIN".equals(user.getRole())) {
            settings = settingRepository.findAll();
        } else {
            settings = settingRepository.findByUserId(user.getId());
        }

        return settings.stream()
                .map(setting -> SettingShowDto.builder()
                        .settingId(setting.getSettingId())
                        .userId(setting.getUser().getId())
                        .settingName(setting.getSettingName())
                        .url(setting.getUrl())
                        .type(setting.getType())
                        .userAgent(setting.getUserAgent())
                        .rate(setting.getRate())
                        .listArea(setting.getListArea())
                        .pagingType(setting.getPagingType())
                        .pagingArea(setting.getPagingArea())
                        .pagingNextbtn(setting.getPagingNextbtn())
                        .maxPage(setting.getMaxPage())
                        .linkArea(setting.getLinkArea())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConditionsShowDto> findConditions(Long settingId, String username) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Setting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("설정아이디를 찾을 수 없습니다."));

        // 관리자라면 모든 설정 조회 가능, 아니면 본인 것만 조회 가능
        if (!user.getRole().equals("ROLE_ADMIN") && !setting.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        List<Conditions> conditionsList = conditionsRepository.findBySettingSettingId(settingId);

        return conditionsList.stream()
                .map(c -> ConditionsShowDto.builder()
                        .settingId(settingId)
                        .conditionsId(c.getConditionsId())
                        .conditionsKey(c.getConditionsKey())
                        .conditionsValue(c.getConditionsValue())
                        .attr(c.getAttr())
                        .build())
                .toList();
    }
}
