package com.codehows.ksisbe.crawling.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawlWorkRepository extends JpaRepository<CrawlWork,Long> {
    // 관리자용
    List<CrawlWork> findAllByIsDeleteAndState(String isDelete, String state);

    // 일반 사용자용
    List<CrawlWork> findAllByStartedByAndIsDeleteAndState(User user, String isDelete, String state);
}
