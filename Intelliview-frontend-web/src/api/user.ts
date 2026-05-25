import service from '@/utils/axios'
import type { AuthUser } from '@/utils/auth'

export interface PracticeStats {
  totalCount: number
  completedCount: number
  viewAnswerCount: number
  totalDuration: number
  completionRate: number
}

export const emptyPracticeStats: PracticeStats = {
  totalCount: 0,
  completedCount: 0,
  viewAnswerCount: 0,
  totalDuration: 0,
  completionRate: 0
}

const unwrap = <T>(response: any, fallback: T): T => response?.data ?? fallback

const userApi = {
  async getProfile() {
    const response = await service.get('/api/user/profile')
    return unwrap<AuthUser | null>(response, null)
  },

  async updateProfile(payload: Partial<AuthUser>) {
    const response = await service.put('/api/user/profile', payload)
    return unwrap<AuthUser | null>(response, null)
  },

  async uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await service.post('/api/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return unwrap<AuthUser | null>(response, null)
  },

  async getPracticeStats() {
    const response = await service.get('/api/practice-history/stats')
    return unwrap<PracticeStats>(response, emptyPracticeStats)
  },

  async getCompletedQuestionIds() {
    const response = await service.get('/api/practice-history/completed-questions')
    return unwrap<number[]>(response, [])
  },

  async recordPractice(questionId: number, viewAnswer = false, duration = 0) {
    const response = await service.post('/api/practice-history/record', {
      questionId,
      viewAnswer,
      duration
    })
    return response?.data
  }
}

export default userApi
