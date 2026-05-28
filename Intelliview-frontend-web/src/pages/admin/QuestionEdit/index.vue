<template>
  <div class="question-edit-page">
    <section class="page-shell">
      <div class="page-head">
        <div class="page-head-main">
          <el-button text class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回题目列表
          </el-button>
          <div>
            <p class="section-kicker">Question Editor</p>
            <h2>编辑题目</h2>
            <span>在独立编辑页维护题目内容，避免列表页和编辑表单互相挤占空间。</span>
          </div>
        </div>
        <div class="page-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveQuestion">保存修改</el-button>
        </div>
      </div>
    </section>

    <section v-loading="loading" class="panel">
      <el-form :model="formData" label-width="92px" class="question-form">
        <el-form-item label="题目名称">
          <el-input v-model="formData.title" placeholder="请输入题目名称" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="题目难度">
            <el-select v-model="formData.difficulty" placeholder="请选择难度">
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="题目编号">
            <el-input :model-value="String(questionId || '')" disabled />
          </el-form-item>
        </div>
        <el-form-item label="关联岗位">
          <el-select v-model="formData.primaryJobRoleId" placeholder="选择后会进入用户端对应岗位题库" clearable filterable>
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
            v-model="formData.description"
            type="textarea"
            :rows="6"
            placeholder="请输入题目描述"
          />
        </el-form-item>
        <el-form-item label="题目答案">
          <div class="editor-wrap">
            <MdEditor
              v-model="formData.answer"
              placeholder="请输入题目答案，支持 Markdown 语法"
              :preview="false"
              style="min-height: 360px; width: 100%;"
            />
          </div>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import service from '@/utils/axios'
import jobRoleApi, { type JobRoleOption } from '@/api/jobRoles'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const jobRoles = ref<JobRoleOption[]>([])
const questionId = computed(() => Number(route.params.questionId || 0))
const bankId = computed(() => Number(route.params.bankId || 0))

const formData = ref({
  title: '',
  difficulty: 'MEDIUM',
  primaryJobRoleId: undefined as number | undefined,
  description: '',
  answer: ''
})

const goBack = () => {
  router.push(`/admin/bank/${bankId.value}`)
}

const fetchQuestion = async () => {
  if (!questionId.value) {
    ElMessage.error('题目编号无效')
    goBack()
    return
  }

  loading.value = true
  try {
    const response = await service.get(`/api/questions/detail/${questionId.value}`) as any
    if (!response?.success || !response?.data) {
      throw new Error(response?.message || '获取题目详情失败')
    }

    const detail = response.data
    formData.value = {
      title: detail.title || '',
      difficulty: detail.difficulty || 'MEDIUM',
      primaryJobRoleId: detail.primaryJobRoleId,
      description: detail.description || detail.questionText || '',
      answer: detail.answer || detail.answerText || ''
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '获取题目详情失败')
  } finally {
    loading.value = false
  }
}

const saveQuestion = async () => {
  if (!formData.value.title.trim()) {
    ElMessage.error('请输入题目名称')
    return
  }

  if (!formData.value.difficulty) {
    ElMessage.error('请选择题目难度')
    return
  }

  saving.value = true
  try {
    await service.put(`/api/admin/questions/${questionId.value}`, {
      title: formData.value.title.trim(),
      difficulty: formData.value.difficulty,
      description: formData.value.description,
      questionText: formData.value.description || formData.value.title.trim(),
      answerText: formData.value.answer,
      primaryJobRoleId: formData.value.primaryJobRoleId,
      isForPractice: true,
      isForInterview: true,
      isVisible: true
    })
    ElMessage.success('题目更新成功')
    goBack()
  } catch (error: any) {
    ElMessage.error(error?.message || '题目更新失败')
  } finally {
    saving.value = false
  }
}

const loadJobRoles = async () => {
  try {
    jobRoles.value = await jobRoleApi.listJobRoles()
  } catch (error) {
    console.error('加载岗位列表失败:', error)
  }
}

onMounted(() => {
  loadJobRoles()
  fetchQuestion()
})
</script>

<style scoped>
.question-edit-page {
  display: grid;
  gap: 12px;
}

.page-shell,
.panel {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(30, 80, 162, 0.1);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-head-main {
  display: grid;
  gap: 8px;
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
  color: #173a74;
  font-size: 22px;
}

.page-head span {
  display: block;
  margin-top: 4px;
  color: #6f84a3;
  font-size: 12px;
}

.page-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.question-form {
  max-width: 1080px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.editor-wrap {
  width: 100%;
  overflow: hidden;
  border-radius: 12px;
}

@media (max-width: 820px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
