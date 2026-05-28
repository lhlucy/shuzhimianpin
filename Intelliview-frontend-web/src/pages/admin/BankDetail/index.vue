<template>
  <div class="bank-detail-container">
    <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-state">
      <el-empty>
        <template #description>
          <p>{{ error }}</p>
          <el-button type="primary" size="small" @click="reloadPage">重试</el-button>
        </template>
      </el-empty>
    </div>

    <div v-else-if="bankDetail" class="bank-content">
      <section class="page-shell">
        <div class="page-head">
          <div class="page-head-main">
            <el-button text class="back-btn" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回题库列表
            </el-button>
            <div>
              <p class="section-kicker">Question List</p>
              <h2>{{ bankDetail.title }}</h2>
              <span>{{ bankDetail.description || '当前题库未填写描述，建议补充题库定位与适用范围。' }}</span>
            </div>
          </div>
          <div class="page-actions">
            <el-button @click="handleEditBank">
              <el-icon><Edit /></el-icon>
              编辑题库
            </el-button>
            <el-button type="primary" @click="handleAddQuestion">
              <el-icon><Plus /></el-icon>
              添加题目
            </el-button>
          </div>
        </div>

        <div class="metric-grid">
          <article>
            <span>题库 ID</span>
            <strong>#{{ bankDetail.id }}</strong>
          </article>
          <article>
            <span>题目总数</span>
            <strong>{{ bankDetail.questionCount }}</strong>
          </article>
          <article>
            <span>浏览量</span>
            <strong>{{ bankDetail.viewCount || 0 }}</strong>
          </article>
          <article>
            <span>状态</span>
            <strong>{{ bankDetail.status ? '启用中' : '已停用' }}</strong>
          </article>
        </div>
      </section>

      <section class="panel compact-panel">
        <div class="query-panel">
          <div class="query-grid">
            <label class="query-item">
              <span>题目编号</span>
              <el-input v-model="searchId" maxlength="20" placeholder="请输入题目编号" clearable />
            </label>
            <label class="query-item">
              <span>题目名称</span>
              <el-input v-model="searchQuery" placeholder="请输入题目名称" clearable />
            </label>
            <label class="query-item">
              <span>题目难度</span>
              <el-select v-model="selectedDifficulty" placeholder="全部" clearable>
                <el-option label="全部" value="" />
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </label>
            <label class="query-item">
              <span>状态</span>
              <el-select v-model="statusFilter" placeholder="全部" clearable>
                <el-option label="全部" value="" />
                <el-option label="有内容" value="filled" />
                <el-option label="待完善" value="empty" />
              </el-select>
            </label>
          </div>

          <div class="query-actions">
            <el-button type="primary" @click="handleFilterQuestions">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </div>

        <div class="table-toolbar">
          <div class="table-toolbar-left">
            <el-button type="primary" @click="handleAddQuestion">
              <el-icon><Plus /></el-icon>
              新增
            </el-button>
            <el-upload
              class="import-upload"
              :show-file-list="false"
              :auto-upload="false"
              accept=".xlsx,.xls"
              :on-change="handleImportFileChange"
            >
              <el-button plain>
                <el-icon><Upload /></el-icon>
                一键导入
              </el-button>
            </el-upload>
            <el-button plain @click="downloadImportTemplate">
              <el-icon><Download /></el-icon>
              模板
            </el-button>
          </div>
          <div class="table-toolbar-right">
            <el-button plain @click="reloadPage">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>

        <el-table :data="filteredQuestions" class="admin-table" table-layout="fixed" :max-height="tableHeight">
          <el-table-column prop="id" label="题目编号" width="108" />
          <el-table-column prop="title" label="题目名称" min-width="300">
            <template #default="{ row }">
              <span class="question-title-text" :title="row.title">
                {{ row.title }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="120">
            <template #default="{ row }">
              <el-tag :type="getDifficultyType(row.difficulty)" effect="light">
                {{ row.difficultyLabel || row.difficulty }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="内容状态" width="120">
            <template #default="{ row }">
              <span>{{ row.description || row.answer ? '已完善' : '待完善' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="关联岗位" min-width="150">
            <template #default="{ row }">
              <el-tag v-if="row.primaryJobRoleId" effect="light">{{ getRoleName(row.primaryJobRoleId) }}</el-tag>
              <span v-else class="muted-text">未关联</span>
            </template>
          </el-table-column>
          <el-table-column label="浏览量" width="110">
            <template #default="{ row }">{{ row.viewCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="170">
            <template #default="{ row }">{{ row.createdAt || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="handleEditQuestion(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteQuestion(row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

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
      </section>
    </div>

    <el-dialog
      :title="editBankDialogTitle"
      v-model="editBankDialogVisible"
      width="620px"
    >
      <el-form :model="editBankFormData" label-width="88px">
        <el-form-item label="题库名称">
          <el-input v-model="editBankFormData.title" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="题库描述">
          <el-input
            v-model="editBankFormData.description"
            type="textarea"
            placeholder="请输入题库描述"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editBankFormData.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editBankDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditBank">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog
      :title="addDialogTitle"
      v-model="addDialogVisible"
      width="760px"
    >
      <el-form :model="addFormData" label-width="88px">
        <el-form-item label="题目名称">
          <el-input v-model="addFormData.title" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item label="题目难度">
          <el-select v-model="addFormData.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联岗位">
          <el-select v-model="addFormData.primaryJobRoleId" placeholder="选择后会进入用户端对应岗位题库" clearable filterable>
            <el-option
              v-for="role in jobRoles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目描述">
          <el-input
            v-model="addFormData.description"
            type="textarea"
            placeholder="请输入题目描述"
            :rows="4"
          />
        </el-form-item>
        <el-form-item label="题目答案">
          <div class="editor-wrap">
            <MdEditor
              v-model="addFormData.answer"
              placeholder="请输入题目答案，支持Markdown语法"
              :preview="false"
              style="min-height: 260px; width: 100%;"
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Edit, ArrowLeft, Search, Refresh, RefreshLeft, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import axios from '@/utils/axios'
import jobRoleApi, { type JobRoleOption } from '@/api/jobRoles'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { apiBaseUrl } from '@/utils/axios'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const searchId = ref('')
const selectedDifficulty = ref('')
const statusFilter = ref('')
const jobRoles = ref<JobRoleOption[]>([])
const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 900)
const tableHeight = computed(() => Math.max(viewportHeight.value - 430, 320))
const bankId = computed(() => Number(route.params.id))

// 题库详情数据
interface BankDetail {
  id: number
  title: string
  questionCount: number
  status: boolean
  createdAt: string
  description?: string
  updatedAt?: string
  icon?: string
  popular?: boolean
  viewCount?: number
}

const bankDetail = ref<BankDetail | null>(null)

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
  primaryJobRoleId?: number
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
  answer: question.answer || question.answerText || '',
  primaryJobRoleId: question.primaryJobRoleId || question.primary_job_role_id
})

// 编辑题库对话框相关状态
const editBankDialogVisible = ref(false)
const editBankDialogTitle = ref('编辑题库')

// 编辑题库表单数据
const editBankFormData = ref({
  title: '',
  description: '',
  status: true
})

// 添加题目对话框相关状态
const addDialogVisible = ref(false)
const addDialogTitle = ref('添加题目')

// 添加题目表单数据
const addFormData = ref({
  title: '',
  difficulty: 'MEDIUM',
  primaryJobRoleId: undefined as number | undefined,
  description: '',
  answer: ''
})

// 返回上一页
const goBack = () => {
  router.push('/admin/questions')
}

const reloadPage = async () => {
  await Promise.all([fetchBankDetail(), fetchBankQuestions()])
}

const syncViewportHeight = () => {
  viewportHeight.value = window.innerHeight
}

// 获取题库详情
const fetchBankDetail = async () => {
  if (!bankId.value) {
    error.value = '题库ID不能为空'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await axios.get(`/api/banks/detail`, {
      params: { id: bankId.value }
    })
    
    const data = unwrapData(response)
    if (data) {
      bankDetail.value = {
        id: data.id,
        title: data.title,
        questionCount: data.questionCount || data.question_count || 0,
        status: true,
        createdAt: data.createdAt || data.created_at || '',
        updatedAt: data.updatedAt || data.updated_at,
        description: data.description || '',
        icon: data.icon,
        popular: data.popular,
        viewCount: data.viewCount || data.view_count || 0
      }
    } else {
      error.value = '获取题库详情失败'
    }
  } catch (err: any) {
    console.error('获取题库详情失败:', err)
    error.value = '获取题库详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 获取题库题目列表
const fetchBankQuestions = async () => {
  if (!bankId.value) return
  
  loading.value = true
  
  try {
    const response = await axios.get(`/api/banks/${bankId.value}/questions`, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    
    const data = unwrapData(response)
    if (data) {
      questions.value = Array.isArray(data.records) ? data.records.map(mapQuestion) : []
      total.value = data.total || 0
      filteredQuestions.value = [...questions.value]
    } else {
      ElMessage.error('获取题目列表失败')
    }
  } catch (err: any) {
    console.error('获取题目列表失败:', err)
    ElMessage.error('获取题目列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 筛选题目
const handleFilterQuestions = () => {
  let filtered = [...questions.value]

  if (searchId.value.trim()) {
    filtered = filtered.filter(q => String(q.id).includes(searchId.value.trim()))
  }
  
  if (selectedDifficulty.value) {
    filtered = filtered.filter(q => q.difficulty === selectedDifficulty.value)
  }

  if (statusFilter.value === 'filled') {
    filtered = filtered.filter(q => Boolean(q.description || q.answer))
  } else if (statusFilter.value === 'empty') {
    filtered = filtered.filter(q => !q.description && !q.answer)
  }

  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.trim().toLowerCase()
    filtered = filtered.filter(q => 
      q.title.toLowerCase().includes(keyword)
    )
  }
  
  filteredQuestions.value = filtered
}

const resetFilters = () => {
  searchId.value = ''
  searchQuery.value = ''
  selectedDifficulty.value = ''
  statusFilter.value = ''
  handleFilterQuestions()
}

// 分页大小变化处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchBankQuestions()
}

// 当前页码变化处理
const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchBankQuestions()
}

// 获取难度标签类型
const getDifficultyType = (difficulty: string): string => {
  const typeMap: Record<string, string> = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return typeMap[difficulty] || 'default'
}

const getRoleName = (roleId: number) => jobRoles.value.find((role) => role.id === roleId)?.name || `岗位 #${roleId}`

const loadJobRoles = async () => {
  try {
    jobRoles.value = await jobRoleApi.listJobRoles()
  } catch (error) {
    console.error('加载岗位列表失败:', error)
  }
}

// 编辑题库
const handleEditBank = () => {
  if (bankDetail.value) {
    editBankDialogTitle.value = '编辑题库'
    editBankFormData.value = {
      title: bankDetail.value.title,
      description: bankDetail.value.description || '',
      status: bankDetail.value.status
    }
    editBankDialogVisible.value = true
  }
}

// 添加题目
const handleAddQuestion = () => {
  addDialogTitle.value = '添加题目'
  addFormData.value = {
    title: '',
    difficulty: 'MEDIUM',
    primaryJobRoleId: undefined,
    description: '',
    answer: ''
  }
  addDialogVisible.value = true
}

// 编辑题目
const handleEditQuestion = (question: Question) => {
  router.push(`/admin/bank/${bankId.value}/question/${question.id}/edit`)
}

// 删除题目
const handleDeleteQuestion = (questionId: number) => {
  ElMessageBox.confirm('确定要删除这个题目吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await axios.delete(`/api/admin/questions/${questionId}`)
    ElMessage.success('题目删除成功')
    await fetchBankQuestions()
    await fetchBankDetail()
  }).catch(() => {
    // 取消删除
  })
}

// 保存编辑的题库
const saveEditBank = async () => {
  // 表单验证
  if (!editBankFormData.value.title) {
    ElMessage.error('请输入题库名称')
    return
  }
  
  if (bankDetail.value) {
    try {
      const response = await axios.put(`/api/banks/update/${bankDetail.value.id}`, {
        title: editBankFormData.value.title,
        description: editBankFormData.value.description
      })
      const data = unwrapData(response)
      bankDetail.value = {
        ...bankDetail.value,
        ...data,
        status: true
      }
      ElMessage.success('题库更新成功')
      editBankDialogVisible.value = false
    } catch (error: any) {
      console.error('题库更新失败:', error)
      ElMessage.error(error?.message || '题库更新失败')
    }
  }
}

// 保存新添加的题目
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
    const createResponse = await axios.post('/api/admin/questions', {
      title: addFormData.value.title,
      difficulty: addFormData.value.difficulty,
      description: addFormData.value.description,
      questionText: addFormData.value.description || addFormData.value.title,
      answerText: addFormData.value.answer,
      primaryJobRoleId: addFormData.value.primaryJobRoleId,
      isForPractice: true,
      isForInterview: true,
      isVisible: true
    })
    const newQuestion = unwrapData(createResponse)
    const attachResponse = await axios.post(`/api/banks/${bankId.value}/questions`, null, {
      params: { questionId: newQuestion.id }
    })
    if (unwrapData(attachResponse) !== true) {
      ElMessage.warning('题目已创建，但当前题库未绑定分类，暂无法显示在该题库下')
    } else {
      ElMessage.success('题目添加成功')
    }
    addDialogVisible.value = false
    await fetchBankQuestions()
    await fetchBankDetail()
  } catch (error: any) {
    console.error('题目添加失败:', error)
    ElMessage.error(error?.message || '题目添加失败')
  }
}

const handleImportFileChange = async (uploadFile: UploadFile) => {
  if (!uploadFile.raw) {
    ElMessage.error('未读取到上传文件')
    return
  }

  if (!bankId.value) {
    ElMessage.error('当前题库不存在，无法导入')
    return
  }

  const formData = new FormData()
  formData.append('file', uploadFile.raw)

  try {
    await axios.post(`/api/admin/questions/import/excel/bank/${bankId.value}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    ElMessage.success('题目导入成功')
    await reloadPage()
  } catch (error: any) {
    console.error('题目导入失败:', error)
    ElMessage.error(error?.message || '题目导入失败')
  }
}

const downloadImportTemplate = async () => {
  try {
    const response = await fetch(`${apiBaseUrl}/api/admin/questions/batch/template`, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token') || ''}`
      }
    })

    if (!response.ok) {
      throw new Error('模板下载失败')
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'question_upload_template.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error: any) {
    console.error('下载模板失败:', error)
    ElMessage.error(error?.message || '下载模板失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  window.addEventListener('resize', syncViewportHeight)
  loadJobRoles()
  reloadPage()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportHeight)
})
</script>

