package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StartSingleCrawlingService {

    private final WebDriverFactory webDriverFactory;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;


    public void startSingleCrawling(Setting setting, User user) {
        CrawlWork crawlWork = CrawlWork.builder()
                .setting(setting)
                .startedBy(user)
                .type("단일")
                .state("RUNNING")
                .failCount(0)
                .totalCount(1)  // 단일 수집이므로 1
                .collectCount(0)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .startAt(LocalDateTime.now())
                .isDelete("N")
                .build();

        crawlWorkRepository.save(crawlWork);

        WebDriver driver = null;
        try {

            if (setting.getUserAgent() != null) {
                driver = webDriverFactory.createDriver(setting.getUserAgent());
                driver.get(setting.getUrl());
            }
            else {
                throw new RuntimeException("유효한 유저에이전트입니다.");
            }

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
                    }
                    else if ("href".equals(attr)) {
                        value = element.getAttribute("href");
                    }
                    else if ("src".equals(attr)) {
                        value = element.getAttribute("src");
                    }
                    else {
                        value = element.getAttribute(attr);
                    }
                    resultMap.put(c.getConditionsKey(), value);
                }catch (NoSuchElementException e) {
                    resultMap.put(c.getConditionsKey(), null);
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResult = objectMapper.writeValueAsString(resultMap);

            CrawlResultItem resultItem = CrawlResultItem.builder()
                    .crawlWork(crawlWork)
                    .seq(1L)  // 단일 수집이므로 seq = 1
                    .pageUrl(setting.getUrl())
                    .resultValue(jsonResult)
                    .state("SUCCESS")
                    .createAt(LocalDateTime.now())
                    .updateAt(LocalDateTime.now())
                    .build();
            crawlResultItemRepository.save(resultItem);

            // 7. 작업 상태 업데이트
            crawlWork.setState("SUCCESS");
            crawlWork.setCollectCount(1);
            crawlWork.setEndAt(LocalDateTime.now());
            crawlWork.setUpdateAt(LocalDateTime.now());
            crawlWorkRepository.save(crawlWork);


        } catch (Exception e) {
            // 실패 처리
            crawlWork.setState("FAILED");
            crawlWork.setFailCount(1);
            crawlWork.setEndAt(LocalDateTime.now());
            crawlWork.setUpdateAt(LocalDateTime.now());
            crawlWorkRepository.save(crawlWork);
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }finally {
            if (driver !=null ) {
                driver.quit();
            }
        }
    }
}