// service/CommentService.java
package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.CommentRequest;
import com.lingshu.dto.response.CommentResponse;
import com.lingshu.entity.Comment;
import com.lingshu.entity.User;
import com.lingshu.mapper.AnswerMapper;
import com.lingshu.mapper.CommentMapper;
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
public class CommentService {

    private final CommentMapper commentMapper;
    private final AnswerMapper answerMapper;
    private final UserMapper userMapper;

    /**
     * 创建评论
     */
    @Transactional
    public CommentResponse createComment(Long userId, Long answerId, CommentRequest request) {
        // 检查回答是否存在
        if (answerMapper.selectById(answerId) == null) {
            throw new RuntimeException("回答不存在");
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setAnswerId(answerId);
        comment.setContent(request.getContent());

        // 处理父评论
        if (request.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(request.getParentId());
            if (parentComment == null) {
                throw new RuntimeException("父评论不存在");
            }
            comment.setParentId(parentComment.getId());
        }

        commentMapper.insert(comment);

        log.info("评论创建成功: userId={}, answerId={}, commentId={}", userId, answerId, comment.getId());

        return convertToResponse(comment);
    }

    /**
     * 获取回答的评论列表（带回复）
     */
    public IPage<CommentResponse> getCommentsByAnswer(Long answerId, int page, int size) {
        Page<Comment> pageInfo = new Page<>(page, size);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("answer_id", answerId)
                .isNull("parent_id")
                .eq("is_deleted", false)
                .orderByDesc("created_at");

        IPage<Comment> comments = commentMapper.selectPage(pageInfo, queryWrapper);

        return comments.convert(this::convertToResponseWithReplies);
    }

    /**
     * 获取评论的回复列表
     */
    public List<CommentResponse> getRepliesByComment(Long commentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", commentId)
                .eq("is_deleted", false)
                .orderByAsc("created_at");

        List<Comment> replies = commentMapper.selectList(queryWrapper);

        return replies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取评论详情
     */
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        return convertToResponseWithReplies(comment);
    }

    /**
     * 更新评论
     */
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查权限
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权更新此评论");
        }

        comment.setContent(request.getContent());
        commentMapper.updateById(comment);

        return convertToResponse(comment);
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查权限
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        comment.setIsDeleted(true);
        commentMapper.updateById(comment);

        log.info("评论删除成功: userId={}, commentId={}", userId, commentId);
    }

    /**
     * 获取回答的评论数量
     */
    public Long getCommentCountByAnswer(Long answerId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("answer_id", answerId)
                .eq("is_deleted", false);
        return commentMapper.selectCount(queryWrapper);
    }

    /**
     * 转换Comment为CommentResponse（带回复）
     */
    private CommentResponse convertToResponseWithReplies(Comment comment) {
        CommentResponse response = convertToResponse(comment);

        // 获取回复
        List<CommentResponse> replies = getRepliesByComment(comment.getId());
        response.setReplies(replies);

        return response;
    }

    /**
     * 转换Comment为CommentResponse
     */
    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .answerId(comment.getAnswerId())
                .parentId(comment.getParentId())
                .build();

        // 添加用户信息
        User user = userMapper.selectById(comment.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserNickname(user.getNickname());
        response.setUserAvatar(user.getAvatar());

        return response;
    }

    /**
     * 增加评论点赞数
     */
    @Transactional
    public void incrementLikeCount(Long commentId) {
        commentMapper.incrementLikeCount(commentId);
    }

    /**
     * 减少评论点赞数
     */
    @Transactional
    public void decrementLikeCount(Long commentId) {
        commentMapper.decrementLikeCount(commentId);
    }
}

