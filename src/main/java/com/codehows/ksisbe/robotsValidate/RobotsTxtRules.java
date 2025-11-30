package com.codehows.ksisbe.robotsValidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RobotsTxtRules {

    private final Map<String, List<Pattern>> disallowMap = new HashMap<>();
    private final List<Pattern> globalDisallow = new ArrayList<>();

    public RobotsTxtRules(Map<String, List<Pattern>> disallowMap, List<Pattern> globalDisallow) {
        this.disallowMap.putAll(disallowMap);
        this.globalDisallow.addAll(globalDisallow);
    }

    public boolean isAllowed(String userAgent, String path) {
        if (userAgent == null) userAgent = "";
        userAgent = userAgent.toLowerCase();

        if (disallowMap.containsKey(userAgent)) {
            return isPathAllowed(disallowMap.get(userAgent), path);
        }
        if (disallowMap.containsKey("*")) {
            return isPathAllowed(disallowMap.get("*"), path);
        }
        return isPathAllowed(globalDisallow, path);
    }

    private boolean isPathAllowed(List<Pattern> disallowList, String path) {
        if (disallowList == null || disallowList.isEmpty()) return true;

        for (Pattern pattern : disallowList) {
            String patternStr = pattern.pattern();

            // 정확히 "/" 일 때만 path도 "/" 인 경우 차단
            if (patternStr.equals("/")) {
                if (path.equals("/")) {
                    return false;
                }
                // "/"가 아니면 계속 다음 검사
                continue;
            }

            // 그 외 패턴은 find() 로 검사
            if (pattern.matcher(path).find()) {
                return false;
            }
        }

        return true;
    }
}
