package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.RuntimeErrorException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StartMultipleCrawlingService {

    private final SettingRepository settingRepository;
    private final WebDriverFactory webDriverFactory;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;
    private final CrawlingFailService crawlingFailService;
    private final ExtractDetailUrlsMulti extractDetailUrlsMulti;
//    private final WorkSaverService  workSaverService;

    @Transactional
    public CrawlWork createCrawlWork(Setting setting, User user) {
        CrawlWork crawlWork = CrawlWork.builder()
                .setting(setting)
                .startedBy(user)
                .type("수동실행")
                .state("RUNNING")
                .failCount(0)
                .totalCount(0)
                .collectCount(0)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .startAt(LocalDateTime.now())
                .isDelete("N")
                .crawlResultItems(new ArrayList<>())
                .build();
        return crawlWorkRepository.save(crawlWork);
    }

    public void startMultipleCrawling(Long settingId, User user) {
        Setting setting = settingRepository.findBySettingIdAndIsDeleteWithConditions(settingId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정입니다."));

        CrawlWork crawlWork = createCrawlWork(setting, user);

        WebDriver driver = null;
        try {
            driver = webDriverFactory.createDriver(setting.getUserAgent());
            driver.get(setting.getUrl());

            //목록페이지에서 상세 url 추출
            List<String> detailUrls = extractDetailUrlsMulti.extractDetailUrls(driver, setting);
            System.out.println("Detail URLs size: " + detailUrls.size());
            detailUrls.forEach(System.out::println);

            int seq = 1;
            int failCount = 0;
            int totalCount = detailUrls.size();
//            int collectCount = 0;

            for (String detailUrl : detailUrls) {
                try {
                    driver.get(detailUrl);
                    Map<String, String> result = crawlDetailPage(driver, setting);
                    saveResultItem(crawlWork, detailUrl, result, seq);
                } catch (Exception e) {
                    e.printStackTrace();
                    failCount++;
                    crawlingFailService.saveFailedResultMulti(crawlWork.getWorkId(), detailUrl, (long) seq);
                } finally {
                    incrementCollectCount(crawlWork);
//                    collectCount++;
                    updateCollectProgress(crawlWork, failCount, totalCount);
                    seq++;
                }
            }
            updateCrawlWorkFinalStatus(crawlWork, failCount);
        }finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Transactional
    public void saveResultItem(CrawlWork crawlWork, String pageUrl, Map<String, String> resultMap, int seq) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(resultMap);

        CrawlResultItem item = CrawlResultItem.builder()
                .crawlWork(crawlWork)
                .seq((long) seq)
                .pageUrl(pageUrl)
                .resultValue(json)
                .state("SUCCESS")
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();
        crawlWork.getCrawlResultItems().add(item);
        item.setCrawlWork(crawlWork);

        try {
            crawlResultItemRepository.save(item);
        } catch (Exception e) {
            // 반드시 예외 로그 남기기
            e.printStackTrace();
            throw e; // 예외 재던지기 (롤백 유도)
        }
    }

    @Transactional
    public void incrementCollectCount(CrawlWork crawlWork) {
        crawlWork.setCollectCount(crawlWork.getCollectCount() + 1);
        crawlWorkRepository.save(crawlWork);
    }

    @Transactional
    public void updateCollectProgress(CrawlWork crawlWork, int failCount, int totalCount) {
        int collectCount = crawlWork.getCollectCount();
        double progress = ((double) collectCount / totalCount) * 100;

        crawlWork.setProgress(progress);
        crawlWork.setFailCount(failCount);
        crawlWork.setTotalCount(totalCount);
        crawlWork.setUpdateAt(LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();


        // 경과 시간 (초)
        long elapsedSeconds = java.time.Duration.between(crawlWork.getStartAt(), now).getSeconds();

        if (collectCount > 0) {  // 0일 때는 나누기 불가 → null 유지
            // 평균 처리 속도 (초/건)
            double avgSpeed = (double) elapsedSeconds / collectCount;

            // 남은 건수
            int remainingCount = totalCount - collectCount;

            // 예상 남은 시간(초)
            long estimatedRemainSeconds = (long) (remainingCount * avgSpeed);

            // 예상 완료 시간
            LocalDateTime expectEndAt = now.plusSeconds(estimatedRemainSeconds);
            crawlWork.setExpectEndAt(expectEndAt);
        }

        crawlWork.setUpdateAt(now);

        crawlWorkRepository.save(crawlWork);
    }

    @Transactional
    public void updateCrawlWorkFinalStatus(CrawlWork crawlWork, int failCount) {
        int total = crawlWork.getTotalCount();
        int successCount = total - failCount;

        // 최종 상태 결정
        String finalState;
        if (failCount == 0) {
            finalState = "SUCCESS";                 // 전체 성공
        } else if (successCount == 0) {
            finalState = "FAILED";                  // 전체 실패
        } else {
            finalState = "PARTIAL";                 // 일부 성공 [ 성공(실패건수4건)]
        }

        crawlWork.setState(finalState);
        crawlWork.setEndAt(LocalDateTime.now());
        crawlWork.setUpdateAt(LocalDateTime.now());

        crawlWorkRepository.save(crawlWork);
    }

    private Map<String, String> crawlDetailPage(WebDriver driver, Setting setting) {
        List<Conditions> conditions = setting.getConditions();
        Map<String, String> resultMap = new HashMap<>();

        for (Conditions c : conditions) {
            String selector = c.getConditionsValue();
            try {
                WebElement element = driver.findElement(By.cssSelector(selector));
                String attr = c.getAttr();
                String value;
                if ("text".equals(attr)) {
                    value = element.getText();
                } else if ("href".equals(attr)) {
                    value = element.getAttribute("href");
                } else if ("src".equals(attr)) {
                    value = element.getAttribute("src");
                } else {
                    value = element.getAttribute(attr);
                }
                resultMap.put(c.getConditionsKey(), value);
            } catch (NoSuchElementException e) {
                resultMap.put(c.getConditionsKey(), null);
            }
        }
        return resultMap;
    }
}
