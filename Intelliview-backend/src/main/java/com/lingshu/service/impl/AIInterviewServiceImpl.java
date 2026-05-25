package com.lingshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.request.AIInterviewAnswerRequest;
import com.lingshu.dto.request.AIInterviewCreateRequest;
import com.lingshu.dto.response.AIInterviewAnswerResponse;
import com.lingshu.dto.response.AIInterviewGrowthAnalysisResponse;
import com.lingshu.dto.response.AIInterviewHistoryItemResponse;
import com.lingshu.dto.response.AIInterviewQuestionResponse;
import com.lingshu.dto.response.AIInterviewResumeParseResponse;
import com.lingshu.dto.response.AIInterviewResponse;
import com.lingshu.dto.response.AIInterviewSummaryResponse;
import com.lingshu.dto.response.ResumeParseResult;
import com.lingshu.entity.AIInterview;
import com.lingshu.entity.AIInterviewAnswer;
import com.lingshu.entity.AIInterviewAssessment;
import com.lingshu.entity.AIInterviewQuestion;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.Question;
import com.lingshu.entity.UserInterviewHistory;
import com.lingshu.entity.UserPracticeHistory;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.AIInterviewAnswerMapper;
import com.lingshu.mapper.AIInterviewAssessmentMapper;
import com.lingshu.mapper.AIInterviewMapper;
import com.lingshu.mapper.AIInterviewQuestionMapper;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.UserInterviewHistoryMapper;
import com.lingshu.mapper.UserPracticeHistoryMapper;
import com.lingshu.service.AIInterviewService;
import com.lingshu.service.CloudKnowledgeAppService;
import com.lingshu.service.ResumeParseService;
import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInterviewServiceImpl implements AIInterviewService {

    private static final int MAX_RESUME_CONTENT_LENGTH = 12000;
    private static final int MAX_TRANSCRIPT_ITEM_LENGTH = 600;
    private static final int GROWTH_ANALYSIS_LIMIT = 10;
    private static final DateTimeFormatter GROWTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final AIInterviewMapper aiInterviewMapper;
    private final AIInterviewQuestionMapper aiInterviewQuestionMapper;
    private final AIInterviewAnswerMapper aiInterviewAnswerMapper;
    private final AIInterviewAssessmentMapper aiInterviewAssessmentMapper;
    private final QuestionMapper questionMapper;
    private final JobRoleMapper jobRoleMapper;
    private final UserPracticeHistoryMapper userPracticeHistoryMapper;
    private final UserInterviewHistoryMapper userInterviewHistoryMapper;
    private final CloudKnowledgeAppService cloudKnowledgeAppService;
    private final ResumeParseService resumeParseService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;
    private final AIInterviewStabilityGuard stabilityGuard;

    @Override
    @Transactional
    public AIInterviewResponse createInterview(AIInterviewCreateRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        AIInterviewResponse reusable = stabilityGuard.findReusableInterview(userId, request.getClientRequestId())
                .map(this::buildInterviewResponseById)
                .orElse(null);
        if (reusable != null) {
            return reusable;
        }
        stabilityGuard.checkCreateRate(userId);

        JobRole jobRole = request.getJobRoleId() == null ? null : jobRoleMapper.selectById(request.getJobRoleId());
        List<String> techStacks = normalizeTags(request.getTechStacks(), 12);

        List<Question> practicedQuestions = resolvePracticedQuestions(userId, request.getPracticedQuestionIds());
        List<String> practicedQuestionTitles = practicedQuestions.stream()
                .map(this::resolveQuestionLabel)
                .filter(StringUtils::hasText)
                .limit(8)
                .collect(Collectors.toList());
        String resumeContent = normalizeResumeContent(request.getResumeContent());
        InterviewBlueprint blueprint = createInterviewBlueprint(request, jobRole, techStacks, practicedQuestionTitles, resumeContent);

        LocalDateTime now = LocalDateTime.now();
        AIInterview interview = new AIInterview();
        interview.setUserId(userId);
        interview.setTitle(request.getInterviewName().trim());
        interview.setJobRoleId(request.getJobRoleId());
        interview.setTargetPosition(request.getTargetPosition().trim());
        interview.setInterviewLanguage(resolveLanguage(request.getInterviewLanguage()));
        interview.setInterviewType("PERSONALIZED");
        interview.setStatus("PENDING");
        interview.setDuration(0);
        interview.setActualDuration(0);
        interview.setTotalScore(0.0);
        interview.setDifficulty(resolveDifficulty(techStacks, resumeContent));
        interview.setSkillTags(writeJsonSilently(techStacks));
        interview.setBrushedQuestionIds(writeJsonSilently(practicedQuestions.stream().map(Question::getId).collect(Collectors.toList())));
        interview.setBrushedQuestionSummary(writeJsonSilently(practicedQuestionTitles));
        interview.setVoiceEnabled(Boolean.TRUE.equals(request.getVoiceEnabled()));
        interview.setResumeFileName(blankToNull(request.getResumeFileName()));
        interview.setResumeContent(blankToNull(resumeContent));
        interview.setInterviewerProfile(writeJsonSilently(blueprint.interviewerProfile));
        interview.setOpeningMessage(blueprint.openingMessage);
        interview.setQuestionCount(blueprint.questions.size());
        interview.setConfigSnapshot(writeJsonSilently(buildInterviewSnapshot(request, jobRole, techStacks, practicedQuestionTitles, resumeContent, blueprint)));
        interview.setCreatedAt(now);
        interview.setUpdatedAt(now);
        aiInterviewMapper.insert(interview);

        int order = 1;
        for (GeneratedQuestion generatedQuestion : blueprint.questions) {
            AIInterviewQuestion question = toInterviewQuestion(interview, generatedQuestion, order++, now);
            aiInterviewQuestionMapper.insert(question);
        }

        stabilityGuard.rememberCreatedInterview(userId, request.getClientRequestId(), interview.getId());
        return buildInterviewResponse(interview);
    }

    @Override
    public AIInterviewResumeParseResponse parseResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "请先上传简历文件");
        }

        String fileName = blankToEmpty(file.getOriginalFilename());
        ResumeParseResult parsed = resumeParseService.parse(file);

        return AIInterviewResumeParseResponse.builder()
                .fileName(fileName)
                .summary(parsed.getSummary())
                .content(parsed.getContent())
                .contentLength(parsed.getContentLength())
                .intentionJob(parsed.getIntentionJob())
                .recruitmentType(parsed.getRecruitmentType())
                .intentionCity(parsed.getIntentionCity())
                .expectedSalary(parsed.getExpectedSalary())
                .build();
    }

    @Override
    @Transactional
    public AIInterviewResponse startInterview(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        if (!"COMPLETED".equalsIgnoreCase(interview.getStatus()) && !"IN_PROGRESS".equalsIgnoreCase(interview.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            interview.setStatus("IN_PROGRESS");
            if (interview.getStartedAt() == null) {
                interview.setStartedAt(now);
            }
            interview.setUpdatedAt(now);
            aiInterviewMapper.updateById(interview);
        }
        return buildInterviewResponse(interview);
    }

    @Override
    public AIInterviewResponse getInterview(Long interviewId) {
        return buildInterviewResponse(requireOwnedInterview(interviewId));
    }

    @Override
    public List<AIInterviewHistoryItemResponse> listHistory(Integer limit) {
        Long userId = securityUtil.getCurrentUserId();
        int size = clampInt(limit == null ? 6 : limit, 1, 200);
        LambdaQueryWrapper<AIInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIInterview::getUserId, userId)
                .orderByDesc(AIInterview::getUpdatedAt)
                .orderByDesc(AIInterview::getCreatedAt)
                .last("LIMIT " + size);
        return aiInterviewMapper.selectList(wrapper).stream()
                .map(this::toHistoryItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AIInterviewGrowthAnalysisResponse getGrowthAnalysis() {
        Long userId = securityUtil.getCurrentUserId();
        LambdaQueryWrapper<AIInterview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIInterview::getUserId, userId)
                .eq(AIInterview::getStatus, "COMPLETED")
                .orderByDesc(AIInterview::getEndedAt)
                .orderByDesc(AIInterview::getUpdatedAt)
                .last("LIMIT " + GROWTH_ANALYSIS_LIMIT);

        List<AIInterview> interviews = aiInterviewMapper.selectList(wrapper).stream()
                .sorted(Comparator.comparing(item -> item.getEndedAt() == null ? item.getUpdatedAt() : item.getEndedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        List<GrowthInterviewSnapshot> snapshots = interviews.stream()
                .map(this::toGrowthSnapshot)
                .collect(Collectors.toList());

        return AIInterviewGrowthAnalysisResponse.builder()
                .overallTrend(buildOverallTrend(snapshots))
                .dimensionTrends(buildDimensionTrends(snapshots))
                .weaknessTracking(buildWeaknessTracking(snapshots))
                .recommendations(buildTrainingRecommendations(snapshots))
                .build();
    }

    @Override
    @Transactional
    public void deleteInterview(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        aiInterviewMapper.deleteById(interview.getId());
    }

    @Override
    public AIInterviewQuestionResponse getNextQuestion(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        AIInterviewQuestion nextQuestion = findNextQuestion(interview.getId());
        return nextQuestion == null ? null : toQuestionResponse(nextQuestion, false);
    }

    @Override
    @Transactional
    public AIInterviewAnswerResponse submitAnswer(Long interviewId, Long questionId, AIInterviewAnswerRequest request) {
        AIInterview interview = requireOwnedInterview(interviewId);
        stabilityGuard.checkAnswerRate(interview.getUserId());
        if (!"IN_PROGRESS".equalsIgnoreCase(interview.getStatus())) {
            throw new BusinessException(ErrorCode.INTERVIEW_STATUS_INVALID.getCode(), "面试未开始或已经结束");
        }

        AIInterviewQuestion question = aiInterviewQuestionMapper.selectById(questionId);
        if (question == null || !Objects.equals(question.getInterviewId(), interviewId)) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND.getCode(), "题目不存在");
        }
        if (aiInterviewAnswerMapper.selectByInterviewIdAndQuestionId(interviewId, questionId) != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该题目已经作答");
        }

        AnswerEvaluation evaluation = evaluateAnswerWithAI(interview, question, request.getContent());
        LocalDateTime now = LocalDateTime.now();

        AIInterviewAnswer answer = new AIInterviewAnswer();
        answer.setInterviewId(interviewId);
        answer.setQuestionId(questionId);
        answer.setContent(request.getContent().trim());
        answer.setTranscriptText(request.getContent().trim());
        answer.setAnswerText(request.getContent().trim());
        answer.setInputMode(StringUtils.hasText(request.getInputMode()) ? request.getInputMode().trim().toUpperCase(Locale.ROOT) : "TEXT");
        answer.setDuration(request.getDuration() == null ? 0 : Math.max(request.getDuration(), 0));
        answer.setConfidenceLevel(evaluation.confidenceLevel);
        answer.setScore(evaluation.score);
        answer.setFeedback(writeJsonSilently(Map.of(
                "summary", evaluation.summary,
                "dimensionScores", evaluation.dimensionScores,
                "strengths", evaluation.strengths,
                "weaknesses", evaluation.weaknesses,
                "suggestions", evaluation.suggestions
        )));
        answer.setKeywordAnalysis(writeJsonSilently(Map.of(
                "hitKeywords", evaluation.hitKeywords,
                "missingKeywords", evaluation.missingKeywords
        )));
        answer.setExpressionAnalysis(writeJsonSilently(Map.of(
                "duration", answer.getDuration(),
                "inputMode", answer.getInputMode()
        )));
        answer.setSubmittedAt(now);
        answer.setCreatedAt(now);
        aiInterviewAnswerMapper.insert(answer);

        String nextAction;
        AIInterviewQuestion nextQuestion;
        if (shouldCreateFollowUp(question, evaluation)) {
            nextAction = "FOLLOW_UP";
            nextQuestion = createFollowUpQuestion(interview, question, evaluation, now);
        } else {
            nextQuestion = findNextQuestion(interviewId);
            nextAction = nextQuestion == null ? "END" : "NEXT";
        }

        boolean completed = nextQuestion == null;
        if (completed) {
            finishInterview(interview);
        }

        String interviewerReply = buildAIInterviewerReply(evaluation, nextAction, nextQuestion);
        return AIInterviewAnswerResponse.builder()
                .answerId(answer.getId())
                .nextAction(nextAction)
                .interviewerReply(interviewerReply)
                .score(evaluation.score)
                .dimensionScores(evaluation.dimensionScores)
                .interviewCompleted(completed)
                .summaryReady(completed)
                .nextQuestion(nextQuestion == null ? null : toQuestionResponse(nextQuestion, false))
                .build();
    }

    private AnswerEvaluation evaluateAnswerWithAI(AIInterview interview, AIInterviewQuestion question, String answerText) {
        try {
            String prompt = buildAnswerEvaluationPrompt(interview, question, answerText);
            String raw = cloudKnowledgeAppService.ask(prompt);
            String json = extractJsonObject(raw);
            if (!StringUtils.hasText(json)) {
                throw new IllegalStateException("AI 未返回 JSON");
            }
            JsonNode root = objectMapper.readTree(json);
            AnswerEvaluation evaluation = new AnswerEvaluation();
            evaluation.score = roundToOneDecimal(clampScore(root.path("score").asDouble(60.0)));
            evaluation.dimensionScores = AIInterviewDimensionModel.parseDimensionScores(root.path("dimensionScores"), evaluation.score);
            evaluation.keywordCoverage = root.path("relevance").asDouble(evaluation.score / 100.0);
            evaluation.tooShort = root.path("tooShort").asBoolean(answerText == null || answerText.trim().length() < 50);
            evaluation.hitKeywords = readStringList(root.path("hitKeywords"));
            evaluation.missingKeywords = readStringList(root.path("missingKeywords"));
            evaluation.strengths = readStringList(root.path("strengths"));
            evaluation.weaknesses = readStringList(root.path("weaknesses"));
            evaluation.suggestions = readStringList(root.path("suggestions"));
            evaluation.summary = root.path("summary").asText("");
            evaluation.followUpQuestion = root.path("followUpQuestion").asText("");
            evaluation.interviewerReply = root.path("interviewerReply").asText("");
            evaluation.canProceed = root.path("canProceed").asBoolean(evaluation.score >= 70 && !evaluation.tooShort);
            evaluation.confidenceLevel = evaluation.score >= 85 ? 9 : evaluation.score >= 75 ? 8 : evaluation.score >= 65 ? 7 : 6;
            if (!StringUtils.hasText(evaluation.summary)) {
                evaluation.summary = evaluation.canProceed ? "回答与题目相关，可以进入下一题。" : "回答和题目匹配度不足，需要继续追问。";
            }
            return evaluation;
        } catch (Exception ex) {
            log.warn("AI answer evaluation failed. interviewId={}", interview.getId(), ex);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI 面试官判断失败，请稍后重试");
        }
    }

    private String buildAnswerEvaluationPrompt(AIInterview interview, AIInterviewQuestion question, String answerText) {
        return "你是严格的技术面试评估官。请基于候选人的岗位、技术栈、简历摘要、题目和回答进行多维评估。"
                + "只输出严格 JSON，不要输出额外文字。"
                + "JSON 结构：{\"canProceed\":布尔值,\"score\":0到100数字,"
                + "\"dimensionScores\":{\"technicalDepth\":0到100数字,\"projectRelevance\":0到100数字,"
                + "\"problemSolving\":0到100数字,\"communicationClarity\":0到100数字,\"jobMatch\":0到100数字},"
                + "\"relevance\":0到1数字,\"tooShort\":布尔值,"
                + "\"summary\":\"一句判断\",\"interviewerReply\":\"面试官此刻回复，最多2句话\","
                + "\"followUpQuestion\":\"如果不能进入下一题，给出一个具体追问；如果可以进入下一题则为空\","
                + "\"hitKeywords\":[\"...\"],\"missingKeywords\":[\"...\"],\"strengths\":[\"...\"],\"weaknesses\":[\"...\"],\"suggestions\":[\"...\"]}。"
                + "能力维度：technicalDepth=技术深度，考察核心原理、关键机制、边界条件；"
                + "projectRelevance=项目匹配，考察是否结合真实项目、技术栈和简历经历；"
                + "problemSolving=问题分析，考察拆解问题、说明思路、对比方案；"
                + "communicationClarity=表达清晰，考察结构化表达、逻辑连贯、重点明确；"
                + "jobMatch=岗位匹配，考察回答是否贴合目标岗位要求。"
                + "评分规则：如果候选人答非所问、只寒暄、只有“你好/不会/不知道”等无效回答，canProceed 必须为 false，score 不超过 35。"
                + "如果回答相关但过短或缺少关键解释，score 不超过 60，canProceed 为 false，并给出追问。"
                + "如果回答基本相关且有具体解释，canProceed 为 true；有项目例子、有原理、有取舍分析，score 可超过 80。"
                + "维度评分必须与总分基本一致，不允许总分高但多数维度低。"
                + "岗位：" + interview.getTargetPosition()
                + "；技术栈：" + String.join("、", readJsonList(interview.getSkillTags()))
                + "；简历摘要：" + buildResumeDigest(interview.getResumeContent())
                + "；当前题目：" + question.getContent()
                + "；题目主题：" + blankToEmpty(question.getTopic())
                + "；候选人回答：" + blankToEmpty(answerText);
    }

    private String buildAIInterviewerReply(AnswerEvaluation evaluation, String nextAction, AIInterviewQuestion nextQuestion) {
        if (StringUtils.hasText(evaluation.interviewerReply)) {
            if ("NEXT".equals(nextAction) && nextQuestion != null && !evaluation.interviewerReply.contains(nextQuestion.getContent())) {
                return evaluation.interviewerReply.trim() + " 下一题：" + nextQuestion.getContent();
            }
            if ("FOLLOW_UP".equals(nextAction) && nextQuestion != null && !evaluation.interviewerReply.contains(nextQuestion.getContent())) {
                return evaluation.interviewerReply.trim() + " " + nextQuestion.getContent();
            }
            return evaluation.interviewerReply.trim();
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI 面试官回复为空，请稍后重试");
    }

    @Override
    @Transactional
    public AIInterviewSummaryResponse endInterview(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        if (!"COMPLETED".equalsIgnoreCase(interview.getStatus())) {
            finishInterview(interview);
        }
        return generateOrLoadSummary(interview);
    }

    @Override
    @Transactional
    public AIInterviewSummaryResponse getSummary(Long interviewId) {
        AIInterview interview = requireOwnedInterview(interviewId);
        if (!"COMPLETED".equalsIgnoreCase(interview.getStatus())) {
            throw new BusinessException(ErrorCode.INTERVIEW_STATUS_INVALID.getCode(), "面试结束后才能查看总结");
        }
        return generateOrLoadSummary(interview);
    }

    private Map<String, Object> buildInterviewSnapshot(AIInterviewCreateRequest request, JobRole jobRole, List<String> techStacks,
                                                       List<String> practicedQuestionTitles, String resumeContent, InterviewBlueprint blueprint) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("interviewName", request.getInterviewName().trim());
        snapshot.put("targetPosition", request.getTargetPosition().trim());
        snapshot.put("interviewLanguage", resolveLanguage(request.getInterviewLanguage()));
        snapshot.put("jobRoleId", request.getJobRoleId());
        snapshot.put("jobRoleName", jobRole == null ? "" : blankToEmpty(jobRole.getName()));
        snapshot.put("techStacks", techStacks);
        snapshot.put("practicedQuestions", practicedQuestionTitles);
        snapshot.put("resumeDigest", buildResumeDigest(resumeContent));
        snapshot.put("voiceEnabled", Boolean.TRUE.equals(request.getVoiceEnabled()));
        snapshot.put("questionCount", blueprint.questions.size());
        snapshot.put("flowVersion", "MOCK_INTERVIEW_V3");
        return snapshot;
    }

    private List<Question> resolvePracticedQuestions(Long userId, List<Long> selectedQuestionIds) {
        List<Long> ids = new ArrayList<>();
        if (selectedQuestionIds != null) {
            selectedQuestionIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(8)
                    .forEach(ids::add);
        }
        if (ids.isEmpty()) {
            userPracticeHistoryMapper.selectByUserId(userId).stream()
                    .map(UserPracticeHistory::getQuestionId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(8)
                    .forEach(ids::add);
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Question> questionMap = questionMapper.selectBatchIds(ids).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Question::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<Question> questions = new ArrayList<>();
        for (Long id : ids) {
            Question question = questionMap.get(id);
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    private InterviewBlueprint createInterviewBlueprint(AIInterviewCreateRequest request, JobRole jobRole, List<String> techStacks,
                                                         List<String> practicedQuestionTitles, String resumeContent) {
        InterviewBlueprint blueprint = generateBlueprintWithAI(request, jobRole, techStacks, practicedQuestionTitles, resumeContent);
        if (blueprint == null || blueprint.questions.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI 面试题生成失败，请稍后重试");
        }
        return blueprint;
    }

    private InterviewBlueprint generateBlueprintWithAI(AIInterviewCreateRequest request, JobRole jobRole, List<String> techStacks,
                                                       List<String> practicedQuestionTitles, String resumeContent) {
        try {
            String prompt = buildBlueprintPrompt(request, jobRole, techStacks, practicedQuestionTitles, resumeContent);
            String raw = cloudKnowledgeAppService.ask(prompt);
            if (!StringUtils.hasText(raw)) {
                return null;
            }
            String json = extractJsonObject(raw);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            JsonNode root = objectMapper.readTree(json);
            InterviewBlueprint blueprint = new InterviewBlueprint();
            blueprint.openingMessage = root.path("openingMessage").asText("");
            blueprint.interviewerProfile = sanitizeInterviewerProfile(root.path("interviewerProfile"));
            blueprint.questions = sanitizeQuestions(root.path("questions"), request.getInterviewLanguage(), techStacks, jobRole);
            if (!StringUtils.hasText(blueprint.openingMessage) || blueprint.interviewerProfile.isEmpty() || blueprint.questions.isEmpty()) {
                return null;
            }
            return blueprint;
        } catch (Exception ex) {
            log.warn("Generate interview blueprint failed", ex);
            return null;
        }
    }

    private String buildBlueprintPrompt(AIInterviewCreateRequest request, JobRole jobRole, List<String> techStacks,
                                        List<String> practicedQuestionTitles, String resumeContent) {
        String language = resolveLanguage(request.getInterviewLanguage());
        String focus = limitText(jobRole == null ? "" : blankToEmpty(jobRole.getInterviewFocus()), 180);
        String practiced = practicedQuestionTitles.isEmpty() ? "无" : limitText(String.join("；", practicedQuestionTitles), 180);
        String resumeDigest = limitText(buildResumeDigest(resumeContent), 220);
        return "只输出JSON。为模拟面试生成5题，结构："
                + "{\"openingMessage\":\"\",\"interviewerProfile\":{\"roleName\":\"\",\"tone\":\"\",\"styleRules\":[\"\"],\"closingStyle\":\"\"},"
                + "\"questions\":[{\"content\":\"\",\"type\":\"OPENING|PROJECT|TECHNICAL|SCENARIO|SUMMARY\",\"topic\":\"\",\"estimatedTime\":120,\"focus\":[\"\"]}]}。"
                + "规则：第1题OPENING，第5题SUMMARY，中间覆盖项目/技术/场景；每题一句话；像真实面试官；不要答案。"
                + "岗位=" + request.getTargetPosition().trim()
                + "；语言=" + language
                + "；技术栈=" + String.join("、", techStacks)
                + "；岗位重点=" + focus
                + "；刷题=" + practiced
                + "；简历=" + resumeDigest;
    }

    private AIInterviewQuestion toInterviewQuestion(AIInterview interview, GeneratedQuestion generatedQuestion, int order, LocalDateTime now) {
        AIInterviewQuestion question = new AIInterviewQuestion();
        question.setInterviewId(interview.getId());
        question.setContent(generatedQuestion.content);
        question.setType(generatedQuestion.type);
        question.setDifficulty(interview.getDifficulty());
        question.setTopic(generatedQuestion.topic);
        question.setEstimatedTime(generatedQuestion.estimatedTime);
        question.setQuestionOrder(order);
        question.setContext(writeJsonSilently(Map.of(
                "focus", generatedQuestion.focus,
                "language", interview.getInterviewLanguage(),
                "techStacks", readJsonList(interview.getSkillTags())
        )));
        question.setQuestionText(generatedQuestion.content);
        question.setQuestionType(generatedQuestion.type);
        question.setSortOrder(order);
        question.setTimeLimit(generatedQuestion.estimatedTime);
        question.setCreatedAt(now);
        return question;
    }

    private AIInterviewQuestion findNextQuestion(Long interviewId) {
        List<AIInterviewQuestion> questions = aiInterviewQuestionMapper.selectByInterviewId(interviewId);
        Set<Long> answeredIds = aiInterviewAnswerMapper.selectByInterviewId(interviewId).stream()
                .map(AIInterviewAnswer::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return questions.stream()
                .sorted(Comparator.comparing(AIInterviewQuestion::getQuestionOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(AIInterviewQuestion::getId, Comparator.nullsLast(Long::compareTo)))
                .filter(question -> !answeredIds.contains(question.getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean shouldCreateFollowUp(AIInterviewQuestion question, AnswerEvaluation evaluation) {
        return question.getParentQuestionId() == null
                && aiInterviewQuestionMapper.countByParentQuestionId(question.getId()) == 0
                && !evaluation.canProceed;
    }

    private AIInterviewQuestion createFollowUpQuestion(AIInterview interview, AIInterviewQuestion parentQuestion,
                                                       AnswerEvaluation evaluation, LocalDateTime now) {
        aiInterviewQuestionMapper.bumpQuestionOrderAfter(interview.getId(), parentQuestion.getQuestionOrder());

        AIInterviewQuestion question = new AIInterviewQuestion();
        question.setInterviewId(interview.getId());
        question.setContent(buildFollowUpContent(evaluation));
        question.setType("FOLLOW_UP");
        question.setDifficulty(parentQuestion.getDifficulty());
        question.setTopic(parentQuestion.getTopic());
        question.setEstimatedTime(90);
        question.setQuestionOrder(parentQuestion.getQuestionOrder() + 1);
        question.setContext(writeJsonSilently(Map.of(
                "followUpReason", evaluation.tooShort ? "TOO_SHORT" : "MISSING_DETAIL",
                "missingKeywords", evaluation.missingKeywords,
                "parentQuestionId", parentQuestion.getId()
        )));
        question.setParentQuestionId(parentQuestion.getId());
        question.setQuestionText(question.getContent());
        question.setQuestionType(question.getType());
        question.setSortOrder(question.getQuestionOrder());
        question.setTimeLimit(question.getEstimatedTime());
        question.setCreatedAt(now);
        aiInterviewQuestionMapper.insert(question);

        interview.setQuestionCount(safeInt(interview.getQuestionCount()) + 1);
        interview.setUpdatedAt(now);
        aiInterviewMapper.updateById(interview);
        return question;
    }

    private void finishInterview(AIInterview interview) {
        List<AIInterviewAnswer> answers = aiInterviewAnswerMapper.selectByInterviewId(interview.getId());
        double averageScore = answers.stream()
                .map(AIInterviewAnswer::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        LocalDateTime now = LocalDateTime.now();
        int durationSeconds = 0;
        if (interview.getStartedAt() != null) {
            durationSeconds = (int) Math.max(0, Duration.between(interview.getStartedAt(), now).getSeconds());
        }

        interview.setStatus("COMPLETED");
        interview.setEndedAt(now);
        interview.setActualDuration(durationSeconds);
        interview.setTotalScore(roundToOneDecimal(averageScore));
        interview.setUpdatedAt(now);
        aiInterviewMapper.updateById(interview);
    }

    private AIInterviewSummaryResponse generateOrLoadSummary(AIInterview interview) {
        AIInterviewAssessment assessment = aiInterviewAssessmentMapper.selectByInterviewId(interview.getId());
        if (assessment != null) {
            return toSummaryResponse(interview, assessment);
        }

        List<AIInterviewQuestion> questions = aiInterviewQuestionMapper.selectByInterviewId(interview.getId());
        List<AIInterviewAnswer> answers = aiInterviewAnswerMapper.selectByInterviewId(interview.getId());
        Map<Long, AIInterviewAnswer> answerMap = answers.stream()
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.toMap(AIInterviewAnswer::getQuestionId, item -> item, (left, right) -> right, LinkedHashMap::new));

        SummaryPayload payload = buildSummaryWithAI(interview, questions, answerMap);
        if (payload == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI 面试报告生成失败，请稍后重试");
        }

        AIInterviewAssessment newAssessment = new AIInterviewAssessment();
        newAssessment.setInterviewId(interview.getId());
        newAssessment.setOverallScore(payload.overallScore);
        newAssessment.setSectionScores(writeJsonSilently(Map.of(
                "summary", payload.summary,
                "dimensionScores", payload.dimensionScores,
                "totalQuestions", safeInt(interview.getQuestionCount()),
                "answeredQuestions", answers.size()
        )));
        newAssessment.setStrengths(writeJsonSilently(payload.strengths));
        newAssessment.setWeaknesses(writeJsonSilently(payload.weaknesses));
        newAssessment.setSuggestions(writeJsonSilently(payload.suggestions));
        newAssessment.setRecommendedResources(writeJsonSilently(Collections.emptyList()));
        newAssessment.setCreatedAt(LocalDateTime.now());
        aiInterviewAssessmentMapper.insert(newAssessment);

        interview.setTotalScore(payload.overallScore);
        interview.setUpdatedAt(LocalDateTime.now());
        aiInterviewMapper.updateById(interview);

        upsertInterviewHistory(interview, newAssessment);
        return toSummaryResponse(interview, newAssessment);
    }

    private SummaryPayload buildSummaryWithAI(AIInterview interview, List<AIInterviewQuestion> questions,
                                              Map<Long, AIInterviewAnswer> answerMap) {
        try {
            String prompt = buildSummaryPrompt(interview, questions, answerMap);
            String raw = cloudKnowledgeAppService.ask(prompt);
            if (!StringUtils.hasText(raw)) {
                return null;
            }
            String json = extractJsonObject(raw);
            if (!StringUtils.hasText(json)) {
                return null;
            }
            JsonNode root = objectMapper.readTree(json);
            SummaryPayload payload = new SummaryPayload();
            payload.overallScore = clampScore(root.path("overallScore").asDouble(70.0));
            payload.dimensionScores = AIInterviewDimensionModel.parseDimensionScores(root.path("dimensionScores"), payload.overallScore);
            payload.summary = root.path("summary").asText("");
            payload.strengths = readStringList(root.path("strengths"));
            payload.weaknesses = readStringList(root.path("weaknesses"));
            payload.suggestions = readStringList(root.path("suggestions"));
            if (!StringUtils.hasText(payload.summary)) {
                return null;
            }
            if (payload.strengths.isEmpty() || payload.weaknesses.isEmpty() || payload.suggestions.isEmpty()) {
                return null;
            }
            payload.overallScore = roundToOneDecimal(payload.overallScore);
            if (!AIInterviewDimensionModel.isComplete(payload.dimensionScores)) {
                payload.dimensionScores = averageDimensionScoresFromAnswers(answerMap.values().stream().collect(Collectors.toList()), payload.overallScore);
            }
            return payload;
        } catch (Exception ex) {
            log.warn("Generate interview summary failed", ex);
            return null;
        }
    }

    private void upsertInterviewHistory(AIInterview interview, AIInterviewAssessment assessment) {
        LambdaQueryWrapper<UserInterviewHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInterviewHistory::getInterviewId, interview.getId());
        UserInterviewHistory history = userInterviewHistoryMapper.selectOne(wrapper);
        if (history == null) {
            history = UserInterviewHistory.builder().createdAt(LocalDateTime.now()).build();
        }
        history.setUserId(interview.getUserId());
        history.setInterviewId(interview.getId());
        history.setInterviewType(interview.getInterviewType());
        history.setScore(assessment.getOverallScore());
        history.setInterviewDate(interview.getEndedAt());
        history.setPosition(interview.getTargetPosition());
        history.setSkillTags(interview.getSkillTags());
        history.setStrengths(assessment.getStrengths());
        history.setWeaknesses(assessment.getWeaknesses());
        history.setDuration(interview.getActualDuration());
        if (history.getId() == null) {
            userInterviewHistoryMapper.insert(history);
        } else {
            userInterviewHistoryMapper.updateById(history);
        }
    }

    private String buildSummaryPrompt(AIInterview interview, List<AIInterviewQuestion> questions,
                                      Map<Long, AIInterviewAnswer> answerMap) {
        StringBuilder transcript = new StringBuilder();
        for (AIInterviewQuestion question : questions) {
            transcript.append("Q").append(question.getQuestionOrder()).append(": ")
                    .append(limitText(question.getContent(), MAX_TRANSCRIPT_ITEM_LENGTH)).append("\n");
            AIInterviewAnswer answer = answerMap.get(question.getId());
            transcript.append("A").append(question.getQuestionOrder()).append(": ")
                    .append(answer == null ? "未作答" : limitText(answer.getContent(), MAX_TRANSCRIPT_ITEM_LENGTH)).append("\n");
        }
        return "你是技术面试复盘助手。请基于下面的模拟面试记录输出严格 JSON，不要输出任何额外文字。"
                + "JSON 结构必须为 {\"overallScore\":数字,"
                + "\"dimensionScores\":{\"technicalDepth\":0到100数字,\"projectRelevance\":0到100数字,"
                + "\"problemSolving\":0到100数字,\"communicationClarity\":0到100数字,\"jobMatch\":0到100数字},"
                + "\"summary\":\"字符串\",\"strengths\":[\"...\"],\"weaknesses\":[\"...\"],\"suggestions\":[\"...\"]}。"
                + "五个维度分别是技术深度、项目匹配、问题分析、表达清晰、岗位匹配。"
                + "要求：overallScore 要与五个维度基本一致；summary 简洁真实；strengths、weaknesses、suggestions 各输出 2-3 条。"
                + "岗位：" + interview.getTargetPosition()
                + "；面试语言：" + blankToEmpty(interview.getInterviewLanguage())
                + "；技术栈：" + String.join("、", readJsonList(interview.getSkillTags()))
                + "；简历摘要：" + buildResumeDigest(interview.getResumeContent())
                + "；记录如下：\n" + transcript;
    }

    private String buildFollowUpContent(AnswerEvaluation evaluation) {
        if (StringUtils.hasText(evaluation.followUpQuestion)) {
            return evaluation.followUpQuestion.trim();
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "AI 未生成追问，请稍后重试");
    }

    private AIInterviewResponse buildInterviewResponse(AIInterview interview) {
        List<AIInterviewAnswer> answers = aiInterviewAnswerMapper.selectByInterviewId(interview.getId());
        AIInterviewQuestion nextQuestion = "COMPLETED".equalsIgnoreCase(interview.getStatus()) ? null : findNextQuestion(interview.getId());
        return AIInterviewResponse.builder()
                .interviewId(interview.getId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .targetPosition(interview.getTargetPosition())
                .interviewLanguage(interview.getInterviewLanguage())
                .techStacks(readJsonList(interview.getSkillTags()))
                .practicedQuestions(readJsonList(interview.getBrushedQuestionSummary()))
                .resumeFileName(interview.getResumeFileName())
                .openingMessage(interview.getOpeningMessage())
                .voiceEnabled(Boolean.TRUE.equals(interview.getVoiceEnabled()))
                .questionCount(safeInt(interview.getQuestionCount()))
                .answeredCount(answers.size())
                .currentQuestion(nextQuestion == null ? null : toQuestionResponse(nextQuestion, false))
                .build();
    }

    private AIInterviewResponse buildInterviewResponseById(Long interviewId) {
        AIInterview interview = aiInterviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND.getCode(), "面试不存在");
        }
        return buildInterviewResponse(interview);
    }

    private AIInterviewHistoryItemResponse toHistoryItemResponse(AIInterview interview) {
        int answeredCount = aiInterviewAnswerMapper.selectByInterviewId(interview.getId()).size();
        return AIInterviewHistoryItemResponse.builder()
                .interviewId(interview.getId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .targetPosition(interview.getTargetPosition())
                .interviewLanguage(interview.getInterviewLanguage())
                .techStacks(readJsonList(interview.getSkillTags()))
                .questionCount(safeInt(interview.getQuestionCount()))
                .answeredCount(answeredCount)
                .totalScore(interview.getTotalScore())
                .duration(interview.getActualDuration())
                .createdAt(interview.getCreatedAt())
                .endedAt(interview.getEndedAt())
                .build();
    }

    private GrowthInterviewSnapshot toGrowthSnapshot(AIInterview interview) {
        AIInterviewAssessment assessment = aiInterviewAssessmentMapper.selectByInterviewId(interview.getId());
        double score = assessment != null && assessment.getOverallScore() != null
                ? assessment.getOverallScore()
                : (interview.getTotalScore() == null ? 0.0 : interview.getTotalScore());
        Map<String, Double> dimensionScores = assessment == null
                ? AIInterviewDimensionModel.defaultScores(score)
                : readDimensionScores(assessment.getSectionScores(), score);

        List<String> weaknesses = new ArrayList<>();
        if (assessment != null) {
            weaknesses.addAll(readJsonList(assessment.getWeaknesses()));
        }

        List<Long> missingQuestionIds = new ArrayList<>();
        for (AIInterviewAnswer answer : aiInterviewAnswerMapper.selectByInterviewId(interview.getId())) {
            Map<String, Object> keywordAnalysis = readJsonObject(answer.getKeywordAnalysis());
            List<String> missingKeywords = readObjectStringList(keywordAnalysis.get("missingKeywords"));
            if (!missingKeywords.isEmpty()) {
                weaknesses.addAll(missingKeywords);
                if (answer.getQuestionId() != null) {
                    missingQuestionIds.add(answer.getQuestionId());
                }
            }
        }

        LocalDateTime completedAt = interview.getEndedAt() == null ? interview.getUpdatedAt() : interview.getEndedAt();
        return new GrowthInterviewSnapshot(interview.getId(), score, dimensionScores,
                normalizeTags(weaknesses, 20), missingQuestionIds, completedAt);
    }

    private AIInterviewGrowthAnalysisResponse.OverallTrend buildOverallTrend(List<GrowthInterviewSnapshot> snapshots) {
        List<Double> scores = snapshots.stream()
                .map(item -> roundToOneDecimal(item.score))
                .collect(Collectors.toList());
        List<String> labels = snapshots.stream()
                .map(item -> item.completedAt == null ? "未知" : item.completedAt.format(GROWTH_LABEL_FORMATTER))
                .collect(Collectors.toList());
        double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double latest = scores.isEmpty() ? 0.0 : scores.get(scores.size() - 1);
        double first = scores.isEmpty() ? 0.0 : scores.get(0);
        return AIInterviewGrowthAnalysisResponse.OverallTrend.builder()
                .averageScore(roundToOneDecimal(average))
                .latestScore(roundToOneDecimal(latest))
                .scoreChange(roundToOneDecimal(latest - first))
                .labels(labels)
                .scoreTrend(scores)
                .interviewCount(snapshots.size())
                .build();
    }

    private List<AIInterviewGrowthAnalysisResponse.DimensionTrend> buildDimensionTrends(List<GrowthInterviewSnapshot> snapshots) {
        return AIInterviewDimensionModel.keys().stream()
                .map(key -> {
                    List<Double> values = snapshots.stream()
                            .map(item -> roundToOneDecimal(item.dimensionScores.getOrDefault(key, item.score)))
                            .collect(Collectors.toList());
                    List<String> labels = snapshots.stream()
                            .map(item -> item.completedAt == null ? "未知" : item.completedAt.format(GROWTH_LABEL_FORMATTER))
                            .collect(Collectors.toList());
                    return AIInterviewGrowthAnalysisResponse.DimensionTrend.builder()
                            .key(key)
                            .name(AIInterviewDimensionModel.labelOf(key))
                            .trend(resolveTrend(values))
                            .values(values)
                            .labels(labels)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AIInterviewGrowthAnalysisResponse.WeaknessTracking> buildWeaknessTracking(List<GrowthInterviewSnapshot> snapshots) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Integer> latestIndex = new HashMap<>();
        for (int i = 0; i < snapshots.size(); i++) {
            for (String keyword : snapshots.get(i).weaknesses) {
                counts.merge(keyword, 1, Integer::sum);
                latestIndex.put(keyword, i);
            }
        }

        return counts.entrySet().stream()
                .sorted((left, right) -> {
                    int byCount = Integer.compare(right.getValue(), left.getValue());
                    return byCount != 0 ? byCount : left.getKey().compareTo(right.getKey());
                })
                .limit(8)
                .map(entry -> AIInterviewGrowthAnalysisResponse.WeaknessTracking.builder()
                        .keyword(entry.getKey())
                        .occurrences(entry.getValue())
                        .status(resolveWeaknessStatus(entry.getValue(), latestIndex.getOrDefault(entry.getKey(), -1), snapshots.size()))
                        .suggestion(buildWeaknessSuggestion(entry.getKey()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<AIInterviewGrowthAnalysisResponse.TrainingRecommendation> buildTrainingRecommendations(List<GrowthInterviewSnapshot> snapshots) {
        List<AIInterviewGrowthAnalysisResponse.WeaknessTracking> weaknesses = buildWeaknessTracking(snapshots);
        List<AIInterviewGrowthAnalysisResponse.TrainingRecommendation> recommendations = weaknesses.stream()
                .limit(5)
                .map(item -> AIInterviewGrowthAnalysisResponse.TrainingRecommendation.builder()
                        .type("QUESTION")
                        .title(item.getKeyword())
                        .reason(item.getOccurrences() >= 2
                                ? "该短板在多次面试中重复出现，建议优先补齐"
                                : "最近一次面试暴露该短板，适合立即安排专项练习")
                        .relatedQuestionIds(findRelatedQuestionIds(snapshots, item.getKeyword()))
                        .build())
                .collect(Collectors.toList());

        if (recommendations.isEmpty()) {
            recommendations.add(AIInterviewGrowthAnalysisResponse.TrainingRecommendation.builder()
                    .type("INTERVIEW")
                    .title("继续完成一次岗位化模拟面试")
                    .reason("当前可分析样本较少，完成更多面试后趋势判断会更稳定")
                    .relatedQuestionIds(Collections.emptyList())
                    .build());
        }
        return recommendations;
    }

    private List<Long> findRelatedQuestionIds(List<GrowthInterviewSnapshot> snapshots, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return snapshots.stream()
                .filter(item -> item.weaknesses.contains(keyword))
                .flatMap(item -> item.relatedQuestionIds.stream())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    private String resolveTrend(List<Double> values) {
        if (values.size() < 2) {
            return "stable";
        }
        double first = values.get(0);
        double latest = values.get(values.size() - 1);
        if (latest - first >= 5.0) {
            return "improving";
        }
        if (first - latest >= 5.0) {
            return "declining";
        }
        return "stable";
    }

    private String resolveWeaknessStatus(int occurrences, int latestIndex, int total) {
        if (total <= 0) {
            return "new";
        }
        if (occurrences >= 3 && latestIndex >= total - 3) {
            return "persistent";
        }
        if (latestIndex == total - 1 && occurrences == 1) {
            return "new";
        }
        if (latestIndex <= total - 3) {
            return "improved";
        }
        return "tracking";
    }

    private String buildWeaknessSuggestion(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "建议结合最近面试报告补充专项练习。";
        }
        return "建议优先练习“" + keyword + "”相关题目，并在回答中补充原理、项目场景和取舍分析。";
    }

    private AIInterviewSummaryResponse toSummaryResponse(AIInterview interview, AIInterviewAssessment assessment) {
        List<AIInterviewQuestion> questions = aiInterviewQuestionMapper.selectByInterviewId(interview.getId());
        List<AIInterviewAnswer> answers = aiInterviewAnswerMapper.selectByInterviewId(interview.getId());
        Map<Long, AIInterviewQuestion> questionMap = questions.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(AIInterviewQuestion::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<AIInterviewSummaryResponse.AIInterviewQuestionReview> questionReviews = answers.stream()
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
        int totalQuestions = safeInt(interview.getQuestionCount());
        int answeredQuestions = answers.size();
        int durationSeconds = safeInt(interview.getActualDuration());

        return AIInterviewSummaryResponse.builder()
                .interviewId(interview.getId())
                .overallScore(assessment.getOverallScore())
                .summary(readSummaryText(assessment.getSectionScores()))
                .dimensionScores(readDimensionScores(assessment.getSectionScores(), assessment.getOverallScore()))
                .strengths(readJsonList(assessment.getStrengths()))
                .weaknesses(readJsonList(assessment.getWeaknesses()))
                .suggestions(readJsonList(assessment.getSuggestions()))
                .totalQuestions(totalQuestions)
                .answeredQuestions(answeredQuestions)
                .durationSeconds(durationSeconds)
                .completionRate(totalQuestions <= 0 ? 0.0 : roundToOneDecimal(answeredQuestions * 100.0 / totalQuestions))
                .averageAnswerScore(roundToOneDecimal(averageAnswerScore))
                .expressionScore(roundToOneDecimal(expressionScore))
                .keywordCoverageScore(roundToOneDecimal(keywordCoverageScore))
                .techStacks(readJsonList(interview.getSkillTags()))
                .overview(buildSummaryOverview(interview, assessment, totalQuestions, answeredQuestions, durationSeconds,
                        averageAnswerScore, expressionScore, keywordCoverageScore, questionReviews.size()))
                .questionReviews(questionReviews)
                .completedAt(interview.getEndedAt())
                .build();
    }

    private AIInterviewSummaryResponse.AIInterviewQuestionReview toQuestionReview(AIInterviewAnswer answer, AIInterviewQuestion question) {
        Map<String, Object> feedback = readJsonObject(answer.getFeedback());
        Map<String, Object> keywordAnalysis = readJsonObject(answer.getKeywordAnalysis());
        return AIInterviewSummaryResponse.AIInterviewQuestionReview.builder()
                .questionId(question == null ? answer.getQuestionId() : question.getId())
                .questionOrder(question == null ? null : question.getQuestionOrder())
                .questionType(question == null ? "" : blankToEmpty(question.getType()))
                .questionContent(question == null ? "" : blankToEmpty(question.getContent()))
                .answerContent(limitText(blankToEmpty(answer.getContent()), 220))
                .score(answer.getScore() == null ? 0.0 : roundToOneDecimal(answer.getScore()))
                .duration(safeInt(answer.getDuration()))
                .confidenceLevel(answer.getConfidenceLevel())
                .feedbackSummary(readObjectText(feedback.get("summary")))
                .dimensionScores(AIInterviewDimensionModel.parseDimensionScores(feedback.get("dimensionScores"),
                        answer.getScore() == null ? 0.0 : answer.getScore()))
                .strengths(readObjectStringList(feedback.get("strengths")))
                .weaknesses(readObjectStringList(feedback.get("weaknesses")))
                .suggestions(readObjectStringList(feedback.get("suggestions")))
                .hitKeywords(readObjectStringList(keywordAnalysis.get("hitKeywords")))
                .missingKeywords(readObjectStringList(keywordAnalysis.get("missingKeywords")))
                .build();
    }

    private Map<String, Object> buildSummaryOverview(AIInterview interview, AIInterviewAssessment assessment,
                                                     int totalQuestions, int answeredQuestions, int durationSeconds,
                                                     double averageAnswerScore, double expressionScore, double keywordCoverageScore,
                                                     int reviewCount) {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("targetPosition", interview.getTargetPosition());
        overview.put("interviewLanguage", interview.getInterviewLanguage());
        overview.put("overallScore", roundToOneDecimal(assessment.getOverallScore() == null ? 0.0 : assessment.getOverallScore()));
        overview.put("dimensionScores", readDimensionScores(assessment.getSectionScores(), assessment.getOverallScore()));
        overview.put("averageAnswerScore", roundToOneDecimal(averageAnswerScore));
        overview.put("expressionScore", roundToOneDecimal(expressionScore));
        overview.put("keywordCoverageScore", roundToOneDecimal(keywordCoverageScore));
        overview.put("questionCount", totalQuestions);
        overview.put("answeredQuestions", answeredQuestions);
        overview.put("durationSeconds", durationSeconds);
        overview.put("reviewCount", reviewCount);
        overview.put("techStacks", readJsonList(interview.getSkillTags()));
        return overview;
    }

    private AIInterviewQuestionResponse toQuestionResponse(AIInterviewQuestion question, boolean answered) {
        return AIInterviewQuestionResponse.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .type(question.getType())
                .topic(question.getTopic())
                .questionOrder(question.getQuestionOrder())
                .estimatedTime(question.getEstimatedTime())
                .followUp(question.getParentQuestionId() != null)
                .answered(answered)
                .build();
    }

    private AIInterview requireOwnedInterview(Long interviewId) {
        AIInterview interview = aiInterviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND.getCode(), "面试不存在");
        }
        if (!Objects.equals(interview.getUserId(), securityUtil.getCurrentUserId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED.getCode(), "无权访问该面试");
        }
        return interview;
    }

    private String extractResumeText(String fileName, MultipartFile file) {
        String lower = blankToEmpty(fileName).toLowerCase(Locale.ROOT);
        try {
            if (lower.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(file.getInputStream())) {
                    return new PDFTextStripper().getText(document);
                }
            }
            if (lower.endsWith(".docx")) {
                try (XWPFDocument document = new XWPFDocument(file.getInputStream());
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            }
            if (lower.endsWith(".txt") || lower.endsWith(".md")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "简历解析失败，请确认文件内容没有损坏");
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "目前仅支持 PDF、DOCX、TXT、MD 格式的简历");
    }

    private String normalizeResumeContent(String resumeContent) {
        if (!StringUtils.hasText(resumeContent)) {
            return "";
        }
        String normalized = resumeContent.replace("\u0000", "")
                .replace("\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();
        if (normalized.length() <= MAX_RESUME_CONTENT_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_RESUME_CONTENT_LENGTH);
    }

    private String buildResumeDigest(String resumeContent) {
        if (!StringUtils.hasText(resumeContent)) {
            return "";
        }
        List<String> lines = Arrays.stream(resumeContent.split("\\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .filter(line -> line.length() > 3)
                .limit(8)
                .collect(Collectors.toList());
        String digest = String.join("；", lines);
        return limitText(digest, 320);
    }

    private Map<String, Object> sanitizeInterviewerProfile(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Collections.emptyMap();
        }
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("roleName", node.path("roleName").asText(""));
        profile.put("tone", node.path("tone").asText(""));
        profile.put("styleRules", readStringList(node.path("styleRules")));
        profile.put("closingStyle", node.path("closingStyle").asText(""));
        return profile;
    }

    private List<GeneratedQuestion> sanitizeQuestions(JsonNode node, String language, List<String> techStacks, JobRole jobRole) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        List<GeneratedQuestion> questions = new ArrayList<>();
        node.forEach(item -> {
            String content = item.path("content").asText("").trim();
            if (!StringUtils.hasText(content)) {
                return;
            }
            GeneratedQuestion question = new GeneratedQuestion();
            question.content = content;
            question.type = normalizeQuestionType(item.path("type").asText(""));
            question.topic = blankToEmpty(item.path("topic").asText(""));
            question.estimatedTime = clampInt(item.path("estimatedTime").asInt(150), 90, 240);
            question.focus = readStringList(item.path("focus"));
            if (question.focus.isEmpty()) {
                question.focus = mergeFocusTags(jobRole, techStacks);
            }
            questions.add(question);
        });
        if (questions.size() != 5) {
            return Collections.emptyList();
        }
        questions.get(0).type = "OPENING";
        questions.get(questions.size() - 1).type = "SUMMARY";
        return questions;
    }

    private String normalizeQuestionType(String rawType) {
        String normalized = blankToEmpty(rawType).trim().toUpperCase(Locale.ROOT);
        if (Arrays.asList("OPENING", "PROJECT", "TECHNICAL", "SCENARIO", "SUMMARY", "FOLLOW_UP").contains(normalized)) {
            return normalized;
        }
        return "TECHNICAL";
    }

    private List<String> mergeFocusTags(JobRole jobRole, List<String> techStacks) {
        List<String> result = new ArrayList<>();
        result.addAll(parseRoleFocus(jobRole == null ? null : jobRole.getInterviewFocus()));
        result.addAll(techStacks);
        return normalizeTags(result, 6);
    }

    private List<String> mergeFocus(List<String> focusTags, String... extra) {
        List<String> merged = new ArrayList<>(focusTags);
        merged.addAll(Arrays.asList(extra));
        return normalizeTags(merged, 6);
    }

    private List<String> parseRoleFocus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray()) {
                return readStringList(node);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return normalizeTags(Arrays.asList(raw.split("[,，/]")), 4);
    }

    private String resolveQuestionLabel(Question question) {
        if (StringUtils.hasText(question.getTitle())) {
            return question.getTitle().trim();
        }
        if (StringUtils.hasText(question.getQuestionText())) {
            return limitText(question.getQuestionText().trim(), 40);
        }
        return "已刷题目";
    }

    private String resolveDifficulty(List<String> techStacks, String resumeContent) {
        if (techStacks.size() >= 4 || buildResumeDigest(resumeContent).length() > 180) {
            return "HARD";
        }
        if (techStacks.size() <= 1 && !StringUtils.hasText(resumeContent)) {
            return "EASY";
        }
        return "MEDIUM";
    }

    private String resolveLanguage(String interviewLanguage) {
        return StringUtils.hasText(interviewLanguage) ? interviewLanguage.trim() : "中文";
    }

    private boolean isEnglishLanguage(String interviewLanguage) {
        String normalized = blankToEmpty(interviewLanguage).toLowerCase(Locale.ROOT);
        return normalized.contains("english") || normalized.contains("英文");
    }

    private List<String> normalizeTags(List<String> values, int limit) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(item -> item.replaceAll("\\s+", " "))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<String> readJsonList(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return Collections.singletonList(rawJson);
        }
    }

    private String readSummaryText(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            return node.path("summary").asText("");
        } catch (Exception ex) {
            return rawJson;
        }
    }

    private Map<String, Double> readDimensionScores(String rawJson, Double fallbackScore) {
        double fallback = fallbackScore == null ? 0.0 : fallbackScore;
        if (!StringUtils.hasText(rawJson)) {
            return AIInterviewDimensionModel.defaultScores(fallback);
        }
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            return AIInterviewDimensionModel.parseDimensionScores(node.path("dimensionScores"), fallback);
        } catch (Exception ex) {
            return AIInterviewDimensionModel.defaultScores(fallback);
        }
    }

    private Map<String, Double> averageDimensionScoresFromAnswers(List<AIInterviewAnswer> answers, double fallbackScore) {
        List<Map<String, Double>> scoreItems = answers.stream()
                .map(answer -> {
                    Map<String, Object> feedback = readJsonObject(answer.getFeedback());
                    double fallback = answer.getScore() == null ? fallbackScore : answer.getScore();
                    return AIInterviewDimensionModel.parseDimensionScores(feedback.get("dimensionScores"), fallback);
                })
                .collect(Collectors.toList());
        return AIInterviewDimensionModel.average(scoreItems, fallbackScore);
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
        if (value instanceof String) {
            return ((String) value).trim();
        }
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

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> {
            if (item != null && item.isTextual() && StringUtils.hasText(item.asText())) {
                result.add(item.asText().trim());
            }
        });
        return result;
    }

    private String writeJsonSilently(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("Write json failed", ex);
            return "{}";
        }
    }

    private String extractJsonObject(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return "";
        }
        return raw.substring(start, end + 1);
    }

    private String limitText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String blankToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clampScore(double score) {
        return Math.max(0.0, Math.min(100.0, score));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class AnswerEvaluation {
        private double score;
        private double keywordCoverage;
        private boolean tooShort;
        private int confidenceLevel;
        private String summary;
        private String followUpQuestion;
        private String interviewerReply;
        private boolean canProceed;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> suggestions;
        private List<String> hitKeywords;
        private List<String> missingKeywords;
        private Map<String, Double> dimensionScores;
    }

    private static class SummaryPayload {
        private double overallScore;
        private String summary;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> suggestions;
        private Map<String, Double> dimensionScores;
    }

    private static class InterviewBlueprint {
        private String openingMessage;
        private Map<String, Object> interviewerProfile = new LinkedHashMap<>();
        private List<GeneratedQuestion> questions = new ArrayList<>();
    }

    private static class GeneratedQuestion {
        private String content;
        private String type;
        private String topic;
        private int estimatedTime;
        private List<String> focus = new ArrayList<>();
    }

    private static class GrowthInterviewSnapshot {
        private final Long interviewId;
        private final double score;
        private final Map<String, Double> dimensionScores;
        private final List<String> weaknesses;
        private final List<Long> relatedQuestionIds;
        private final LocalDateTime completedAt;

        private GrowthInterviewSnapshot(Long interviewId, double score, Map<String, Double> dimensionScores,
                                        List<String> weaknesses, List<Long> relatedQuestionIds, LocalDateTime completedAt) {
            this.interviewId = interviewId;
            this.score = score;
            this.dimensionScores = dimensionScores;
            this.weaknesses = weaknesses;
            this.relatedQuestionIds = relatedQuestionIds;
            this.completedAt = completedAt;
        }
    }
}
