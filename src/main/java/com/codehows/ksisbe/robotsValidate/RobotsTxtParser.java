package com.codehows.ksisbe.robotsValidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RobotsTxtParser {

    public static RobotsTxtRules parse(String content) {
        Map<String, List<Pattern>> disallowMap = new HashMap<>();
        Map<String, List<Pattern>> allowMap = new HashMap<>();
        List<Pattern> globalDisallow = new ArrayList<>();
        List<Pattern> globalAllow = new ArrayList<>();

        // 기존: String currentAgent
        // 변경: 여러 User-agent 를 저장
        List<String> currentAgents = new ArrayList<>();

        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(":", 2);
            if (parts.length < 2) continue;

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            switch (key) {
                case "user-agent":
                    String agent = value.toLowerCase();

                    // 새로운 그룹이 시작된 경우 판단
                    // 규칙 없이 user-agent만 연속해서 나오면 같은 그룹
                    if (!currentAgents.isEmpty()) {
                        String prev = currentAgents.get(currentAgents.size() - 1);
                        if (!disallowMap.get(prev).isEmpty() || !allowMap.get(prev).isEmpty()) {
                            // 이전 그룹에 규칙이 있었으면 새로운 그룹 시작
                            currentAgents.clear();
                        }
                    }

                    currentAgents.add(agent);

                    // agent별 리스트 생성
                    disallowMap.putIfAbsent(agent, new ArrayList<>());
                    allowMap.putIfAbsent(agent, new ArrayList<>());
                    break;

                case "disallow":
                    Pattern dis = convertToRegex(value);

                    if (currentAgents.isEmpty()) {
                        globalDisallow.add(dis);
                    } else {
                        for (String a : currentAgents) {
                            disallowMap.get(a).add(dis);
                        }
                    }
                    break;

                case "allow":
                    Pattern allow = convertToRegex(value);

                    if (currentAgents.isEmpty()) {
                        globalAllow.add(allow);
                    } else {
                        for (String a : currentAgents) {
                            allowMap.get(a).add(allow);
                        }
                    }
                    break;
            }
        }

        return new RobotsTxtRules(disallowMap, allowMap, globalDisallow, globalAllow);
    }

    private static Pattern convertToRegex(String rule) {
        if (rule.equals("")) return Pattern.compile("$^");
        return Pattern.compile(rule.replace("*", ".*"));
    }
}

