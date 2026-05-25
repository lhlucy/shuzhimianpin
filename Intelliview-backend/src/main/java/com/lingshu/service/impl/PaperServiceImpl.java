package com.lingshu.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Paper;
import com.lingshu.entity.PaperQuestion;
import com.lingshu.entity.Question;
import com.lingshu.mapper.PaperMapper;
import com.lingshu.mapper.PaperQuestionMapper;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 试卷服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {
    
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    
    @Override
    public Map<String, Object> getPapers(Integer page, Integer size, String sortBy, String sortDirection) {
        // 构建分页对象
        Page<Paper> pageInfo = new Page<>(page + 1, size);
        
        // 查询试卷列表
        Page<Paper> result = paperMapper.selectPage(pageInfo);
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("records", result.getRecords());
        response.put("current", result.getCurrent() - 1); // 转换为前端期望的从0开始的页码
        response.put("size", result.getSize());
        response.put("pages", result.getPages());
        
        return response;
    }
    
    @Override
    public Map<String, Object> getPapersByStatus(Integer page, Integer size, Boolean status) {
        // 构建分页对象
        Page<Paper> pageInfo = new Page<>(page + 1, size);
        
        // 查询试卷列表
        Page<Paper> result = paperMapper.selectPageByStatus(pageInfo, status);
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("records", result.getRecords());
        response.put("current", result.getCurrent() - 1); // 转换为前端期望的从0开始的页码
        response.put("size", result.getSize());
        response.put("pages", result.getPages());
        
        return response;
    }
    
    @Override
    public Paper getPaperDetail(Long id) {
        return paperMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Paper createPaper(Paper paper) {
        paper.setStatus(true);
        paper.setQuestionCount(0);
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.insert(paper);
        return paper;
    }
    
    @Override
    @Transactional
    public Paper updatePaper(Long id, Paper paper) {
        Paper existingPaper = paperMapper.selectById(id);
        if (existingPaper == null) {
            return null;
        }
        
        existingPaper.setTitle(paper.getTitle());
        existingPaper.setDescription(paper.getDescription());
        existingPaper.setDuration(paper.getDuration());
        existingPaper.setStatus(paper.getStatus());
        existingPaper.setUpdatedAt(LocalDateTime.now());
        
        paperMapper.updateById(existingPaper);
        return existingPaper;
    }
    
    @Override
    @Transactional
    public boolean deletePaper(Long id) {
        int result = paperMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean togglePaperStatus(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            return false;
        }
        
        paper.setStatus(!paper.getStatus());
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.updateById(paper);
        
        return paper.getStatus();
    }

    @Override
    public Map<String, Object> getPaperQuestions(Long paperId, Integer page, Integer size) {
        // 获取试卷的题目ID列表
        List<Long> questionIds = paperQuestionMapper.getQuestionIdsByPaperId(paperId);
        
        if (questionIds.isEmpty()) {
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("total", 0);
            emptyResponse.put("records", List.of());
            emptyResponse.put("current", page);
            emptyResponse.put("size", size);
            emptyResponse.put("pages", 0);
            return emptyResponse;
        }
        
        // 计算分页参数
        int start = page * size;
        if (start >= questionIds.size()) {
            Map<String, Object> emptyResponse = new HashMap<>();
            emptyResponse.put("total", questionIds.size());
            emptyResponse.put("records", List.of());
            emptyResponse.put("current", page);
            emptyResponse.put("size", size);
            emptyResponse.put("pages", (int) Math.ceil((double) questionIds.size() / size));
            return emptyResponse;
        }
        int end = Math.min(start + size, questionIds.size());
        
        // 分页获取题目ID
        List<Long> pagedQuestionIds = questionIds.subList(start, end);
        
        // 查询题目详情
        List<Question> questions = questionMapper.selectBatchIds(pagedQuestionIds);
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", questionIds.size());
        response.put("records", questions);
        response.put("current", page);
        response.put("size", size);
        response.put("pages", (int) Math.ceil((double) questionIds.size() / size));
        
        return response;
    }

    @Override
    @Transactional
    public boolean addQuestionToPaper(Long paperId, Long questionId, Integer sortOrder) {
        // 检查试卷是否存在
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            return false;
        }
        
        // 检查题目是否存在
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return false;
        }
        
        // 检查试卷是否已包含该题目
        Integer count = paperQuestionMapper.checkPaperContainsQuestion(paperId, questionId);
        if (count > 0) {
            return false;
        }
        
        // 创建试卷题目关联
        PaperQuestion paperQuestion = new PaperQuestion();
        paperQuestion.setPaperId(paperId);
        paperQuestion.setQuestionId(questionId);
        paperQuestion.setSortOrder(sortOrder != null ? sortOrder : 0);
        paperQuestion.setCreatedAt(LocalDateTime.now());
        
        paperQuestionMapper.insert(paperQuestion);
        
        // 更新试卷题目数量
        int questionCount = paperQuestionMapper.getQuestionCountByPaperId(paperId);
        paper.setQuestionCount(questionCount);
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.updateById(paper);
        
        return true;
    }

    @Override
    @Transactional
    public boolean removeQuestionFromPaper(Long paperId, Long questionId) {
        // 检查试卷是否存在
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            return false;
        }
        
        // 检查试卷是否包含该题目
        Integer count = paperQuestionMapper.checkPaperContainsQuestion(paperId, questionId);
        if (count == 0) {
            return false;
        }
        
        // 删除试卷题目关联
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("paper_id", paperId);
        deleteMap.put("question_id", questionId);
        paperQuestionMapper.deleteByMap(deleteMap);
        
        // 更新试卷题目数量
        int questionCount = paperQuestionMapper.getQuestionCountByPaperId(paperId);
        paper.setQuestionCount(questionCount);
        paper.setUpdatedAt(LocalDateTime.now());
        paperMapper.updateById(paper);
        
        return true;
    }
}
