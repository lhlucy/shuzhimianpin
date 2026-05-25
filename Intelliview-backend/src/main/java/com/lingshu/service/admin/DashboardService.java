package com.lingshu.service.admin;

import java.util.Map;
import java.util.List;

/**
 * 管理端仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取系统概览统计数据
     * @return 系统统计数据
     */
    Map<String, Object> getSystemStats();

    /**
     * 获取最近活动日志
     * @return 最近活动列表
     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * 获取题目难度分布
     * @return 难度分布数据
     */
    Map<String, Integer> getQuestionDifficultyDistribution();

    /**
     * 获取题目类型分布
     * @return 类型分布数据
     */
    Map<String, Integer> getQuestionTypeDistribution();

    /**
     * 获取 AI 面试高频短板统计
     * @return 短板标签及出现次数
     */
    List<Map<String, Object>> getWeaknessTags();

}
