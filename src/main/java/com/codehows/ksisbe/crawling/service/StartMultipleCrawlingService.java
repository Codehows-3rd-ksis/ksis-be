package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.status.service.CrawlProgressPushService;
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
    private final CrawlProgressPushService crawlProgressPushService;
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
        return crawlWorkRepository.saveAndFlush(crawlWork);
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
            int failCount = extractDetailUrlsMulti.extractDetailUrls(crawlWork, driver, setting);

            updateCrawlWorkFinalStatus(crawlWork);
        } catch (CrawlStopException e) {
          crawlWork.setState("STOPPED");
          crawlWork.setEndAt(LocalDateTime.now());
          crawlWorkRepository.save(crawlWork);
          updateCrawlWorkFinalStatus(crawlWork);
        } finally {
            if (driver != null) driver.quit();
        }
    }

    @Transactional
    public CrawlResultItem saveResultItem(CrawlWork crawlWork, String pageUrl, Map<String, String> resultMap, int seq) throws Exception {
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

        return crawlResultItemRepository.save(item);
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
        crawlProgressPushService.pushProgress(crawlWork);
    }

    @Transactional
    public void updateCrawlWorkFinalStatus(CrawlWork crawlWork) {
        int total = crawlWork.getTotalCount();
        int successCount = total - crawlWork.getFailCount();

        // 최종 상태 결정
        String finalState;
        if (crawlWork.getFailCount() == 0) {
            finalState = "SUCCESS";                 // 전체 성공
        } else if (successCount == 0) {
            finalState = "FAILED";                  // 전체 실패
        } else {
            System.out.println(crawlWork.getState());
            if ("STOPPED".equals(crawlWork.getState())
                    || "STOP_REQUEST".equals(crawlWork.getState())) {
                finalState = "STOPPED";
            } else finalState = "PARTIAL";                 // 일부 성공 [ 성공(실패건수4건)]
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
