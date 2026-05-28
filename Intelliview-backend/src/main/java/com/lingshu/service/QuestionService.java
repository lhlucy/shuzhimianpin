package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.QuestionFilterRequest;
import com.lingshu.dto.response.QuestionDetailResponse;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.Question;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.QuestionMapper;

import com.lingshu.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final JobRoleMapper jobRoleMapper;
    private final FavoriteService favoriteService;
    private final SecurityUtil securityUtil;

    /**
     * 获取问题详情
     */
    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || !Boolean.TRUE.equals(question.getIsVisible())) {
            throw new RuntimeException("问题不存在或未发布");
        }

        // 增加浏览次数
        questionMapper.incrementBrowseCount(id, LocalDateTime.now());

        QuestionDetailResponse response = convertToDetailResponse(question);
        
        // 检查用户是否收藏了该问题
        try {
            Long userId = securityUtil.getCurrentUserId();
            boolean isFavorited = favoriteService.isFavorite(userId, id);
            response.setIsFavorited(isFavorited);
        } catch (RuntimeException e) {
            // 用户未登录，设置为false
            response.setIsFavorited(false);
        }
        
        return response;
    }

    /**
     * 获取问题列表（带过滤条件）
     */
    public IPage<QuestionResponse> getQuestions(QuestionFilterRequest request) {
        // 创建分页对象
        Page<Question> page = new Page<>(request.getPage(), request.getSize());

        // 构建查询条件
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true);

        // 关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            queryWrapper.like("title", request.getKeyword()).or()
                    .like("description", request.getKeyword()).or()
                    .like("question_text", request.getKeyword());
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            queryWrapper.eq("category_id", request.getCategoryId());
        }

        Long primaryJobRoleId = resolvePrimaryJobRoleId(request.getPrimaryJobRoleId(), request.getPrimaryJobRoleCode());
        if (primaryJobRoleId != null) {
            queryWrapper.eq("primary_job_role_id", primaryJobRoleId);
        }

        if (request.getQuestionType() != null && !request.getQuestionType().isEmpty()) {
            queryWrapper.eq("question_type", request.getQuestionType().toUpperCase());
        }

        if (request.getIsForInterview() != null) {
            queryWrapper.eq("is_for_interview", request.getIsForInterview());
        }

        if (request.getIsForPractice() != null) {
            queryWrapper.eq("is_for_practice", request.getIsForPractice());
        }

        // 难度筛选
        if (request.getDifficulty() != null) {
            try {
                Question.Difficulty difficultyEnum = Question.Difficulty.valueOf(request.getDifficulty().toUpperCase());
                queryWrapper.eq("difficulty", difficultyEnum);
            } catch (IllegalArgumentException e) {
                // 难度解析失败，忽略筛选
            }
        }

        // 排序
        buildSort(queryWrapper, request.getSortBy(), request.getSortDirection());

        // 执行查询
        IPage<Question> questions = questionMapper.selectPage(page, queryWrapper);

        // 转换为响应对象
        return questions.convert(this::convertToResponse);
    }

    private Long resolvePrimaryJobRoleId(Long primaryJobRoleId, String primaryJobRoleCode) {
        if (primaryJobRoleId != null) {
            return primaryJobRoleId;
        }
        if (primaryJobRoleCode == null || primaryJobRoleCode.isEmpty()) {
            return null;
        }
        JobRole jobRole = jobRoleMapper.findByCode(primaryJobRoleCode);
        return jobRole != null ? jobRole.getId() : null;
    }

    /**
     * 获取推荐问题列表
     */
    public IPage<QuestionResponse> getRecommendedQuestions(int page, int size) {
        Page<Question> pageInfo = new Page<>(page, size);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true)
                .orderByDesc("mark_count")
                .orderByDesc("browse_count")
                .orderByDesc("created_at");

        IPage<Question> questions = questionMapper.selectPage(pageInfo, queryWrapper);
        return questions.convert(this::convertToResponse);
    }

    /**
     * 获取相关问题
     */
    public List<QuestionResponse> getRelatedQuestions(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        List<Long> relatedIds = question.getRelatedQuestionIdList();
        if (relatedIds.isEmpty()) {
            return List.of();
        }

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", relatedIds)
                .eq("is_visible", true);

        List<Question> questions = questionMapper.selectList(queryWrapper);
        return questions.stream()
                .map(q -> this.convertToResponse(q))
                .collect(Collectors.toList());
    }

    /**
     * 获取热门问题
     */
    public IPage<QuestionResponse> getHotQuestions(int page, int size) {
        Page<Question> pageInfo = new Page<>(page, size);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true)
                .orderByDesc("browse_count")
                .orderByDesc("view_count")
                .orderByDesc("mark_count");

        IPage<Question> questions = questionMapper.selectPage(pageInfo, queryWrapper);
        return questions.convert(this::convertToResponse);
    }

    /**
     * 根据ID列表获取问题
     */
    public List<Question> getQuestionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return questionMapper.selectBatchIds(ids);
    }

    // 构建排序条件
    private void buildSort(QueryWrapper<Question> queryWrapper, String sortBy, String sortDirection) {
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

        switch (sortBy) {
            case "markCount":
                queryWrapper.orderBy(true, isAsc, "mark_count");
                break;
            case "browseCount":
                queryWrapper.orderBy(true, isAsc, "browse_count");
                break;
            case "createdAt":
                queryWrapper.orderBy(true, isAsc, "created_at");
                break;
            case "difficulty":
                queryWrapper.orderBy(true, isAsc, "difficulty");
                break;
            case "sortOrder":
                queryWrapper.orderBy(true, isAsc, "sort_order");
                break;
            default:
                queryWrapper.orderByDesc("created_at");
                break;
        }
    }

    // 转换为响应对象
    private QuestionResponse convertToResponse(Question question) {
        // 分类名称映射
        String categoryName = "其他";
        // 根据categoryId获取分类名称
        if (question.getCategoryId() != null) {
            switch (question.getCategoryId().intValue()) {
                case 1:
                    categoryName = "算法";
                    break;
                case 2:
                    categoryName = "Java";
                    break;
                case 3:
                    categoryName = "Python";
                    break;
                case 4:
                    categoryName = "前端";
                    break;
                case 5:
                    categoryName = "后端";
                    break;
                case 6:
                    categoryName = "数据库";
                    break;
                case 7:
                    categoryName = "操作系统";
                    break;
                case 8:
                    categoryName = "计算机网络";
                    break;
                default:
                    categoryName = "其他";
            }
        }
        
        // 标签列表
        java.util.List<QuestionResponse.TagResponse> tags = new java.util.ArrayList<>();
        
        return QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .slug(question.getSlug())
                .description(question.getDescription())
                .difficulty(question.getDifficulty().name())
                .difficultyLabel(question.getDifficulty().getLabel())
                .categoryId(question.getCategoryId())
                .categoryName(categoryName)
                .tags(tags)
                .submitCount(question.getSubmitCount())
                .acceptCount(question.getAcceptCount())
                .acceptRate(question.getAcceptRate())
                .viewCount(question.getViewCount())
                .likeCount(question.getLikeCount())
                .isVisible(question.getIsVisible())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    // 转换为详情响应对象
    private QuestionDetailResponse convertToDetailResponse(Question question) {
        // 分类名称映射
        String categoryName = "其他";
        // 根据categoryId获取分类名称
        if (question.getCategoryId() != null) {
            switch (question.getCategoryId().intValue()) {
                case 1:
                    categoryName = "算法";
                    break;
                case 2:
                    categoryName = "Java";
                    break;
                case 3:
                    categoryName = "Python";
                    break;
                case 4:
                    categoryName = "前端";
                    break;
                case 5:
                    categoryName = "后端";
                    break;
                case 6:
                    categoryName = "数据库";
                    break;
                case 7:
                    categoryName = "操作系统";
                    break;
                case 8:
                    categoryName = "计算机网络";
                    break;
                default:
                    categoryName = "其他";
            }
        }
        
        // 构建分类对象
        QuestionDetailResponse.Category category = new QuestionDetailResponse.Category();
        category.setId(question.getCategoryId());
        category.setName(categoryName);
        
        QuestionDetailResponse response = QuestionDetailResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .slug(question.getSlug())
                .description(question.getDescription())
                .questionText(question.getQuestionText())
                .answerText(question.getAnswerText())
                .difficulty(question.getDifficulty().name())
                .difficultyLabel(question.getDifficulty().getLabel())
                .markCount(question.getMarkCount())
                .shareCount(question.getShareCount())
                .browseCount(question.getBrowseCount())
                .viewCount(question.getViewCount())
                .likeCount(question.getLikeCount())
                .answerCount(0) // 暂时设为0，实际项目中可从数据库查询
                .lastBrowseTime(question.getLastBrowseTime())
                .keyPoints(question.getKeyPoints())
                .categoryId(question.getCategoryId())
                .categoryName(categoryName)
                .category(category)
                .metadata(question.getMetadataMap())
                .submitCount(question.getSubmitCount())
                .acceptCount(question.getAcceptCount())
                .acceptRate(question.getAcceptRate())
                .isVisible(question.getIsVisible())
                .sortOrder(question.getSortOrder())
                .createdBy(question.getCreatedBy())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
        response.setPrimaryJobRoleId(question.getPrimaryJobRoleId());
        return response;
    }
    /**
     * 根据slug获取问题
     */
    @Transactional
    public QuestionDetailResponse getQuestionBySlug(String slug) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("slug", slug)
                .eq("is_visible", true);

        Question question = questionMapper.selectOne(queryWrapper);
        if (question == null) {
            throw new RuntimeException("问题不存在或未发布");
        }

        // 增加浏览次数
        questionMapper.incrementBrowseCount(question.getId(), LocalDateTime.now());

        QuestionDetailResponse response = convertToDetailResponse(question);
        
        // 检查用户是否收藏了该问题
        try {
            Long userId = securityUtil.getCurrentUserId();
            boolean isFavorited = favoriteService.isFavorite(userId, question.getId());
            response.setIsFavorited(isFavorited);
        } catch (RuntimeException e) {
            // 用户未登录，设置为false
            response.setIsFavorited(false);
        }
        
        return response;
    }

    /**
     * 增加分享次数
     */
    @Transactional
    public void incrementShareCount(Long questionId) {
        questionMapper.incrementShareCount(questionId);
    }

    /**
     * 增加查看次数
     */
    @Transactional
    public void incrementViewCount(Long questionId) {
        questionMapper.incrementViewCount(questionId);
    }

    /**
     * 获取难度分布统计
     */
    public Map<String, Long> getDifficultyStatistics() {
        Map<String, Long> stats = new HashMap<>();

        for (Question.Difficulty difficulty : Question.Difficulty.values()) {
            QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("difficulty", difficulty)
                    .eq("is_visible", true);
            long count = questionMapper.selectCount(queryWrapper);
            stats.put(difficulty.name(), count);
        }

        return stats;
    }

    /**
     * 获取最近更新的问题
     */
    public IPage<QuestionResponse> getRecentlyUpdatedQuestions(int page, int size) {
        Page<Question> pageInfo = new Page<>(page, size);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true)
                .orderByDesc("updated_at");

        IPage<Question> questions = questionMapper.selectPage(pageInfo, queryWrapper);
        return questions.convert(this::convertToResponse);
    }

    /**
     * 获取标记最多的问题
     */
    public IPage<QuestionResponse> getMostMarkedQuestions(int page, int size) {
        Page<Question> pageInfo = new Page<>(page, size);
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_visible", true)
                .orderByDesc("mark_count")
                .orderByDesc("created_at");

        IPage<Question> questions = questionMapper.selectPage(pageInfo, queryWrapper);
        return questions.convert(this::convertToResponse);
    }

    /**
     * 增加标记次数
     */
    @Transactional
    public void incrementMarkCount(Long questionId) {
        questionMapper.incrementMarkCount(questionId);
    }

    /**
     * 增加点赞次数
     */
    @Transactional
    public void incrementLikeCount(Long questionId) {
        questionMapper.incrementLikeCount(questionId);
    }

    /**
     * 减少点赞次数
     */
    @Transactional
    public void decrementLikeCount(Long questionId) {
        questionMapper.decrementLikeCount(questionId);
    }
}
