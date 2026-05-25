import service from '@/utils/axios';

/**
 * 刷题记录相关API调用封装
 */
export const practiceHistoryApi = {
  /**
   * 获取用户的刷题记录列表
   * @param page 页码
   * @param size 每页数量
   * @returns 刷题记录列表
   */
  async getUserPracticeHistory(page: number = 0, size: number = 10) {
    const response = await service.get('/api/practice-history/list', {
      params: { page, size }
    });
    return response.data || [];
  },

  /**
   * 获取用户的刷题统计信息
   * @returns 刷题统计信息
   */
  async getUserPracticeStats() {
    const response = await service.get('/api/practice-history/stats');
    return response.data || {};
  },

  /**
   * 获取用户已完成的题目ID列表
   * @returns 已完成题目ID列表
   */
  async getCompletedQuestionIds() {
    const response = await service.get('/api/practice-history/completed-questions');
    return response.data || [];
  },

  /**
   * 检查题目是否已完成
   * @param questionId 题目ID
   * @returns 是否已完成
   */
  async isQuestionCompleted(questionId: number) {
    const response = await service.get(`/api/practice-history/completed/${questionId}`);
    return response.data || false;
  }
};

export default practiceHistoryApi;