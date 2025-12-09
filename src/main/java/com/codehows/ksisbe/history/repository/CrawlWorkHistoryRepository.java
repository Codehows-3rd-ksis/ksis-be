package com.codehows.ksisbe.history.repository;

import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.setting.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlWorkHistoryRepository extends JpaRepository<CrawlWork, Long> {
    List<CrawlWork> findByIsDelete(String isDelete);

    @Query("SELECT cw " +
            "FROM CrawlWork cw " +
            "WHERE (cw.setting.user.id = :userId OR cw.startedBy.id = :userId) " +
            "AND cw.isDelete = 'N'")
    List<CrawlWork> findByUserId(@Param("userId") Long userId);
}


