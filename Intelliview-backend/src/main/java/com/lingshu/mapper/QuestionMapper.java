package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    // 根据slug查询问题
    Question findBySlug(@Param("slug") String slug);
    
    // 根据slug检查问题是否存在
    boolean existsBySlug(@Param("slug") String slug);
    
    // 根据ID列表查询问题
    List<Question> findByIdIn(@Param("ids") List<Long> ids);
    
    // 根据标题模糊查询可见的问题（忽略大小写）
    List<Question> findByTitleContainingIgnoreCaseAndIsVisibleTrue(@Param("title") String title);
    
    // 分页查询可见的问题
    IPage<Question> findByIsVisibleTrue(IPage<Question> page);
    
    // 查询所有可见的问题
    List<Question> findByIsVisibleTrue();
    
    // 根据难度分页查询可见的问题
    IPage<Question> findByDifficultyAndIsVisibleTrue(IPage<Question> page, @Param("difficulty") Question.Difficulty difficulty);
    List<Question> findByDifficultyAndIsVisibleTrue(@Param("difficulty") Question.Difficulty difficulty);
    
    // 根据难度分页查询所有问题（包括不可见的）
    IPage<Question> findByDifficulty(IPage<Question> page, @Param("difficulty") Question.Difficulty difficulty);
    List<Question> findByDifficulty(@Param("difficulty") Question.Difficulty difficulty);
    
    // 根据分类ID分页查询可见的问题
    IPage<Question> findByCategoryIdAndIsVisibleTrue(IPage<Question> page, @Param("categoryId") Long categoryId);
    List<Question> findByCategoryIdAndIsVisibleTrue(@Param("categoryId") Long categoryId);
    
    // 根据分类ID分页查询所有问题（包括不可见的）
    IPage<Question> findByCategoryId(IPage<Question> page, @Param("categoryId") Long categoryId);
    List<Question> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // 搜索所有问题（包括不可见的）
    IPage<Question> searchAll(IPage<Question> page, @Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("difficulty") Question.Difficulty difficulty);
    IPage<Question> searchVisible(IPage<Question> page, @Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("difficulty") Question.Difficulty difficulty);
    
    // 增加浏览次数
    @Update("UPDATE questions SET browse_count = browse_count + 1, last_browse_time = #{now} WHERE id = #{id}")
    void incrementBrowseCount(@Param("id") Long id, @Param("now") LocalDateTime now);
    
    @Update("UPDATE questions SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
    
    @Update("UPDATE questions SET mark_count = mark_count + 1 WHERE id = #{id}")
    void incrementMarkCount(@Param("id") Long id);
    
    @Update("UPDATE questions SET mark_count = mark_count - 1 WHERE id = #{id}")
    void decrementMarkCount(@Param("id") Long id);
    
    @Update("UPDATE questions SET share_count = share_count + 1 WHERE id = #{id}")
    void incrementShareCount(@Param("id") Long id);
    
    @Update("UPDATE questions SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);
    
    @Update("UPDATE questions SET like_count = like_count - 1 WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);
    
    // 统计问题数量
    @Select("SELECT COUNT(*) FROM questions WHERE is_visible = true")
    Long countVisibleQuestions();
    
    @Select("SELECT COUNT(*) FROM questions WHERE difficulty = #{difficulty} AND is_visible = true")
    Long countByDifficultyAndIsVisibleTrue(@Param("difficulty") Question.Difficulty difficulty);
    
    @Select("SELECT COUNT(*) FROM questions WHERE category_id = #{categoryId} AND is_visible = true")
    Long countByCategoryIdAndIsVisibleTrue(@Param("categoryId") Long categoryId);
    
    @Select("SELECT COUNT(*) FROM questions WHERE category_id = #{categoryId}")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
    
    // 查询热门问题
    @Select("SELECT * FROM questions WHERE is_visible = true ORDER BY browse_count DESC")
    IPage<Question> findHotQuestions(IPage<Question> page);
    
    // 查询推荐问题
    @Select("SELECT * FROM questions WHERE is_visible = true ORDER BY created_at DESC")
    IPage<Question> findRecommendedQuestions(IPage<Question> page);
    
    // 查询可见的问题（带标签）
    @Select("SELECT * FROM questions WHERE is_visible = true")
    List<Question> findByIsVisibleTrueWithTags();

    // 根据查询条件分页查询问题
    @Select("SELECT * FROM questions WHERE is_visible = true AND (title LIKE CONCAT('%', #{query}, '%') OR content LIKE CONCAT('%', #{query}, '%')) AND (#{categoryId} IS NULL OR category_id = #{categoryId}) AND (#{difficulty} IS NULL OR difficulty = #{difficulty})")
    IPage<Question> selectPageByQuery(Page<Question> page, @Param("query") String query, @Param("categoryId") Long categoryId, @Param("difficulty") String difficulty);

    // 根据标签ID查询问题
    @Select("SELECT q.* FROM questions q JOIN question_tags qt ON q.id = qt.question_id WHERE qt.tag_id = #{tagId} AND q.is_visible = true")
    List<Question> selectByTagId(@Param("tagId") Long tagId);

    // 查询热门问题
    @Select("SELECT * FROM questions WHERE is_visible = true ORDER BY browse_count DESC LIMIT #{limit}")
    List<Question> selectHotQuestions(@Param("limit") int limit);

    // 查询最新问题
    @Select("SELECT * FROM questions WHERE is_visible = true ORDER BY created_at DESC LIMIT #{limit}")
    List<Question> selectLatestQuestions(@Param("limit") int limit);
    
    // 推荐问题标题
    @Select("SELECT DISTINCT title FROM questions WHERE is_visible = true AND title LIKE CONCAT('%', #{keyword}, '%') LIMIT 10")
    List<String> suggestTitles(@Param("keyword") String keyword);
    
    // 根据标签名搜索问题
    @Select("SELECT q.* FROM questions q JOIN question_tags qt ON q.id = qt.question_id JOIN tags t ON qt.tag_id = t.id WHERE t.name LIKE CONCAT('%', #{tagName}, '%') AND q.is_visible = true")
    IPage<Question> searchByTagName(IPage<Question> page, @Param("tagName") String tagName);
}

