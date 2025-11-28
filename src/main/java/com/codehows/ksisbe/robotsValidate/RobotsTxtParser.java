package com.codehows.ksisbe.robotsValidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobotsTxtParser {

    /**
     * robots.txt 문자열을 받아 파싱해 RobotsTxtRules 반환
     */
    public static RobotsTxtRules parse(String content) {
        Map<String, List<String>> disallowMap = new HashMap<>();
        List<String> globalDisallow = new ArrayList<>();

        String currentUserAgent = null;

        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(":", 2);
            if (parts.length < 2) continue;

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            if ("user-agent".equals(key)) {
                currentUserAgent = value.toLowerCase();
                disallowMap.putIfAbsent(currentUserAgent, new ArrayList<>());
            } else if ("disallow".equals(key)) {
                if (currentUserAgent == null) {
                    globalDisallow.add(value);
                } else {
                    disallowMap.get(currentUserAgent).add(value);
                }
            }
        }

        return new RobotsTxtRules(disallowMap, globalDisallow);
    }
}