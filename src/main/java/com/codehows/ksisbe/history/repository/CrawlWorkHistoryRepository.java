package com.codehows.ksisbe.history.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlWorkHistoryRepository extends JpaRepository<CrawlWork, Long> {

}


