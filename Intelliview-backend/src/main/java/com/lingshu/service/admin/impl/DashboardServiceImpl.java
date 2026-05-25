package com.lingshu.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lingshu.entity.Paper;
import com.lingshu.entity.Question;
import com.lingshu.entity.QuestionBank;
import com.lingshu.entity.User;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.PaperMapper;
import com.lingshu.mapper.QuestionBankMapper;
import com.lingshu.mapper.UserMapper;
import com.lingshu.service.admin.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final PaperMapper paperMapper;
    private final QuestionBankMapper questionBankMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取题目总数
        long questionCount = questionMapper.selectCount(null);
        stats.put("questionCount", questionCount);

        // 获取试卷总数
        long paperCount = paperMapper.selectCount(null);
        stats.put("paperCount", paperCount);

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

        List<Paper> latestPapers = paperMapper.selectList(
                new QueryWrapper<Paper>().orderByDesc("updated_at").last("LIMIT 5"));
        for (Paper paper : latestPapers) {
            activities.add(createActivity(
                    "试卷管理",
                    "创建或更新了试卷「" + nullToDefault(paper.getTitle(), "未命名试卷") + "」",
                    "系统",
                    paper.getUpdatedAt() != null ? paper.getUpdatedAt() : paper.getCreatedAt()));
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

}
