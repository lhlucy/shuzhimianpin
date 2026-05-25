<template>
  <div style="padding: 20px; background: #f9fafb; min-height: 100vh;">
    <el-page-header @back="goBack" content="题库详情" style="margin-bottom: 20px;" />
    
    <!-- 加载状态 -->
    <div v-if="loading" style="margin-bottom: 20px;">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" style="margin-bottom: 20px;">
      <el-empty>
        <template #description>
          <p>{{ error }}</p>
          <el-button type="primary" size="small" @click="fetchBankDetails">重试</el-button>
        </template>
      </el-empty>
    </div>
    
    <el-card v-else-if="bank">
      <h2 style="margin-bottom: 10px; color: #1f2937;">{{ bank.title }}</h2>
      <p style="color: #6b7280; margin-bottom: 20px;">{{ bank.description }}</p>
      <el-tag type="info">{{ bank.questionCount }} 题</el-tag>
      <el-tag v-if="bank.popular" type="success" style="margin-left: 10px;">热门</el-tag>
      <div style="margin-top: 20px;">
        <h3 style="margin-bottom: 10px; color: #1f2937;">题目列表</h3>
        <el-table :data="questions" style="width: 100%">
          <el-table-column prop="title" label="题目" />
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="scope">
              <el-tag :type="getDifficultyType(scope.row.difficulty)" size="small">
                {{ scope.row.difficultyLabel || (scope.row.difficulty === 'EASY' ? '简单' : scope.row.difficulty === 'MEDIUM' ? '中等' : '困难') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="收藏" width="100">
            <template #default="scope">
              <el-button
                :type="favoriteQuestionIds.has(scope.row.id) ? 'warning' : 'default'"
                size="small"
                @click="toggleFavorite(scope.row.id)"
              >
                {{ favoriteQuestionIds.has(scope.row.id) ? '已收藏' : '收藏' }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewQuestion(scope.row.id)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px;">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import service from '@/utils/axios'

const route = useRoute()
const router = useRouter()

interface QuestionBank {
  id: number
  title: string
  description: string
  questionCount: number
  icon: string
  popular: boolean
  viewCount: number
}

interface Question {
  id: number
  title: string
  description: string
  difficulty: string
  difficultyLabel: string
  categoryId: number
  markCount: number
  shareCount: number
  browseCount: number
  viewCount: number
  slug: string
}

const bank = ref<QuestionBank | null>(null)
const questions = ref<Question[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = ref(10)
const total = ref(0)
const favoriteQuestionIds = ref<Set<number>>(new Set())

const getDifficultyType = (difficulty: string): string => {
  const typeMap: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return typeMap[difficulty] || 'default'
}

const goBack = () => {
  router.back()
}

const viewQuestion = (id: number) => {
  router.push(`/question/${id}`)
}

const loadFavorites = async () => {
  try {
    const response = await service.get('/api/questions/favorites', {
      params: { page: 0, size: 200 }
    }) as any
    const records = response?.data?.records || response?.records || []
    favoriteQuestionIds.value = new Set(
      records.map((item: any) => Number(item.id)).filter((id: number) => Number.isFinite(id) && id > 0)
    )
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  }
}

const toggleFavorite = async (questionId: number) => {
  const favorited = favoriteQuestionIds.value.has(questionId)
  try {
    if (favorited) {
      await service.delete(`/api/favorites/${questionId}`, { data: {} })
    } else {
      await service.post(`/api/favorites/${questionId}`, {})
    }

    const next = new Set(favoriteQuestionIds.value)
    if (favorited) {
      next.delete(questionId)
    } else {
      next.add(questionId)
    }
    favoriteQuestionIds.value = next
    ElMessage.success(favorited ? '已取消收藏' : '收藏成功')
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error?.message || '收藏操作失败，请稍后重试')
  }
}

// 从API获取题库详情
const fetchBankDetail = async (bankId: string) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/banks/detail`, {
      params: { id: bankId },
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    
    if (response.data.success) {
      bank.value = response.data.data
    } else {
      throw new Error(response.data.message || '获取题库详情失败')
    }
  } catch (err: any) {
    console.error('获取题库详情失败:', err)
    throw err
  }
}

// 从API获取题库题目数据
const fetchQuestions = async () => {
  try {
    const bankId = route.params.id as string
    const token = localStorage.getItem('token')
    
    const response = await axios.get(`/api/banks/${bankId}/questions`, {
      params: {
        page: currentPage.value,
        size: pageSize.value
      },
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    
    if (response.data.success) {
      questions.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      throw new Error(response.data.message || '获取题目失败')
    }
  } catch (err: any) {
    console.error('获取题目失败:', err)
    throw err
  }
}

// 从API获取题库和题目数据
const fetchBankDetails = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const bankId = route.params.id as string
    
    // 并行请求题库详情和题目列表
    await Promise.all([
      fetchBankDetail(bankId),
      fetchQuestions()
    ])
  } catch (err: any) {
    console.error('获取题库详情失败:', err)
    error.value = err.message || '网络错误，请检查后端服务是否正常'
    ElMessage.error(error.value)
    
    // 如果API调用失败，使用默认数据
    const bankId = route.params.id as string
    bank.value = {
      id: parseInt(bankId),
      title: `题库 ${bankId}`,
      description: '该题库相关的面试题目汇总',
      questionCount: 0,
      icon: 'el-icon-document',
      popular: false,
      viewCount: 0
    }
    questions.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchQuestions()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchQuestions()
}

onMounted(() => {
  loadFavorites()
  fetchBankDetails()
})
</script>

<style scoped>
</style>
