package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.PaperQuestion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 试卷题目关联Mapper
 */
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {

    /**
     * 根据试卷ID获取题目列表
     * @param paperId 试卷ID
     * @return 题目ID列表
     */
    @Select("SELECT question_id FROM paper_questions WHERE paper_id = #{paperId} ORDER BY sort_order ASC")
    List<Long> getQuestionIdsByPaperId(@Param("paperId") Long paperId);

    /**
     * 根据试卷ID获取题目数量
     * @param paperId 试卷ID
     * @return 题目数量
     */
    @Select("SELECT COUNT(*) FROM paper_questions WHERE paper_id = #{paperId}")
    Integer getQuestionCountByPaperId(@Param("paperId") Long paperId);

    /**
     * 检查试卷是否包含指定题目
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @return 是否包含
     */
    @Select("SELECT COUNT(*) FROM paper_questions WHERE paper_id = #{paperId} AND question_id = #{questionId}")
    Integer checkPaperContainsQuestion(@Param("paperId") Long paperId, @Param("questionId") Long questionId);

}
