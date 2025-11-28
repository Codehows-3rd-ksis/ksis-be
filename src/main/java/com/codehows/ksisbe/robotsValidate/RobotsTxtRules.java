package com.codehows.ksisbe.robotsValidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobotsTxtRules {

    private final Map<String, List<String>> disallowMap = new HashMap<>();
    private final List<String> globalDisallow = new ArrayList<>();

    public RobotsTxtRules(Map<String, List<String>> disallowMap, List<String> globalDisallow) {
        this.disallowMap.putAll(disallowMap);
        this.globalDisallow.addAll(globalDisallow);
    }

    /**
     * 주어진 userAgent에 대해 path가 허용되는지 검사
     */
    public boolean isAllowed(String userAgent, String path) {
        if (userAgent == null) userAgent = "";
        userAgent = userAgent.toLowerCase();

        if (disallowMap.containsKey(userAgent)) {
            return isPathAllowed(disallowMap.get(userAgent), path);
        }
        return isPathAllowed(globalDisallow, path);
    }

    private boolean isPathAllowed(List<String> disallowList, String path) {
        if (disallowList == null || disallowList.isEmpty()) return true;
        for (String disallowedPath : disallowList) {
            if (disallowedPath.equals("")) continue; // 빈 disallow는 허용 의미
            if (path.startsWith(disallowedPath)) {
                return false;
            }
        }
        return true;
    }
}
