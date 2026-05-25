import service from '@/utils/axios'

const AI_INTERVIEW_TIMEOUT = 30000

export interface AIInterviewCreatePayload {
  interviewName: string
  jobRoleId?: number
  targetPosition: string
  interviewLanguage: string
  techStacks: string[]
  practicedQuestionIds?: number[]
  resumeFileName?: string
  resumeContent?: string
  voiceEnabled?: boolean
}

export interface AIInterviewQuestion {
  questionId: number
  content: string
  type: string
  topic: string
  questionOrder: number
  estimatedTime: number
  followUp: boolean
  answered: boolean
}

export interface AIInterviewSession {
  interviewId: number
  title: string
  status: string
  targetPosition: string
  interviewLanguage: string
  techStacks: string[]
  practicedQuestions: string[]
  resumeFileName?: string
  openingMessage?: string
  voiceEnabled?: boolean
  questionCount: number
  answeredCount: number
  currentQuestion?: AIInterviewQuestion | null
}

export interface AIInterviewHistoryItem {
  interviewId: number
  title: string
  status: string
  targetPosition: string
  interviewLanguage: string
  techStacks: string[]
  questionCount: number
  answeredCount: number
  totalScore?: number
  duration?: number
  resumeFileName?: string
  createdAt: string
  endedAt?: string
}

export interface AIInterviewResumeParseResult {
  fileName: string
  summary: string
  content: string
  contentLength: number
  intentionJob?: string
  recruitmentType?: string
  intentionCity?: string
  expectedSalary?: string
}

export interface AIInterviewAnswerPayload {
  content: string
  inputMode?: string
  duration?: number
}

export interface AIInterviewAnswerResult {
  answerId: number
  nextAction: 'FOLLOW_UP' | 'NEXT' | 'END'
  interviewerReply: string
  score: number
  dimensionScores?: Record<string, number>
  interviewCompleted: boolean
  summaryReady: boolean
  nextQuestion?: AIInterviewQuestion | null
}

export interface AIInterviewTranscriptionResult {
  transcript: string
  fileName?: string
  mimeType?: string
}

export interface AIInterviewSpeechPayload {
  text: string
  interviewLanguage?: string
}

export interface AIInterviewSpeechResult {
  audioUrl: string
  expiresAt?: number
  voice: string
  format: string
  text: string
}

export interface AIInterviewAvatarSession {
  enabled: boolean
  connected: boolean
  playerType?: number
  sid?: string
  server?: string
  auth?: string
  appid?: string
  userId?: string
  roomId?: string
  timeStr?: string
  streamUrl?: string
  width?: number
  height?: number
  message?: string
}

export interface AIInterviewAvatarSpeakPayload {
  text: string
}

export interface AIInterviewSummary {
  interviewId: number
  overallScore: number
  dimensionScores?: Record<string, number>
  summary: string
  strengths: string[]
  weaknesses: string[]
  suggestions: string[]
  totalQuestions: number
  answeredQuestions: number
  durationSeconds?: number
  completionRate?: number
  averageAnswerScore?: number
  expressionScore?: number
  keywordCoverageScore?: number
  techStacks?: string[]
  overview?: Record<string, any>
  questionReviews?: Array<{
    questionId: number
    questionOrder?: number
    questionType?: string
    questionContent?: string
    answerContent?: string
    score?: number
    dimensionScores?: Record<string, number>
    duration?: number
    confidenceLevel?: number
    feedbackSummary?: string
    strengths?: string[]
    weaknesses?: string[]
    suggestions?: string[]
    hitKeywords?: string[]
    missingKeywords?: string[]
  }>
  completedAt: string
}

