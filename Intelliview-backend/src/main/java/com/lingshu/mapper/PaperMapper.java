package com.lingshu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Paper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷Mapper接口
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {
    
    /**
     * 分页获取试卷列表
     * @param page 分页对象
     * @return 分页结果
     */
    Page<Paper> selectPage(Page<Paper> page);
    
    /**
     * 根据状态获取试卷列表
     * @param page 分页对象
     * @param status 状态
     * @return 分页结果
     */
    Page<Paper> selectPageByStatus(Page<Paper> page, Boolean status);
}
