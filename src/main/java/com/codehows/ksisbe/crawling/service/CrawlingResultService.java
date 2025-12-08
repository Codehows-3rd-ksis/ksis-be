//package com.codehows.ksisbe.crawling.service;
//
//import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
//import com.codehows.ksisbe.crawling.entity.CrawlWork;
//import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
//import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionSynchronizationManager;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class CrawlingResultService {
//
//    private final CrawlResultItemRepository crawlResultItemRepository;
//    private final CrawlWorkRepository crawlWorkRepository;
//
//    @Transactional
//    public void saveResult(Long workId, String url, Map<String, String> map) throws JsonProcessingException {
//        CrawlWork work = crawlWorkRepository.findById(workId)
//                .orElseThrow(() -> new RuntimeException("work not found: " + workId));
//
//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(map);
//
//        CrawlResultItem item = CrawlResultItem.builder()
//                .crawlWork(work)
//                .seq(1L)
//                .pageUrl(url)
//                .resultValue(json)
//                .state("SUCCESS")
//                .createAt(LocalDateTime.now())
//                .updateAt(LocalDateTime.now())
//                .build();
//
//        crawlResultItemRepository.save(item);
//    }
//}