export interface AIInterviewGrowthAnalysis {
  overallTrend: {
    averageScore: number
    latestScore: number
    scoreChange: number
    labels: string[]
    scoreTrend: number[]
    interviewCount: number
  }
  dimensionTrends: Array<{
    key: string
    name: string
    trend: 'improving' | 'declining' | 'stable'
    values: number[]
    labels: string[]
  }>
  weaknessTracking: Array<{
    keyword: string
    status: 'persistent' | 'new' | 'improved' | 'tracking'
    occurrences: number
    suggestion: string
  }>
  recommendations: Array<{
    type: 'QUESTION' | 'INTERVIEW' | string
    title: string
    reason: string
    relatedQuestionIds: number[]
  }>
}

const aiInterviewApi = {
  async createInterview(payload: AIInterviewCreatePayload) {
    const response = await service.post('/api/interviews/ai/personalized', payload, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSession
  },

  async parseResume(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    const response = await service.post('/api/interviews/ai/resume/parse', formData, {
      timeout: AI_INTERVIEW_TIMEOUT,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data as AIInterviewResumeParseResult
  },

  async transcribeAudio(file: File, interviewLanguage?: string) {
    const formData = new FormData()
    formData.append('file', file)
    if (interviewLanguage) {
      formData.append('interviewLanguage', interviewLanguage)
    }
    const response = await service.post('/api/interviews/ai/voice/transcribe', formData, {
      timeout: AI_INTERVIEW_TIMEOUT,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data as AIInterviewTranscriptionResult
  },

  async synthesizeSpeech(payload: AIInterviewSpeechPayload) {
    const response = await service.post('/api/interviews/ai/voice/synthesize', payload, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSpeechResult
  },

  async startInterview(interviewId: number) {
    const response = await service.post(`/api/interviews/ai/${interviewId}/start`, null, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSession
  },

  async getInterview(interviewId: number) {
    const response = await service.get(`/api/interviews/ai/${interviewId}`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSession
  },

  async initAvatarSession(interviewId: number) {
    const response = await service.get(`/api/interviews/ai/${interviewId}/avatar/session`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewAvatarSession
  },

  async speakAvatar(interviewId: number, payload: AIInterviewAvatarSpeakPayload) {
    const response = await service.post(`/api/interviews/ai/${interviewId}/avatar/speak`, payload, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as boolean
  },

  async stopAvatarSession(interviewId: number) {
    const response = await service.delete(`/api/interviews/ai/${interviewId}/avatar/session`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as boolean
  },

  stopAvatarSessionOnPageExit(interviewId: number) {
    const url = `/api/interviews/ai/${interviewId}/avatar/session`
    const absoluteUrl = service.defaults.baseURL ? `${service.defaults.baseURL}${url}` : url
    try {
      fetch(absoluteUrl, {
        method: 'DELETE',
        keepalive: true,
        credentials: 'include'
      }).catch(() => undefined)
    } catch {
      // 页面关闭阶段忽略网络异常
    }
  },

  async getInterviewHistory(limit = 6) {
    const response = await service.get('/api/interviews/ai/history', {
      timeout: AI_INTERVIEW_TIMEOUT,
      params: { limit }
    })
    return response.data as AIInterviewHistoryItem[]
  },

  async getGrowthAnalysis() {
    const response = await service.get('/api/interviews/ai/growth-analysis', {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewGrowthAnalysis
  },

  async deleteInterview(interviewId: number) {
    const response = await service.delete(`/api/interviews/ai/${interviewId}`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as boolean
  },

  async getNextQuestion(interviewId: number) {
    const response = await service.get(`/api/interviews/ai/${interviewId}/questions/next`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewQuestion | null
  },

  async submitAnswer(interviewId: number, questionId: number, payload: AIInterviewAnswerPayload) {
    const response = await service.post(`/api/interviews/ai/${interviewId}/questions/${questionId}/answers`, payload, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewAnswerResult
  },

  async endInterview(interviewId: number) {
    const response = await service.post(`/api/interviews/ai/${interviewId}/end`, null, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSummary
  },

  async getSummary(interviewId: number) {
    const response = await service.get(`/api/interviews/ai/${interviewId}/summary`, {
      timeout: AI_INTERVIEW_TIMEOUT
    })
    return response.data as AIInterviewSummary
  }
}

export default aiInterviewApi
