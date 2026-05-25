<template>
  <div class="interviews-page">
    <section class="summary-strip">
      <div class="summary-copy">
        <p class="section-kicker">Interview Management</p>
        <h2>面试管理</h2>
        <span>集中查看 AI 面试记录，并统一维护目标岗位、技术选择与面试关注点。</span>
      </div>
      <div class="summary-metrics">
        <article>
          <strong>{{ interviewCount }}</strong>
          <span>面试记录</span>
        </article>
        <article>
          <strong>{{ completedCount }}</strong>
          <span>已完成</span>
        </article>
        <article>
          <strong>{{ activeRoleCount }}</strong>
          <span>启用岗位</span>
        </article>
      </div>
    </section>

    <article class="panel">
      <div class="panel-header">
        <div>
          <h3>面试记录</h3>
          <p>查询后直接查看后台面试报告，不跳转到用户端报告页面。</p>
        </div>
      </div>

      <div class="query-panel">
        <div class="query-grid">
          <label class="query-item">
            <span>检索关键词</span>
            <el-input
              v-model="recordFilters.keyword"
              placeholder="用户名、岗位或面试名称"
              clearable
              @keyup.enter="fetchInterviewRecords"
            />
          </label>
          <label class="query-item">
            <span>状态</span>
            <el-select v-model="recordFilters.status" placeholder="全部" clearable>
              <el-option label="全部" value="" />
              <el-option
                v-for="item in statusOptions"
                :key="item"
                :label="formatInterviewStatus(item)"
                :value="item"
              />
            </el-select>
          </label>
          <label class="query-item">
            <span>面试岗位</span>
            <el-select v-model="recordFilters.jobRoleId" placeholder="全部" clearable filterable>
              <el-option label="全部" value="" />
              <el-option
                v-for="item in roleOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </label>
        </div>

        <div class="query-actions">
          <el-button type="primary" @click="fetchInterviewRecords">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetRecordFilters">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </div>
      </div>

      <div class="table-shell">
        <el-table
          v-loading="recordLoading"
          :data="interviewRecords"
          class="admin-table"
          table-layout="fixed"
          :max-height="recordTableHeight"
        >
          <el-table-column prop="interviewId" label="编号" width="78" />
          <el-table-column label="面试信息" min-width="260">
            <template #default="{ row }">
              <div class="stacked-cell">
                <strong>{{ row.title || row.targetPosition || '未命名面试' }}</strong>
                <span>{{ row.nickname || row.username || '未知用户' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="目标岗位" min-width="180">
            <template #default="{ row }">
              <div class="stacked-cell">
                <strong>{{ row.jobRoleName || row.targetPosition || '-' }}</strong>
                <span>{{ formatTechStacks(row.techStacks) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="96">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" effect="light">
                {{ formatInterviewStatus(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" width="96">
            <template #default="{ row }">{{ formatScore(row.totalScore) }}</template>
          </el-table-column>
          <el-table-column label="进度" width="92">
            <template #default="{ row }">{{ row.answeredCount }}/{{ row.questionCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="158">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRecordDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-if="!recordLoading && !interviewRecords.length" description="暂无面试记录" />
    </article>

    <article class="panel">
      <div class="panel-header">
        <div>
          <h3>面试参数</h3>
          <p>目标岗位与技术选择直接维护到后端，用户创建模拟面试会实时读取这里的配置。</p>
        </div>
        <div class="toolbar">
          <el-input
            v-model="roleKeyword"
            placeholder="搜索岗位编码或名称"
            clearable
            @keyup.enter="fetchInterviewRoles"
            @clear="fetchInterviewRoles"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="openRoleDialog()">新增岗位</el-button>
        </div>
      </div>

      <div class="table-shell">
        <el-table
          v-loading="roleLoading"
          :data="interviewRoles"
          class="admin-table"
          table-layout="fixed"
          :max-height="roleTableHeight"
        >
          <el-table-column label="目标岗位" min-width="210">
            <template #default="{ row }">
              <div class="stacked-cell">
                <strong>{{ row.name }}</strong>
                <span>{{ row.code }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="技术选择" min-width="300">
            <template #default="{ row }">
              <div class="tag-list compact">
                <span
                  v-for="tag in normalizeList(row.typicalTechStack).slice(0, 6)"
                  :key="`${row.id}-${tag}`"
                  class="mini-tag"
                >
                  {{ tag }}
                </span>
                <span v-if="!normalizeList(row.typicalTechStack).length" class="muted-text">未配置</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="108">
            <template #default="{ row }">
              <span>{{ row.isActive === false ? '已停用' : '启用中' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" min-width="160">
            <template #default="{ row }">{{ formatDateTime(row.updatedAt || row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button link type="primary" @click="openRoleDialog(row)">编辑</el-button>
                <el-button
                  v-if="row.isActive !== false"
                  link
                  type="danger"
                  @click="handleDeactivateRole(row)"
                >
                  停用
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty v-if="!roleLoading && !interviewRoles.length" description="暂无岗位参数" />
    </article>

    <el-dialog
      v-model="roleDialogVisible"
      :title="editingRoleId ? '编辑面试岗位' : '新增面试岗位'"
      width="760px"
    >
      <el-form :model="roleForm" label-width="94px">
        <div class="dialog-grid">
          <el-form-item label="岗位编码">
            <el-input v-model="roleForm.code" placeholder="如：java_backend" />
          </el-form-item>
          <el-form-item label="目标岗位">
            <el-input v-model="roleForm.name" placeholder="请输入目标岗位" />
          </el-form-item>
          <el-form-item label="岗位分类">
            <el-input v-model="roleForm.category" placeholder="如：后端开发" />
          </el-form-item>
          <el-form-item label="经验要求">
            <el-input v-model="roleForm.applicableExperience" placeholder="如：1-3 年 / 校招" />
          </el-form-item>
          <el-form-item label="难度等级">
            <el-input v-model="roleForm.difficultyLevel" placeholder="如：ENTRY" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="roleForm.isActive" active-text="启用" inactive-text="停用" />
          </el-form-item>
        </div>
        <el-form-item label="岗位描述">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入岗位描述"
          />
        </el-form-item>
        <el-form-item label="技术选择">
          <el-input
            v-model="roleForm.typicalTechStack"
            type="textarea"
            :rows="4"
            placeholder="请输入技术栈，使用逗号、顿号或换行分隔"
          />
        </el-form-item>
        <el-form-item label="面试关注点">
          <el-input
            v-model="roleForm.interviewFocus"
            type="textarea"
            :rows="4"
            placeholder="请输入面试关注点，使用逗号、顿号或换行分隔"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingRole" @click="submitRoleForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="面试报告"
      width="920px"
      top="6vh"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-modal">
        <template v-if="selectedRecord && selectedSummary">
          <div class="detail-overview">
            <article>
              <span>面试名称</span>
              <strong>{{ selectedRecord.title || selectedRecord.targetPosition || '未命名面试' }}</strong>
            </article>
            <article>
              <span>候选用户</span>
              <strong>{{ selectedRecord.nickname || selectedRecord.username || '-' }}</strong>
            </article>
            <article>
              <span>目标岗位</span>
              <strong>{{ selectedRecord.jobRoleName || selectedRecord.targetPosition || '-' }}</strong>
            </article>
            <article>
              <span>技术选择</span>
              <strong>{{ formatTechStacks(selectedSummary.techStacks || selectedRecord.techStacks) }}</strong>
            </article>
            <article>
              <span>综合得分</span>
              <strong>{{ formatScore(selectedSummary.overallScore) }}</strong>
            </article>
            <article>
              <span>完成进度</span>
              <strong>{{ selectedSummary.answeredQuestions }}/{{ selectedSummary.totalQuestions || 0 }}</strong>
            </article>
          </div>

          <div class="report-block">
            <h4>报告摘要</h4>
            <p>{{ selectedSummary.summary || '当前暂无报告摘要。' }}</p>
          </div>

          <div class="report-grid">
            <div class="report-block">
              <h4>优势表现</h4>
              <ul v-if="selectedSummary.strengths?.length" class="report-list">
                <li v-for="item in selectedSummary.strengths" :key="item">{{ item }}</li>
              </ul>
              <p v-else>暂无优势项。</p>
            </div>
            <div class="report-block">
              <h4>待提升点</h4>
              <ul v-if="selectedSummary.weaknesses?.length" class="report-list">
                <li v-for="item in selectedSummary.weaknesses" :key="item">{{ item }}</li>
              </ul>
              <p v-else>暂无待提升项。</p>
            </div>
          </div>

          <div class="report-block">
            <h4>改进建议</h4>
            <ul v-if="selectedSummary.suggestions?.length" class="report-list">
              <li v-for="item in selectedSummary.suggestions" :key="item">{{ item }}</li>
            </ul>
            <p v-else>暂无建议。</p>
          </div>

          <div class="report-block">
            <h4>逐题反馈</h4>
            <div class="review-list">
              <article
                v-for="item in selectedSummary.questionReviews || []"
                :key="`${item.questionId}-${item.questionOrder}`"
                class="review-card"
              >
                <div class="review-head">
                  <strong>Q{{ item.questionOrder || '-' }} {{ item.questionType || '题目' }}</strong>
                  <span>得分 {{ formatScore(item.score) }}</span>
                </div>
                <p class="review-question">{{ item.questionContent || '暂无题目内容' }}</p>
                <p class="review-answer">{{ item.answerContent || '暂无作答内容' }}</p>
                <p class="review-feedback">{{ item.feedbackSummary || '暂无反馈摘要' }}</p>
              </article>
              <el-empty v-if="!selectedSummary.questionReviews?.length" description="暂无逐题反馈" />
            </div>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="暂无面试报告数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { RefreshLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi, { type AdminInterviewRecord, type AdminJobRoleRecord } from '@/api/admin'
import type { AIInterviewSummary } from '@/api/aiInterview'

const interviewRecords = ref<AdminInterviewRecord[]>([])
const interviewRoles = ref<AdminJobRoleRecord[]>([])
const recordLoading = ref(false)
const roleLoading = ref(false)
const savingRole = ref(false)
const detailLoading = ref(false)
const roleDialogVisible = ref(false)
const detailVisible = ref(false)
const selectedRecord = ref<AdminInterviewRecord | null>(null)
const selectedSummary = ref<AIInterviewSummary | null>(null)
const editingRoleId = ref<number | null>(null)
const roleKeyword = ref('')
const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 900)

const recordFilters = reactive<{
  keyword: string
  status: string
  jobRoleId: number | ''
}>({
  keyword: '',
  status: '',
  jobRoleId: ''
})

const roleForm = reactive({
  code: '',
  name: '',
  description: '',
  applicableExperience: '',
  category: '',
  difficultyLevel: '',
  typicalTechStack: '',
  interviewFocus: '',
  isActive: true
})

const interviewCount = computed(() => interviewRecords.value.length)
const completedCount = computed(() => interviewRecords.value.filter((item) => item.status === 'COMPLETED').length)
const activeRoleCount = computed(() => interviewRoles.value.filter((item) => item.isActive !== false).length)
const roleOptions = computed(() => interviewRoles.value.filter((item) => item.isActive !== false))
const statusOptions = computed(() => Array.from(new Set(interviewRecords.value.map((item) => item.status).filter(Boolean))))
const recordTableHeight = computed(() => Math.max(viewportHeight.value - 430, 300))
const roleTableHeight = computed(() => Math.max(viewportHeight.value - 430, 260))

const syncViewportHeight = () => {
  viewportHeight.value = window.innerHeight
}

const normalizeList = (rawValue?: string | string[]) => {
  if (!rawValue) {
    return []
  }
  if (Array.isArray(rawValue)) {
    return rawValue.filter(Boolean)
  }

  const normalized = rawValue
    .trim()
    .replace(/^\[/, '')
    .replace(/\]$/, '')
    .replace(/"/g, '')
    .replace(/'/g, '')

  return Array.from(new Set(normalized
    .split(/[,，、|/;；\n\r]+/)
    .map((item) => item.trim())
    .filter(Boolean)))
}

const listToTextarea = (rawValue?: string | string[]) => normalizeList(rawValue).join('、')
const serializeListField = (rawValue: string) => JSON.stringify(normalizeList(rawValue))
const formatDateTime = (value?: string) => value || '-'
const formatScore = (value?: number) => (typeof value === 'number' ? value.toFixed(1) : '-')
const formatTechStacks = (items?: string[]) => (items && items.length ? items.join(' / ') : '未配置')

const formatInterviewStatus = (status?: string) => {
  const statusMap: Record<string, string> = {
    PENDING: '待开始',
    CREATED: '待开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return status ? (statusMap[status] || status) : '-'
}

const getStatusTagType = (status?: string) => {
  const typeMap: Record<string, string> = {
    PENDING: 'info',
    CREATED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status || ''] || 'info'
}

const fetchInterviewRecords = async () => {
  recordLoading.value = true
  try {
    interviewRecords.value = await adminApi.getInterviewRecords({
      keyword: recordFilters.keyword.trim() || undefined,
      status: recordFilters.status || undefined,
      jobRoleId: recordFilters.jobRoleId === '' ? undefined : Number(recordFilters.jobRoleId)
    })
  } catch (error: any) {
    ElMessage.error(error?.message || '获取面试记录失败')
  } finally {
    recordLoading.value = false
  }
}

const fetchInterviewRoles = async () => {
  roleLoading.value = true
  try {
    interviewRoles.value = await adminApi.getInterviewRoles({
      keyword: roleKeyword.value.trim() || undefined
    })
  } catch (error: any) {
    ElMessage.error(error?.message || '获取面试岗位失败')
  } finally {
    roleLoading.value = false
  }
}

const resetRecordFilters = () => {
  recordFilters.keyword = ''
  recordFilters.status = ''
  recordFilters.jobRoleId = ''
  fetchInterviewRecords()
}

const resetRoleForm = () => {
  roleForm.code = ''
  roleForm.name = ''
  roleForm.description = ''
  roleForm.applicableExperience = ''
  roleForm.category = ''
  roleForm.difficultyLevel = ''
  roleForm.typicalTechStack = ''
  roleForm.interviewFocus = ''
  roleForm.isActive = true
}

const openRoleDialog = (role?: AdminJobRoleRecord) => {
  editingRoleId.value = role?.id ?? null
  resetRoleForm()

  if (role) {
    roleForm.code = role.code || ''
    roleForm.name = role.name || ''
    roleForm.description = role.description || ''
    roleForm.applicableExperience = role.applicableExperience || ''
    roleForm.category = role.category || ''
    roleForm.difficultyLevel = role.difficultyLevel || ''
    roleForm.typicalTechStack = listToTextarea(role.typicalTechStack)
    roleForm.interviewFocus = listToTextarea(role.interviewFocus)
    roleForm.isActive = role.isActive !== false
  }

  roleDialogVisible.value = true
}

const submitRoleForm = async () => {
  if (!roleForm.code.trim() || !roleForm.name.trim()) {
    ElMessage.error('岗位编码和目标岗位不能为空')
    return
  }

  savingRole.value = true
  try {
    const payload = {
      code: roleForm.code.trim(),
      name: roleForm.name.trim(),
      description: roleForm.description.trim(),
      applicableExperience: roleForm.applicableExperience.trim(),
      category: roleForm.category.trim(),
      difficultyLevel: roleForm.difficultyLevel.trim(),
      typicalTechStack: serializeListField(roleForm.typicalTechStack),
      interviewFocus: serializeListField(roleForm.interviewFocus),
      isActive: roleForm.isActive
    }

    if (editingRoleId.value) {
      await adminApi.updateInterviewRole(editingRoleId.value, payload)
      ElMessage.success('面试岗位更新成功')
    } else {
      await adminApi.createInterviewRole(payload)
      ElMessage.success('面试岗位创建成功')
    }

    roleDialogVisible.value = false
    await fetchInterviewRoles()
  } catch (error: any) {
    ElMessage.error(error?.message || '岗位保存失败')
  } finally {
    savingRole.value = false
  }
}

const handleDeactivateRole = async (role: AdminJobRoleRecord) => {
  ElMessageBox.confirm(`确定停用岗位“${role.name}”吗？`, '停用确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const result = await adminApi.deactivateInterviewRole(role.id)
    ElMessage.success(result?.usageCount ? `岗位已停用，关联 ${result.usageCount} 条使用记录` : '岗位已停用')
    await fetchInterviewRoles()
  }).catch(() => undefined)
}

const openRecordDetail = async (record: AdminInterviewRecord) => {
  selectedRecord.value = record
  selectedSummary.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    selectedSummary.value = await adminApi.getInterviewSummary(record.interviewId)
  } catch (error: any) {
    ElMessage.error(error?.message || '获取面试报告失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', syncViewportHeight)
  Promise.all([fetchInterviewRecords(), fetchInterviewRoles()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportHeight)
})
</script>

<style scoped>
.interviews-page {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.summary-strip,
.panel {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(30, 80, 162, 0.1);
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
}

.summary-strip {
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.section-kicker {
  margin-bottom: 6px;
  color: #1e50a2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.summary-copy h2 {
  color: #173a74;
  font-size: 19px;
}

.summary-copy span {
  display: block;
  margin-top: 6px;
  max-width: 560px;
  color: #7084a2;
  line-height: 1.6;
  font-size: 12px;
}

.summary-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  min-width: 360px;
}

.summary-metrics article {
  padding: 10px 12px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f6f9ff 0%, #eef4ff 100%);
  text-align: center;
}

.summary-metrics strong {
  display: block;
  color: #173a74;
  font-size: 20px;
  font-weight: 900;
}

.summary-metrics span {
  margin-top: 4px;
  color: #6d84a5;
  font-size: 11px;
}

.panel {
  padding: 14px 16px;
  min-width: 0;
  overflow: hidden;
}

.panel-header {
  margin-bottom: 10px;
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

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar .el-input {
  width: 220px;
}

.query-panel {
  padding-bottom: 12px;
  margin-bottom: 12px;
  border-bottom: 1px solid #e9edf5;
  display: grid;
  gap: 10px;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
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

.table-shell {
  min-width: 0;
  overflow: auto hidden;
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

.stacked-cell {
  display: grid;
  gap: 3px;
  min-width: 0;
}

.stacked-cell strong {
  color: #25324a;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stacked-cell span {
  color: #7b8ca8;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-list,
.table-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-list.compact {
  max-width: 100%;
  overflow: hidden;
}

.mini-tag {
  padding: 3px 8px;
  border-radius: 999px;
  background: #f3f6fb;
  color: #476287;
  font-size: 11px;
}

.muted-text {
  color: #8b98af;
  font-size: 11px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.detail-modal {
  min-height: 220px;
  max-height: 72vh;
  overflow: auto;
  padding-right: 6px;
}

.detail-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.detail-overview article,
.report-block,
.review-card {
  border-radius: 14px;
  background: #f7faff;
}

.detail-overview article {
  padding: 12px 14px;
  display: grid;
  gap: 6px;
}

.detail-overview span,
.report-block h4 {
  color: #6d84a5;
  font-size: 11px;
}

.detail-overview strong {
  color: #1d365d;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.report-block {
  padding: 14px 16px;
  margin-bottom: 12px;
}

.report-block h4 {
  margin-bottom: 8px;
  font-weight: 800;
}

.report-block p {
  color: #30435f;
  line-height: 1.8;
  font-size: 13px;
}

.report-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.report-list {
  display: grid;
  gap: 8px;
  padding-left: 18px;
  color: #30435f;
  font-size: 13px;
  line-height: 1.8;
}

.review-list {
  display: grid;
  gap: 10px;
}

.review-card {
  padding: 12px 14px;
}

.review-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: #173a74;
  font-size: 12px;
  font-weight: 800;
}

.review-question,
.review-answer,
.review-feedback {
  margin-top: 8px;
  color: #30435f;
  font-size: 12px;
  line-height: 1.8;
}

.review-answer {
  color: #5a6c87;
}

.review-feedback {
  color: #1d4d96;
}

@media (max-width: 1080px) {
  .summary-strip,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-metrics {
    min-width: 0;
    width: 100%;
  }

  .query-grid,
  .detail-overview,
  .report-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .summary-metrics,
  .dialog-grid,
  .query-grid,
  .detail-overview,
  .report-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    width: 100%;
    flex-direction: column;
  }

  .toolbar .el-input {
    width: 100%;
  }

  .query-actions {
    justify-content: flex-start;
  }
}
</style>
