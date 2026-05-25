import service from '@/utils/axios'

export interface JobRoleOption {
  id: number
  code: string
  name: string
  description: string
  applicableExperience: string
  typicalTechStack: string[]
  interviewFocus: string[]
}

export interface JobRoleConfig {
  role: JobRoleOption
  skillDimensions: Array<{
    code: string
    name: string
    weight: number
    description: string
  }>
  template?: {
    templateCode: string
    name: string
    interviewType: string
    difficulty: string
    durationMinutes: number
    defaultQuestionCount: number
    followUpIntensity: string
    questionTypeDistribution: Record<string, number>
    difficultyDistribution: Record<string, number>
    scoringWeights: Record<string, number>
  }
}

const jobRoleApi = {
  async listJobRoles() {
    const response = await service.get('/api/job-roles')
    return response.data || []
  },

  async getJobRoleConfig(code: string) {
    const response = await service.get(`/api/job-roles/${code}`)
    return response.data || null
  }
}

export default jobRoleApi
