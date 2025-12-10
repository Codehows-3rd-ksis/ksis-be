package com.codehows.ksisbe.crawlSetting;

import com.codehows.ksisbe.crawlSetting.dto.DomRect;
import com.codehows.ksisbe.crawlSetting.dto.HighlightResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlService {
    /**
     * HTML + 전체 페이지 캡처 동시 수행
     */
    public Map<String, Object> captureFullPageWithHtml(String url) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        ChromeDriver  driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(3000);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // ✅ HTML 소스 추출
            String html = driver.getPageSource();

            // ✅ 페이지 전체 높이 계산
            long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");

            // ✅ 전체 스크린샷 병합
            BufferedImage combined = new BufferedImage(1920, (int) scrollHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = combined.createGraphics();

            long scrolled = 0;
            while (scrolled < scrollHeight) {

                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(bytes));

                int height = img.getHeight();

                // 마지막 조각 자투리 처리
                if (scrolled + height > scrollHeight) {
                    height = (int) (scrollHeight - scrolled);
                    img = img.getSubimage(0, img.getHeight() - height, img.getWidth(), height);
                }

                // 이어붙이기
                g2d.drawImage(img, 0, (int) scrolled, null);

                scrolled += height;

                if (scrolled < scrollHeight) {
                    js.executeScript("window.scrollTo(0, arguments[0]);", scrolled);
                    Thread.sleep(500);
                }
            }

            g2d.dispose();

            // ✅ 이미지 바이트 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(combined, "png", baos);

            // ✅ 결과 묶어서 반환
            Map<String, Object> result = new HashMap<>();
            result.put("html", html);
            result.put("image", baos.toByteArray());
            return result;

        } finally {
            driver.quit();
        }

    }

    public Map<String, Object> captureFullPageWithHtml2(String url) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        ChromeDriver  driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(3000);

            JavascriptExecutor js = (JavascriptExecutor) driver;

            // ✅ HTML 소스 추출
            String html = driver.getPageSource();

            // ✅ 페이지 전체 높이 계산
            long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");

            // ✅ 전체 스크린샷 병합
            BufferedImage combined = new BufferedImage(1920, (int) scrollHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = combined.createGraphics();

            long scrolled = 0;
            while (scrolled < scrollHeight) {

                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(bytes));

                int height = img.getHeight();

                // 마지막 조각 자투리 처리
                if (scrolled + height > scrollHeight) {
                    height = (int) (scrollHeight - scrolled);
                    img = img.getSubimage(0, img.getHeight() - height, img.getWidth(), height);
                }

                // 이어붙이기
                g2d.drawImage(img, 0, (int) scrolled, null);

                scrolled += height;

                if (scrolled < scrollHeight) {
                    js.executeScript("window.scrollTo(0, arguments[0]);", scrolled);
                    Thread.sleep(500);
                }
            }

            g2d.dispose();

            // ✅ 이미지 바이트 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(combined, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // ① 모든 DOM 노드의 좌표 가져오기
            String script = """
                function getCssSelector(el) {
                    if (!el || el.nodeType !== 1) return null;

                    const meaningfulClassList = ['t1', 'wrap1texts', 'unique-class-name'];

                    const path = [];
                    while (el && el.nodeType === 1) {
                        let selector = el.tagName.toLowerCase();

                        if (el.id) {
                            selector += '#' + el.id;
                            path.unshift(selector);
                            break;
                        }

                        const classes = Array.from(el.classList);
                        const meaningfulClasses = classes.filter(c => meaningfulClassList.includes(c));

                        if (meaningfulClasses.length > 0) {
                            selector += '.' + meaningfulClasses.join('.');
                            path.unshift(selector);
                            break;
                        }

                        // nth-of-type 계산
                        let nth = 1;
                        let sibling = el;
                        while (sibling = sibling.previousElementSibling) {
                            if (sibling.tagName === el.tagName) nth++;
                        }
                        selector += ':nth-of-type(' + nth + ')';

                        path.unshift(selector);
                        el = el.parentElement;
                    }

                    return path.join(' > ');
                }

                return Array.from(document.querySelectorAll('*')).map(el => {
                    const r = el.getBoundingClientRect();
                    return {
                        selector: getCssSelector(el),
                        x: r.x,
                        y: r.y + window.pageYOffset,
                        width: r.width,
                        height: r.height
                    };
                });
            """;

            List<Map<String, Object>> rects = (List<Map<String, Object>>) js.executeScript(script);

            // ---------- DTO 변환 ----------
            List<DomRect> domRectList = rects.stream().map(m -> {
                DomRect dr = new DomRect();
                dr.setSelector((String) m.get("selector"));
                dr.setX(((Number) m.get("x")).intValue());
                dr.setY(((Number) m.get("y")).intValue());
                dr.setWidth(((Number) m.get("width")).intValue());
                dr.setHeight(((Number) m.get("height")).intValue());
                return dr;
            }).toList();

            // ---------- 최종 반환 ----------
            Map<String, Object> result = new HashMap<>();
            result.put("html", html);
            result.put("image", imageBytes);
            result.put("domRects", domRectList);

            return result;

        } finally {
            driver.quit();
        }

    }


    public String extractDetailUrl(String url, String listAreaSelector, String detailLinkSelector) throws Exception {

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");

        ChromeDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(2000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 리스트 영역
            WebElement listArea = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(listAreaSelector))
            );

            // 상세 링크
            WebElement link = listArea.findElement(By.cssSelector(detailLinkSelector));

            String originalWindow = driver.getWindowHandle();
            link.click();
            Thread.sleep(1500);

            // 새 창 처리
            for (String win : driver.getWindowHandles()) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                }
            }

            // 최종 상세 페이지 URL
            return driver.getCurrentUrl();

        } finally {
            driver.quit();
        }
    }
}