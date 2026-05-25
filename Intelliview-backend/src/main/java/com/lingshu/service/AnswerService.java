// service/AnswerService.java
package com.lingshu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.AnswerRequest;
import com.lingshu.dto.response.AnswerResponse;
import com.lingshu.entity.Answer;
import com.lingshu.entity.User;
import com.lingshu.mapper.AnswerMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;

    /**
     * 创建回答
     */
    @Transactional
    public AnswerResponse createAnswer(Long userId, Long questionId, AnswerRequest request) {
        // 检查问题是否存在
        if (questionMapper.selectById(questionId) == null) {
            throw new RuntimeException("问题不存在");
        }

        // 创建回答
        Answer answer = new Answer();
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setContent(request.getContent());
        answer.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);

        answerMapper.insert(answer);

        log.info("回答创建成功: userId={}, questionId={}, answerId={}", userId, questionId, answer.getId());

        return convertToResponse(answer);
    }

    /**
     * 获取问题的回答列表
     */
    public IPage<AnswerResponse> getAnswersByQuestion(Long questionId, int page, int size) {
        IPage<Answer> answerPage = new Page<>(page, size);
        IPage<Answer> answers = answerMapper.findByQuestionIdAndIsDeletedFalseOrderByCreatedAtDesc(answerPage, questionId);

        return answers.convert(this::convertToResponse);
    }

    /**
     * 获取用户的回答列表
     */
    public IPage<AnswerResponse> getAnswersByUser(Long userId, int page, int size) {
        IPage<Answer> answerPage = new Page<>(page, size);
        IPage<Answer> answers = answerMapper.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(answerPage, userId);

        return answers.convert(this::convertToResponse);
    }

    /**
     * 获取回答详情
     */
    public AnswerResponse getAnswerById(Long answerId) {
        Answer answer = answerMapper.findByIdAndIsDeletedFalse(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 增加浏览次数
        answer.setViewCount(answer.getViewCount() + 1);
        answerMapper.updateById(answer);

        return convertToResponse(answer);
    }

    /**
     * 更新回答
     */
    @Transactional
    public AnswerResponse updateAnswer(Long userId, Long answerId, AnswerRequest request) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 检查权限
        if (!answer.getUserId().equals(userId)) {
            throw new RuntimeException("无权更新此回答");
        }

        // 更新回答
        answer.setContent(request.getContent());
        answer.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : answer.getIsAnonymous());

        answerMapper.updateById(answer);
        return convertToResponse(answer);
    }

    /**
     * 删除回答
     */
    @Transactional
    public void deleteAnswer(Long userId, Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 检查权限
        if (!answer.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此回答");
        }

        answer.setIsDeleted(true);
        answerMapper.updateById(answer);

        log.info("回答删除成功: userId={}, answerId={}", userId, answerId);
    }

    /**
     * 获取问题的回答数量
     */
    public Long getAnswerCountByQuestion(Long questionId) {
        return answerMapper.countByQuestionId(questionId);
    }

    /**
     * 获取问题的最佳回答
     */
    public List<AnswerResponse> getTopAnswersByQuestion(Long questionId, int limit) {
        List<Answer> answers = answerMapper.findTopAnswersByQuestionId(questionId, limit);

        return answers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换Answer为AnswerResponse
     */
    private AnswerResponse convertToResponse(Answer answer) {
        AnswerResponse response = AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .likeCount(answer.getLikeCount())
                .viewCount(answer.getViewCount())
                .isAnonymous(answer.getIsAnonymous())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .questionId(answer.getQuestionId())
                .build();

        // 如果不是匿名回答，添加用户信息
        if (!answer.getIsAnonymous()) {
            User user = userMapper.selectById(answer.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setUserNickname(user.getNickname());
            response.setUserAvatar(user.getAvatar());
        }

        return response;
    }
}
