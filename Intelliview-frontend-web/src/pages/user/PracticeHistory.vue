<template>
  <div class="history-page">
    <aside class="app-sidebar">
      <router-link to="/user" class="brand">
        <span class="brand-logo"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="side-nav" aria-label="历史记录导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/history' }">
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <main class="history-main">
      <div class="history-tabs">
        <button type="button" :class="{ active: activeTab === 'interview' }" @click="activeTab = 'interview'">面试记录</button>
        <button type="button" :class="{ active: activeTab === 'practice' }" @click="activeTab = 'practice'">刷题记录</button>
      </div>

      <div class="role-filter-bar">
        <span>岗位筛选</span>
        <el-select v-model="selectedRoleKey" placeholder="全部岗位" class="role-filter-select" filterable>
          <el-option
            v-for="role in roleFilterOptions"
            :key="role.key"
            :label="`${role.label}（${role.count}）`"
            :value="role.key"
          />
        </el-select>
      </div>

      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="6" animated />
      </div>

      <template v-else>
        <section v-if="activeTab === 'interview'" class="history-panel">
          <div class="history-toolbar">
            <div>
              <strong>面试历史</strong>
              <p>支持自定义命名、查看最终报告，也可以删除不需要的记录。</p>
            </div>
            <span class="history-count">共 {{ filteredInterviewHistory.length }} 场</span>
          </div>

          <div class="history-scroll">
            <article v-for="item in pagedInterviewCards" :key="item.id" class="history-card">
              <div class="history-icon" :class="item.theme">
                <el-icon><VideoCamera /></el-icon>
              </div>

              <div class="history-copy">
                <h2>{{ item.title }}</h2>
                <p>{{ item.time }}　{{ item.roleName }}　时长 {{ item.duration }}　{{ item.questions }} 道题　{{ item.statusText }}</p>
              </div>

              <div class="metric-row interview-metrics">
                <div v-for="metric in item.metrics" :key="metric.label">
                  <span>{{ metric.label }}</span>
                  <i><b :style="{ width: `${metric.value}%`, background: metric.color }"></b></i>
                  <strong>{{ metric.value }}</strong>
                </div>
              </div>

              <div class="score-box">
                <strong :class="item.theme">{{ item.score }}</strong>
                <span>综合评分</span>
                <div class="card-actions">
                  <button type="button" @click="openInterviewAction(item)">
                    {{ item.completed ? '查看报告' : '继续面试' }}
                  </button>
                  <button type="button" class="danger-btn" @click="deleteInterviewRecord(item.id)">删除记录</button>
                </div>
              </div>
            </article>

            <el-empty v-if="!interviewCards.length" description="暂无面试记录" />
          </div>

          <div v-if="interviewPageCount > 1" class="pagination-wrap">
            <el-pagination
              v-model:current-page="interviewPage"
              :page-size="interviewPageSize"
              layout="prev, pager, next"
              :total="filteredInterviewHistory.length"
              background
            />
          </div>
        </section>

        <section v-else class="history-list">
          <article v-for="item in practiceCards" :key="item.id" class="history-card">
            <div class="history-icon teal">
              <el-icon><Collection /></el-icon>
            </div>

            <div class="history-copy">
              <h2>{{ item.title }}</h2>
              <p>{{ item.time }}　{{ item.roleName }}　{{ item.completedText }}</p>
            </div>

            <div class="metric-row practice-metrics">
              <div>
                <span>完成状态</span>
                <i><b :style="{ width: item.completed ? '100%' : '55%', background: item.completed ? '#26b96d' : '#ffb547' }"></b></i>
                <strong>{{ item.completed ? '已完成' : '未完成' }}</strong>
              </div>
              <div>
                <span>查看答案</span>
                <i><b :style="{ width: item.viewAnswer ? '100%' : '45%', background: item.viewAnswer ? '#416ee6' : '#c9d2e3' }"></b></i>
                <strong>{{ item.viewAnswer ? '已查看' : '未查看' }}</strong>
              </div>
            </div>

            <div class="score-box practice-box">
              <strong class="teal">{{ item.viewAnswer ? '已看答案' : '继续练习' }}</strong>
              <span>刷题状态</span>
              <button type="button" @click="openPracticeQuestion(item.questionId)">继续练习</button>
            </div>
          </article>

          <el-empty v-if="!practiceCards.length" description="暂无刷题记录" />
        </section>
      </template>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Collection, House, Monitor, Star, TrendCharts, User, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import aiInterviewApi, { type AIInterviewHistoryItem } from '@/api/aiInterview'
