package com.codehows.ksisbe.setting.service;

import com.codehows.ksisbe.setting.dto.*;
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
                .orElseThrow(() -> new UsernameNotFoundException("유효하지 않은 유저"));

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

    //설정수정 조건조회
    @Transactional(readOnly = true)
    public List<ConditionsShowDto> findConditions(Long settingId, String username) {
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Setting setting = settingRepository.findBySettingIdAndIsDelete(settingId,"N")
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

    //설정수정
    @Transactional
    public void updateSetting(Long userId, Long settingId, SettingUpdateDto settingUpdateDto) {
        Setting setting = settingRepository.findBySettingIdAndIsDelete(settingId, "N")
                .orElseThrow(() -> new IllegalArgumentException("해당 설정을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저입니다."));

        // 권한 체크
        if (!user.getRole().equals("ROLE_ADMIN") && !setting.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        // 단일/다중 타입에 따른 필드 초기화
        if ("단일".equalsIgnoreCase(settingUpdateDto.getType())) {
            settingUpdateDto.setListArea(null);
            settingUpdateDto.setPagingType(null);
            settingUpdateDto.setPagingArea(null);
            settingUpdateDto.setPagingNextbtn(null);
            settingUpdateDto.setMaxPage(null);
            settingUpdateDto.setLinkArea(null);
        } else if (!"다중".equalsIgnoreCase(settingUpdateDto.getType())) {
            throw new IllegalArgumentException("없는 세팅 타입: " + settingUpdateDto.getType());
        }
        //설정정보수정
        setting.setSettingName(settingUpdateDto.getSettingName());
        setting.setUrl(settingUpdateDto.getUrl());
        setting.setType(settingUpdateDto.getType());
        setting.setUserAgent(settingUpdateDto.getUserAgent());
        setting.setRate(settingUpdateDto.getRate());
        setting.setListArea(settingUpdateDto.getListArea());
        setting.setPagingType(settingUpdateDto.getPagingType());
        setting.setPagingArea(settingUpdateDto.getPagingArea());
        setting.setPagingNextbtn(settingUpdateDto.getPagingNextbtn());
        setting.setMaxPage(settingUpdateDto.getMaxPage());
        setting.setLinkArea(settingUpdateDto.getLinkArea());
        setting.setUpdateAt(LocalDateTime.now());

        //클라이언트에서 받은 setting conditions
        List<ConditionsUpdateDto> newConditionsDto = settingUpdateDto.getConditions();
        //기존 DB에 저장되어있는 conditions
        List<Conditions> existingConditions = setting.getConditions();

        //아이디가 있는 것들만 추출
        List<Long> newConditionsId = new ArrayList<>();
        for (ConditionsUpdateDto c : newConditionsDto) {
            if (c.getConditionsId() != null) {
                newConditionsId.add(c.getConditionsId());
            }
        }
        //아이디가 있는 것들만 추출한 것에서 존재하지 않는 아이디 찾기
        List<Conditions> toRemove = new ArrayList<>(); //삭제할 아이디 리스트
        for (Conditions c : existingConditions) {
            if (!newConditionsId.contains(c.getConditionsId())) {
                toRemove.add(c);
            }
        }
        // 삭제 대상 조건들을 기존 리스트에서 제거
        for (Conditions c : toRemove) {
            existingConditions.remove(c);
        }
        //수정, 추가
        for (ConditionsUpdateDto newDto : newConditionsDto) {
            if (newDto.getConditionsId() != null) {
                // 수정: 기존 조건 중 해당 ID 찾기
                for (Conditions existingCondition : existingConditions) {
                    if (existingCondition.getConditionsId().equals(newDto.getConditionsId())) {
                        existingCondition.setConditionsKey(newDto.getConditionsKey());
                        existingCondition.setConditionsValue(newDto.getConditionsValue());
                        existingCondition.setAttr(newDto.getAttr());
                        break;
                    }
                }
            } else {
                // 추가: 새 조건인 경우 새 엔티티 생성해서 리스트에 추가
                Conditions newCondition = new Conditions();
                newCondition.setConditionsKey(newDto.getConditionsKey());
                newCondition.setConditionsValue(newDto.getConditionsValue());
                newCondition.setAttr(newDto.getAttr());
                newCondition.setSetting(setting); // 연관관계 설정
                existingConditions.add(newCondition);
            }

        }
        setting.setConditions(existingConditions);
    }
    
    //설정삭제
    public void deleteSetting(Long settingId, String username) {
        Setting setting = settingRepository.findBySettingIdAndIsDelete(settingId, "N")
                .orElseThrow(() -> new IllegalArgumentException("유효한 설정아이디 입니다."));
        User user = userRepository.findByUsernameAndIsDelete(username, "N")
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저입니다."));

        // 권한 체크
        if (!user.getRole().equals("ROLE_ADMIN") && !setting.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        setting.setIsDelete("Y");
        settingRepository.save(setting);
    }
}
