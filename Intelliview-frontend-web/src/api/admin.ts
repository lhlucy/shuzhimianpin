import service from '@/utils/axios'
import type { AIInterviewSummary } from '@/api/aiInterview'

export interface AdminDashboardStats {
  questionCount: number
  paperCount: number
  bankCount: number
  userCount: number
}

export interface AdminActivityItem {
  title: string
  description: string
  user?: string
  time?: string
}

export interface AdminWeaknessTag {
  keyword: string
  count: number
  suggestion?: string
}

export interface AdminUser {
  id?: number
  username: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  role?: string
  enabled?: boolean
  createTime?: string
  lastLoginTime?: string
}

export interface QuestionBankRecord {
  id: number
  name?: string
  title?: string
  questionCount: number
  createdAt: string
  updatedAt?: string
  description?: string
  status?: boolean
  viewCount?: number
}

export interface AdminInterviewRecord {
  interviewId: number
  title: string
  status: string
  targetPosition: string
  interviewLanguage: string
  jobRoleId?: number
  jobRoleName?: string
  username?: string
  nickname?: string
  techStacks: string[]
  questionCount: number
  answeredCount: number
  totalScore?: number
  duration?: number
  createdAt: string
  endedAt?: string
}

export interface AdminJobRoleRecord {
  id: number
  code: string
  name: string
  description?: string
  applicableExperience?: string
  category?: string
  difficultyLevel?: string
  typicalTechStack?: string
  interviewFocus?: string
  isActive?: boolean
  createdAt?: string
  updatedAt?: string
}

interface PageResult<T> {
  records: T[]
  total: number
  current: number
}

const unwrap = <T>(response: any, fallback: T): T => response?.data ?? fallback

const adminApi = {
  async getDashboardStats() {
    const response = await service.get('/api/admin/dashboard/stats')
    return unwrap<AdminDashboardStats>(response, {
      questionCount: 0,
      paperCount: 0,
      bankCount: 0,
      userCount: 0
    })
  },

  async getRecentActivities() {
    const response = await service.get('/api/admin/dashboard/activities')
    return unwrap<AdminActivityItem[]>(response, [])
  },

  async getDifficultyDistribution() {
    const response = await service.get('/api/admin/dashboard/stats/questions/difficulty')
    return unwrap<Record<string, number>>(response, {})
  },

  async getTypeDistribution() {
    const response = await service.get('/api/admin/dashboard/stats/questions/type')
    return unwrap<Record<string, number>>(response, {})
  },

  async getWeaknessTags() {
    const response = await service.get('/api/admin/dashboard/weakness-tags')
    return unwrap<AdminWeaknessTag[]>(response, [])
  },

  async getUsers(keyword?: string) {
    const response = await service.get(keyword ? '/api/admin/users/search' : '/api/admin/users', {
      params: keyword ? { username: keyword } : undefined
    })
    return unwrap<AdminUser[]>(response, [])
  },

  async updateUserRole(username: string, role: 'ROLE_ADMIN' | 'ROLE_USER') {
    await service.put('/api/admin/users/role', { username, role })
  },

  async getQuestionBanks(page: number, size: number) {
    const response = await service.get('/api/banks/list', {
      params: {
        page,
        size,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      }
    })

    return unwrap<PageResult<any>>(response, {
      records: [],
      total: 0,
      current: 0
    })
  },

  async createQuestionBank(payload: { title: string; description: string }) {
    await service.post('/api/banks/create', payload)
  },

  async updateQuestionBank(id: number, payload: { title: string; description: string }) {
    await service.put(`/api/banks/update/${id}`, payload)
  },

  async deleteQuestionBank(id: number) {
    await service.delete(`/api/banks/delete/${id}`)
  },

  async getInterviewRecords(params?: { keyword?: string; status?: string; jobRoleId?: number }) {
    const response = await service.get('/api/admin/interviews', { params })
    return unwrap<AdminInterviewRecord[]>(response, [])
  },

  async getInterviewSummary(interviewId: number) {
    const response = await service.get(`/api/admin/interviews/${interviewId}/summary`)
    return unwrap<AIInterviewSummary | null>(response, null)
  },

  async getInterviewRoles(params?: { keyword?: string; activeOnly?: boolean }) {
    const response = await service.get('/api/admin/job-roles', { params })
    return unwrap<AdminJobRoleRecord[]>(response, [])
  },

  async createInterviewRole(payload: Omit<AdminJobRoleRecord, 'id' | 'createdAt' | 'updatedAt'>) {
    const response = await service.post('/api/admin/job-roles', payload)
    return unwrap<AdminJobRoleRecord | null>(response, null)
  },

  async updateInterviewRole(id: number, payload: Partial<AdminJobRoleRecord>) {
    const response = await service.put(`/api/admin/job-roles/${id}`, payload)
    return unwrap<AdminJobRoleRecord | null>(response, null)
  },

  async deactivateInterviewRole(id: number) {
    const response = await service.delete(`/api/admin/job-roles/${id}`)
    return unwrap<{ archived: boolean; usageCount: number } | null>(response, null)
  }
}

export default adminApi
