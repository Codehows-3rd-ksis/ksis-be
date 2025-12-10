package com.codehows.ksisbe.crawlSetting;

import com.codehows.ksisbe.crawlSetting.dto.HighlightRequest;
import com.codehows.ksisbe.crawlSetting.dto.HighlightResponse;
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

        try {

            // 1) 상세페이지 URL만 가져오기
            String detailUrl = crawlService.extractDetailUrl(
                    url, listAreaSelector, detailLinkSelector
            );

            // 2) 해당 상세페이지 미리보기 생성
            Map<String, Object> data = crawlService.captureFullPageWithHtml2(detailUrl);

            // 3) Base64 변환 + 결과 조립
            Map<String, Object> result = new HashMap<>();
            result.put("image", Base64.getEncoder().encodeToString((byte[]) data.get("image")));
            result.put("html", data.get("html"));
            result.put("domRects", data.get("domRects"));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
