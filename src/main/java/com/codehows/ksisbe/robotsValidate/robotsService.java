package com.codehows.ksisbe.robotsValidate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class robotsService {

    private final RestTemplate restTemplate = new RestTemplate();

    public RobotValidationResponse validate(String pageUrl, String userAgent) {
        String domain = extractDomain(pageUrl);
        String robotsUrl = domain + "/robots.txt";

        String robotsTxt = fetchRobotsTxt(robotsUrl);
        if (robotsTxt.isEmpty()) {
            return new RobotValidationResponse(true, "robots.txt 없음", "크롤링 허용");
        }

        RobotsTxtRules rules = RobotsTxtParser.parse(robotsTxt);

        String path = extractPath(pageUrl);

        boolean allowed = rules.isAllowed(userAgent, path);

        return RobotValidationResponse.builder()
                .allow(allowed)
                .robotsTxt(robotsTxt)
                .message(allowed ? "크롤링이 허용됩니다." : "robots.txt에 의해 차단되었습니다.")
                .build();
    }

    private String extractDomain(String url) {
        URI uri = URI.create(url);
        return uri.getScheme() + "://" + uri.getHost();
    }

    private String extractPath(String url) {
        URI uri = URI.create(url);
        String path = uri.getRawPath();
        if (uri.getRawQuery() != null) {
            path += "?" + uri.getRawQuery();
        }
        return path;
    }

    private String fetchRobotsTxt(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return "";
        }
    }
}

