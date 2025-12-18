package com.codehows.ksisbe.setting.repository;

import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettingRepositoryCustom {
    Page<Setting> search(Long userId, String role, SearchCondition condition, Pageable pageable);
}
