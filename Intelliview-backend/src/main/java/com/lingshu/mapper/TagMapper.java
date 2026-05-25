package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    // 根据名称查询标签
    Tag findByName(@Param("name") String name);
    
    // 根据slug查询标签
    Tag findBySlug(@Param("slug") String slug);
    
    // 根据名称模糊查询标签（忽略大小写）
    List<Tag> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // 增加问题计数
    @Update("UPDATE tags SET question_count = question_count + 1 WHERE id = #{id}")
    void incrementQuestionCount(@Param("id") Long id);
    
    @Update("UPDATE tags SET question_count = question_count - 1 WHERE id = #{id}")
    void decrementQuestionCount(@Param("id") Long id);
    
    // 分页查询标签
    IPage<Tag> selectPage(Page<Tag> page, @Param("query") String query);

    // 查询热门标签
    List<Tag> selectHotTags(@Param("limit") int limit);

    // 根据名称查询标签
    Tag selectByName(@Param("name") String name);

    // 根据slug查询标签
    Tag selectBySlug(@Param("slug") String slug);
}

