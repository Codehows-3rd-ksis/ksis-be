package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.user.User;
import com.codehows.ksisbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrawlResultService {

    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCrawlWorkProgress(CrawlWork crawlWork) {
        crawlWorkRepository.save(crawlWork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CrawlResultItem saveResultItemTransaction(CrawlResultItem item) {
        return crawlResultItemRepository.save(item);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CrawlWork createCrawlWorkTransaction(CrawlWork crawlWork) {
        Long userId = crawlWork.getStartedBy().getId();
        User managedUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        crawlWork.setStartedBy(managedUser);
        return crawlWorkRepository.save(crawlWork);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFinalTransaction(CrawlWork crawlWork) {
        crawlWorkRepository.save(crawlWork);
    }
}
