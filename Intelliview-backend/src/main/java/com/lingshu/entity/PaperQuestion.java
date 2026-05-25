package com.lingshu.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 试卷题目关联实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PaperQuestion {

    /**
     * ID
     */
    private Long id;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
