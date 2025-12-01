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

        String currentAgent = null;

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
                    currentAgent = value.toLowerCase();
                    disallowMap.putIfAbsent(currentAgent, new ArrayList<>());
                    allowMap.putIfAbsent(currentAgent, new ArrayList<>());
                    break;

                case "disallow":
                    Pattern dis = convertToRegex(value);
                    if (currentAgent == null) globalDisallow.add(dis);
                    else disallowMap.get(currentAgent).add(dis);
                    break;

                case "allow":
                    Pattern allow = convertToRegex(value);
                    if (currentAgent == null) globalAllow.add(allow);
                    else allowMap.get(currentAgent).add(allow);
                    break;
            }
        }

        return new RobotsTxtRules(disallowMap, allowMap, globalDisallow, globalAllow);
    }

    private static Pattern convertToRegex(String rule) {
        if (rule.equals("")) return Pattern.compile("$^"); // always false
        return Pattern.compile(rule.replace("*", ".*"));
    }
}