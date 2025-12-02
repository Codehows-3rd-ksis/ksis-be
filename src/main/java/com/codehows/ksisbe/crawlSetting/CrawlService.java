package com.codehows.ksisbe.crawlSetting;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

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

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Thread.sleep(3000);

            // ✅ HTML 소스 추출
            String html = driver.getPageSource();

            // ✅ 페이지 전체 높이 계산
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long scrollHeight = (Long) js.executeScript("return document.body.scrollHeight");

            // ✅ 전체 스크린샷 병합
            BufferedImage combined = new BufferedImage(1920, (int) scrollHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = combined.createGraphics();

            long scrolled = 0;
            while (scrolled < scrollHeight) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(bytes));

                int height = img.getHeight();
                if (scrolled + height > scrollHeight) {
                    height = (int) (scrollHeight - scrolled);
                    img = img.getSubimage(0, img.getHeight() - height, img.getWidth(), height);
                }

                g2d.drawImage(img, 0, (int) scrolled, null);
                scrolled += height;
                if (scrolled < scrollHeight)
                    js.executeScript("window.scrollTo(0, arguments[0]);", scrolled);

                Thread.sleep(600);
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
}