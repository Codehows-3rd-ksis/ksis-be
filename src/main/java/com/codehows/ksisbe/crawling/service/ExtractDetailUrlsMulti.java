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

    private WebDriver driver;
    private SettingRepository settingRepository;

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
                    if (!isPageNumberVisible(driver, pagingArea, currentPageNum)) {
                        if (clickNextButton(driver, pagingNextbtn)) {
                            waitForPageLoad(driver, setting);
                        } else {
                            hasNextPagingGroup = false;
                        }
                    }
                }
        }
        return detailUrls;
    }

    private void waitForPageLoad(WebDriver driver, Setting setting) {
        new WebDriverWait(driver, Duration.ofSeconds(setting.getRate())).until(
                webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
    }

    private boolean isPageNumberVisible(WebDriver driver, String pagingAreaSelector, int pageNum) {
        try {
            WebElement pagingArea = driver.findElement(By.cssSelector(pagingAreaSelector));
            List<WebElement> pageButtons = pagingArea.findElements(By.tagName("a, strong, span, button, li"));
            for (WebElement btn : pageButtons) {
                if (btn.getText().equals(String.valueOf(pageNum))) {
                    return btn.isDisplayed();
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return false;
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
                    for (WebElement el : found) {
                        String url = el.getAttribute("href");
                        if (url != null && !url.isEmpty()) {
                            links.add(url);
                        }
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