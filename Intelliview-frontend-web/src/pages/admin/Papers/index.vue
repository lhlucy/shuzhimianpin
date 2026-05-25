<template>
  <div class="papers-page">
    <!-- 头部组件 -->
    <Header />
    
    <div class="main-content">
      <div class="papers-header">
        <h2>试卷管理</h2>
        <div class="papers-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索试卷"
            clearable
            style="width: 200px; margin-right: 10px;"
            @input="handleFilterPapers"
          />
          <el-select
            v-model="selectedStatus"
            placeholder="选择状态"
            style="width: 120px; margin-right: 10px;"
            @change="fetchPapers"
          >
            <el-option label="全部" value="" />
            <el-option label="启用" value="true" />
            <el-option label="禁用" value="false" />
          </el-select>
          <el-button type="primary" @click="handleAddPaper">
            <el-icon><Plus /></el-icon> 新建试卷
          </el-button>
        </div>
      </div>
      
      <div class="papers-list-card">
        <el-table :data="filteredPapersData" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="试卷名称" min-width="200">
            <template #default="scope">
              <el-link 
                type="primary"
                @click.prevent="navigateToPaperDetail(scope.row.id)"
              >
                {{ scope.row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="300">
            <template #default="scope">
              <span>{{ scope.row.description }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="questionCount" label="题目数量" width="120">
            <template #default="scope">
              <el-tag type="info">{{ scope.row.questionCount }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="考试时长" width="120">
            <template #default="scope">
              <span>{{ scope.row.duration }} 分钟</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag
                :type="scope.row.status ? 'success' : 'danger'"
                :effect="'light'"
                @click="togglePaperStatus(scope.row)"
                style="cursor: pointer;"
              >
                {{ scope.row.status ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="handleEditPaper(scope.row)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDeletePaper(scope.row.id)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="papers-pagination">
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
    
    <!-- 新建/编辑试卷对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="700px"
    >
      <el-form :model="formData" label-width="100px" style="max-width: 600px;">
        <el-form-item label="试卷名称">
          <el-input v-model="formData.title" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="试卷描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            placeholder="请输入试卷描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="考试时长">
          <el-input-number v-model="formData.duration" :min="1" :max="300" placeholder="请输入考试时长（分钟）" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch 
            v-model="formData.status" 
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
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSavePaper">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 底部组件 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Header from '../../../components/Header/index.vue'
import Footer from '../../../components/Footer/index.vue'
import axios from '../../../utils/axios'

const router = useRouter()

const searchQuery = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 对话框相关状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建试卷')
const currentEditId = ref<number | null>(null)

// 试卷数据类型定义
interface Paper {
  id: number
  title: string
  description: string
  questionCount: number
  duration: number
  createdAt: string
  status: boolean
}

// 表单数据类型定义
interface PaperForm {
  id?: number
  title: string
  description: string
  duration: number
  status: boolean
}

const originalPapersData = ref<Paper[]>([])

// 筛选后的试卷数据
const filteredPapersData = ref<Paper[]>([])

// 表单数据
const formData = ref<PaperForm>({
  title: '',
  description: '',
  duration: 90,
  status: true
})

/**
 * 筛试卷
 */
const handleFilterPapers = () => {
  let filtered = [...originalPapersData.value]
  
  // 根据搜索关键词筛选
  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.trim().toLowerCase()
    filtered = filtered.filter(p => 
      p.title.toLowerCase().includes(keyword) ||
      p.description.toLowerCase().includes(keyword)
    )
  }
  
  // 根据状态筛选
  if (selectedStatus.value) {
    const status = selectedStatus.value === 'true'
    filtered = filtered.filter(p => p.status === status)
  }
  
  filteredPapersData.value = filtered
  if (searchQuery.value.trim()) {
    total.value = filtered.length
  }
}

/**
 * 打开新建试卷对话框
 */
const handleAddPaper = () => {
  dialogTitle.value = '新建试卷'
  currentEditId.value = null
  formData.value = {
    title: '',
    description: '',
    duration: 90,
    status: true
  }
  dialogVisible.value = true
}

/**
 * 打开编辑试卷对话框
 * @param paper 试卷数据
 */
const handleEditPaper = (paper: Paper) => {
  dialogTitle.value = '编辑试卷'
  currentEditId.value = paper.id
  formData.value = {
    title: paper.title,
    description: paper.description,
    duration: paper.duration,
    status: paper.status
  }
  dialogVisible.value = true
}

/**
 * 保存试卷
 */
const handleSavePaper = async () => {
  // 表单验证
  if (!formData.value.title) {
    ElMessage.error('请输入试卷名称')
    return
  }
  
  if (!formData.value.duration || formData.value.duration <= 0) {
    ElMessage.error('请输入有效的考试时长')
    return
  }

  try {
    if (currentEditId.value) {
      await axios.put(`/api/papers/update/${currentEditId.value}`, formData.value)
      ElMessage.success('试卷编辑成功')
    } else {
      await axios.post('/api/papers/create', formData.value)
      ElMessage.success('试卷创建成功')
    }
    dialogVisible.value = false
    await fetchPapers()
  } catch (error: any) {
    console.error('保存试卷失败:', error)
    ElMessage.error(error?.message || '保存试卷失败，请稍后重试')
  }
}

/**
 * 删除试卷
 * @param id 试卷ID
 */
const handleDeletePaper = (id: number) => {
  ElMessageBox.confirm('确定要删除这个试卷吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await axios.delete(`/api/papers/delete/${id}`)
    ElMessage.success('试卷删除成功')
    await fetchPapers()
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 切换试卷状态
 * @param paper 试卷数据
 */
const togglePaperStatus = async (paper: Paper) => {
  try {
    const response = await axios.put(`/api/papers/toggle-status/${paper.id}`)
    const nextStatus = Boolean((response as any)?.data ?? response)
    paper.status = nextStatus
    ElMessage.success(`试卷${paper.status ? '启用' : '禁用'}成功`)
    handleFilterPapers()
  } catch (error: any) {
    console.error('切换试卷状态失败:', error)
    ElMessage.error(error?.message || '切换试卷状态失败')
  }
}

/**
 * 导航到试卷详情页面
 * @param paperId 试卷ID
 */
const navigateToPaperDetail = (paperId: number) => {
  router.push(`/admin/paper/${paperId}`)
}

/**
 * 分页大小变化处理
 * @param size 每页条数
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPapers()
}

/**
 * 当前页码变化处理
 * @param current 当前页码
 */
const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchPapers()
}

/**
 * 从后端API获取试卷数据
 */
const fetchPapers = async () => {
  try {
    const url = selectedStatus.value ? '/api/papers/list-by-status' : '/api/papers/list'
    const response = await axios.get(url, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        ...(selectedStatus.value ? { status: selectedStatus.value === 'true' } : {}),
        sortBy: 'createdAt',
        sortDirection: 'desc'
      }
    })

    const data = (response as any)?.data ?? response
    if (!data || data.total === undefined || !Array.isArray(data.records)) {
      throw new Error('试卷数据格式不正确')
    }

    total.value = data.total
    originalPapersData.value = data.records.map((paper: any) => ({
      id: paper.id,
      title: paper.title,
      description: paper.description || '',
      questionCount: paper.questionCount || paper.question_count || 0,
      duration: paper.duration || 0,
      createdAt: paper.createdAt || paper.created_at || '',
      status: paper.status !== false
    }))
    currentPage.value = Number(data.current || 0) + 1
    handleFilterPapers()
  } catch (error) {
    console.error('获取试卷数据失败:', error)
    ElMessage.error('获取试卷数据失败，请稍后重试')
    originalPapersData.value = []
    filteredPapersData.value = []
    total.value = 0
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPapers()
})
</script>

<style scoped>
.papers-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 600px;
}

.main-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 试卷管理头部 */
.papers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.papers-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.papers-actions {
  display: flex;
  align-items: center;
}

.papers-actions .el-input,
.papers-actions .el-select {
  margin-right: 10px;
}

.papers-actions .el-button {
  border-radius: 4px;
  transition: all 0.3s ease;
}

.papers-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  border-radius: 4px;
  font-size: 12px;
  padding: 4px 10px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

/* 试卷列表卡片 */
.papers-list-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  overflow: hidden;
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

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-tag:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 分页样式 */
.papers-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .papers-page {
    padding: 12px;
  }
  
  .main-content {
    padding: 0;
  }
  
  .papers-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
  }
  
  .papers-header h2 {
    font-size: 18px;
  }
  
  .papers-actions {
    width: 100%;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .papers-actions .el-input,
  .papers-actions .el-select {
    flex: 1;
    min-width: 150px;
    margin-right: 0;
  }
  
  .papers-list-card {
    padding: 16px;
  }
  
  .el-table-column {
    min-width: 100px;
  }
}
</style>
