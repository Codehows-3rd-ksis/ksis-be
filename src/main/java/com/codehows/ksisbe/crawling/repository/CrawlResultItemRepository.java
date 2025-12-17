package com.codehows.ksisbe.crawling.repository;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawlResultItemRepository extends JpaRepository<CrawlResultItem,Long> {
    List<CrawlResultItem> findByCrawlWorkOrderBySeqAsc(CrawlWork crawlWork);;
}
