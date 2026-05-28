package com.lingshu.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.entity.AIInterviewAssessment;
import com.lingshu.entity.AIInterview;
import com.lingshu.entity.LoginRecord;
import com.lingshu.entity.Question;
import com.lingshu.entity.QuestionBank;
import com.lingshu.entity.User;
import com.lingshu.mapper.AIInterviewMapper;
import com.lingshu.mapper.AIInterviewAssessmentMapper;
import com.lingshu.mapper.LoginRecordMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.QuestionBankMapper;
import com.lingshu.mapper.UserMapper;
import com.lingshu.service.admin.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 管理端仪表盘服务实现类
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final QuestionMapper questionMapper;
    private final QuestionBankMapper questionBankMapper;
    private final UserMapper userMapper;
    private final AIInterviewAssessmentMapper aiInterviewAssessmentMapper;
    private final AIInterviewMapper aiInterviewMapper;
    private final LoginRecordMapper loginRecordMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取题目总数
        long questionCount = questionMapper.selectCount(null);
        stats.put("questionCount", questionCount);

        // 获取题库总数
        long bankCount = questionBankMapper.selectCount(null);
        stats.put("bankCount", bankCount);

        // 获取用户总数
        long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();

        List<Question> latestQuestions = questionMapper.selectList(
                new QueryWrapper<Question>().orderByDesc("created_at").last("LIMIT 5"));
        for (Question question : latestQuestions) {
            activities.add(createActivity(
                    "题目管理",
                    "创建或更新了题目「" + nullToDefault(question.getTitle(), "未命名题目") + "」",
                    "系统",
                    question.getUpdatedAt() != null ? question.getUpdatedAt() : question.getCreatedAt()));
        }

        List<QuestionBank> latestBanks = questionBankMapper.selectList(
                new QueryWrapper<QuestionBank>().orderByDesc("updated_at").last("LIMIT 5"));
        for (QuestionBank bank : latestBanks) {
            activities.add(createActivity(
                    "题库管理",
                    "创建或更新了题库「" + nullToDefault(bank.getTitle(), "未命名题库") + "」",
                    "系统",
                    bank.getUpdatedAt() != null ? bank.getUpdatedAt() : bank.getCreatedAt()));
        }

        List<User> latestUsers = userMapper.selectList(
                new QueryWrapper<User>().orderByDesc("create_time").last("LIMIT 5"));
        for (User user : latestUsers) {
            activities.add(createActivity(
                    "用户管理",
                    "新增用户「" + nullToDefault(user.getUsername(), "未命名用户") + "」",
                    "系统",
                    user.getCreateTime()));
        }

        activities.removeIf(activity -> activity.get("time") == null);
        activities.sort(Comparator.comparing(
                activity -> (LocalDateTime) activity.get("time"),
                Comparator.nullsLast(Comparator.reverseOrder())));

        return activities.size() > 10 ? activities.subList(0, 10) : activities;
    }

    @Override
    public Map<String, Integer> getQuestionDifficultyDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("EASY", 0);
        distribution.put("MEDIUM", 0);
        distribution.put("HARD", 0);

        List<Map<String, Object>> rows = questionMapper.selectMaps(
                new QueryWrapper<Question>()
                        .select("difficulty", "COUNT(*) AS count")
                        .eq("is_visible", true)
                        .groupBy("difficulty"));

        for (Map<String, Object> row : rows) {
            String difficulty = Objects.toString(row.get("difficulty"), "");
            distribution.put(difficulty, toInteger(row.get("count")));
        }

        return distribution;
    }

    @Override
    public Map<String, Integer> getQuestionTypeDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();

        List<Map<String, Object>> rows = questionMapper.selectMaps(
                new QueryWrapper<Question>()
                        .select("COALESCE(question_type, '未分类') AS questionType", "COUNT(*) AS count")
                        .eq("is_visible", true)
                        .groupBy("question_type")
                        .orderByDesc("count"));

        for (Map<String, Object> row : rows) {
            String type = Objects.toString(row.get("questionType"), "未分类");
            distribution.put(type, toInteger(row.get("count")));
        }

        return distribution;
    }

    @Override
    public List<Map<String, Object>> getWeaknessTags() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        List<AIInterviewAssessment> assessments = aiInterviewAssessmentMapper.selectList(
                new QueryWrapper<AIInterviewAssessment>().orderByDesc("created_at").last("LIMIT 200"));

        for (AIInterviewAssessment assessment : assessments) {
            for (String weakness : readStringList(assessment.getWeaknesses())) {
                counts.merge(weakness, 1, Integer::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted((left, right) -> {
                    int byCount = Integer.compare(right.getValue(), left.getValue());
                    return byCount != 0 ? byCount : left.getKey().compareTo(right.getKey());
                })
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("keyword", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("suggestion", "建议补充“" + entry.getKey() + "”相关题目与训练案例");
                    return item;
                })
                .toList();
    }

    @Override
    public Map<String, Object> getOutcomeMetrics() {
        long userCount = userMapper.selectCount(null);
        long completedInterviewCount = aiInterviewMapper.selectCount(
                new QueryWrapper<AIInterview>().eq("status", "COMPLETED"));

        List<Map<String, Object>> perUserRows = aiInterviewMapper.selectMaps(
                new QueryWrapper<AIInterview>()
                        .select("user_id AS userId", "COUNT(*) AS interviewCount")
                        .groupBy("user_id"));

        double averageInterviewPerUser = userCount == 0
                ? 0.0
                : (double) completedInterviewCount / userCount;

        List<Map<String, Object>> firstLatestRows = aiInterviewMapper.selectMaps(
                new QueryWrapper<AIInterview>()
                        .select("user_id AS userId", "total_score AS score", "COALESCE(ended_at, updated_at, created_at) AS completedAt")
                        .eq("status", "COMPLETED")
                        .isNotNull("total_score")
                        .orderByAsc("user_id")
                        .orderByAsc("COALESCE(ended_at, updated_at, created_at)"));

        Map<String, List<Double>> userScores = new LinkedHashMap<>();
        for (Map<String, Object> row : firstLatestRows) {
            String userId = Objects.toString(row.get("userId"), "");
            if (StringUtils.hasText(userId)) {
                userScores.computeIfAbsent(userId, ignored -> new ArrayList<>()).add(toDouble(row.get("score")));
            }
        }

        List<Double> scoreChanges = userScores.values().stream()
                .filter(scores -> scores.size() >= 2)
                .map(scores -> scores.get(scores.size() - 1) - scores.get(0))
                .toList();
        double averageScoreLift = scoreChanges.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        LocalDateTime now = LocalDateTime.now();
        long active7d = countActiveUsers(now.minusDays(7));
        long active30d = countActiveUsers(now.minusDays(30));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("averageInterviewPerUser", roundToOneDecimal(averageInterviewPerUser));
        result.put("averageScoreLift", roundToOneDecimal(averageScoreLift));
        result.put("scoreLiftSampleUsers", scoreChanges.size());
        result.put("activeUser7d", active7d);
        result.put("activeUser30d", active30d);
        result.put("retention7dRate", userCount == 0 ? 0.0 : roundToOneDecimal(active7d * 100.0 / userCount));
        result.put("retention30dRate", userCount == 0 ? 0.0 : roundToOneDecimal(active30d * 100.0 / userCount));
        result.put("satisfactionScore", 0.0);
        result.put("satisfactionSampleCount", 0);
        result.put("interviewUserCount", perUserRows.size());
        result.put("completedInterviewCount", completedInterviewCount);
        return result;
    }

    /**
     * 创建活动记录
     */
    private Map<String, Object> createActivity(String title, String description, String user, LocalDateTime time) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("title", title);
        activity.put("description", description);
        activity.put("user", user);
        activity.put("time", time);
        return activity;
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(Objects.toString(value, "0"));
    }

    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        return Double.parseDouble(Objects.toString(value, "0"));
    }

    private long countActiveUsers(LocalDateTime since) {
        List<Map<String, Object>> rows = loginRecordMapper.selectMaps(
                new QueryWrapper<LoginRecord>()
                        .select("COUNT(DISTINCT user_id) AS count")
                        .ge("login_time", since)
                        .eq("success", true));
        if (rows.isEmpty()) {
            return 0L;
        }
        return toInteger(rows.get(0).get("count"));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private List<String> readStringList(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(rawJson, new TypeReference<List<String>>() {});
            return values.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .toList();
        } catch (Exception ignored) {
            return StringUtils.hasText(rawJson) ? List.of(rawJson.trim()) : List.of();
        }
    }

}
