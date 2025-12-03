package com.codehows.ksisbe.setting.repository;

import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Long> {
    // 특정 userId와 type으로 조회
    List<Setting> findByUserId(Long userId);
}
