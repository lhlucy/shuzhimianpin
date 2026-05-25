package com.lingshu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingshu.dto.request.SearchRequest;
import com.lingshu.dto.response.SearchResponse;
import com.lingshu.entity.Question;
import com.lingshu.entity.Tag;
import com.lingshu.entity.SearchHistory;
import com.lingshu.mapper.QuestionMapper;
import com.lingshu.mapper.TagMapper;
import com.lingshu.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final QuestionMapper questionMapper;
    private final TagMapper tagMapper;
    private final SearchHistoryMapper searchHistoryMapper;

    /**
     * 搜索题目 - 支持关键词搜索
     */
    public SearchResponse search(SearchRequest request) {
        return searchWithDatabase(request);
    }

    /**
     * 内部获取热门题目
     */
    private IPage<Question> getHotQuestionsInternal(int page, int size) {
        Page<Question> pageInfo = new Page<>(page, size);
        return questionMapper.searchAll(pageInfo, null, null, null);
    }

    /**
     * 获取热门题目
     */
    public SearchResponse getHotQuestions(int page, int size) {
        SearchResponse response = new SearchResponse();

        // 获取热门题目
        IPage<Question> questions = getHotQuestionsInternal(page, size);
        List<SearchResponse.QuestionResponse> responses = questions.getRecords().stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());

        response.setQuestions(responses);
        response.setTotal((int) questions.getTotal());
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    /**
     * 获取搜索统计信息
     */
    public Map<String, Object> getSearchStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 1. 总题目数
        long totalQuestions = questionMapper.selectCount(null);
        statistics.put("totalQuestions", totalQuestions);

        // 2. 难度分布
        Map<String, Long> difficultyStats = new HashMap<>();
        for (Question.Difficulty difficulty : Question.Difficulty.values()) {
            long count = questionMapper.countByDifficultyAndIsVisibleTrue(difficulty);
            difficultyStats.put(difficulty.name(), count);
        }
        statistics.put("difficultyStats", difficultyStats);

        // 3. 标签使用统计(按question_count排序)
        List<Tag> tags = tagMapper.selectList(null);
        List<SearchResponse.TagResponse> topTags = tags.stream()
                .filter(tag -> tag.getQuestionCount() != null && tag.getQuestionCount() > 0)
                .sorted((t1, t2) -> t2.getQuestionCount() - t1.getQuestionCount())
                .limit(10)
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());

        // 格式化标签数据
        List<Map<String, Object>> topTagsFormatted = topTags.stream()
                .map(tag -> {
                    Map<String, Object> tagInfo = new HashMap<>();
                    tagInfo.put("id", tag.getId());
                    tagInfo.put("name", tag.getName());
                    tagInfo.put("count", tag.getQuestionCount());
                    return tagInfo;
                })
                .collect(Collectors.toList());
        statistics.put("topTags", topTagsFormatted);

        // 4. 热门关键词
        statistics.put("popularKeywords", getPopularKeywords(10));

        // 5. 分类统计（预留）
        // 待实现statistics.put("categoryStats", getCategoryStatistics());

        // 6. 搜索趋势（预留）
        // statistics.put("searchTrends", getSearchTrends());

        return statistics;
    }

    /**
     * 数据库搜索实现
     */
    private SearchResponse searchWithDatabase(SearchRequest request) {
        SearchResponse response = new SearchResponse();

        // 分页设置
        Page<Question> pageInfo = new Page<>(request.getPage(), request.getSize());

        // 处理难度参数
        Question.Difficulty difficultyEnum = null;
        List<String> difficultyList = request.getDifficultyList();
        if (difficultyList != null && !difficultyList.isEmpty()) {
            try {
                String difficultyStr = difficultyList.get(0);
                if (difficultyStr != null) {
                    difficultyEnum = Question.Difficulty.valueOf(
                            difficultyStr.toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                log.warn("难度参数无效: {}", difficultyList.get(0));
            }
        }

        Long categoryId = null;
        List<Long> categoryIds = request.getCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryId = categoryIds.get(0);
        }

        // 执行搜索
        IPage<Question> questions = questionMapper.searchAll(pageInfo, request.getKeyword(), categoryId, difficultyEnum);

        // 转换结果
        List<SearchResponse.QuestionResponse> questionResponses = questions.getRecords().stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());

        response.setQuestions(questionResponses);
        response.setTotal((int) questions.getTotal());
        response.setPage(request.getPage());
        response.setSize(request.getSize());

        return response;
    }

    /**
     * 获取搜索建议
     */
    public List<String> getSearchSuggestions(String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.length() < 2) {
            return new ArrayList<>();
        }

        // 获取标题建议
        return questionMapper.suggestTitles(keyword);
    }

    /**
     * 获取热门关键词
     */
    public List<String> getPopularKeywords(int limit) {
        // 查询搜索历史中的热门关键词
        QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("keyword, count(*) as count")
                .groupBy("keyword")
                .orderByDesc("count")
                .last("LIMIT " + limit);
        List<Map<String, Object>> results = searchHistoryMapper.selectMaps(queryWrapper);
        
        return results.stream()
                .map(result -> (String) result.get("keyword"))
                .collect(Collectors.toList());
    }

    /**
     * 按标签搜索
     */
    public SearchResponse searchByTag(String tagName, int page, int size) {
        SearchResponse response = new SearchResponse();

        // 按标签名称搜索题目
        Page<Question> pageInfo = new Page<>(page, size);
        IPage<Question> questions = questionMapper.searchByTagName(pageInfo, tagName);

        List<SearchResponse.QuestionResponse> questionResponses = questions.getRecords().stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());

        response.setQuestions(questionResponses);
        response.setTotal((int) questions.getTotal());
        response.setPage(page);
        response.setSize(size);

        return response;
    }

    /**
     * 获取所有标签（按使用次数排序）
     */
    public List<SearchResponse.TagResponse> getAllTags() {
        List<Tag> tags = tagMapper.selectList(null);
        // 按使用次数排序
        tags.sort((t1, t2) -> {
            int count1 = t1.getQuestionCount() != null ? t1.getQuestionCount() : 0;
            int count2 = t2.getQuestionCount() != null ? t2.getQuestionCount() : 0;
            return Integer.compare(count2, count1); // 降序排列
        });

        return tags.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    /**
     * 搜索标签
     */
    public List<SearchResponse.TagResponse> searchTags(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllTags();
        }

        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", keyword);
        List<Tag> tags = tagMapper.selectList(queryWrapper);
        
        // 按使用次数排序
        tags.sort((t1, t2) -> {
            int count1 = t1.getQuestionCount() != null ? t1.getQuestionCount() : 0;
            int count2 = t2.getQuestionCount() != null ? t2.getQuestionCount() : 0;
            return Integer.compare(count2, count1); // 降序排列
        });

        return tags.stream()
                .map(this::convertToTagResponse)
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 转换Question为响应对象
     */
    private SearchResponse.QuestionResponse convertToSearchResponse(Question question) {
        if (question == null) {
            return null;
        }

        return SearchResponse.QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .slug(question.getSlug())
                .description(question.getDescription())
                .difficulty(question.getDifficulty().name())
                .difficultyLabel(question.getDifficulty().getLabel())
                .categoryId(question.getCategoryId())
                .markCount(question.getMarkCount())
                .browseCount(question.getBrowseCount())
                .viewCount(question.getViewCount())
                .submitCount(question.getSubmitCount())
                .acceptCount(question.getAcceptCount())
                .acceptRate(question.getAcceptRate())
                .createdAt(question.getCreatedAt())
                .build();
    }

    /**
     * 转换Tag为响应对象
     */
    private SearchResponse.TagResponse convertToTagResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        return SearchResponse.TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .color(tag.getColor())
                .questionCount(tag.getQuestionCount())
                .description(tag.getDescription())
                .build();
    }

    /**
     * 获取标签使用统计（用于Controller层）
     */
    public Map<String, Long> getTagUsageStatistics() {
        List<Tag> tags = tagMapper.selectList(null);
        Map<String, Long> statistics = new HashMap<>();

        for (Tag tag : tags) {
            statistics.put(tag.getName(),
                    tag.getQuestionCount() != null ? tag.getQuestionCount().longValue() : 0L);
        }

        return statistics;
    }

    /**
     * 获取搜索热力图数据
     */
    public Map<String, Object> getSearchHeatMap() {
        Map<String, Object> heatMap = new HashMap<>();

        // 难度分布统计
        Map<String, Long> difficultyCounts = new HashMap<>();
        for (Question.Difficulty difficulty : Question.Difficulty.values()) {
            long count = questionMapper.countByDifficultyAndIsVisibleTrue(difficulty);
            difficultyCounts.put(difficulty.name(), count);
        }
        heatMap.put("difficultyDistribution", difficultyCounts);

        // 热门标签TOP20
        List<Map<String, Object>> hotTags = tagMapper.selectList(null)
                .stream()
                .filter(tag -> tag.getQuestionCount() != null && tag.getQuestionCount() > 0)
                .sorted((t1, t2) -> t2.getQuestionCount() - t1.getQuestionCount())
                .limit(20)
                .map(tag -> {
                    Map<String, Object> tagInfo = new HashMap<>();
                    tagInfo.put("name", tag.getName());
                    tagInfo.put("count", tag.getQuestionCount());
                    tagInfo.put("color", tag.getColor());
                    return tagInfo;
                })
                .collect(Collectors.toList());
        heatMap.put("hotTags", hotTags);

        return heatMap;
    }

    /**
     * 保存搜索历史
     */
    public void saveSearchHistory(Long userId, String keyword, String ipAddress, String userAgent) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        try {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(userId);
            searchHistory.setKeyword(keyword.trim());
            searchHistory.setIpAddress(ipAddress);
            searchHistory.setUserAgent(userAgent);
            searchHistoryMapper.insert(searchHistory);
        } catch (Exception e) {
            log.error("保存搜索历史失败", e);
        }
    }

    /**
     * 获取用户搜索历史
     */
    public List<String> getUserSearchHistory(Long userId, int limit) {
        try {
            QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("created_at")
                    .last("LIMIT " + limit);
            List<SearchHistory> histories = searchHistoryMapper.selectList(queryWrapper);
            
            return histories.stream()
                    .map(SearchHistory::getKeyword)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户搜索历史失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 清空用户搜索历史
     */
    public void clearSearchHistory(Long userId) {
        try {
            QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            searchHistoryMapper.delete(queryWrapper);
        } catch (Exception e) {
            log.error("清空搜索历史失败", e);
        }
    }
}
