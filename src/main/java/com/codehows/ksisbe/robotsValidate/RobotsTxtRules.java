package com.codehows.ksisbe.robotsValidate;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RobotsTxtRules {

    private final Map<String, List<Pattern>> disallowMap;
    private final Map<String, List<Pattern>> allowMap;

    private final List<Pattern> globalDisallow;
    private final List<Pattern> globalAllow;

    public RobotsTxtRules(
            Map<String, List<Pattern>> disallowMap,
            Map<String, List<Pattern>> allowMap,
            List<Pattern> globalDisallow,
            List<Pattern> globalAllow
    ) {
        this.disallowMap = disallowMap;
        this.allowMap = allowMap;
        this.globalDisallow = globalDisallow;
        this.globalAllow = globalAllow;
    }

    public boolean isAllowed(String userAgent, String path) {
        userAgent = (userAgent == null ? "" : userAgent.toLowerCase());

        List<Pattern> disList = disallowMap.getOrDefault(userAgent, disallowMap.getOrDefault("*", globalDisallow));
        List<Pattern> allowList = allowMap.getOrDefault(userAgent, allowMap.getOrDefault("*", globalAllow));

        return evaluateRules(disList, allowList, path);
    }

    private boolean evaluateRules(List<Pattern> disallow, List<Pattern> allow, String path) {
        int longestDis = matchLength(disallow, path);
        int longestAllow = matchLength(allow, path);

        // Allow 규칙이 더 길게 매칭되면 허용
        if (longestAllow > longestDis) return true;

        // disallow가 걸리면 차단
        if (longestDis > 0) return false;

        // 아무 것도 안 걸리면 허용
        return true;
    }

    private int matchLength(List<Pattern> list, String path) {
        int max = 0;
        for (Pattern p : list) {
            var m = p.matcher(path);
            if (m.find()) {
                max = Math.max(max, m.end());
            }
        }
        return max;
    }
}