import practiceHistoryApi from '@/api/practiceHistory'
import jobRoleApi, { type JobRoleOption } from '@/api/jobRoles'

const router = useRouter()
const loading = ref(false)
const activeTab = ref<'interview' | 'practice'>('interview')
const interviewHistory = ref<AIInterviewHistoryItem[]>([])
const practiceHistory = ref<any[]>([])
const jobRoles = ref<JobRoleOption[]>([])
const selectedRoleKey = ref('all')
const interviewPage = ref(1)
const interviewPageSize = 6

const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '成长中心', path: '/user/growth', icon: TrendCharts },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const formatDateTime = (value?: string) => {
  if (!value) return '暂无时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = `${date.getMonth() + 1}`.padStart(2, '0')
  const d = `${date.getDate()}`.padStart(2, '0')
  const h = `${date.getHours()}`.padStart(2, '0')
  const min = `${date.getMinutes()}`.padStart(2, '0')
  return `${y}.${m}.${d} ${h}:${min}`
}

const formatDuration = (seconds?: number) => {
  const totalSeconds = Number(seconds || 0)
  if (totalSeconds <= 0) return '0 秒'
  if (totalSeconds < 60) return `${totalSeconds} 秒`
  const minutes = Math.floor(totalSeconds / 60)
  const remainSeconds = totalSeconds % 60
  return remainSeconds ? `${minutes} 分 ${remainSeconds} 秒` : `${minutes} 分钟`
}

const normalizeRoleText = (value?: string) => (value || '').toLowerCase().replace(/\s+/g, '')

const textRoleKey = (value?: string) => {
  const normalized = normalizeRoleText(value)
  return normalized ? `text:${normalized}` : ''
}

const getPracticeRoleId = (item: any) => Number(item?.jobRoleId || item?.primaryJobRoleId || item?.targetJobRoleId || 0)
const getPracticeRoleLabel = (item: any) => item?.jobRoleName || item?.primaryJobRoleName || item?.targetPosition || item?.roleName || '未关联岗位'

const matchesSelectedRole = (itemRoleId: number, itemRoleLabel?: string) => {
  if (selectedRoleKey.value === 'all') return true
  if (selectedRoleKey.value.startsWith('id:')) {
    const selectedId = Number(selectedRoleKey.value.slice(3))
    if (itemRoleId) return itemRoleId === selectedId
    const selectedRole = jobRoles.value.find((role) => role.id === selectedId)
    return Boolean(selectedRole && normalizeRoleText(itemRoleLabel).includes(normalizeRoleText(selectedRole.name)))
  }
  return textRoleKey(itemRoleLabel) === selectedRoleKey.value
}

const filteredInterviewHistory = computed(() =>
  interviewHistory.value.filter((item) => matchesSelectedRole(Number(item.jobRoleId || 0), item.targetPosition || item.title))
)

const filteredPracticeHistory = computed(() =>
  practiceHistory.value.filter((item) => matchesSelectedRole(getPracticeRoleId(item), getPracticeRoleLabel(item)))
)