<style scoped>
.bank-detail-container {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.loading-state,
.error-state {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
  padding: 32px;
}

.bank-content {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.page-shell,
.panel {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(30, 80, 162, 0.1);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
  min-width: 0;
  overflow: hidden;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-head-main {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.back-btn {
  justify-self: flex-start;
  padding-left: 0;
}

.section-kicker {
  margin-bottom: 6px;
  color: #1e50a2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.page-head h2 {
  font-size: 21px;
  color: #173a74;
}

.page-head span {
  display: block;
  margin-top: 2px;
  max-width: 720px;
  color: #6f84a3;
  line-height: 1.6;
  font-size: 12px;
}

.page-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.metric-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.metric-grid article {
  padding: 10px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f6f9ff 0%, #eef4ff 100%);
}

.metric-grid span {
  color: #6d84a5;
  font-size: 11px;
}

.metric-grid strong {
  display: block;
  margin-top: 6px;
  color: #173a74;
  font-size: 18px;
  font-weight: 800;
}

.query-panel {
  padding: 4px 0 12px;
  border-bottom: 1px solid #e9edf5;
  display: grid;
  gap: 12px;
  min-width: 0;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px 20px;
}

.query-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.query-item span {
  color: #56657f;
  font-size: 13px;
  font-weight: 600;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-toolbar {
  padding: 12px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.table-toolbar-left,
.table-toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.import-upload {
  display: inline-flex;
}

.panel-header {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.panel-header h3 {
  color: #173a74;
  font-size: 17px;
}

.panel-header p {
  margin-top: 6px;
  color: #7387a4;
  font-size: 12px;
}

.admin-table {
  border: 1px solid rgba(30, 80, 162, 0.08);
  border-radius: 14px;
  overflow: hidden;
  width: 100%;
  min-width: 0;
}

.admin-table :deep(.el-table__header-wrapper th) {
  background: #f7faff;
  color: #173a74;
  font-size: 12px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.admin-table :deep(.el-table__row td) {
  font-size: 12px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.action-buttons,
.table-actions {
  display: flex;
  gap: 6px;
}

.question-title-text {
  display: inline-block;
  max-width: 100%;
  color: #25324a;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.muted-text {
  color: #9aa3b6;
  font-size: 12px;
}

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.editor-wrap {
  width: 100%;
  overflow-x: auto;
}

@media (max-width: 768px) {
  .page-head,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .metric-grid {
    grid-template-columns: 1fr 1fr;
  }

  .query-grid {
    grid-template-columns: 1fr 1fr;
  }

  .query-actions,
  .table-toolbar {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 520px) {
  .page-shell,
  .panel,
  .loading-state,
  .error-state {
    padding: 14px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .query-grid {
    grid-template-columns: 1fr;
  }
}
</style>
