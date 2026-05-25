import service from '@/utils/axios'

export interface UserResume {
  id: number
  fileName: string
  originalFileName: string
  fileUrl: string
  fileType?: string
  fileSize?: number
  content?: string
  summary?: string
  intentionJob?: string
  recruitmentType?: string
  intentionCity?: string
  expectedSalary?: string
  parsedAt?: string
  createdAt?: string
}

const unwrap = <T>(response: any, fallback: T): T => response?.data ?? fallback

const resumeApi = {
  async listResumes() {
    const response = await service.get('/api/user/resumes')
    return unwrap<UserResume[]>(response, [])
  },

  async uploadResume(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await service.post('/api/user/resumes', formData, {
      timeout: 30000,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return unwrap<UserResume | null>(response, null)
  },

  async deleteResume(id: number) {
    await service.delete(`/api/user/resumes/${id}`)
  }
}

export default resumeApi
