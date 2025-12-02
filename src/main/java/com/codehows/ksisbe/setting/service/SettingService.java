package com.codehows.ksisbe.setting.service;

import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository settingRepository;
    private final UserRepository userRepository;

    @Transactional
    public Setting createSetting(Long userId, Setting setting) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저"));

        setting.setUser(user);
        setting.setCreateAt(LocalDateTime.now());
        setting.setIsDelete("N");

        //Conditions 엔티티가 DB에 저장되기 전에, 각 Conditions 객체에 부모 Setting 객체를 미리 연결
        if (setting.getConditions() != null) {
            for (Conditions c : setting.getConditions()) {
                c.setSetting(setting);
            }
        }

        // 단일 타입이면 일부 필드는 null 처리
        if ("단일".equalsIgnoreCase(setting.getType())) {
            setting.setListArea(null);
            setting.setPagingType(null);
            setting.setPagingArea(null);
            setting.setPagingNextBtn(null);
            setting.setMaxPage(null);
            setting.setLinkArea(null);
        } else if (!"다중".equalsIgnoreCase(setting.getType())) {
            throw new IllegalArgumentException("없는세팅타입: " + setting.getType());
        }

        return settingRepository.save(setting);
    }
}
