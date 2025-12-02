package com.codehows.ksisbe.crawlSetting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
