package com.codehows.ksisbe.crawlSetting;

import com.codehows.ksisbe.crawlSetting.dto.HighlightResponse;
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


//    public Map<String, Object> captureFullPageWithHtml(String url) throws Exception {
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless=new");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--hide-scrollbars");
//
//        ChromeDriver driver = new ChromeDriver(options);
//
//        try {
//            driver.get(url);
//
//            // 🚀 페이지 로드 및 안정화 대기 시간 충분히 부여
//            Thread.sleep(3000);
//
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//
//            // 애니메이션 멈춤 (렌더링 안정화에 도움)
//            driver.executeCdpCommand("Animation.setPlaybackRate", Map.of("playbackRate", 0));
//            // getLayoutMetrics 명령은 필요 없을 수 있으나, 명시적 호출로 레이아웃 확정을 유도
//            driver.executeCdpCommand("Page.getLayoutMetrics", Map.of());
//
//            // 뷰포트 크기 설정
//            int viewportWidth = 1920;
//            int viewportHeight = 1080;
//            Map<String, Object> metrics = new HashMap<>();
//            metrics.put("width", viewportWidth);
//            metrics.put("height", viewportHeight);
//            metrics.put("deviceScaleFactor", 0);
//            metrics.put("mobile", false);
//            driver.executeCdpCommand("Emulation.setDeviceMetricsOverride", metrics);
//
//            // 🎯 캡처 직전에 다시 한번 최상단으로 스크롤 명령 및 대기
//            js.executeScript("window.scrollTo(0,0)");
//            Thread.sleep(1500);
//
//            // HTML 소스 가져오기
//            String html = driver.getPageSource();
//
//            // CDP 명령어를 이용한 전체 페이지 스크린샷 캡처
//            Map<String, Object> captureParams = new HashMap<>();
//            captureParams.put("format", "png");
//            captureParams.put("captureBeyondViewport", true);
//            captureParams.put("fromSurface", true);
//
//            Map<String, Object> cdpResult = driver.executeCdpCommand("Page.captureScreenshot", captureParams);
//
//            // Base64 인코딩된 이미지 데이터를 바이트 배열로 디코딩
//            String base64Image = (String) cdpResult.get("data");
//            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
//
//            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
//            System.out.println("Captured image size: " + img.getWidth() + "x" + img.getHeight());
//
//            // 결과 맵에 html과 이미지 넣기
//            Map<String, Object> result = new HashMap<>();
//            result.put("html", html);
//            result.put("image", imageBytes);
//
//            return result;
//
//        } finally {
//            driver.quit();
//        }
//    }
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

}