const roleFilterOptions = computed(() => {
  const counts = new Map<string, number>()
  const labels = new Map<string, string>()
  const addOption = (key: string, label: string) => {
    if (!key || !label) return
    counts.set(key, (counts.get(key) || 0) + 1)
    labels.set(key, label)
  }

  interviewHistory.value.forEach((item) => {
    const key = item.jobRoleId ? `id:${item.jobRoleId}` : textRoleKey(item.targetPosition || item.title)
    addOption(key, item.targetPosition || item.title || '未关联岗位')
  })
  practiceHistory.value.forEach((item) => {
    const roleId = getPracticeRoleId(item)
    const label = getPracticeRoleLabel(item)
    addOption(roleId ? `id:${roleId}` : textRoleKey(label), label)
  })

  const configuredOptions = jobRoles.value
    .map((role) => ({
      key: `id:${role.id}`,
      label: role.name,
      count: counts.get(`id:${role.id}`) || 0
    }))
    .filter((role) => role.count > 0)

  const configuredKeys = new Set(configuredOptions.map((role) => role.key))
  const extraOptions = [...counts.entries()]
    .filter(([key]) => key !== 'all' && !configuredKeys.has(key))
    .map(([key, count]) => ({
      key,
      label: labels.get(key) || '未关联岗位',
      count
    }))
    .sort((a, b) => b.count - a.count)

  return [
    { key: 'all', label: '全部岗位', count: interviewHistory.value.length + practiceHistory.value.length },
    ...configuredOptions,
    ...extraOptions
  ]
})

const interviewCards = computed(() =>
  filteredInterviewHistory.value.map((item, index: number) => {
    const score = Number(item.totalScore || 0)
    const progress = Math.min(100, Math.round(((item.answeredCount || 0) / Math.max(item.questionCount || 1, 1)) * 100))
    const completed = item.status === 'COMPLETED'
    return {
      id: item.interviewId,
      title: item.title || item.targetPosition || '模拟面试',
      roleName: item.targetPosition || '未关联岗位',
      time: formatDateTime(item.createdAt),
      duration: formatDuration(item.duration || 0),
      questions: item.questionCount || 0,
      score,
      completed,
      status: item.status,
      statusText: completed ? '已生成报告' : '进行中',
      theme: index % 2 === 0 ? 'orange' : 'blue',
      metrics: [
        { label: '答题进度', value: progress, color: '#ff5a2a' },
        { label: '综合得分', value: Math.min(100, score), color: '#416ee6' },
        { label: '题量覆盖', value: Math.min(100, (item.questionCount || 0) * 10), color: '#26b96d' },
        { label: '完成状态', value: completed ? 100 : Math.max(35, progress), color: '#8b48e8' }
      ]
    }
  })
)

const interviewPageCount = computed(() => Math.max(1, Math.ceil(interviewCards.value.length / interviewPageSize)))

const pagedInterviewCards = computed(() => {
  const start = (interviewPage.value - 1) * interviewPageSize
  return interviewCards.value.slice(start, start + interviewPageSize)
})

const practiceCards = computed(() =>
  filteredPracticeHistory.value.map((item: any) => {
    return {
      id: item.id || `${item.questionId}-${item.practiceTime || item.createdAt || ''}`,
      questionId: Number(item.questionId || item.id || 0),
      title: item.questionTitle || item.title || `题目 ${item.questionId || ''}`,
      roleName: getPracticeRoleLabel(item),
      time: formatDateTime(item.practiceTime || item.createdAt || item.updatedAt),
      completed: Boolean(item.completed),
      completedText: item.completed ? '已完成' : '未完成',
      viewAnswer: Boolean(item.viewAnswer)
    }
  })
)

