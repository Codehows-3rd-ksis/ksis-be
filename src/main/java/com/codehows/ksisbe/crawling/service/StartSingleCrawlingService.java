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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StartSingleCrawlingService {

    private final SettingRepository settingRepository;
    private final WebDriverFactory webDriverFactory;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;
    private final CrawlingFailService crawlingFailService;
//    private final CrawlingResultService  crawlingResultService;

    // CrawlWork 저장은 별도 트랜잭션 (커밋이 바로 됨)
    @Transactional
    public CrawlWork createCrawlWork(Setting setting, User user) {
        CrawlWork crawlWork = CrawlWork.builder()
                .setting(setting)
                .startedBy(user)
                .type("단일")
                .state("RUNNING")
                .failCount(0)
                .totalCount(1)
                .collectCount(0)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .startAt(LocalDateTime.now())
                .isDelete("N")
                .build();
        return crawlWorkRepository.save(crawlWork);
    }

    // 크롤링 실행 메서드는 트랜잭션 없이 (또는 필요 최소한으로만) 운영
    public void startSingleCrawling(Long settingId, User user) {
        Setting setting = settingRepository.findBySettingIdAndIsDeleteWithConditions(settingId)
                .orElseThrow(() -> new RuntimeException("유효한 설정입니다"));

        // 1) crawlWork 생성 및 DB 저장 (커밋 보장)
        CrawlWork crawlWork = createCrawlWork(setting, user);

        WebDriver driver = null;
        try {
            driver = webDriverFactory.createDriver(setting.getUserAgent());
            driver.get("https://exapmel.com1234");

            //크롤링 결과 수집
            Map<String, String> resultMap = collectResults(setting, driver);

            //crawlWork 상태 SUCCESS 업데이트 트랜잭션으로 처리
            updateCrawlWorkSuccess(crawlWork);

            //결과 insert
//            crawlingResultService.saveResult(crawlWork.getWorkId(), setting.getUrl(), resultMap);
            saveCrawlResult(crawlWork, setting.getUrl(), resultMap);

        } catch (Exception e) {
            // 4) 실패 시 별도 트랜잭션으로 처리 (REQUIRES_NEW)
            crawlingFailService.saveFailedResult(crawlWork.getWorkId(), setting);

            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private Map<String, String> collectResults(Setting setting, WebDriver driver) {
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

    @Transactional
    public void saveCrawlResult(CrawlWork crawlWork, String pageUrl, Map<String, String> resultMap) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResult = objectMapper.writeValueAsString(resultMap);

        CrawlResultItem resultItem = CrawlResultItem.builder()
                .crawlWork(crawlWork)
                .seq(1L)
                .pageUrl(pageUrl)
                .resultValue(jsonResult)
                .state("SUCCESS")
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        crawlResultItemRepository.save(resultItem);
    }

    @Transactional
    public void updateCrawlWorkSuccess(CrawlWork crawlWork) {
        crawlWork.setState("SUCCESS");
        crawlWork.setCollectCount(1);
        crawlWork.setEndAt(LocalDateTime.now());
        crawlWork.setUpdateAt(LocalDateTime.now());

        crawlWorkRepository.save(crawlWork);
    }
}