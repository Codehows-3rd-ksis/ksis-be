package com.codehows.ksisbe.crawling.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrawlWorkRepository extends JpaRepository<CrawlWork,Long> {
    // 관리자용
    List<CrawlWork> findAllByIsDeleteAndStateOrderByCreateAtDesc(String isDelete, String state);

    // 일반 사용자용
    List<CrawlWork> findAllByStartedByAndIsDeleteAndStateOrderByCreateAtDesc(User user, String isDelete, String state);

    Optional<CrawlWork> findByWorkIdAndStartedByUsername(Long workId, String username);
    Optional<CrawlWork> findByWorkId(Long workId);

    @Query("select c.state from CrawlWork c where c.workId = :workId")
    String findState(@Param("workId") Long workId);
}
