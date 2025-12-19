package com.codehows.ksisbe.crawling.service;

import com.codehows.ksisbe.setting.entity.Setting;
import com.codehows.ksisbe.setting.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractDetailUrlsMulti {

    private final SettingRepository settingRepository;

    protected List<String> extractDetailUrls(WebDriver driver, Setting setting) {
        List<String> detailUrls = new ArrayList<>();

        String pagingType = setting.getPagingType();
        int maxPage = setting.getMaxPage();
        String listArea = setting.getListArea();
        String linkArea = setting.getLinkArea();
        String pagingArea = setting.getPagingArea();
        String pagingNextbtn = setting.getPagingNextbtn();

        switch (pagingType) {
            case "Numeric":
                int currentPageNum = 1;
                boolean hasNextPagingGroup = true;

                while (hasNextPagingGroup && currentPageNum <= maxPage) {
                    boolean clicked = clickpageNumber(driver, pagingArea, currentPageNum);
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

                    detailUrls.addAll(extractLinksFromListArea(driver, listArea, linkArea));

                    currentPageNum++;
                    if (currentPageNum > maxPage) {
                        break;
                    }
                }
                break;
            case "Next_Btn":
                boolean hasNextPage = true;
                while (hasNextPage) {
                    detailUrls.addAll(extractLinksFromListArea(driver, listArea, linkArea));

                    if (clickNextButton(driver, pagingNextbtn)) {
                        waitForPageLoad(driver, setting);
                    } else {
                        hasNextPage = false;
                    }
                }
                break;
            case "AJAX":
                int currentAjaxPage = 1;
                while (currentAjaxPage <= maxPage) {
                    detailUrls.addAll(extractLinksFromListArea(driver, listArea, linkArea));

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

                    currentAjaxPage++;
                }
                break;
        }
        return detailUrls;
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

    private List<String> extractLinksFromListArea(WebDriver driver, String listArea, String linkArea) {
        List<String> links = new ArrayList<>();

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
                if (!found.isEmpty()) {
                    for (int i = 0; i < found.size(); i++) {
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
//                            System.out.println("URL :" + currentUrl);
                        links.add(currentUrl);

                        // 다시 원래 페이지로 돌아가기
                        driver.navigate().back();

                        // 원래 페이지 로드 대기 (필요시)
                        wait.until(webDriver -> webDriver.getCurrentUrl().equals(originalUrl));
                    }
                    // 처음으로 발견된 우선순위만 사용하도록 break
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Flexible extract error: " + e.getMessage());
        }
        return links;
    }
}