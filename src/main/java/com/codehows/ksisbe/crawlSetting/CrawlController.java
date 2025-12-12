package com.codehows.ksisbe.crawlSetting;

import com.codehows.ksisbe.crawlSetting.dto.HighlightRequest;
import com.codehows.ksisbe.crawlSetting.dto.HighlightResponse;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class CrawlController {

    private final CrawlService crawlService;

    /**
     * ✅ 전체 페이지 미리보기 캡처
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewPage(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        try {
            Map<String, Object> data = crawlService.captureFullPageWithHtml(url);

            Map<String, Object> result = new HashMap<>();
            result.put("image", Base64.getEncoder().encodeToString((byte[]) data.get("image")));
            result.put("html", data.get("html"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/preview2")
    public ResponseEntity<Map<String, Object>> previewPage2(@RequestBody Map<String, String> req) {
        String url = req.get("url");

        try {
            Map<String, Object> data = crawlService.captureFullPageWithHtml2(url);

            Map<String, Object> result = new HashMap<>();
            result.put("image", Base64.getEncoder().encodeToString((byte[]) data.get("image")));
            result.put("html", data.get("html"));
            result.put("domRects", data.get("domRects")); // ★ 추가

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/preview/detail")
    public ResponseEntity<Map<String, Object>> detailPreviewPage(@RequestBody Map<String, String> req) {

        String url = req.get("url");
        String listAreaSelector = req.get("listArea");
        String detailLinkSelector = req.get("linkArea");

        WebDriver driver = null;

        try {
            // 1) 드라이버 생성
            driver = crawlService.createDriver();
            driver.get(url);
            Thread.sleep(1000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 2) 리스트 영역 찾기
            WebElement listRoot = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(listAreaSelector))
            );

            // 3) 리스트 내부의 상세링크 대표 a태그 찾기 (linkArea는 이걸 의미함)
            WebElement linkEl = listRoot.findElement(By.cssSelector(detailLinkSelector));

            if (linkEl == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "linkArea로 지정한 상세 링크 요소를 찾을 수 없습니다."));
            }

            // 4) 상세페이지로 이동 후 캡처
            Map<String, Object> data = crawlService.captureDetailPage(driver, linkEl);

            // 5) Base64 변환
            Map<String, Object> result = new HashMap<>();
            result.put("image", Base64.getEncoder().encodeToString((byte[]) data.get("image")));
            result.put("html", data.get("html"));
            result.put("domRects", data.get("domRects"));
            result.put("detailUrl", data.get("detailUrl"));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        } finally {
            if (driver != null) driver.quit();
        }
    }
}