const loadHistoryData = async () => {
  loading.value = true
  try {
    const [interviewResult, practiceResult, roleResult] = await Promise.allSettled([
      aiInterviewApi.getInterviewHistory(50),
      practiceHistoryApi.getUserPracticeHistory(0, 50),
      jobRoleApi.listJobRoles()
    ])

    if (interviewResult.status === 'fulfilled') {
      interviewHistory.value = Array.isArray(interviewResult.value) ? interviewResult.value : []
      interviewPage.value = Math.min(interviewPage.value, Math.max(1, Math.ceil(interviewHistory.value.length / interviewPageSize)))
    } else {
      interviewHistory.value = []
      interviewPage.value = 1
    }

    if (practiceResult.status === 'fulfilled') {
      const result = practiceResult.value as any
      practiceHistory.value = result?.records || result?.list || (Array.isArray(result) ? result : [])
    } else {
      practiceHistory.value = []
    }

    if (roleResult.status === 'fulfilled') {
      jobRoles.value = Array.isArray(roleResult.value) ? roleResult.value : []
    } else {
      jobRoles.value = []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const openInterviewAction = (item: any) => {
  if (!item?.id) {
    ElMessage.warning('该条记录缺少面试编号，暂时无法查看')
    return
  }
  if (item.completed) {
    router.push(`/user/interview/ai/session/${item.id}?report=1`)
    return
  }
  router.push(`/user/interview/ai/session/${item.id}`)
}

const deleteInterviewRecord = async (interviewId: number) => {
  if (!interviewId) {
    ElMessage.warning('该条记录缺少面试编号，暂时无法删除')
    return
  }
  try {
    await ElMessageBox.confirm('删除后该场面试的题目、作答和报告会一起移除，是否继续？', '删除面试记录', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await aiInterviewApi.deleteInterview(interviewId)
    interviewHistory.value = interviewHistory.value.filter((item) => item.interviewId !== interviewId)
    interviewPage.value = Math.min(interviewPage.value, Math.max(1, Math.ceil(filteredInterviewHistory.value.length / interviewPageSize)))
    ElMessage.success('面试记录已删除')
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error?.message || '删除面试记录失败，请稍后重试')
  }
}

const openPracticeQuestion = (questionId: number) => {
  if (!questionId) {
    ElMessage.warning('该条记录缺少题目编号，暂时无法跳转')
    return
  }
  router.push(`/question/${questionId}`)
}

watch([selectedRoleKey, activeTab], () => {
  interviewPage.value = 1
})

watch(filteredInterviewHistory, () => {
  interviewPage.value = Math.min(interviewPage.value, Math.max(1, Math.ceil(filteredInterviewHistory.value.length / interviewPageSize)))
})

onMounted(loadHistoryData)
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f6f7f9;
  color: #242733;
}

.app-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 200px;
  border-right: 1px solid #edf0f5;
  background: #ffffff;
}

.brand,
.side-nav a {
  text-decoration: none;
}

.brand {
  height: 66px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-logo {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  overflow: hidden;
  background: transparent;
  box-shadow: 0 8px 18px rgba(255, 90, 42, 0.18);
}

.brand-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand strong {
  color: #252936;
  font-size: 16px;
  font-weight: 900;
}

.side-nav {
  padding: 18px 12px;
  display: grid;
  gap: 8px;
}

.side-nav a {
  height: 42px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 11px;
  border-radius: 8px;
  color: #747d90;
  font-size: 14px;
  font-weight: 800;
}

.side-nav a.active,
.side-nav a.router-link-active {
  color: #ff5a2a;
  background: #fff1eb;
}

.history-main {
  margin-left: 200px;
  padding: 0 30px 70px;
}

.history-tabs {
  height: 52px;
  display: flex;
  align-items: stretch;
  gap: 34px;
  border-bottom: 1px solid #e8edf5;
  background: #ffffff;
  margin: 0 -30px 30px;
  padding-left: 30px;
}

.history-tabs button {
  border: none;
  border-bottom: 2px solid transparent;
  color: #99a3b6;
  background: transparent;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
}

.history-tabs button.active {
  color: #ff5a2a;
  border-bottom-color: #ff5a2a;
}

.role-filter-bar {
  margin: -14px 0 24px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
}

.role-filter-bar span {
  color: #778197;
  font-size: 13px;
  font-weight: 900;
}

.role-filter-select {
  width: 240px;
}

.loading-wrap {
  padding: 24px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
}

.history-panel {
  display: grid;
  gap: 16px;
}

.history-toolbar {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
}

.history-toolbar strong {
  color: #242733;
  font-size: 18px;
  font-weight: 900;
}

.history-toolbar p {
  margin-top: 6px;
  color: #8f98aa;
  font-size: 13px;
}

.history-count {
  flex-shrink: 0;
  padding: 8px 14px;
  border-radius: 999px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
}

.history-scroll {
  max-height: calc(100vh - 260px);
  overflow-y: auto;
  padding-right: 4px;
  display: grid;
  gap: 16px;
}

.history-list {
  display: grid;
  gap: 16px;
}

.history-card {
  padding: 24px;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 160px;
  grid-template-areas:
    "icon copy score"
    "icon metrics score";
  column-gap: 22px;
  row-gap: 18px;
  align-items: start;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(27, 36, 56, 0.025);
}

.history-icon {
  grid-area: icon;
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 9px;
}

.history-icon.orange {
  color: #416ee6;
  background: #eef3ff;
}

.history-icon.blue {
  color: #26b96d;
  background: #ecfbf2;
}

.history-icon.teal {
  color: #ff8a00;
  background: #fff4e6;
}

.history-copy h2 {
  color: #242733;
  font-size: 17px;
  font-weight: 900;
  line-height: 1.35;
}

.history-copy p {
  margin-top: 6px;
  color: #8f98aa;
  font-size: 13px;
  line-height: 1.7;
}

.history-copy {
  grid-area: copy;
  min-width: 0;
}

.metric-row {
  grid-area: metrics;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  min-width: 0;
}

.metric-row div {
  min-width: 0;
  display: grid;
  gap: 7px;
}

.metric-row span {
  color: #8f98aa;
  font-size: 12px;
}

.metric-row i {
  height: 4px;
  overflow: hidden;
  border-radius: 999px;
  background: #eef1f6;
}

.metric-row b {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.metric-row strong {
  color: #252936;
  font-size: 12px;
}

.practice-metrics {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.score-box {
  grid-area: score;
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  justify-items: center;
  gap: 5px;
  padding-left: 8px;
  border-left: 1px solid #edf0f5;
}

.score-box strong {
  font-size: 32px;
  line-height: 1;
  font-weight: 900;
}

.score-box strong.orange {
  color: #ff5a2a;
}

.score-box strong.blue {
  color: #416ee6;
}

.score-box strong.teal {
  color: #18a999;
}

.score-box span {
  color: #9aa3b6;
  font-size: 12px;
}

.score-box button {
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 7px;
  color: #ffffff;
  background: #ff5a2a;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.practice-box button {
  background: #18a999;
}

.card-actions {
  margin-top: 7px;
  display: grid;
  gap: 8px;
  width: 100%;
}

.score-box .danger-btn {
  color: #d94c4c;
  background: #fff1f1;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 960px) {
  .app-sidebar {
    position: static;
    width: 100%;
  }

  .history-main {
    margin-left: 0;
  }

  .history-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .role-filter-bar {
    justify-content: flex-start;
  }

  .history-scroll {
    max-height: none;
  }

  .metric-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .history-card {
    grid-template-columns: 1fr;
    grid-template-areas:
      "icon"
      "copy"
      "metrics"
      "score";
  }

  .score-box {
    align-items: flex-start;
    justify-items: start;
    padding-left: 0;
    border-left: none;
    border-top: 1px solid #edf0f5;
    padding-top: 16px;
  }

  .practice-metrics,
  .metric-row {
    grid-template-columns: 1fr;
  }

  .role-filter-bar {
    align-items: stretch;
    flex-direction: column;
  }

  .role-filter-select {
    width: 100%;
  }
}
</style>
