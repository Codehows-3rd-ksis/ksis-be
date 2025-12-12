package com.codehows.ksisbe.history.repository;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlResultItemHistoryRepository extends JpaRepository<CrawlResultItem, Long> {
    // crawlWork의 workId가 일치하는 항목들을 모두 조회
    List<CrawlResultItem> findByCrawlWork_WorkId(Long workId);
}


