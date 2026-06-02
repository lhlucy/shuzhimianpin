package com.lingshu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AIInterviewDimensionModel {

    static final String TECHNICAL_DEPTH = "technicalDepth";
    static final String PROJECT_RELEVANCE = "projectRelevance";
    static final String PROBLEM_SOLVING = "problemSolving";
    static final String COMMUNICATION_CLARITY = "communicationClarity";
    static final String JOB_MATCH = "jobMatch";

    private static final List<String> KEYS = List.of(
            TECHNICAL_DEPTH,
            PROJECT_RELEVANCE,
            PROBLEM_SOLVING,
            COMMUNICATION_CLARITY,
            JOB_MATCH
    );

    private AIInterviewDimensionModel() {
    }

    static Map<String, Double> parseDimensionScores(JsonNode node, double fallbackScore) {
        Map<String, Double> scores = defaultScores(fallbackScore);
        if (node == null || !node.isObject()) {
            return scores;
        }
        for (String key : KEYS) {
            if (node.has(key) && node.get(key).isNumber()) {
                scores.put(key, roundToOneDecimal(clampScore(node.get(key).asDouble(fallbackScore))));
            }
        }
        return scores;
    }

    static Map<String, Double> parseDimensionScores(Object value, double fallbackScore) {
        Map<String, Double> scores = defaultScores(fallbackScore);
        if (!(value instanceof Map<?, ?>)) {
            return scores;
        }
        Map<?, ?> raw = (Map<?, ?>) value;
        for (String key : KEYS) {
            Object rawValue = raw.get(key);
            Double score = parseNumber(rawValue);
            if (score != null) {
                scores.put(key, roundToOneDecimal(clampScore(score)));
            }
        }
        return scores;
    }

    static Map<String, Double> average(List<Map<String, Double>> scoreItems, double fallbackScore) {
        Map<String, Double> result = defaultScores(fallbackScore);
        if (scoreItems == null || scoreItems.isEmpty()) {
            return result;
        }
        for (String key : KEYS) {
            double sum = 0.0;
            int count = 0;
            for (Map<String, Double> scoreItem : scoreItems) {
                if (scoreItem != null && scoreItem.get(key) != null) {
                    sum += scoreItem.get(key);
                    count++;
                }
            }
            result.put(key, roundToOneDecimal(count == 0 ? fallbackScore : sum / count));
        }
        return result;
    }

    static Map<String, Double> defaultScores(double fallbackScore) {
        double score = roundToOneDecimal(clampScore(fallbackScore));
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String key : KEYS) {
            scores.put(key, score);
        }
        return scores;
    }

    static Map<String, Double> defaultWeights() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put(TECHNICAL_DEPTH, 30.0);
        weights.put(PROJECT_RELEVANCE, 25.0);
        weights.put(PROBLEM_SOLVING, 20.0);
        weights.put(COMMUNICATION_CLARITY, 15.0);
        weights.put(JOB_MATCH, 10.0);
        return weights;
    }

    static boolean isComplete(Map<String, Double> scores) {
        if (scores == null) {
            return false;
        }
        return KEYS.stream().allMatch(key -> scores.get(key) != null);
    }

    static boolean hasSpread(Map<String, Double> scores) {
        if (!isComplete(scores)) {
            return false;
        }
        double min = KEYS.stream().map(scores::get).mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = KEYS.stream().map(scores::get).mapToDouble(Double::doubleValue).max().orElse(0.0);
        return max - min >= 0.5;
    }

    static List<String> keys() {
        return KEYS;
    }

    static String labelOf(String key) {
        if (!StringUtils.hasText(key)) {
            return "";
        }
        switch (key) {
            case TECHNICAL_DEPTH:
                return "技术深度";
            case PROJECT_RELEVANCE:
                return "项目匹配";
            case PROBLEM_SOLVING:
                return "问题分析";
            case COMMUNICATION_CLARITY:
                return "表达清晰";
            case JOB_MATCH:
                return "岗位匹配";
            default:
                return key;
        }
    }

    private static Double parseNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String && StringUtils.hasText((String) value)) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
