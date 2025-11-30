package com.codehows.ksisbe.robotsValidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RobotsTxtParser {

    public static RobotsTxtRules parse(String content) {
        Map<String, List<Pattern>> disallowMap = new HashMap<>();
        List<Pattern> globalDisallow = new ArrayList<>();

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
            }
            else if ("disallow".equals(key)) {
                Pattern pattern = convertToRegexPattern(value);

                if (currentUserAgent == null) {
                    globalDisallow.add(pattern);
                } else {
                    disallowMap.get(currentUserAgent).add(pattern);
                }
            }
        }

        return new RobotsTxtRules(disallowMap, globalDisallow);
    }

    /**
     * robots.txt의 Disallow 경로를 Regex로 변환
     */
    private static Pattern convertToRegexPattern(String rule) {
        if (rule.equals("")) {
            // 빈 disallow는 "전부 허용" 의미
            return Pattern.compile("$^");
        }

        // 기본적으로 * 를 ".*" 로 치환
        String regex = rule.replace("*", ".*");

        return Pattern.compile(regex);
    }
}