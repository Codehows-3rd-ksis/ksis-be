package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.crawling.entity.CrawlResultItem;
import com.codehows.ksisbe.crawling.entity.CrawlWork;
import com.codehows.ksisbe.crawling.repository.CrawlResultItemRepository;
import com.codehows.ksisbe.crawling.repository.CrawlWorkRepository;
import com.codehows.ksisbe.setting.entity.Conditions;
import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import com.codehows.ksisbe.status.service.CrawlProgressPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtractDetailUrlsMulti {

    private final SettingRepository settingRepository;
    private final CrawlResultItemRepository crawlResultItemRepository;
    private final CrawlWorkRepository crawlWorkRepository;
    private final CrawlingFailService crawlingFailService;
    private final CrawlProgressPushService crawlProgressPushService;
    private final CrawlResultService crawlResultService;

    protected int extractDetailUrls(CrawlWork crawlWork,  WebDriver driver, Setting setting) {
//        int failCount = 0;
        int collected = 0;
        int total = 0;
        int globalSeq = 1;
//        CrawlWork crawlWork = crawlWorkRepository.findByWorkId(workId)
//                .orElseThrow(EntityNotFoundException::new);

        String pagingType = setting.getPagingType();
        int maxPage = setting.getMaxPage();
        String listArea = setting.getListArea();
        String linkArea = setting.getLinkArea();
        String pagingArea = setting.getPagingArea();
        String pagingNextbtn = setting.getPagingNextbtn();

        int currentPage = 1;
        switch (pagingType) {
            case "Numeric":
                boolean hasNextPagingGroup = true;

                while (hasNextPagingGroup && currentPage <= maxPage) {
                    checkStop(crawlWork);
                    boolean clicked = clickpageNumber(driver, pagingArea, currentPage);
                    if (!clicked) {
                        if (clickNextButton(driver, pagingNextbtn)) {
                            waitForPageLoad(driver, setting);
                            continue; // 다음 숫자 버튼 그룹 로딩 후 재시도
                        } else {
                            hasNextPagingGroup = false;
                            break;
                        }
                    }
                    waitForPageLoad(driver, setting);

                    collected = crawlPageFromListArea(crawlWork, currentPage, driver, listArea, linkArea, setting, globalSeq);
                    System.out.println("Collected: " + collected);
                    total += collected;
                    globalSeq = total + 1;
                    System.out.println("Total: " + total);
                    currentPage++;
                    if (currentPage > maxPage) {
                        crawlWork.setCollectCount(crawlWork.getCollectCount());
                        crawlWorkRepository.saveAndFlush(crawlWork);
                        updateCollectProgress(crawlWork, 0, total);
                        crawlProgressPushService.pushCollect(crawlWork, null);
                        break;
                    }

                    if (collected == 0) {
                        // 더 이상 페이지에 데이터가 없음
                        crawlWork.setCollectCount(crawlWork.getCollectCount());
                        crawlWorkRepository.saveAndFlush(crawlWork);
                        updateCollectProgress(crawlWork, 0, total);
                        crawlProgressPushService.pushCollect(crawlWork, null);
                        break;
                    }
                }
                break;
            case "Next_Btn":
                boolean hasNextPage = true;
                while (hasNextPage) {
                    collected = crawlPageFromListArea(crawlWork, currentPage, driver, listArea, linkArea, setting, globalSeq);
                    total += collected;
                    checkStop(crawlWork);
                    failCount += crawlPageFromListArea(crawlWork, currentPage, driver, listArea, linkArea, setting);

                    if (clickNextButton(driver, pagingNextbtn)) {
                        waitForPageLoad(driver, setting);
                    } else {
                        hasNextPage = false;
                    }
                    currentPage++;

                    if (collected == 0) {
                        // 더 이상 페이지에 데이터가 없음
                        crawlWork.setCollectCount(crawlWork.getCollectCount());
                        crawlWorkRepository.saveAndFlush(crawlWork);
                        updateCollectProgress(crawlWork, 0, total);
                        crawlProgressPushService.pushCollect(crawlWork, null);
                        break;
                    }
                }
                break;
            case "AJAX":
                while (currentPage <= maxPage) {
                    collected = crawlPageFromListArea(crawlWork, currentPage, driver, listArea, linkArea, setting, globalSeq);
                    total += collected;
                    checkStop(crawlWork);
                    failCount += crawlPageFromListArea(crawlWork, currentPage, driver, listArea, linkArea, setting);

                    if (pagingNextbtn != null && !pagingNextbtn.isEmpty()) {
                        // 다음 AJAX 버튼 클릭
                        if (clickNextButton(driver, pagingNextbtn)) {
                            waitForAjaxLoad(driver, setting);
                        } else {
                            break;
                        }
                    } else {
                        // 버튼이 없으면 스크롤 방식 등, 필요시 구현
                        // 예시: 스크롤 후 기다림
                        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                        waitForAjaxLoad(driver, setting);
                    }

                    currentPage++;

                    if (collected == 0) {
                        // 더 이상 페이지에 데이터가 없음
                        crawlWork.setCollectCount(crawlWork.getCollectCount());
                        crawlWorkRepository.saveAndFlush(crawlWork);
                        updateCollectProgress(crawlWork, 0, total);
                        crawlProgressPushService.pushCollect(crawlWork, null);
                        break;
                    }
                }
                break;
        }
        return total;
    }

    private void waitForPageLoad(WebDriver driver, Setting setting) {
        new WebDriverWait(driver, Duration.ofSeconds(setting.getRate())).until(
                webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
    }

    private void waitForAjaxLoad(WebDriver driver, Setting setting) {
        new WebDriverWait(driver, Duration.ofSeconds(setting.getRate())).until(webDriver -> {
            try {
                Object active = ((JavascriptExecutor) webDriver)
                        .executeScript("return (typeof jQuery !== 'undefined') ? jQuery.active : 0");
                return active instanceof Long && (Long) active == 0L;
            } catch (Exception e) {
                // jQuery가 없으면 바로 true로 처리
                return true;
            }
        });
    }

    private boolean clickpageNumber(WebDriver driver, String pagingAreaSelector, int pageNum) {
        try {
            WebElement pagingArea = driver.findElement(By.cssSelector(pagingAreaSelector));
            List<WebElement> pagebuttons = pagingArea.findElements(By.cssSelector("a, strong, span, button, li"));
            for (WebElement btn : pagebuttons) {
                if (btn.getText().equals(String.valueOf(pageNum))) {
                    btn.click();
                    return true;
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return false;
    }

    private boolean clickNextButton(WebDriver driver, String pagingNextbtn) {
        try {
            WebElement nextBtn = driver.findElement(By.cssSelector(pagingNextbtn));
            if (nextBtn.isDisplayed() && nextBtn.isEnabled()) {
                nextBtn.click();
                return true;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return false;
    }

    private int crawlPageFromListArea(CrawlWork crawlWork, int pageNum, WebDriver driver, String listArea, String linkArea, Setting setting, int globalSeq) {
        int collectedCount = 0;
        int failCount = 0;

        try {
            WebElement listRoot = driver.findElement(By.cssSelector(listArea));

            // 1) linkAreaCss에서 마지막 태그 추출
            String[] parts = linkArea.split(">");
            String last = parts[parts.length - 1].trim(); // 예: a:nth-of-type(1)

            // 2) nth-of-type, 숫자 제거 → tag만 뽑기
            String pureTag = last.replaceAll(":.*", ""); // "a", "div", "li" 등

            // 3) 우선순위 탐색
            List<By> selectorPriority = List.of(
                    By.cssSelector(pureTag + "[href]"),       // a[href]
                    By.tagName("a"),                          // 모든 링크
                    By.cssSelector("li a"),                   // li 내부 링크
                    By.cssSelector("div a")                   // div 내부 링크
            );

            for (By selector : selectorPriority) {
                List<WebElement> found = listRoot.findElements(selector);
                int totalCount = setting.getMaxPage() * found.size();
                if (!found.isEmpty()) {
                    for (int i = 0; i < found.size(); i++) {
                    for (int i = 0; i < found.size(); i++, seq++) {
                        checkStop(crawlWork);

                        WebElement freshListRoot = driver.findElement(By.cssSelector(listArea));
                        List<WebElement> freshFound = freshListRoot.findElements(selector);

                        if (i >= freshFound.size()) break;

                        WebElement el = freshFound.get(i);

                        // 현재 페이지 URL 저장 (원래 페이지)
                        String originalUrl = driver.getCurrentUrl();

                        // 링크 클릭
                        el.click();

                        // 페이지 로드 대기 (예: 명시적 대기 사용)
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        wait.until(webDriver -> !webDriver.getCurrentUrl().equals(originalUrl));

                        // 현재 URL 가져오기
                        String currentUrl = driver.getCurrentUrl();

                        //디테일 페이지 수집
                        CrawlResultItem resultItem = null;
                        try{
                            Map<String, String> result = crawlDetailPage(driver, setting);
                            resultItem = saveResultItem(crawlWork, currentUrl, result, globalSeq);
                            resultItem = saveResultItem(crawlWork, currentUrl, result, found.size() * (pageNum-1) + seq);
                            checkStop(crawlWork);
                        } catch (CrawlStopException e) {
                            throw e;
                        } catch (Exception e) {
                            e.printStackTrace();
                            failCount++;
                            resultItem = crawlingFailService.saveFailedResultMulti(crawlWork.getWorkId(), currentUrl, (long)globalSeq );
                        }finally {
                            crawlWork.setCollectCount(crawlWork.getCollectCount() + 1);
                            String currentState = crawlWorkRepository.findState(crawlWork.getWorkId());
                            if (!"STOP_REQUEST".equals(currentState) && !"STOPPED".equals(currentState)) {
                                crawlWorkRepository.saveAndFlush(crawlWork);
                            }
                            updateCollectProgress(crawlWork, failCount, totalCount);
                            crawlProgressPushService.pushCollect(crawlWork, resultItem);
                            failCount = 0;
                            globalSeq++;
                        }

                        // 다시 원래 페이지로 돌아가기
                        driver.navigate().back();

                        // 원래 페이지 로드 대기 (필요시)
                        wait.until(webDriver -> webDriver.getCurrentUrl().equals(originalUrl));
                        collectedCount++;
                    }
                    // 처음으로 발견된 우선순위만 사용하도록 break
                    break;
                }
            }

        } catch (CrawlStopException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Flexible extract error: " + e.getMessage());
        }

        return collectedCount;
    }

    public void updateCollectProgress(CrawlWork crawlWork, int failCount, int totalCount) {
        String currentState = crawlWorkRepository.findState(crawlWork.getWorkId());
        if ("STOP_REQUEST".equals(currentState) || "STOPPED".equals(currentState)) {
            return; // 상태 업데이트 금지
        }

        int collectCount = crawlWork.getCollectCount();
        double progress = ((double) collectCount / totalCount) * 100;

        crawlWork.setProgress(progress);
        crawlWork.setFailCount(crawlWork.getFailCount() + failCount);
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

//        crawlWorkRepository.save(crawlWork);
        crawlResultService.updateCrawlWorkProgress(crawlWork);
        crawlProgressPushService.pushProgress(crawlWork);
    }

    private CrawlResultItem saveResultItem(CrawlWork crawlWork, String pageUrl, Map<String, String> resultMap, int seq) throws Exception {
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

        return crawlResultService.saveResultItemTransaction(item);
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
            } catch (java.util.NoSuchElementException e) {
                resultMap.put(c.getConditionsKey(), null);
            }
        }
        return resultMap;
    }

    private void checkStop(CrawlWork crawlWork) {
//        crawlWorkRepository.flush(); // 최신 상태 보장
//        CrawlWork fresh = crawlWorkRepository.findById(crawlWork.getWorkId())
//                .orElseThrow();

        String state = crawlWorkRepository.findState(crawlWork.getWorkId());
//        if ("STOP_REQUEST".equals(fresh.getState())) {
        if ("STOP_REQUEST".equals(state)) {
                throw new CrawlStopException();
        }
    }
}