// service/LikeService.java
package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lingshu.entity.Answer;
import com.lingshu.entity.Comment;
import com.lingshu.entity.LikeRecord;
import com.lingshu.entity.Question;
import com.lingshu.mapper.AnswerMapper;
import com.lingshu.mapper.CommentMapper;
import com.lingshu.mapper.LikeRecordMapper;
import com.lingshu.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final AnswerMapper answerMapper;
    private final CommentMapper commentMapper;
    private final QuestionMapper questionMapper;

    /**
     * 点赞问题
     */
    @Transactional
    public void likeQuestion(Long userId, Long questionId) {
        // 验证问题是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        // 检查是否已点赞
        LikeRecord existingLike = likeRecordMapper.selectByUserAndTarget(userId, "QUESTION", questionId);
        if (existingLike != null) {
            throw new RuntimeException("已点赞");
        }

        // 创建点赞记录
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetType("QUESTION");
        likeRecord.setTargetId(questionId);
        likeRecordMapper.insert(likeRecord);

        // 更新问题点赞数
        question.setLikeCount(question.getLikeCount() + 1);
        questionMapper.updateById(question);

        log.info("问题点赞成功: userId={}, questionId={}", userId, questionId);
    }

    /**
     * 取消点赞问题
     */
    @Transactional
    public void unlikeQuestion(Long userId, Long questionId) {
        // 检查是否已点赞
        LikeRecord likeRecord = likeRecordMapper.selectByUserAndTarget(userId, "QUESTION", questionId);
        if (likeRecord == null) {
            throw new RuntimeException("未点赞");
        }

        // 删除点赞记录
        likeRecordMapper.deleteById(likeRecord.getId());

        // 更新问题点赞数
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }
        question.setLikeCount(Math.max(0, question.getLikeCount() - 1));
        questionMapper.updateById(question);

        log.info("问题取消点赞成功: userId={}, questionId={}", userId, questionId);
    }

    /**
     * 点赞回答
     */
    @Transactional
    public void likeAnswer(Long userId, Long answerId) {
        // 验证回答是否存在
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        // 检查是否已点赞
        LikeRecord existingLike = likeRecordMapper.selectByUserAndTarget(userId, "ANSWER", answerId);
        if (existingLike != null) {
            throw new RuntimeException("已点赞");
        }

        // 创建点赞记录
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetType("ANSWER");
        likeRecord.setTargetId(answerId);
        likeRecordMapper.insert(likeRecord);

        // 更新回答点赞数
        answer.setLikeCount(answer.getLikeCount() + 1);
        answerMapper.updateById(answer);

        log.info("回答点赞成功: userId={}, answerId={}", userId, answerId);
    }

    /**
     * 取消点赞回答
     */
    @Transactional
    public void unlikeAnswer(Long userId, Long answerId) {
        // 检查是否已点赞
        LikeRecord likeRecord = likeRecordMapper.selectByUserAndTarget(userId, "ANSWER", answerId);
        if (likeRecord == null) {
            throw new RuntimeException("未点赞");
        }

        // 删除点赞记录
        likeRecordMapper.deleteById(likeRecord.getId());

        // 更新回答点赞数
        Answer answer = answerMapper.selectById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }
        answer.setLikeCount(Math.max(0, answer.getLikeCount() - 1));
        answerMapper.updateById(answer);

        log.info("回答取消点赞成功: userId={}, answerId={}", userId, answerId);
    }

    /**
     * 点赞评论
     */
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        // 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否已点赞
        LikeRecord existingLike = likeRecordMapper.selectByUserAndTarget(userId, "COMMENT", commentId);
        if (existingLike != null) {
            throw new RuntimeException("已点赞");
        }

        // 创建点赞记录
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetType("COMMENT");
        likeRecord.setTargetId(commentId);
        likeRecordMapper.insert(likeRecord);

        // 更新评论点赞数
        commentMapper.incrementLikeCount(commentId);

        log.info("评论点赞成功: userId={}, commentId={}", userId, commentId);
    }

    /**
     * 取消点赞评论
     */
    @Transactional
    public void unlikeComment(Long userId, Long commentId) {
        // 检查是否已点赞
        LikeRecord likeRecord = likeRecordMapper.selectByUserAndTarget(userId, "COMMENT", commentId);
        if (likeRecord == null) {
            throw new RuntimeException("未点赞");
        }

        // 删除点赞记录
        likeRecordMapper.deleteById(likeRecord.getId());

        // 更新评论点赞数
        commentMapper.decrementLikeCount(commentId);

        log.info("评论取消点赞成功: userId={}, commentId={}", userId, commentId);
    }

    /**
     * 检查用户是否已点赞
     */
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        LikeRecord likeRecord = likeRecordMapper.selectByUserAndTarget(userId, targetType, targetId);
        return likeRecord != null;
    }

    /**
     * 获取目标点赞数
     */
    public Long getLikeCount(String targetType, Long targetId) {
        return likeRecordMapper.countByTarget(targetType, targetId);
    }

    /**
     * 获取用户点赞的回答ID列表
     */
    public List<Long> getUserLikedAnswerIds(Long userId) {
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", "ANSWER");

        List<LikeRecord> likeRecords = likeRecordMapper.selectList(queryWrapper);
        return likeRecords.stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户点赞的评论ID列表
     */
    public List<Long> getUserLikedCommentIds(Long userId) {
        QueryWrapper<LikeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("target_type", "COMMENT");

        List<LikeRecord> likeRecords = likeRecordMapper.selectList(queryWrapper);
        return likeRecords.stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toList());
    }
}

