package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingshu.entity.QuestionBank;

import java.util.List;

public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 查询题库关联的分类ID列表
     */
    List<Long> selectCategoryIdsByBankId(Long bankId);
    
    /**
     * 根据分类ID列表查询题库列表
     */
    List<QuestionBank> selectBanksByCategoryIds(List<Long> categoryIds);
}
