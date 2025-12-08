package com.codehows.ksisbe.crawlSetting;

import com.codehows.ksisbe.crawlSetting.dto.DomRect;
import com.codehows.ksisbe.crawlSetting.dto.HighlightResponse;
import com.codehows.ksisbe.crawlSetting.dto.PreviewResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    private int toInt(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        throw new IllegalArgumentException("Unexpected JS return type: " + value.getClass());
    }

    public HighlightResponse getRect(String url, String cssSelector) throws Exception {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(2000);

            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500);

            // ✅ 페이지 전체 높이 계산
            long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");

            // --- element 찾기 ---
            WebElement element = driver.findElement(By.cssSelector(cssSelector));

            // DOM 기준 boundingClientRect
            Object rectX = js.executeScript("return arguments[0].getBoundingClientRect().x;", element);
            Object rectY = js.executeScript("return arguments[0].getBoundingClientRect().y;", element);
            Object rectW = js.executeScript("return arguments[0].getBoundingClientRect().width;", element);
            Object rectH = js.executeScript("return arguments[0].getBoundingClientRect().height;", element);

            // ★ 전체 페이지 기준 절대 좌표로 변환
            // 이유: boundingClientRect.y 는 "현재 뷰포트 내" 위치이기 때문에 스크롤량을 더해줘야 함
            Object absoluteY = js.executeScript(
                    "return window.pageYOffset + arguments[0].getBoundingClientRect().top;",
                    element
            );

            // Rect 결과 구성
            HighlightResponse res = new HighlightResponse();
            res.setX(toInt(rectX));
            res.setY(toInt(absoluteY));
            res.setWidth(toInt(rectW));
            res.setHeight(toInt(rectH));

            return res;

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

}