package com.codehows.ksisbe.history.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.query.dto.SearchCondition;
import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CrawlWorkHistoryRepositoryCustom {
    Page<CrawlWork> search(Long userId, String role, SearchCondition condition, Pageable pageable);
    Page<CrawlWork> searchByUserLog(Long userId, SearchCondition condition, Pageable pageable);
}
