package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.scheduler.entity.Scheduler;
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
    public CrawlWork createCrawlWork(Setting setting, User user, Scheduler scheduler) {
        CrawlWork crawlWork = CrawlWork.builder()
                .setting(setting)
                .startedBy(scheduler == null ? user : scheduler.getUser())
                .scheduler(scheduler)
                .type(scheduler == null ? "수동실행" : "스케줄러")
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

    public void startMultipleCrawling(Long settingId, User user, Scheduler scheduler) {
        Setting setting = settingRepository.findBySettingIdAndIsDeleteWithConditions(settingId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 설정입니다."));
        CrawlWork crawlWork = createCrawlWork(setting, user, scheduler);
        WebDriver driver = null;
        try {
            driver = webDriverFactory.createDriver(setting.getUserAgent());
            driver.get(setting.getUrl());

            //목록페이지에서 상세 url 추출
            int failCount = extractDetailUrlsMulti.extractDetailUrls(crawlWork, driver, setting);

            updateCrawlWorkFinalStatus(crawlWork);
        }finally {
            if (driver != null) {
                driver.quit();
            }
        }
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
