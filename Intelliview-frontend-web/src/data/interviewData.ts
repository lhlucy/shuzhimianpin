import { onMounted, ref } from 'vue'
import { ChatDotRound, Cpu, DataAnalysis, Monitor, Platform } from '@element-plus/icons-vue'
import service from '@/utils/axios'
import jobRoleApi from '@/api/jobRoles'

export interface RoleQuestion {
  id: number
  title: string
  difficulty: '简单' | '中等' | '困难'
  category: string
  done: boolean
  favorite?: boolean
  detail: string
  points: string[]
  answer: string[]
}

export interface RoleBank {
  id?: number
  code: string
  name: string
  subtitle: string
  color: string
  icon: any
  hotQuestion: string
  heat: string
  focus: string[]
  questions: RoleQuestion[]
}

const iconMap: Record<string, any> = {
  java_backend: Cpu,
  frontend: Monitor,
  python: Platform,
  algorithm: DataAnalysis
}

const colorMap: Record<string, string> = {
  java_backend: '#e87722',
  frontend: '#2f80ed',
  python: '#18a999',
  algorithm: '#8b5cf6'
}

const difficultyLabel = (difficulty?: string): RoleQuestion['difficulty'] => {
  if (difficulty === 'EASY') return '简单'
  if (difficulty === 'HARD') return '困难'
  return '中等'
}

const normalizeRolePayload = (payload: any) => {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  return []
}

const normalizeQuestionRecords = (payload: any) => {
  const data = payload?.data ?? payload
  if (Array.isArray(data?.records)) return data.records
  if (Array.isArray(data?.list)) return data.list
  return []
}

const loadRoleQuestions = async (roleCode: string, focus: string[]) => {
  try {
    const response = await service.get('/api/questions', {
      params: {
        page: 0,
        size: 200,
        primaryJobRoleCode: roleCode,
        isForPractice: true,
        sortBy: 'interviewFrequency',
        sortDirection: 'desc'
      }
    })

    return normalizeQuestionRecords(response).map((question: any, index: number) => ({
      id: question.id,
      title: question.title,
      difficulty: question.difficultyLabel || difficultyLabel(question.difficulty),
      category: question.categoryName || question.questionType || focus[index % Math.max(focus.length, 1)] || '综合能力',
      done: false,
      favorite: false,
      detail: question.description || question.questionText || '来自真实题库的岗位练习题。',
      points: focus.length ? focus : ['核心概念', '项目结合', '表达结构'],
      answer: ['点击显示答案后会读取题库详情中的真实参考答案。']
    }))
  } catch (error) {
    console.error('加载岗位题目失败:', error)
    return []
  }
}

export const useRoleBanks = () => {
  const roleBanks = ref<RoleBank[]>([])
  const loading = ref(false)

  const loadRoleBanks = async () => {
    loading.value = true
    try {
      const payload = await jobRoleApi.listJobRoles()
      const roles = normalizeRolePayload(payload)
      const banks = await Promise.all(
        roles.map(async (role: any) => {
          const focus = role.interviewFocus || role.typicalTechStack || []
          const questions = await loadRoleQuestions(role.code, focus)
          return {
            id: role.id,
            code: role.code,
            name: role.name,
            subtitle: role.description || `${focus.slice(0, 4).join('、')}专项训练`,
            color: colorMap[role.code] || '#ff5a2a',
            icon: iconMap[role.code] || ChatDotRound,
            hotQuestion: questions[0]?.title || '暂无推荐题目',
            heat: `${questions.length} 道推荐题`,
            focus,
            questions
          }
        })
      )
      roleBanks.value = banks
    } finally {
      loading.value = false
    }
  }

  onMounted(loadRoleBanks)

  return {
    roleBanks,
    loading,
    reloadRoleBanks: loadRoleBanks
  }
}

export const interviewTypes = ['校招一面', '社招技术面', '项目深挖', '主管终面']

export const assistantQuickQuestions = [
  '这题面试官想考什么？',
  '给我一个 1 分钟回答模板',
  '这题有哪些加分点？'
]

export const navIcon = ChatDotRound
