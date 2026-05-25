package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    // 根据回答ID查询父评论为空且未删除的评论并按创建时间降序排序
    IPage<Comment> findByAnswerIdAndParentIdIsNullAndIsDeletedFalseOrderByCreatedAtDesc(IPage<Comment> page, @Param("answerId") Long answerId);
    
    // 根据父评论ID查询未删除的评论并按创建时间升序排序
    List<Comment> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("parentId") Long parentId);
    
    // 根据回答ID查询未删除的评论
    List<Comment> findByAnswerIdAndIsDeletedFalse(@Param("answerId") Long answerId);
    
    // 根据回答ID统计未删除的评论数量
    @Select("SELECT COUNT(*) FROM comments WHERE answer_id = #{answerId} AND is_deleted = false")
    Long countByAnswerId(@Param("answerId") Long answerId);
    
    // 分页查询评论
    IPage<Comment> selectPage(Page<Comment> page, @Param("answerId") Long answerId);

    // 根据回答ID查询评论
    List<Comment> selectByAnswerId(@Param("answerId") Long answerId);

    // 根据用户ID查询评论
    List<Comment> selectByUserId(@Param("userId") Long userId);

    // 根据父评论ID查询评论
    List<Comment> selectByParentId(@Param("parentId") Long parentId);

    // 增加点赞数
    int incrementLikeCount(@Param("id") Long id);

    // 减少点赞数
    int decrementLikeCount(@Param("id") Long id);
}

