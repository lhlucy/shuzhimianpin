package com.lingshu.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.KeyPoint;
import com.lingshu.dto.request.QuestionCreateRequest;
import com.lingshu.dto.response.QuestionDetailResponse;
import com.lingshu.entity.JobRole;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.dto.response.QuestionResponse.TagResponse;
import com.lingshu.dto.response.QuestionSimpleResponse;
import com.lingshu.entity.Question;
import com.lingshu.entity.Tag;
import com.lingshu.exception.BusinessException;
import com.lingshu.exception.ErrorCode;
import com.lingshu.mapper.CategoryMapper;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.TagMapper;
import com.lingshu.util.SlugUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class AdminQuestionService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final QuestionMapper questionMapper;
    private final CategoryMapper categoryMapper;
    private final JobRoleMapper jobRoleMapper;
    private final TagMapper tagMapper;
    
    public AdminQuestionService(QuestionMapper questionMapper, CategoryMapper categoryMapper, JobRoleMapper jobRoleMapper, TagMapper tagMapper) {
        this.questionMapper = questionMapper;
        this.categoryMapper = categoryMapper;
        this.jobRoleMapper = jobRoleMapper;
        this.tagMapper = tagMapper;
    }

    /**
     * 创建面试题 - 管理员操作
     */
    @Transactional
    public QuestionDetailResponse createQuestion(QuestionCreateRequest request, Long userId) {
        // 1. 验证分类是否存在
        if (request.getCategoryId() != null) {
            if (categoryMapper.selectById(request.getCategoryId()) == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND.getCode(), ErrorCode.CATEGORY_NOT_FOUND.getMessage());
            }
        }

        // 2. 构建问题对象
        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setSlug(generateUniqueSlug(request.getTitle()));
        question.setDescription(request.getDescription());
        question.setQuestionText(request.getQuestionText());
        question.setAnswerText(request.getAnswerText());

        // 设置难度
        try {
            question.setDifficulty(Question.Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        } catch (IllegalArgumentException e) {
            question.setDifficulty(Question.Difficulty.MEDIUM);
        }

        question.setCategoryId(request.getCategoryId());
        question.setQuestionType(normalizeQuestionType(request.getQuestionType()));
        question.setPrimaryJobRoleId(resolvePrimaryJobRoleId(request.getPrimaryJobRoleId(), request.getPrimaryJobRoleCode()));
        question.setSkillDimensionSummary(request.getSkillDimensionSummary());
        question.setStandardAnswerPoints(toJsonArray(request.getStandardAnswerPoints()));
        question.setCommonMistakes(toJsonArray(request.getCommonMistakes()));
        question.setFollowUpPrompts(toJsonArray(request.getFollowUpPrompts()));
        question.setScoringPoints(toJsonArray(request.getScoringPoints()));
        question.setRecommendedResources(toJsonArray(request.getRecommendedResources()));
        question.setIsForInterview(Boolean.TRUE.equals(request.getIsForInterview()));
        question.setIsForPractice(Boolean.TRUE.equals(request.getIsForPractice()));
        question.setInterviewFrequency(request.getInterviewFrequency() != null ? request.getInterviewFrequency() : 0);
        question.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType().toUpperCase() : "ADMIN");
        question.setIsVisible(request.getIsVisible());
        question.setSortOrder(request.getSortOrder());
        question.setCreatedBy(userId);
        question.setSource(question.getSourceType());

        // 3. 处理关键点
        if (request.getKeyPoints() != null && !request.getKeyPoints().isEmpty()) {
            // 设置排序
            for (int i = 0; i < request.getKeyPoints().size(); i++) {
                KeyPoint keyPoint = request.getKeyPoints().get(i);
                if (keyPoint.getSortOrder() == null) {
                    keyPoint.setSortOrder(i + 1);
                }
            }
            question.setKeyPoints(request.getKeyPoints());
        }

        // 4. 处理相关问题
        if (request.getRelatedQuestionIds() != null && !request.getRelatedQuestionIds().isEmpty()) {
            // 只保留存在的问题ID
            List<Question> existingQuestions = questionMapper.findByIdIn(request.getRelatedQuestionIds());
            List<Long> existingIds = existingQuestions.stream()
                    .map(q -> q.getId())
                    .collect(Collectors.toList());
            question.setRelatedQuestionIdList(existingIds);
        }

        // 5. 处理元数据
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            question.setMetadataMap(request.getMetadata());
        }
        mergeKeywords(question, request.getKeywords());

        // 6. 保存问题
        questionMapper.insert(question);

        // 7. 处理问题标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            processQuestionTags(question, request.getTags());
        }

        // 8. 更新分类问题数
        if (request.getCategoryId() != null) {
            categoryMapper.incrementQuestionCount(request.getCategoryId());
        }

        log.info("问题创建成功: id={}, title={}, userId={}",
                question.getId(), question.getTitle(), userId);

        // 9. 转换为响应对象
        return convertToDetailResponse(question);
    }

    /**
     * 更新面试题 - 管理员操作
     */
    @Transactional
    public QuestionDetailResponse updateQuestion(Long id, QuestionCreateRequest request, Long userId) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND.getCode(), ErrorCode.QUESTION_NOT_FOUND.getMessage());
        }

        // 记录原分类ID以便后续更新计数
        Long oldCategoryId = question.getCategoryId();
        Long newCategoryId = request.getCategoryId();

        // 更新问题基本信息
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setQuestionText(request.getQuestionText());
        question.setAnswerText(request.getAnswerText());

        try {
            question.setDifficulty(Question.Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 难度解析失败，保持原难度
        }

        question.setCategoryId(request.getCategoryId());
        question.setQuestionType(normalizeQuestionType(request.getQuestionType()));
        question.setPrimaryJobRoleId(resolvePrimaryJobRoleId(request.getPrimaryJobRoleId(), request.getPrimaryJobRoleCode()));
        question.setSkillDimensionSummary(request.getSkillDimensionSummary());
        question.setStandardAnswerPoints(toJsonArray(request.getStandardAnswerPoints()));
        question.setCommonMistakes(toJsonArray(request.getCommonMistakes()));
        question.setFollowUpPrompts(toJsonArray(request.getFollowUpPrompts()));
        question.setScoringPoints(toJsonArray(request.getScoringPoints()));
        question.setRecommendedResources(toJsonArray(request.getRecommendedResources()));
        question.setIsForInterview(Boolean.TRUE.equals(request.getIsForInterview()));
        question.setIsForPractice(Boolean.TRUE.equals(request.getIsForPractice()));
        question.setInterviewFrequency(request.getInterviewFrequency() != null ? request.getInterviewFrequency() : 0);
        question.setSourceType(StringUtils.hasText(request.getSourceType()) ? request.getSourceType().toUpperCase() : question.getSourceType());
        question.setSource(question.getSourceType());
        question.setIsVisible(request.getIsVisible());
        question.setSortOrder(request.getSortOrder());

        // 更新关键点
        if (request.getKeyPoints() != null) {
            for (int i = 0; i < request.getKeyPoints().size(); i++) {
                KeyPoint keyPoint = request.getKeyPoints().get(i);
                if (keyPoint.getSortOrder() == null) {
                    keyPoint.setSortOrder(i + 1);
                }
            }
            question.setKeyPoints(request.getKeyPoints());
        }

        // 更新相关问题
        if (request.getRelatedQuestionIds() != null) {
            List<Question> existingQuestions = questionMapper.findByIdIn(request.getRelatedQuestionIds());
            List<Long> existingIds = existingQuestions.stream()
                    .map(q -> q.getId())
                    .collect(Collectors.toList());
            question.setRelatedQuestionIdList(existingIds);
        }

        // 更新元数据
        if (request.getMetadata() != null) {
            question.setMetadataMap(request.getMetadata());
        }
        mergeKeywords(question, request.getKeywords());

        // 更新标签
        if (request.getTags() != null) {
            // 清空原标签
            question.getTags().clear();
            // 添加新标签
            processQuestionTags(question, request.getTags());
        }

        // 更新分类问题计数
        if (oldCategoryId != null && !oldCategoryId.equals(newCategoryId)) {
            categoryMapper.decrementQuestionCount(oldCategoryId);
        }
        if (newCategoryId != null && (oldCategoryId == null || !oldCategoryId.equals(newCategoryId))) {
            categoryMapper.incrementQuestionCount(newCategoryId);
        }

        questionMapper.updateById(question);
        return convertToDetailResponse(question);
    }

    /**
     * 获取问题详情 - 管理员操作
     */
    public QuestionDetailResponse getQuestionDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND.getCode(), ErrorCode.QUESTION_NOT_FOUND.getMessage());
        }

        // 增加浏览次数
        questionMapper.incrementBrowseCount(id, LocalDateTime.now());

        return convertToDetailResponse(question);
    }


    /**
     * 搜索问题 - 按关键词
     */
    public List<QuestionSimpleResponse> searchQuestions(String keyword) {
        List<Question> questions = questionMapper.findByTitleContainingIgnoreCaseAndIsVisibleTrue(keyword);
        return questions.stream()
                .map(q -> {
                    QuestionSimpleResponse response = new QuestionSimpleResponse();
                    response.setId(q.getId());
                    response.setTitle(q.getTitle());
                    response.setSlug(q.getSlug());
                    response.setDifficulty(q.getDifficulty().name());
                    response.setDifficultyLabel(q.getDifficulty().getLabel());
                    return response;
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<Tag> searchTags(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return tagMapper.selectList(null).stream()
                    .limit(10)
                    .collect(Collectors.toList());
        }
        return tagMapper.findByNameContainingIgnoreCase(keyword).stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 处理问题标签
     */
    private void processQuestionTags(Question question, List<String> tagNames) {
        for (String tagName : tagNames) {
            Tag tag = tagMapper.findByName(tagName);
            if (tag == null) {
                // 创建新标签
                tag = new Tag();
                tag.setName(tagName);
                tag.setSlug(SlugUtil.toSlug(tagName));
                tagMapper.insert(tag);
            }

            // 添加标签到问题
            question.getTags().add(tag);

            // 增加标签问题计数
            tagMapper.incrementQuestionCount(tag.getId());
        }
    }

    private String normalizeQuestionType(String questionType) {
        return StringUtils.hasText(questionType) ? questionType.trim().toUpperCase() : "TECHNICAL";
    }

    private Long resolvePrimaryJobRoleId(Long primaryJobRoleId, String primaryJobRoleCode) {
        if (primaryJobRoleId != null) {
            return primaryJobRoleId;
        }
        if (!StringUtils.hasText(primaryJobRoleCode)) {
            return null;
        }
        JobRole jobRole = jobRoleMapper.findByCode(primaryJobRoleCode.trim());
        return jobRole != null ? jobRole.getId() : null;
    }

    private void mergeKeywords(Question question, List<String> keywords) {
        Map<String, Object> metadataMap = question.getMetadataMap();
        if (keywords == null || keywords.isEmpty()) {
            metadataMap.remove("keywords");
        } else {
            List<String> normalizedKeywords = keywords.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());
            metadataMap.put("keywords", normalizedKeywords);
        }
        question.setMetadataMap(metadataMap);
    }

    private String toJsonArray(List<String> values) {
        List<String> normalizedValues = values == null ? List.of() : values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
        try {
            return OBJECT_MAPPER.writeValueAsString(normalizedValues);
        } catch (JsonProcessingException e) {
            throw new BusinessException("QUESTION_JSON_ERROR", "题目扩展字段序列化失败");
        }
    }

    /**
     * 生成唯一slug
     */
    private String generateUniqueSlug(String title) {
        String slug = SlugUtil.generateQuestionSlug(title);
        String baseSlug = slug;
        int counter = 1;

        while (questionMapper.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    /**
     * 转换为详情响应对象
     */
    private QuestionDetailResponse convertToDetailResponse(Question question) {
        QuestionDetailResponse response = new QuestionDetailResponse();
        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setSlug(question.getSlug());
        response.setDescription(question.getDescription());
        response.setQuestionText(question.getQuestionText());
        response.setAnswerText(question.getAnswerText());
        response.setDifficulty(question.getDifficulty().name());
        response.setDifficultyLabel(question.getDifficulty().getLabel());
        response.setCategoryId(question.getCategoryId());
        response.setMarkCount(question.getMarkCount());
        response.setShareCount(question.getShareCount());
        response.setBrowseCount(question.getBrowseCount());
        response.setViewCount(question.getViewCount());
        response.setLastBrowseTime(question.getLastBrowseTime());
        response.setKeyPoints(question.getKeyPoints());
        response.setSubmitCount(question.getSubmitCount());
        response.setAcceptCount(question.getAcceptCount());
        response.setAcceptRate(question.getAcceptRate());
        response.setIsVisible(question.getIsVisible());
        response.setSortOrder(question.getSortOrder());
        response.setCreatedBy(question.getCreatedBy());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());

        // 设置分类名称
        if (question.getCategoryId() != null) {
            // 这里不设置分类名称，因为 category 是懒加载的，可能会导致 N+1 问题
        }

        // 设置标签
        if (question.getTags() != null) {
            List<TagResponse> tagResponses = question.getTags().stream()
                    .map(tag -> {
                        TagResponse tagResponse = new TagResponse();
                        tagResponse.setId(tag.getId());
                        tagResponse.setName(tag.getName());
                        tagResponse.setSlug(tag.getSlug());
                        tagResponse.setColor(tag.getColor());
                        return tagResponse;
                    })
                    .collect(Collectors.toList());
            response.setTags(tagResponses);
        }

        // 设置相关问题
        List<Long> relatedIds = question.getRelatedQuestionIdList();
        if (!relatedIds.isEmpty()) {
            List<Question> relatedQuestions = questionMapper.findByIdIn(relatedIds);
            List<QuestionSimpleResponse> relatedResponses = relatedQuestions.stream()
                    .map(q -> {
                        QuestionSimpleResponse relatedResponse = new QuestionSimpleResponse();
                        relatedResponse.setId(q.getId());
                        relatedResponse.setTitle(q.getTitle());
                        relatedResponse.setSlug(q.getSlug());
                        relatedResponse.setDifficulty(q.getDifficulty().name());
                        relatedResponse.setDifficultyLabel(q.getDifficulty().getLabel());
                        return relatedResponse;
                    })
                    .collect(Collectors.toList());
            response.setRelatedQuestions(relatedResponses);
        }

        return response;
    }

    /**
     * 创建问题 - 兼容旧接口
     */
    @Transactional
    public QuestionResponse createQuestion(com.lingshu.dto.request.QuestionRequest request) {
        // 转换为新的请求对象
        QuestionCreateRequest newRequest = new QuestionCreateRequest();
        newRequest.setTitle(request.getTitle());
        newRequest.setDescription(request.getDescription());
        newRequest.setQuestionText(request.getQuestionText());
        newRequest.setAnswerText(request.getAnswerText());
        newRequest.setDifficulty(request.getDifficulty());
        newRequest.setCategoryId(request.getCategoryId());
        newRequest.setIsVisible(request.getIsVisible());
        newRequest.setSortOrder(request.getSortOrder());

        // 处理标签ID
        if (request.getTagIds() != null) {
            // 这里可以根据tagIds获取标签名称并设置到newRequest中
        }

        // 调用新的创建方法
        QuestionDetailResponse detailResponse = createQuestion(newRequest, 1L); // 默认用户ID

        // 转换为响应对象
        QuestionResponse response = new QuestionResponse();
        response.setId(detailResponse.getId());
        response.setTitle(detailResponse.getTitle());
        response.setSlug(detailResponse.getSlug());
        response.setDescription(detailResponse.getDescription());
        response.setDifficulty(detailResponse.getDifficulty());
        response.setDifficultyLabel(detailResponse.getDifficultyLabel());
        response.setCategoryId(detailResponse.getCategoryId());
        response.setSubmitCount(detailResponse.getSubmitCount());
        response.setAcceptCount(detailResponse.getAcceptCount());
        response.setAcceptRate(detailResponse.getAcceptRate());
        response.setIsVisible(detailResponse.getIsVisible());
        response.setCreatedAt(detailResponse.getCreatedAt());
        response.setUpdatedAt(detailResponse.getUpdatedAt());
        return response;
    }

    @Transactional
    public QuestionResponse updateQuestion(Long id, com.lingshu.dto.request.QuestionRequest request) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 更新问题信息
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setQuestionText(request.getQuestionText());
        question.setAnswerText(request.getAnswerText());

        try {
            question.setDifficulty(Question.Difficulty.valueOf(request.getDifficulty().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // 难度解析失败，保持原难度
        }

        question.setCategoryId(request.getCategoryId());
        question.setIsVisible(request.getIsVisible());
        question.setSortOrder(request.getSortOrder());

        questionMapper.updateById(question);
        return convertToResponse(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (questionMapper.selectById(id) == null) {
            throw new RuntimeException("问题不存在");
        }
        questionMapper.deleteById(id);
    }

    public com.baomidou.mybatisplus.core.metadata.IPage<QuestionResponse> getQuestions(int page, int size,
                                               String keyword, Long categoryId,
                                               String difficulty) {
        com.baomidou.mybatisplus.core.metadata.IPage<Question> pageInfo = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);

        Question.Difficulty difficultyEnum = null;
        if (difficulty != null) {
            try {
                difficultyEnum = Question.Difficulty.valueOf(difficulty.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 难度解析失败，忽略筛选
            }
        }

        com.baomidou.mybatisplus.core.metadata.IPage<Question> questions = questionMapper.searchAll(pageInfo, keyword, categoryId, difficultyEnum);

        return questions.convert(this::convertToResponse);
    }

    public QuestionResponse getQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        return convertToResponse(question);
    }

    private QuestionResponse convertToResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setTitle(question.getTitle());
        response.setSlug(question.getSlug());
        response.setDescription(question.getDescription());
        response.setDifficulty(question.getDifficulty().name());
        response.setDifficultyLabel(question.getDifficulty().getLabel());
        response.setCategoryId(question.getCategoryId());
        response.setSubmitCount(question.getSubmitCount());
        response.setAcceptCount(question.getAcceptCount());
        response.setAcceptRate(question.getAcceptRate());
        response.setIsVisible(question.getIsVisible());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        return response;
    }
}
