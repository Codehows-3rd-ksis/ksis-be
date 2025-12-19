package com.codehows.ksisbe.setting.repository;

import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long>, SettingRepositoryCustom {
    // 특정 userId와 type으로 조회
    List<Setting> findByUserId(Long userId);

    Optional<Setting> findBySettingIdAndIsDelete(Long settingId, String isDelete);

    List<Setting> findByUserIdAndIsDelete(Long userId, String isDelete);

    List<Setting> findByIsDelete(String isDelete);

    @Query("SELECT s FROM Setting s LEFT JOIN FETCH s.conditions WHERE s.settingId = :id AND s.isDelete = 'N'")
    Optional<Setting> findBySettingIdAndIsDeleteWithConditions(@Param("id") Long id);
}
