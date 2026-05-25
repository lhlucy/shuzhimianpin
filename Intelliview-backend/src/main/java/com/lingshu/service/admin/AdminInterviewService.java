package com.lingshu.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.response.AdminInterviewRecordResponse;
import com.lingshu.dto.response.AIInterviewSummaryResponse;
import com.lingshu.entity.AIInterview;
import com.lingshu.entity.AIInterviewAnswer;
import com.lingshu.entity.AIInterviewAssessment;
import com.lingshu.entity.AIInterviewQuestion;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.User;
import com.lingshu.mapper.AIInterviewAnswerMapper;
import com.lingshu.mapper.AIInterviewAssessmentMapper;
import com.lingshu.mapper.AIInterviewMapper;
import com.lingshu.mapper.AIInterviewQuestionMapper;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminInterviewService {

    private final AIInterviewMapper aiInterviewMapper;
    private final AIInterviewAnswerMapper aiInterviewAnswerMapper;
    private final AIInterviewAssessmentMapper aiInterviewAssessmentMapper;
    private final AIInterviewQuestionMapper aiInterviewQuestionMapper;
    private final UserMapper userMapper;
    private final JobRoleMapper jobRoleMapper;
    private final ObjectMapper objectMapper;

    public List<AdminInterviewRecordResponse> listInterviews(String keyword, String status, Long jobRoleId) {
        LambdaQueryWrapper<AIInterview> wrapper = new LambdaQueryWrapper<AIInterview>()
                .orderByDesc(AIInterview::getCreatedAt)
                .orderByDesc(AIInterview::getId);

        if (StringUtils.hasText(status)) {
            wrapper.eq(AIInterview::getStatus, status.trim());
        }
        if (jobRoleId != null) {
            wrapper.eq(AIInterview::getJobRoleId, jobRoleId);
        }
        List<AIInterview> interviews = aiInterviewMapper.selectList(wrapper);
        if (interviews == null || interviews.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = loadUsers(interviews);
        Map<Long, JobRole> jobRoleMap = loadJobRoles(interviews);

        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            interviews = interviews.stream()
                    .filter(item -> matchesKeyword(item, userMap.get(item.getUserId()), jobRoleMap.get(item.getJobRoleId()), normalizedKeyword))
                    .collect(Collectors.toList());
        }

        if (interviews.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> answeredCountMap = loadAnsweredCounts(interviews);

        return interviews.stream()
                .map(item -> {
                    User user = userMap.get(item.getUserId());
                    JobRole jobRole = jobRoleMap.get(item.getJobRoleId());
                    return AdminInterviewRecordResponse.builder()
                            .interviewId(item.getId())
                            .title(item.getTitle())
                            .status(item.getStatus())
                            .targetPosition(item.getTargetPosition())
                            .interviewLanguage(item.getInterviewLanguage())
                            .jobRoleId(item.getJobRoleId())
                            .jobRoleName(jobRole != null ? jobRole.getName() : null)
                            .username(user != null ? user.getUsername() : null)
                            .nickname(user != null ? user.getNickname() : null)
                            .techStacks(parseList(item.getSkillTags()))
                            .questionCount(item.getQuestionCount() == null ? 0 : item.getQuestionCount())
                            .answeredCount(answeredCountMap.getOrDefault(item.getId(), 0))
                            .totalScore(item.getTotalScore())
                            .duration(item.getActualDuration() != null ? item.getActualDuration() : item.getDuration())
                            .createdAt(item.getCreatedAt())
                            .endedAt(item.getEndedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public AIInterviewSummaryResponse getInterviewSummary(Long interviewId) {
        AIInterview interview = aiInterviewMapper.selectById(interviewId);
        if (interview == null) {
            return null;
        }

        AIInterviewAssessment assessment = aiInterviewAssessmentMapper.selectByInterviewId(interviewId);
        List<AIInterviewQuestion> questions = aiInterviewQuestionMapper.selectByInterviewId(interviewId);
        List<AIInterviewAnswer> answers = aiInterviewAnswerMapper.selectByInterviewId(interviewId);
        Map<Long, AIInterviewQuestion> questionMap = questions.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(AIInterviewQuestion::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<AIInterviewSummaryResponse.AIInterviewQuestionReview> reviews = answers.stream()
                .map(answer -> toQuestionReview(answer, questionMap.get(answer.getQuestionId())))
                .collect(Collectors.toList());

        double averageAnswerScore = answers.stream()
                .map(AIInterviewAnswer::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double expressionScore = answers.stream()
                .map(AIInterviewAnswer::getConfidenceLevel)
                .filter(Objects::nonNull)
                .mapToDouble(level -> clampScore(level * 10.0))
                .average()
                .orElse(0.0);
        double keywordCoverageScore = answers.stream()
                .mapToDouble(this::calculateKeywordCoverageScore)
                .average()
                .orElse(0.0);
        int totalQuestions = interview.getQuestionCount() == null ? questions.size() : interview.getQuestionCount();
        int answeredQuestions = answers.size();
        int duration = interview.getActualDuration() != null ? interview.getActualDuration() : interview.getDuration();
        String summaryText = assessment != null ? readSummaryText(assessment.getSectionScores()) : buildFallbackSummary(interview, answers);
        List<String> strengths = assessment != null ? readJsonList(assessment.getStrengths()) : collectFeedbackItems(answers, "strengths");
        List<String> weaknesses = assessment != null ? readJsonList(assessment.getWeaknesses()) : collectFeedbackItems(answers, "weaknesses");
        List<String> suggestions = assessment != null ? readJsonList(assessment.getSuggestions()) : collectFeedbackItems(answers, "suggestions");
        double overallScore = assessment != null && assessment.getOverallScore() != null
                ? assessment.getOverallScore()
                : (interview.getTotalScore() != null ? interview.getTotalScore() : averageAnswerScore);

        return AIInterviewSummaryResponse.builder()
                .interviewId(interview.getId())
                .overallScore(roundToOneDecimal(overallScore))
                .summary(summaryText)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .suggestions(suggestions)
                .totalQuestions(totalQuestions)
                .answeredQuestions(answeredQuestions)
                .durationSeconds(duration)
                .completionRate(totalQuestions <= 0 ? 0.0 : roundToOneDecimal(answeredQuestions * 100.0 / totalQuestions))
                .averageAnswerScore(roundToOneDecimal(averageAnswerScore))
                .expressionScore(roundToOneDecimal(expressionScore))
                .keywordCoverageScore(roundToOneDecimal(keywordCoverageScore))
                .techStacks(parseList(interview.getSkillTags()))
                .overview(buildSummaryOverview(interview, totalQuestions, answeredQuestions, duration,
                        averageAnswerScore, expressionScore, keywordCoverageScore, reviews.size(), overallScore))
                .questionReviews(reviews)
                .completedAt(interview.getEndedAt())
                .build();
    }

    private Map<Long, User> loadUsers(List<AIInterview> interviews) {
        Set<Long> userIds = interviews.stream()
                .map(AIInterview::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, JobRole> loadJobRoles(List<AIInterview> interviews) {
        Set<Long> jobRoleIds = interviews.stream()
                .map(AIInterview::getJobRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (jobRoleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jobRoleMapper.selectBatchIds(jobRoleIds).stream()
                .collect(Collectors.toMap(JobRole::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Integer> loadAnsweredCounts(List<AIInterview> interviews) {
        Set<Long> interviewIds = interviews.stream()
                .map(AIInterview::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (interviewIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return aiInterviewAnswerMapper.selectList(new LambdaQueryWrapper<AIInterviewAnswer>()
                        .in(AIInterviewAnswer::getInterviewId, interviewIds))
                .stream()
                .filter(answer -> answer.getInterviewId() != null)
                .collect(Collectors.groupingBy(
                        AIInterviewAnswer::getInterviewId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private boolean matchesKeyword(AIInterview interview, User user, JobRole jobRole, String keyword) {
        return containsIgnoreCase(interview.getTitle(), keyword)
                || containsIgnoreCase(interview.getTargetPosition(), keyword)
                || containsIgnoreCase(interview.getInterviewLanguage(), keyword)
                || containsIgnoreCase(interview.getSkillTags(), keyword)
                || containsIgnoreCase(user != null ? user.getUsername() : null, keyword)
                || containsIgnoreCase(user != null ? user.getNickname() : null, keyword)
                || containsIgnoreCase(jobRole != null ? jobRole.getName() : null, keyword)
                || containsIgnoreCase(jobRole != null ? jobRole.getCode() : null, keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return StringUtils.hasText(value)
                && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private List<String> parseList(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return Collections.emptyList();
        }

        String normalized = rawValue.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "");

        String[] segments = normalized.split("[,，、|/;；\\n\\r]+");
        LinkedHashSet<String> items = new LinkedHashSet<>();
        for (String segment : segments) {
            String trimmed = segment == null ? "" : segment.trim();
            if (StringUtils.hasText(trimmed)) {
                items.add(trimmed);
            }
        }
        return new ArrayList<>(items);
    }

    private AIInterviewSummaryResponse.AIInterviewQuestionReview toQuestionReview(AIInterviewAnswer answer, AIInterviewQuestion question) {
        Map<String, Object> feedback = readJsonObject(answer.getFeedback());
        Map<String, Object> keywordAnalysis = readJsonObject(answer.getKeywordAnalysis());
        return AIInterviewSummaryResponse.AIInterviewQuestionReview.builder()
                .questionId(question == null ? answer.getQuestionId() : question.getId())
                .questionOrder(question == null ? null : question.getQuestionOrder())
                .questionType(question == null ? "" : safeText(question.getType()))
                .questionContent(question == null ? "" : safeText(question.getContent()))
                .answerContent(limitText(safeText(answer.getContent()), 220))
                .score(roundToOneDecimal(answer.getScore() == null ? 0.0 : answer.getScore()))
                .duration(answer.getDuration() == null ? 0 : answer.getDuration())
                .confidenceLevel(answer.getConfidenceLevel())
                .feedbackSummary(readObjectText(feedback.get("summary")))
                .strengths(readObjectStringList(feedback.get("strengths")))
                .weaknesses(readObjectStringList(feedback.get("weaknesses")))
                .suggestions(readObjectStringList(feedback.get("suggestions")))
                .hitKeywords(readObjectStringList(keywordAnalysis.get("hitKeywords")))
                .missingKeywords(readObjectStringList(keywordAnalysis.get("missingKeywords")))
                .build();
    }

    private Map<String, Object> buildSummaryOverview(AIInterview interview, int totalQuestions, int answeredQuestions, int durationSeconds,
                                                     double averageAnswerScore, double expressionScore, double keywordCoverageScore,
                                                     int reviewCount, double overallScore) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("targetPosition", interview.getTargetPosition());
        overview.put("interviewLanguage", interview.getInterviewLanguage());
        overview.put("overallScore", roundToOneDecimal(overallScore));
        overview.put("averageAnswerScore", roundToOneDecimal(averageAnswerScore));
        overview.put("expressionScore", roundToOneDecimal(expressionScore));
        overview.put("keywordCoverageScore", roundToOneDecimal(keywordCoverageScore));
        overview.put("questionCount", totalQuestions);
        overview.put("answeredQuestions", answeredQuestions);
        overview.put("durationSeconds", durationSeconds);
        overview.put("reviewCount", reviewCount);
        overview.put("techStacks", parseList(interview.getSkillTags()));
        return overview;
    }

    private List<String> readJsonList(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return parseList(rawJson);
        }
    }

    private String readSummaryText(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return "";
        }
        try {
            return objectMapper.readTree(rawJson).path("summary").asText("");
        } catch (Exception ex) {
            return rawJson;
        }
    }

    private Map<String, Object> readJsonObject(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String readObjectText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private List<String> readObjectStringList(Object value) {
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (Object item : (List<?>) value) {
            if (item != null) {
                String text = String.valueOf(item).trim();
                if (StringUtils.hasText(text)) {
                    result.add(text);
                }
            }
        }
        return result;
    }

    private double calculateKeywordCoverageScore(AIInterviewAnswer answer) {
        Map<String, Object> keywordAnalysis = readJsonObject(answer.getKeywordAnalysis());
        int hitCount = readObjectStringList(keywordAnalysis.get("hitKeywords")).size();
        int missingCount = readObjectStringList(keywordAnalysis.get("missingKeywords")).size();
        int total = hitCount + missingCount;
        if (total <= 0) {
            return answer.getScore() == null ? 0.0 : clampScore(answer.getScore());
        }
        return roundToOneDecimal(hitCount * 100.0 / total);
    }

    private String buildFallbackSummary(AIInterview interview, List<AIInterviewAnswer> answers) {
        if (answers.isEmpty()) {
            return "当前面试还没有生成完整报告，暂无可展示的答题反馈。";
        }
        return String.format("本场面试共作答 %d 题，岗位为%s，建议结合逐题反馈继续完善表达结构与技术细节。",
                answers.size(),
                StringUtils.hasText(interview.getTargetPosition()) ? interview.getTargetPosition() : "目标岗位");
    }

    private List<String> collectFeedbackItems(List<AIInterviewAnswer> answers, String key) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (AIInterviewAnswer answer : answers) {
            Map<String, Object> feedback = readJsonObject(answer.getFeedback());
            result.addAll(readObjectStringList(feedback.get(key)));
            if (result.size() >= 6) {
                break;
            }
        }
        return new ArrayList<>(result);
    }

    private String safeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String limitText(String text, int maxLength) {
        String value = safeText(text);
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
