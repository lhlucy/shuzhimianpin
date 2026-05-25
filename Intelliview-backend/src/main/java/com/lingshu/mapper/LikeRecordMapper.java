package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    // 根据用户ID、目标类型和目标ID查询点赞记录
    LikeRecord findByUserIdAndTargetTypeAndTargetId(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    // 根据目标类型和目标ID统计点赞数量
    @Select("SELECT COUNT(*) FROM like_records WHERE target_type = #{targetType} AND target_id = #{targetId}")
    Long countByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    
    // 根据用户ID和目标类型查询目标ID列表
    @Select("SELECT target_id FROM like_records WHERE user_id = #{userId} AND target_type = #{targetType}")
    List<Long> findTargetIdsByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") String targetType);
    
    // 根据用户和目标查询点赞记录
    LikeRecord selectByUserAndTarget(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);


}

