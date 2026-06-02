// service/FavoriteService.java
package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.response.QuestionResponse;
import com.lingshu.entity.JobRole;
import com.lingshu.entity.Question;
import com.lingshu.entity.UserFavorite;
import com.lingshu.mapper.JobRoleMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.UserFavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);
    
    private final UserFavoriteMapper favoriteMapper;
    private final QuestionMapper questionMapper;
    private final JobRoleMapper jobRoleMapper;

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, Long questionId) {
        // 检查问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 检查是否已经收藏
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("question_id", questionId);
        if (favoriteMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("已经收藏过该问题");
        }

        // 创建收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setQuestionId(questionId);

        favoriteMapper.insert(favorite);

        // 增加问题的收藏数
        questionMapper.incrementMarkCount(questionId);

        log.info("用户收藏问题: userId={}, questionId={}", userId, questionId);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long questionId) {
        // 使用删除操作的返回值来判断是否存在收藏记录
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("question_id", questionId);
        
        int deleteCount = favoriteMapper.delete(queryWrapper);
        if (deleteCount == 0) {
            throw new RuntimeException("尚未收藏该问题");
        }

        // 减少问题的收藏数
        questionMapper.decrementMarkCount(questionId);

        log.info("用户取消收藏: userId={}, questionId={}", userId, questionId);
    }

    /**
     * 获取用户收藏列表
     */
    @Transactional(readOnly = true)
    public IPage<QuestionResponse> getUserFavorites(Long userId, int page, int size) {
        Page<UserFavorite> pageInfo = new Page<>(page, size);
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("created_at");
        IPage<UserFavorite> favorites = favoriteMapper.selectPage(pageInfo, queryWrapper);

        if (favorites.getRecords().isEmpty()) {
            return new Page<>(page, size);
        }

        // 提取问题ID
        List<Long> questionIds = favorites.getRecords()
                .stream()
                .map(UserFavorite::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, JobRole> roleMap = loadRoleMap(questions);

        // 按问题ID分组
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // 转换为响应对象
        List<QuestionResponse> responses = favorites.getRecords()
                .stream()
                .map(favorite -> {
                    Question question = questionMap.get(favorite.getQuestionId());
                    if (question == null) {
                        throw new RuntimeException("问题不存在: " + favorite.getQuestionId());
                    }
                    QuestionResponse response = convertToResponse(question, roleMap.get(question.getPrimaryJobRoleId()));
                    response.setFavoriteTime(favorite.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());

        Page<QuestionResponse> resultPage = new Page<>(page, size);
        resultPage.setRecords(responses);
        resultPage.setTotal(favorites.getTotal());
        return resultPage;
    }

    /**
     * 检查是否收藏
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long questionId) {
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("question_id", questionId);
        return favoriteMapper.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取用户收藏的问题ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getUserFavoriteIds(Long userId) {
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .select("question_id");
        List<UserFavorite> favorites = favoriteMapper.selectList(queryWrapper);
        return favorites.stream()
                .map(UserFavorite::getQuestionId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏数量
     */
    @Transactional(readOnly = true)
    public Long getUserFavoriteCount(Long userId) {
        QueryWrapper<UserFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return favoriteMapper.selectCount(queryWrapper);
    }

    /**
     * 获取用户收藏的问题列表
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getUserFavoriteQuestions(Long userId) {
        List<Long> questionIds = getUserFavoriteIds(userId);

        if (questionIds.isEmpty()) {
            return List.of();
        }

        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, JobRole> roleMap = loadRoleMap(questions);

        return questions.stream()
                .map(question -> convertToResponse(question, roleMap.get(question.getPrimaryJobRoleId())))
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     */
    private Map<Long, JobRole> loadRoleMap(List<Question> questions) {
        List<Long> roleIds = questions.stream()
                .map(Question::getPrimaryJobRoleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return jobRoleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(JobRole::getId, Function.identity()));
    }

    private QuestionResponse convertToResponse(Question question, JobRole jobRole) {
        return QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .slug(question.getSlug())
                .description(question.getDescription())
                .difficulty(question.getDifficulty().name())
                .difficultyLabel(question.getDifficulty().getLabel())
                .categoryId(question.getCategoryId())
                .categoryName(null) // 后续可通过categoryId查询
                .primaryJobRoleId(question.getPrimaryJobRoleId())
                .primaryJobRoleCode(jobRole != null ? jobRole.getCode() : null)
                .primaryJobRoleName(jobRole != null ? jobRole.getName() : null)
                .tags(null) // 后续可通过questionId查询
                .submitCount(question.getSubmitCount())
                .acceptCount(question.getAcceptCount())
                .acceptRate(question.getAcceptRate())
                .isVisible(question.getIsVisible())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}
