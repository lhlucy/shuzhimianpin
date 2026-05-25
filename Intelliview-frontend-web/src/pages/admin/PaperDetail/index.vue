<template>
  <div class="paper-detail-container">
    <div class="main-content">
      <!-- 顶部导航 -->
      <div class="top-nav">
        <el-page-header @back="goBack" content="试卷详情" />
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <el-empty>
          <template #description>
            <p>{{ error }}</p>
            <el-button type="primary" size="small" @click="fetchPaperDetail">重试</el-button>
          </template>
        </el-empty>
      </div>
      
      <!-- 试卷详情 -->
      <div v-else-if="paperDetail" class="paper-content">
        <!-- 试卷信息卡片 -->
        <el-card class="paper-info-card">
          <template #header>
            <div class="card-header">
              <h2 class="paper-title">{{ paperDetail.title }}</h2>
              <div class="paper-actions">
                <el-button type="primary" size="small" @click="handleEditPaper">
                  <el-icon><Edit /></el-icon> 编辑试卷
                </el-button>
                <el-button type="success" size="small" @click="handleAddQuestion">
                  <el-icon><Plus /></el-icon> 添加题目
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="paper-info">
            <div class="info-row">
              <span class="info-label">试卷ID：</span>
              <span class="info-value">{{ paperDetail.id }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">题目数量：</span>
              <span class="info-value">{{ paperDetail.questionCount }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">考试时长：</span>
              <span class="info-value">{{ paperDetail.duration }} 分钟</span>
            </div>
            <div class="info-row">
              <span class="info-label">状态：</span>
              <el-tag :type="paperDetail.status ? 'success' : 'danger'">
                {{ paperDetail.status ? '启用' : '禁用' }}
              </el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间：</span>
              <span class="info-value">{{ paperDetail.createdAt }}</span>
            </div>
            <div class="info-row" v-if="paperDetail.description">
              <span class="info-label">描述：</span>
              <span class="info-value">{{ paperDetail.description }}</span>
            </div>
          </div>
        </el-card>
        
        <!-- 题目列表 -->
        <div class="questions-section">
          <h3 class="section-title">
            <el-icon><List /></el-icon> 题目列表
          </h3>
          
          <!-- 搜索和筛选 -->
          <div class="questions-filter">
            <el-input
              v-model="searchQuery"
              placeholder="搜索题目"
              clearable
              style="width: 300px; margin-right: 10px;"
              @input="handleFilterQuestions"
            />
            <el-select
              v-model="selectedDifficulty"
              placeholder="选择难度"
              style="width: 150px; margin-right: 10px;"
              @change="handleFilterQuestions"
            >
              <el-option label="全部" value="" />
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </div>
          
          <!-- 题目表格 -->
          <el-table :data="filteredQuestions" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="题目" min-width="400">
              <template #default="scope">
                <el-link 
                  type="primary"
                  @click="navigateToQuestionDetail(scope.row.id)"
                  style="cursor: pointer;"
                >
                  {{ scope.row.title }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="difficulty" label="难度" width="120">
              <template #default="scope">
                <el-tag :type="getDifficultyType(scope.row.difficulty)">
                  {{ scope.row.difficultyLabel || scope.row.difficulty }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="100" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button 
                    size="small" 
                    type="primary" 
                    @click="handleEditQuestion(scope.row)"
                  >
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                  <el-button 
                    size="small" 
                    type="danger" 
                    @click="handleDeleteQuestion(scope.row.id)"
                  >
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑题目对话框 -->
    <el-dialog
      :title="editDialogTitle"
      v-model="editDialogVisible"
      width="900px"
    >
      <el-form :model="editFormData" label-width="100px" style="max-width: 800px;">
        <el-form-item label="题目名称">
          <el-input v-model="editFormData.title" placeholder="请输入题目名称" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="题目难度">
          <el-select v-model="editFormData.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目描述">
          <el-input
            v-model="editFormData.description"
            type="textarea"
            placeholder="请输入题目描述"
            :rows="4"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="题目答案">
          <div style="width: 100%; overflow-x: auto;">
            <MdEditor
              v-model="editFormData.answer"
              placeholder="请输入题目答案，支持Markdown语法"
              :preview="false"
              style="min-height: 300px; width: 100%;"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditQuestion">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑试卷对话框 -->
    <el-dialog
      :title="editPaperDialogTitle"
      v-model="editPaperDialogVisible"
      width="700px"
    >
      <el-form :model="editPaperFormData" label-width="100px" style="max-width: 600px;">
        <el-form-item label="试卷名称">
          <el-input v-model="editPaperFormData.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷描述">
          <el-input
            v-model="editPaperFormData.description"
            type="textarea"
            placeholder="请输入试卷描述"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="editPaperFormData.duration" :min="1" :max="300" placeholder="请输入考试时长（分钟）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch 
            v-model="editPaperFormData.status" 
            active-text="启用" 
            inactive-text="禁用"
            active-color="#667eea"
            inactive-color="#ff4d4f"
            style="--el-switch-on-color: #667eea; --el-switch-off-color: #ff4d4f;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editPaperDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditPaper">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加题目对话框 -->
    <el-dialog
      :title="addDialogTitle"
      v-model="addDialogVisible"
      width="900px"
    >
      <el-form :model="addFormData" label-width="100px" style="max-width: 800px;">
        <el-form-item label="题目名称">
          <el-input v-model="addFormData.title" placeholder="请输入题目名称" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="题目难度">
          <el-select v-model="addFormData.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目描述">
          <el-input
            v-model="addFormData.description"
            type="textarea"
            placeholder="请输入题目描述"
            :rows="4"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="题目答案">
          <div style="width: 100%; overflow-x: auto;">
            <MdEditor
              v-model="addFormData.answer"
              placeholder="请输入题目答案，支持Markdown语法"
              :preview="false"
              style="min-height: 300px; width: 100%;"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddQuestion">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Edit, Delete, List } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const selectedDifficulty = ref('')

// 试卷详情数据
interface PaperDetail {
  id: number
  title: string
  questionCount: number
  duration: number
  status: boolean
  createdAt: string
  description?: string
  updatedAt?: string
}

const paperDetail = ref<PaperDetail | null>(null)

// 题目数据类型
interface Question {
  id: number
  title: string
  difficulty: string
  difficultyLabel?: string
  viewCount: number
  createdAt: string
  updatedAt?: string
  description?: string
  answer?: string
}

const questions = ref<Question[]>([])
const filteredQuestions = ref<Question[]>([])

const unwrapData = (response: any) => response?.data ?? response

const getDifficultyLabel = (difficulty: string) => {
  const labelMap: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return labelMap[difficulty] || difficulty
}

const mapQuestion = (question: any): Question => ({
  id: question.id,
  title: question.title,
  difficulty: question.difficulty,
  difficultyLabel: question.difficultyLabel || getDifficultyLabel(question.difficulty),
  viewCount: question.viewCount || question.view_count || question.browseCount || 0,
  createdAt: question.createdAt || question.created_at || '',
  updatedAt: question.updatedAt || question.updated_at,
  description: question.description || question.questionText || '',
  answer: question.answer || question.answerText || ''
})

// 编辑题目对话框相关状态
const editDialogVisible = ref(false)
const editDialogTitle = ref('编辑题目')
const currentEditQuestion = ref<Question | null>(null)

// 编辑题目表单数据
const editFormData = ref({
  title: '',
  difficulty: '',
  description: '',
  answer: ''
})

// 编辑试卷对话框相关状态
const editPaperDialogVisible = ref(false)
const editPaperDialogTitle = ref('编辑试卷')

// 编辑试卷表单数据
const editPaperFormData = ref({
  title: '',
  description: '',
  duration: 90,
  status: true
})

// 添加题目对话框相关状态
const addDialogVisible = ref(false)
const addDialogTitle = ref('添加题目')

// 添加题目表单数据
const addFormData = ref({
  title: '',
  difficulty: 'MEDIUM',
  description: '',
  answer: ''
})

/**
 * 返回上一页
 */
const goBack = () => {
  router.push('/admin/papers')
}

/**
 * 获取试卷详情
 */
const fetchPaperDetail = async () => {
  const paperId = route.params.id as string
  if (!paperId) {
    error.value = '试卷ID不能为空'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await axios.get(`/api/papers/detail/${paperId}`)
    const data = unwrapData(response)

    if (data) {
      paperDetail.value = {
        id: data.id,
        title: data.title,
        questionCount: data.questionCount || data.question_count || 0,
        duration: data.duration || 0,
        status: data.status !== false,
        createdAt: data.createdAt || data.created_at || '',
        updatedAt: data.updatedAt || data.updated_at,
        description: data.description || ''
      }
    } else {
      error.value = '获取试卷详情失败'
    }
  } catch (err: any) {
    console.error('获取试卷详情失败:', err)
    error.value = '获取试卷详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

/**
 * 获取试卷题目列表
 */
const fetchPaperQuestions = async () => {
  const paperId = route.params.id as string
  if (!paperId) return
  
  loading.value = true
  
  try {
    const response = await axios.get(`/api/papers/${paperId}/questions`, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    const data = unwrapData(response)

    questions.value = Array.isArray(data?.records) ? data.records.map(mapQuestion) : []
    total.value = data?.total || 0
    filteredQuestions.value = [...questions.value]
  } catch (err: any) {
    console.error('获取题目列表失败:', err)
    ElMessage.error('获取题目列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 筛选题目
 */
const handleFilterQuestions = () => {
  let filtered = [...questions.value]
  
  // 根据难度筛选
  if (selectedDifficulty.value) {
    filtered = filtered.filter(q => q.difficulty === selectedDifficulty.value)
  }
  
  // 根据搜索关键词筛选
  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.trim().toLowerCase()
    filtered = filtered.filter(q => 
      q.title.toLowerCase().includes(keyword)
    )
  }
  
  filteredQuestions.value = filtered
}

/**
 * 分页大小变化处理
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchPaperQuestions()
}

/**
 * 当前页码变化处理
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchPaperQuestions()
}

/**
 * 获取难度标签类型
 * @param difficulty 难度级别
 * @returns 标签类型
 */
const getDifficultyType = (difficulty: string): string => {
  const typeMap: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return typeMap[difficulty] || 'default'
}

/**
 * 编辑试卷
 */
const handleEditPaper = () => {
  if (paperDetail.value) {
    editPaperDialogTitle.value = '编辑试卷'
    editPaperFormData.value = {
      title: paperDetail.value.title,
      description: paperDetail.value.description || '',
      duration: paperDetail.value.duration,
      status: paperDetail.value.status
    }
    editPaperDialogVisible.value = true
  }
}

/**
 * 添加题目
 */
const handleAddQuestion = () => {
  addDialogTitle.value = '添加题目'
  addFormData.value = {
    title: '',
    difficulty: 'MEDIUM',
    description: '',
    answer: ''
  }
  addDialogVisible.value = true
}

/**
 * 编辑题目
 * @param question 题目数据
 */
const handleEditQuestion = (question: Question) => {
  editDialogTitle.value = '编辑题目'
  currentEditQuestion.value = question
  editFormData.value = {
    title: question.title,
    difficulty: question.difficulty,
    description: question.description || '',
    answer: (question as any).answer || ''
  }
  editDialogVisible.value = true
}

/**
 * 删除题目
 * @param questionId 题目ID
 */
const handleDeleteQuestion = (questionId: number) => {
  ElMessageBox.confirm('确定要删除这个题目吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const paperId = route.params.id as string
    await axios.delete(`/api/papers/${paperId}/questions/${questionId}`)
    ElMessage.success('题目已从试卷移除')
    await fetchPaperQuestions()
    await fetchPaperDetail()
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 保存编辑的题目
 */
const saveEditQuestion = async () => {
  // 表单验证
  if (!editFormData.value.title) {
    ElMessage.error('请输入题目名称')
    return
  }
  if (!editFormData.value.difficulty) {
    ElMessage.error('请选择题目难度')
    return
  }
  
  if (currentEditQuestion.value) {
    try {
      await axios.put(`/api/admin/questions/${currentEditQuestion.value.id}`, {
        title: editFormData.value.title,
        difficulty: editFormData.value.difficulty,
        description: editFormData.value.description,
        questionText: editFormData.value.description || editFormData.value.title,
        answerText: editFormData.value.answer,
        isVisible: true
      })
      ElMessage.success('题目更新成功')
      editDialogVisible.value = false
      await fetchPaperQuestions()
    } catch (error: any) {
      console.error('题目更新失败:', error)
      ElMessage.error(error?.message || '题目更新失败')
    }
  }
}

/**
 * 保存编辑的试卷
 */
const saveEditPaper = async () => {
  // 表单验证
  if (!editPaperFormData.value.title) {
    ElMessage.error('请输入试卷名称')
    return
  }
  
  if (!editPaperFormData.value.duration || editPaperFormData.value.duration <= 0) {
    ElMessage.error('请输入有效的考试时长')
    return
  }
  
  if (paperDetail.value) {
    try {
      const response = await axios.put(`/api/papers/update/${paperDetail.value.id}`, editPaperFormData.value)
      const data = unwrapData(response)
      paperDetail.value = {
        ...paperDetail.value,
        ...data,
        description: data?.description || editPaperFormData.value.description
      }
      ElMessage.success('试卷更新成功')
      editPaperDialogVisible.value = false
    } catch (error: any) {
      console.error('试卷更新失败:', error)
      ElMessage.error(error?.message || '试卷更新失败')
    }
  }
}

/**
 * 保存新添加的题目
 */
const saveAddQuestion = async () => {
  // 表单验证
  if (!addFormData.value.title) {
    ElMessage.error('请输入题目名称')
    return
  }
  if (!addFormData.value.difficulty) {
    ElMessage.error('请选择题目难度')
    return
  }
  
  try {
    const paperId = route.params.id as string
    const createResponse = await axios.post('/api/admin/questions', {
      title: addFormData.value.title,
      difficulty: addFormData.value.difficulty,
      description: addFormData.value.description,
      questionText: addFormData.value.description || addFormData.value.title,
      answerText: addFormData.value.answer,
      isVisible: true
    })
    const newQuestion = unwrapData(createResponse)
    await axios.post(`/api/papers/${paperId}/questions`, null, {
      params: {
        questionId: newQuestion.id,
        sortOrder: total.value
      }
    })
    ElMessage.success('题目添加成功')
    addDialogVisible.value = false
    await fetchPaperQuestions()
    await fetchPaperDetail()
  } catch (error: any) {
    console.error('题目添加失败:', error)
    ElMessage.error(error?.message || '题目添加失败')
  }
}

/**
 * 导航到题目详情页面
 * @param questionId 题目ID
 */
const navigateToQuestionDetail = (questionId: number) => {
  router.push(`/question/${questionId}`)
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPaperDetail()
  fetchPaperQuestions()
})
</script>

<style scoped>
.paper-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部导航样式 */
.top-nav {
  margin-bottom: 24px;
}

/* 回退按钮统一样式 */
:deep(.el-page-header) {
  padding: 0 !important;
}

:deep(.el-page-header__left) {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.el-page-header__back) {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: #666;
  border: 1px solid #eaeaea;
}

:deep(.el-page-header__back:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  border-color: #667eea;
  transform: translateY(-1px);
}

:deep(.el-page-header__title) {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.loading-state,
.error-state {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 40px;
  margin-bottom: 24px;
}

.paper-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 试卷信息卡片 */
.paper-info-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.paper-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.paper-actions {
  display: flex;
  gap: 12px;
}

.paper-actions .el-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.paper-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.paper-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-weight: 600;
  color: #666;
  width: 120px;
  flex-shrink: 0;
  font-size: 14px;
}

.info-value {
  color: #333;
  flex: 1;
  font-size: 14px;
}

/* 题目列表部分 */
.questions-section {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 24px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 2px solid #667eea;
}

.questions-filter {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-top: 16px;
}

:deep(.el-table th) {
  background: #f8f9fa !important;
  color: #495057 !important;
  font-weight: 600 !important;
  font-size: 14px !important;
  padding: 16px !important;
  border-bottom: 2px solid #e9ecef !important;
}

:deep(.el-table td) {
  padding: 16px !important;
  border-bottom: 1px solid #f0f0f0 !important;
  font-size: 14px !important;
  color: #495057 !important;
}

:deep(.el-table tr:hover > td) {
  background: #f8f9fa !important;
}

:deep(.el-table .el-link) {
  font-weight: 500;
  color: #667eea;
  transition: color 0.3s ease;
}

:deep(.el-table .el-link:hover) {
  color: #764ba2;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .el-button {
  border-radius: 4px;
  font-size: 12px;
  padding: 4px 10px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 分页样式 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-pagination button) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-pagination button:hover) {
  color: #667eea;
  border-color: #667eea;
}

:deep(.el-pagination .el-pager li.active) {
  background-color: #667eea;
  border-color: #667eea;
  color: white;
}

:deep(.el-pagination .el-pager li:hover:not(.active)) {
  color: #667eea;
}

/* 难度标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .paper-detail-container {
    padding: 12px;
  }
  
  .main-content {
    padding: 0;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
  }
  
  .paper-title {
    font-size: 20px;
  }
  
  .paper-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  
  .paper-info {
    padding: 16px;
    gap: 12px;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    padding: 8px 0;
  }
  
  .info-label {
    width: auto;
    font-size: 13px;
  }
  
  .questions-section {
    padding: 16px;
  }
  
  .questions-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 12px;
  }
  
  .questions-filter .el-input,
  .questions-filter .el-select {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .section-title {
    font-size: 16px;
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
}
</style>
