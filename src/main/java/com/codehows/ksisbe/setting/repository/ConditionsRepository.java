package com.codehows.ksisbe.setting.repository;

import com.codehows.ksisbe.setting.entity.Conditions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConditionsRepository extends JpaRepository<Conditions, Long> {

    List<Conditions> findBySettingSettingId(Long settingId);
}
