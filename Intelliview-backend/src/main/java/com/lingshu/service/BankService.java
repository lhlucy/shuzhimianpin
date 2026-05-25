package com.lingshu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.entity.Question;
import com.lingshu.entity.QuestionBank;
import com.lingshu.mapper.QuestionBankMapper;
import com.lingshu.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankService {

    private final QuestionBankMapper questionBankMapper;
    private final QuestionMapper questionMapper;

    /**
     * 获取题库列表
     */
    public Map<String, Object> getBanks(Integer page, Integer size, String sortBy, String sortDirection) {
        // 构建分页对象
        Page<QuestionBank> pageInfo = new Page<>(page + 1, size);
        
        // 构建查询条件和排序
        // 这里简化处理，实际项目中可以根据sortBy和sortDirection构建动态排序
        Page<QuestionBank> result = questionBankMapper.selectPage(pageInfo, null);
        result.getRecords().forEach(this::refreshBankQuestionCount);
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("records", result.getRecords());
        response.put("current", result.getCurrent() - 1); // 转换为前端期望的从0开始的页码
        response.put("size", result.getSize());
        response.put("pages", result.getPages());
        
        return response;
    }

    /**
     * 创建题库
     */
    public QuestionBank createBank(QuestionBank bank) {
        bank.setQuestionCount(0);
        bank.setViewCount(0);
        bank.setPopular(false);
        bank.setCreatedAt(LocalDateTime.now());
        bank.setUpdatedAt(LocalDateTime.now());
        questionBankMapper.insert(bank);
        return bank;
    }

    /**
     * 更新题库
     */
    public QuestionBank updateBank(Long id, QuestionBank bank) {
        QuestionBank existing = questionBankMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(bank.getTitle());
        existing.setDescription(bank.getDescription());
        if (bank.getIcon() != null) {
            existing.setIcon(bank.getIcon());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        questionBankMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除题库
     */
    public boolean deleteBank(Long id) {
        return questionBankMapper.deleteById(id) > 0;
    }

    /**
     * 获取题库详情
     */
    public QuestionBank getBankDetail(Long id) {
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank != null) {
            refreshBankQuestionCount(bank);
        }
        return bank;
    }

    /**
     * 获取题库的题目列表
     */
    public Map<String, Object> getBankQuestions(Long bankId, Integer page, Integer size) {
        // 构建分页对象
        Page<Question> pageInfo = new Page<>(page + 1, size);
        
        // 查询题库关联的分类ID
        List<Long> categoryIds = questionBankMapper.selectCategoryIdsByBankId(bankId);
        if (categoryIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("total", 0);
            response.put("records", List.of());
            response.put("current", page);
            response.put("size", size);
            response.put("pages", 0);
            return response;
        }
        
        // 构建查询条件：根据分类ID查询题目，且题目可见
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.in("category_id", categoryIds)
                .eq("is_visible", true)
                .orderByDesc("created_at");
        
        // 执行分页查询
        Page<Question> result = questionMapper.selectPage(pageInfo, queryWrapper);
        
        // 转换为响应DTO
        List<Map<String, Object>> records = result.getRecords().stream().map(this::convertToQuestionMap).collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotal());
        response.put("records", records);
        response.put("current", result.getCurrent() - 1); // 转换为前端期望的从0开始的页码
        response.put("size", result.getSize());
        response.put("pages", result.getPages());
        
        return response;
    }

    /**
     * 将题目归入题库关联的第一个分类。
     */
    public boolean addQuestionToBank(Long bankId, Long questionId) {
        List<Long> categoryIds = questionBankMapper.selectCategoryIdsByBankId(bankId);
        if (categoryIds.isEmpty()) {
            return false;
        }

        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return false;
        }

        question.setCategoryId(categoryIds.get(0));
        questionMapper.updateById(question);
        return true;
    }
    
    /**
     * 将Question实体转换为包含difficultyLabel的Map
     */
    private Map<String, Object> convertToQuestionMap(Question question) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", question.getId());
        map.put("title", question.getTitle());
        map.put("slug", question.getSlug());
        map.put("description", question.getDescription());
        map.put("difficulty", question.getDifficulty().name());
        map.put("difficultyLabel", question.getDifficulty().getLabel());
        map.put("categoryId", question.getCategoryId());
        map.put("markCount", question.getMarkCount());
        map.put("shareCount", question.getShareCount());
        map.put("browseCount", question.getBrowseCount());
        map.put("viewCount", question.getViewCount());
        map.put("createdAt", question.getCreatedAt());
        map.put("updatedAt", question.getUpdatedAt());
        return map;
    }

    /**
     * 根据标签筛选题库
     */
    public Map<String, Object> getBanksByTag(Long tagId) {
        // 1. 根据标签ID查询关联的题目
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question> questionQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        questionQueryWrapper.inSql("id", "SELECT question_id FROM question_tags WHERE tag_id = " + tagId)
                .eq("is_visible", true);
        List<Question> questions = questionMapper.selectList(questionQueryWrapper);
        
        // 2. 从题目中提取唯一的分类ID列表
        Set<Long> categoryIdSet = new HashSet<>();
        for (Question question : questions) {
            if (question.getCategoryId() != null) {
                categoryIdSet.add(question.getCategoryId());
            }
        }
        
        // 3. 根据分类ID列表查询关联的题库
        List<QuestionBank> banks = questionBankMapper.selectBanksByCategoryIds(List.copyOf(categoryIdSet));
        banks.forEach(this::refreshBankQuestionCount);
        
        // 4. 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("total", banks.size());
        response.put("records", banks);
        
        return response;
    }

    private void refreshBankQuestionCount(QuestionBank bank) {
        if (bank == null || bank.getId() == null) {
            return;
        }

        List<Long> categoryIds = questionBankMapper.selectCategoryIdsByBankId(bank.getId());
        if (categoryIds == null || categoryIds.isEmpty()) {
            bank.setQuestionCount(0);
            return;
        }

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.in("category_id", categoryIds)
                .eq("is_visible", true);

        long count = questionMapper.selectCount(queryWrapper);
        bank.setQuestionCount((int) count);
    }
}
