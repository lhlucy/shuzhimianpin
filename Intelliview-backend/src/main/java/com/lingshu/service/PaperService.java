package com.lingshu.service;


import com.lingshu.entity.Paper;
import java.util.Map;

/**
 * 试卷服务接口
 */
public interface PaperService {
    
    /**
     * 获取试卷列表（分页）
     * @param page 页码
     * @param size 每页条数
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return 分页结果
     */
    Map<String, Object> getPapers(Integer page, Integer size, String sortBy, String sortDirection);
    
    /**
     * 根据状态获取试卷列表
     * @param page 页码
     * @param size 每页条数
     * @param status 状态
     * @return 分页结果
     */
    Map<String, Object> getPapersByStatus(Integer page, Integer size, Boolean status);
    
    /**
     * 获取试卷详情
     * @param id 试卷ID
     * @return 试卷信息
     */
    Paper getPaperDetail(Long id);
    
    /**
     * 创建试卷
     * @param paper 试卷信息
     * @return 创建结果
     */
    Paper createPaper(Paper paper);
    
    /**
     * 更新试卷
     * @param id 试卷ID
     * @param paper 试卷信息
     * @return 更新结果
     */
    Paper updatePaper(Long id, Paper paper);
    
    /**
     * 删除试卷
     * @param id 试卷ID
     * @return 删除结果
     */
    boolean deletePaper(Long id);
    
    /**
     * 切换试卷状态
     * @param id 试卷ID
     * @return 新状态
     */
    boolean togglePaperStatus(Long id);

    /**
     * 获取试卷题目列表
     * @param paperId 试卷ID
     * @param page 页码
     * @param size 每页条数
     * @return 题目列表
     */
    Map<String, Object> getPaperQuestions(Long paperId, Integer page, Integer size);

    /**
     * 向试卷添加题目
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @param sortOrder 排序顺序
     * @return 是否成功
     */
    boolean addQuestionToPaper(Long paperId, Long questionId, Integer sortOrder);

    /**
     * 从试卷移除题目
     * @param paperId 试卷ID
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean removeQuestionFromPaper(Long paperId, Long questionId);
}
