package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Setting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CrawlingFailService {


    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedResult(Long crawlWorkId, Setting setting) {
        CrawlWork managed = crawlWorkRepository.findById(crawlWorkId)
                .orElseThrow(() -> new RuntimeException("CrawlWork not found"));
        System.out.println(crawlWorkId);


        managed.setState("FAILED");
        managed.setFailCount(managed.getFailCount() + 1);
        managed.setEndAt(LocalDateTime.now());
        managed.setUpdateAt(LocalDateTime.now());

        CrawlResultItem resultItem = CrawlResultItem.builder()
                .crawlWork(managed)
                .seq(1L)
                .pageUrl(setting.getUrl())
                .resultValue("")
                .state("FAILED")
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        crawlResultItemRepository.save(resultItem);
    }
}
