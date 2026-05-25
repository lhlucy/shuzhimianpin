package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // 根据名称查询分类
    Category findByName(@Param("name") String name);
    
    // 根据slug查询分类
    Category findBySlug(@Param("slug") String slug);
    
    // 根据父ID查询分类并按排序顺序
    List<Category> findByParentIdOrderBySortOrder(@Param("parentId") Long parentId);
    
    // 查询可见的分类并按排序顺序
    List<Category> findByIsVisibleTrueOrderBySortOrder();
    
    // 增加问题计数
    @Update("UPDATE categories SET question_count = question_count + 1 WHERE id = #{id}")
    void incrementQuestionCount(@Param("id") Long id);
    
    @Update("UPDATE categories SET question_count = question_count - 1 WHERE id = #{id}")
    void decrementQuestionCount(@Param("id") Long id);
    
    // 分页查询分类
    IPage<Category> selectPage(Page<Category> page, @Param("query") String query);

    // 查询所有可见分类
    List<Category> selectAllVisible();

    // 根据父ID查询分类
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    // 查询分类树
    List<Category> selectCategoryTree();
}

