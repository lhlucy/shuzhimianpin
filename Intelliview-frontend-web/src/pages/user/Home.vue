<template>
  <div class="home-page">
    <header class="prototype-header">
      <router-link to="/user" class="prototype-brand" aria-label="数智面聘首页">
        <span class="prototype-brand-mark">
          <img src="/images/shuzhimianpin_logo.png" alt="" />
        </span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="prototype-nav" aria-label="首页导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path">
          {{ item.label }}
        </router-link>
      </nav>

      <div class="prototype-actions">
        <el-dropdown v-if="isAuthenticated" trigger="click" @command="handleAvatarCommand">
          <button class="avatar-link" type="button" aria-label="用户菜单">
            <el-avatar :size="34" :src="authUser?.avatar" class="user-avatar">{{ avatarInitial }}</el-avatar>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-else>
          <router-link class="login-link" to="/login">登录</router-link>
          <router-link class="register-link" to="/register">免费注册</router-link>
        </template>
      </div>
    </header>

    <main>
      <section class="hero-section">
        <div class="hero-inner">
          <div class="hero-copy">
          <div class="hero-badge">
            <el-icon><MagicStick /></el-icon>
            <span>AI 驱动 · 精准提升 · 全程智能陪练</span>
          </div>

          <h1>
            你的专属 AI 面试教练
            <strong>数智面聘</strong>
          </h1>

          <p class="hero-desc">
            海量技术岗位题库 · 模拟真实面试场景 · AI 实时点评反馈
            <span>助你在竞争激烈的求职场中脱颖而出</span>
          </p>

          <div class="hero-actions">
            <el-button type="primary" class="primary-btn" @click="goInterview">进入模拟面试</el-button>
            <el-button class="ghost-btn" @click="goPractice">
              <el-icon><VideoPlay /></el-icon>
              开始刷题
            </el-button>
          </div>

          <div class="stats" aria-label="平台数据">
            <article>
              <strong>{{ totalQuestionCount }}</strong>
              <span>真实题库</span>
            </article>
            <article>
              <strong>{{ practiceStats.completedCount }}</strong>
              <span>我的已完成</span>
            </article>
            <article>
              <strong>{{ interviewCount }}</strong>
              <span>我的模拟面试</span>
            </article>
          </div>
          </div>

          <aside class="ability-radar-card">
            <div class="radar-orbit one"></div>
            <div class="radar-orbit two"></div>
            <div class="radar-head">
              <div>
                <span>能力雷达</span>
                <h2>综合成长画像</h2>
              </div>
              <div class="radar-switch">
                <button
                  v-for="count in radarCountOptions"
                  :key="count"
                  type="button"
                  :class="{ active: selectedRadarCount === count }"
                  @click="selectedRadarCount = count"
                >
                  {{ count }} 场
                </button>
              </div>
            </div>

            <div ref="radarChartRef" class="home-radar-chart" aria-label="首页能力雷达图"></div>

            <div class="radar-metrics">
              <article v-for="item in radarDimensionItems" :key="item.key">
                <i :style="{ background: item.color }"></i>
                <span>{{ item.label }}</span>
                <strong>{{ item.score }}</strong>
              </article>
            </div>

            <p class="radar-tip">
              {{ radarLoading ? '正在整理最近面试能力数据...' : radarTipText }}
            </p>
          </aside>

        </div>
      </section>

      <section class="resource-section">
        <div class="resource-inner">
          <div class="section-title recommend-title">
            <h2>推荐题目</h2>
            <p>从高频题开始，快速建立面试回答框架</p>
          </div>

          <div class="question-grid">
            <article
              v-for="question in recommendedQuestions"
              :key="question.id"
              class="question-card"
              tabindex="0"
              role="link"
              @click="openRecommendedQuestion(question)"
              @keydown.enter.prevent="openRecommendedQuestion(question)"
            >
              <span>{{ question.role }} · {{ question.difficulty }}</span>
              <h3>{{ question.title }}</h3>
              <p>{{ question.category }}</p>
            </article>
          </div>

          <section class="visual-section">
            <div class="visual-copy">
              <span class="visual-eyebrow">面试回顾</span>
              <h2>最近一次面试记录</h2>
              <p>回到你最近一次模拟面试，快速继续答题、查看得分与复盘表现。</p>
            </div>

            <div v-if="isAuthenticated && recentInterview" class="interview-record-card">
              <div class="record-main">
                <div class="record-status">
                  <span class="status-badge" :class="recentInterview.statusClass">{{ recentInterview.statusText }}</span>
                  <strong>{{ recentInterview.title }}</strong>
                  <p>{{ recentInterview.targetPosition }}</p>
                </div>

                <div class="record-meta">
                  <article>
                    <span>开始时间</span>
                    <strong>{{ recentInterview.createdAt }}</strong>
                  </article>
                  <article>
                    <span>答题进度</span>
                    <strong>{{ recentInterview.progressText }}</strong>
                  </article>
                  <article>
                    <span>综合得分</span>
                    <strong>{{ recentInterview.scoreText }}</strong>
                  </article>
                </div>

                <div class="record-progress">
                  <div class="progress-head">
                    <span>面试进度</span>
                    <strong>{{ recentInterview.progressPercent }}%</strong>
                  </div>
                  <div class="progress-track">
                    <span class="progress-bar" :style="{ width: `${recentInterview.progressPercent}%` }"></span>
                  </div>
                </div>
              </div>

              <div class="record-side">
                <div class="record-side-card">
                  <span>技术栈</span>
                  <div class="tech-stack-list">
                    <i v-for="tech in recentInterview.techStacks" :key="tech">{{ tech }}</i>
                    <i v-if="!recentInterview.techStacks.length">待补充</i>
                  </div>
                </div>
                <div class="record-actions">
                  <button type="button" class="record-primary" @click="openInterviewSession(recentInterview.id)">继续面试</button>
                  <button type="button" class="record-ghost" @click="goHistory">查看历史</button>
                </div>
              </div>
            </div>

            <div v-else-if="isAuthenticated" class="interview-empty-card">
              <strong>还没有模拟面试记录</strong>
              <p>现在开始一场 AI 模拟面试，系统会在这里展示你最近一次的面试进度与结果。</p>
              <button type="button" class="record-primary" @click="goInterview">立即开始</button>
            </div>

            <div v-else class="interview-empty-card login-card">
              <strong>请登录后查看模拟面试历史</strong>
              <p>登录后即可同步最近一次面试记录、答题进度和历史报告，便于持续复盘与提升。</p>
              <button type="button" class="record-primary" @click="goLogin">去登录</button>
            </div>
          </section>

          <section class="role-section">
            <div class="section-title role-section-title">
              <h2>项目所有岗位</h2>
              <p>覆盖主流技术方向，按目标岗位快速进入专项训练</p>
            </div>

            <div class="role-grid">
              <article v-for="role in roleBanks" :key="role.code" class="role-card">
                <div class="role-icon" :style="{ color: role.color, background: `${role.color}14` }">
                  <el-icon><component :is="role.icon" /></el-icon>
                </div>
                <h3>{{ role.name }}</h3>
                <p>{{ role.subtitle }}</p>
                <button type="button" @click="goPractice(role.code)">进入岗位题库</button>
              </article>
            </div>
          </section>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick, VideoPlay } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { RadarComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { useRoleBanks } from '@/data/interviewData'
import service from '@/utils/axios'
import aiInterviewApi, { type AIInterviewHistoryItem, type AIInterviewSummary } from '@/api/aiInterview'
import userApi, { emptyPracticeStats } from '@/api/user'
import { authUser, clearAuthSession, getAvatarInitial, isAuthenticated, loadCurrentUser, refreshAuthState } from '@/utils/auth'

echarts.use([RadarChart, RadarComponent, TooltipComponent, CanvasRenderer])

const router = useRouter()
const { roleBanks } = useRoleBanks()
const practiceStats = ref({ ...emptyPracticeStats })
const interviewCount = ref(0)
const realQuestionCount = ref(0)
const interviewHistory = ref<AIInterviewHistoryItem[]>([])
const radarChartRef = ref<HTMLElement>()
const radarSummaries = ref<Record<number, AIInterviewSummary>>({})
const selectedRadarCount = ref(3)
const radarLoading = ref(false)
const radarCountOptions = [1, 3, 5]
let radarChart: echarts.ECharts | null = null
let radarSeq = 0
const avatarInitial = computed(() => getAvatarInitial())
const RADAR_DIMENSIONS = [
  { key: 'technicalDepth', label: '技术深度', color: '#ff5a2a' },
  { key: 'projectRelevance', label: '项目匹配', color: '#2f6fdb' },
  { key: 'problemSolving', label: '问题分析', color: '#19a36b' },
  { key: 'communicationClarity', label: '表达清晰', color: '#b47a12' },
  { key: 'jobMatch', label: '岗位匹配', color: '#7b58d8' }
]
const navItems = [
  { label: '首页', path: '/user' },
  { label: '岗位刷题', path: '/user/practice' },
  { label: '模拟面试', path: '/user/interview/ai/create' },
  { label: '历史记录', path: '/user/history' },
  { label: '收藏', path: '/user/favorites' },
  { label: '成长中心', path: '/user/growth' }
]

const recommendedQuestions = computed(() =>
  roleBanks.value.flatMap((role) =>
    role.questions.slice(0, 1).map((question) => ({
      id: question.id,
      title: question.title,
      difficulty: question.difficulty,
      category: question.category,
      role: role.name,
      roleCode: role.code
    }))
  )
)
const totalQuestionCount = computed(() =>
  realQuestionCount.value || roleBanks.value.reduce((total, role) => total + role.questions.length, 0)
)
const completedInterviewHistory = computed(() => interviewHistory.value.filter((item) => item.status === 'COMPLETED'))
const radarSourceInterviews = computed(() => completedInterviewHistory.value.slice(0, selectedRadarCount.value))
const radarDimensionItems = computed(() => {
  const summaries = radarSourceInterviews.value
    .map((item) => radarSummaries.value[item.interviewId])
    .filter(Boolean)

  return RADAR_DIMENSIONS.map((dimension) => {
    const values = summaries
      .map((summary) => Number(summary.dimensionScores?.[dimension.key]))
      .filter((value) => Number.isFinite(value))
    const fallback = completedInterviewHistory.value[0]?.totalScore || 0
    const score = values.length
      ? Math.round(values.reduce((sum, value) => sum + value, 0) / values.length)
      : Math.round(Number(fallback || 0))
    return { ...dimension, score: Math.max(0, Math.min(100, score)) }
  })
})
const radarTipText = computed(() => {
  const count = radarSourceInterviews.value.length
  if (!isAuthenticated.value) return '登录后可结合最近面试报告生成真实能力画像。'
  if (!count) return '完成模拟面试后，这里会生成你的能力雷达图。'
  return `当前结合最近 ${count} 场已完成面试生成。`
})
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
const recentInterview = computed(() => {
  const item = interviewHistory.value[0]
  if (!item) return null

  const progressPercent = Math.min(100, Math.round(((item.answeredCount || 0) / Math.max(item.questionCount || 1, 1)) * 100))
  const statusText = item.status === 'COMPLETED' ? '已完成' : item.status === 'IN_PROGRESS' ? '进行中' : '待开始'
  const statusClass = item.status === 'COMPLETED' ? 'completed' : item.status === 'IN_PROGRESS' ? 'progress' : 'pending'

  return {
    id: item.interviewId,
    title: item.title || '模拟面试',
    targetPosition: item.targetPosition || '目标岗位待设置',
    createdAt: formatDateTime(item.createdAt),
    progressPercent,
    progressText: `${item.answeredCount || 0}/${item.questionCount || 0} 题`,
    scoreText: typeof item.totalScore === 'number' ? `${Math.round(item.totalScore)} 分` : '待生成',
    statusText,
    statusClass,
    techStacks: item.techStacks?.slice(0, 4) || []
  }
})

const loadRealQuestionCount = async () => {
  try {
    const response = await service.get('/api/questions', {
      params: {
        page: 0,
        size: 1,
        isForPractice: true
      }
    }) as any

    const total = response?.data?.total
    if (typeof total === 'number') {
      realQuestionCount.value = total
    }
  } catch (error) {
    console.error('加载真实题库总量失败:', error)
  }
}

const goPractice = (role?: string) => {
  router.push(role ? `/user/practice?role=${role}` : '/user/practice')
}

const goInterview = () => {
  router.push('/user/interview/ai/create')
}

const goHistory = () => {
  router.push('/user/history')
}

const goLogin = () => {
  router.push('/login')
}

const openInterviewSession = (interviewId: number) => {
  router.push(`/user/interview/ai/session/${interviewId}`)
}

const openRecommendedQuestion = (question: { id?: number, roleCode?: string }) => {
  if (question.id && question.roleCode) {
    router.push(`/user/practice?role=${question.roleCode}&question=${question.id}`)
    return
  }
  router.push(question.roleCode ? `/user/practice?role=${question.roleCode}` : '/user/practice')
}

const loadRadarSummaries = async () => {
  const seq = ++radarSeq
  const ids = radarSourceInterviews.value.map((item) => item.interviewId)
  const missingIds = ids.filter((id) => !radarSummaries.value[id])
  if (!ids.length) {
    await renderRadarChart()
    return
  }
  radarLoading.value = Boolean(missingIds.length)
  try {
    const results = await Promise.allSettled(missingIds.map((id) => aiInterviewApi.getSummary(id)))
    if (seq !== radarSeq) return
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') {
        radarSummaries.value[missingIds[index]] = result.value
      }
    })
    await renderRadarChart()
  } finally {
    if (seq === radarSeq) radarLoading.value = false
  }
}

const renderRadarChart = async () => {
  await nextTick()
  if (!radarChartRef.value) return
  await new Promise<void>((resolve) => requestAnimationFrame(() => resolve()))
  if (!radarChartRef.value) return
  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }
  const items = radarDimensionItems.value
  radarChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: () => items.map((item) => `${item.label}: ${item.score}`).join('<br/>')
    },
    radar: {
      radius: '66%',
      center: ['50%', '50%'],
      splitNumber: 4,
      indicator: items.map((item) => ({ name: item.label, max: 100 })),
      axisName: {
        color: '#4a566b',
        fontWeight: 800
      },
      splitLine: { lineStyle: { color: ['rgba(45, 65, 105, 0.12)'] } },
      splitArea: { areaStyle: { color: ['rgba(255, 255, 255, 0.52)', 'rgba(255, 90, 42, 0.045)'] } },
      axisLine: { lineStyle: { color: 'rgba(45, 65, 105, 0.12)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: items.map((item) => item.score),
        name: '能力画像',
        symbol: 'circle',
        symbolSize: 7,
        areaStyle: { color: 'rgba(255, 90, 42, 0.2)' },
        lineStyle: { width: 3, color: '#ff5a2a' },
        itemStyle: { color: '#ff5a2a' }
      }]
    }]
  })
  radarChart.resize()
}

const resizeRadarChart = () => {
  radarChart?.resize()
}

const handleAvatarCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/user/profile')
    return
  }

  if (command === 'logout') {
    clearAuthSession()
    router.push('/login')
  }
}

onMounted(async () => {
  refreshAuthState()
  await loadRealQuestionCount()
  window.addEventListener('resize', resizeRadarChart)
  if (!isAuthenticated.value) {
    await renderRadarChart()
    return
  }

  await loadCurrentUser().catch(() => refreshAuthState())
  const [stats, history] = await Promise.allSettled([
    userApi.getPracticeStats(),
    aiInterviewApi.getInterviewHistory(50)
  ])

  if (stats.status === 'fulfilled') {
    practiceStats.value = stats.value
  }
  if (history.status === 'fulfilled') {
    interviewHistory.value = history.value
    interviewCount.value = history.value.length
    await loadRadarSummaries()
  }
})

watch(selectedRadarCount, () => loadRadarSummaries())
watch(radarDimensionItems, () => renderRadarChart(), { deep: true, flush: 'post' })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeRadarChart)
  radarChart?.dispose()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background:
    linear-gradient(180deg, rgba(255, 253, 251, 0.94) 0%, rgba(248, 249, 252, 0.92) 44%, #f3f5f9 100%),
    url('/images/home-hero-lines-image2.png') top right / min(72vw, 1100px) auto no-repeat;
}

main {
  overflow: hidden;
}

.prototype-header {
  position: sticky;
  top: 0;
  z-index: 50;
  height: 52px;
  padding: 0 36px;
  display: grid;
  grid-template-columns: 210px minmax(0, 1fr) 210px;
  align-items: center;
  border-bottom: 1px solid #eef0f4;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(14px);
}

.prototype-brand,
.prototype-nav a,
.prototype-actions a {
  text-decoration: none;
}

.prototype-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.prototype-brand-mark {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 6px;
  overflow: hidden;
  background: transparent;
  box-shadow: 0 8px 18px rgba(255, 90, 42, 0.18);
}

.prototype-brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.prototype-brand strong {
  color: #242733;
  font-size: 16px;
  font-weight: 900;
}

.prototype-nav {
  min-width: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  overflow-x: auto;
}

.prototype-nav a {
  color: #737b8c;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.prototype-nav a:hover,
.prototype-nav a.router-link-active {
  color: #ff5a2a;
}

.prototype-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.login-link,
.register-link {
  height: 32px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 800;
}

.login-link {
  border: 1px solid #ffb39d;
  color: #ff5a2a;
  background: #ffffff;
}

.user-link {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avatar-link {
  border: none;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  cursor: pointer;
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 18px rgba(36, 39, 51, 0.12);
  background: linear-gradient(135deg, #ff8b63, #ff5a2a);
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
}

:deep(.el-dropdown-menu__item) {
  font-weight: 700;
}

.register-link {
  border: 1px solid #ff5a2a;
  color: #ffffff;
  background: #ff5a2a;
}

.hero-section {
  position: relative;
  min-height: calc(100vh - 52px);
  display: flex;
  align-items: stretch;
  justify-content: center;
  padding: 34px 0 64px;
  border-bottom: 1px solid rgba(234, 238, 245, 0.85);
  overflow: hidden;
}

.hero-section::before,
.hero-section::after {
  content: "";
  position: absolute;
  pointer-events: none;
}

.hero-section::before {
  width: 420px;
  height: 420px;
  right: 8%;
  top: 12%;
  border: 1px solid rgba(255, 90, 42, 0.12);
  border-radius: 50%;
  animation: floatHalo 7s ease-in-out infinite alternate;
}

.hero-section::after {
  width: 220px;
  height: 220px;
  right: 30%;
  bottom: 7%;
  border: 1px solid rgba(47, 111, 219, 0.12);
  border-radius: 50%;
  animation: floatHalo 8s ease-in-out infinite alternate-reverse;
}

.hero-inner {
  position: relative;
  z-index: 1;
  width: min(1280px, calc(100% - 40px));
  margin: 0 auto;
  padding: 58px 0 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 460px);
  align-items: center;
  gap: clamp(32px, 6vw, 82px);
}

.hero-copy {
  min-width: 0;
  animation: heroRise 0.72s ease both;
}

.hero-badge {
  width: fit-content;
  margin: 0 0 28px;
  padding: 7px 16px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  border: 1px solid #ffd7c7;
  border-radius: 999px;
  background: #fff6f2;
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 700;
}

.hero-inner h1 {
  color: #242733;
  font-size: clamp(44px, 7vw, 72px);
  line-height: 1.24;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-inner h1 strong {
  display: block;
  color: #ff5a2a;
  font-size: clamp(48px, 7.2vw, 76px);
}

.hero-desc {
  margin: 26px 0 0;
  max-width: 620px;
  color: #626b7d;
  font-size: 16px;
  line-height: 1.8;
}

.hero-desc span {
  display: block;
}

.hero-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.primary-btn,
.ghost-btn {
  min-width: 138px;
  height: 46px;
  border-radius: 8px;
  font-weight: 800;
}

:deep(.primary-btn.el-button--primary) {
  border-color: #ff5a2a;
  background: #ff5a2a;
  box-shadow: 0 10px 20px rgba(255, 90, 42, 0.18);
}

:deep(.primary-btn.el-button--primary:hover) {
  border-color: #f24b1b;
  background: #f24b1b;
}

:deep(.ghost-btn.el-button) {
  color: #242733;
  border-color: #e5e9f0;
  background: #ffffff;
}

.stats {
  width: min(460px, 100%);
  margin: 28px 0 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.stats article {
  min-width: 0;
  padding: 0 18px;
  display: grid;
  gap: 3px;
  border-right: 1px solid #e7ebf2;
}

.stats article:last-child {
  border-right: none;
}

.stats strong {
  color: #2b2e3a;
  font-size: 25px;
  line-height: 1.1;
  font-weight: 900;
}

.stats span {
  color: #98a1b3;
  font-size: 12px;
}

.ability-radar-card {
  position: relative;
  min-height: 560px;
  padding: 26px;
  overflow: hidden;
  border: 1px solid rgba(224, 230, 240, 0.86);
  border-radius: 30px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(246, 248, 252, 0.78)),
    rgba(255, 255, 255, 0.84);
  box-shadow:
    0 28px 70px rgba(34, 45, 72, 0.13),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(22px);
  animation: radarEnter 0.82s 0.12s cubic-bezier(.2,.8,.2,1) both;
}

.ability-radar-card::before {
  content: "";
  position: absolute;
  inset: 12px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 24px;
  pointer-events: none;
}

.radar-orbit {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 90, 42, 0.14);
  pointer-events: none;
}

.radar-orbit.one {
  width: 240px;
  height: 240px;
  right: -74px;
  top: -64px;
  animation: orbitDrift 9s linear infinite;
}

.radar-orbit.two {
  width: 180px;
  height: 180px;
  left: -66px;
  bottom: 110px;
  border-color: rgba(47, 111, 219, 0.13);
  animation: orbitDrift 11s linear infinite reverse;
}

.radar-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.radar-head span {
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 900;
}

.radar-head h2 {
  margin-top: 6px;
  color: #222938;
  font-size: 22px;
  font-weight: 900;
}

.radar-switch {
  flex: none;
  padding: 4px;
  display: inline-flex;
  gap: 4px;
  border: 1px solid #e5ebf4;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
}

.radar-switch button {
  height: 28px;
  min-width: 42px;
  border: none;
  border-radius: 999px;
  color: #667286;
  background: transparent;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.radar-switch button.active {
  color: #ffffff;
  background: #2f6fdb;
  box-shadow: 0 8px 18px rgba(47, 111, 219, 0.22);
}

.radar-switch button:hover {
  transform: translateY(-1px);
}

.home-radar-chart {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 310px;
  margin-top: 12px;
}

.radar-metrics {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.radar-metrics article {
  min-width: 0;
  padding: 11px 12px;
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr) auto;
  align-items: center;
  gap: 9px;
  border: 1px solid rgba(229, 234, 244, 0.9);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.68);
}

.radar-metrics i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.radar-metrics span {
  color: #687386;
  font-size: 12px;
  font-weight: 800;
}

.radar-metrics strong {
  color: #252936;
  font-size: 18px;
  font-weight: 900;
}

.radar-tip {
  position: relative;
  z-index: 1;
  margin-top: 14px;
  padding: 12px 14px;
  border: 1px solid rgba(255, 90, 42, 0.16);
  border-radius: 16px;
  color: #7b5360;
  background: rgba(255, 244, 239, 0.68);
  font-size: 12px;
  line-height: 1.7;
}

.resource-section {
  background: transparent;
  padding: 28px 0 64px;
}

.resource-inner {
  width: min(1120px, calc(100% - 40px));
  margin: 0 auto;
}

.section-title {
  text-align: center;
}

.section-title h2 {
  color: #242733;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 900;
}

.section-title p {
  margin-top: 10px;
  color: #7c8495;
  font-size: 14px;
}

.role-grid {
  margin-top: 30px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.role-card,
.question-card {
  min-width: 0;
  border: 1px solid #e9edf4;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 34px rgba(29, 37, 56, 0.05);
  backdrop-filter: blur(12px);
}

.role-card {
  min-height: 196px;
  padding: 22px;
  display: grid;
  gap: 12px;
  text-align: left;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.role-card:hover,
.question-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(29, 37, 56, 0.08);
  border-color: #dfe5ee;
}

.role-icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  font-size: 22px;
}

.role-card h3,
.question-card h3 {
  color: #242733;
  font-size: 18px;
  font-weight: 900;
}

.role-card p {
  color: #667085;
  font-size: 14px;
  line-height: 1.7;
}

.role-card button {
  width: fit-content;
  margin-top: auto;
  border: none;
  background: transparent;
  color: #ff5a2a;
  font-weight: 800;
  cursor: pointer;
}

.recommend-title {
  margin-top: 8px;
}

.visual-section,
.role-section {
  margin-top: 52px;
}

.visual-section {
  padding: 34px;
  border: 1px solid #e6ebf2;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 253, 0.9));
  box-shadow: 0 20px 48px rgba(25, 33, 50, 0.05);
}

.visual-copy {
  text-align: center;
}

.visual-eyebrow {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 999px;
  background: #f2f5fb;
  color: #667085;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.visual-copy h2 {
  margin-top: 16px;
  color: #242733;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 900;
}

.visual-copy p {
  margin: 10px auto 0;
  max-width: 620px;
  color: #7c8495;
  font-size: 14px;
  line-height: 1.8;
}

.visual-panel {
  margin-top: 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 0.9fr);
  gap: 22px;
}

.interview-record-card,
.interview-empty-card {
  margin-top: 28px;
  padding: 24px;
  border: 1px solid #ebeff5;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
}

.interview-record-card {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(260px, 0.9fr);
  gap: 22px;
}

.record-main,
.record-side-card {
  padding: 24px;
  border: 1px solid #ebeff5;
  border-radius: 20px;
  background: linear-gradient(180deg, #fbfcfe, #f5f7fb);
}

.record-status {
  display: grid;
  gap: 10px;
}

.status-badge {
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.status-badge.completed {
  color: #167c47;
  background: #ecfbf2;
}

.status-badge.progress {
  color: #315fd9;
  background: #edf3ff;
}

.status-badge.pending {
  color: #9a6a0b;
  background: #fff5df;
}

.record-status strong {
  color: #242733;
  font-size: 26px;
  line-height: 1.2;
  font-weight: 900;
}

.record-status p {
  color: #7b8496;
  font-size: 14px;
}

.record-meta {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.record-meta article {
  padding: 16px 16px 14px;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid #eef2f7;
}

.record-meta span,
.record-side-card span,
.progress-head span {
  display: block;
  color: #9099aa;
  font-size: 12px;
  font-weight: 700;
}

.record-meta strong,
.progress-head strong {
  display: block;
  margin-top: 8px;
  color: #2e3545;
  font-size: 16px;
  font-weight: 800;
}

.record-progress {
  margin-top: 22px;
}

.progress-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-track {
  position: relative;
  margin-top: 10px;
  height: 10px;
  border-radius: 999px;
  background: #edf1f7;
  overflow: hidden;
}

.progress-bar {
  position: absolute;
  inset: 0 auto 0 0;
  border-radius: inherit;
  background: linear-gradient(90deg, #ff7a4d, #ff5a2a);
}

.record-side {
  display: grid;
  gap: 16px;
  align-content: start;
}

.tech-stack-list {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-stack-list i {
  padding: 7px 12px;
  border-radius: 999px;
  background: #ffffff;
  border: 1px solid #ecf0f6;
  color: #556074;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

.record-actions {
  display: grid;
  gap: 12px;
}

.record-primary,
.record-ghost {
  height: 44px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.record-primary {
  border: 1px solid #ff5a2a;
  color: #ffffff;
  background: #ff5a2a;
  box-shadow: 0 12px 24px rgba(255, 90, 42, 0.16);
}

.record-ghost {
  border: 1px solid #e4eaf2;
  color: #2f3748;
  background: #ffffff;
}

.interview-empty-card {
  text-align: center;
}

.interview-empty-card strong {
  display: block;
  color: #242733;
  font-size: 24px;
  line-height: 1.3;
  font-weight: 900;
}

.interview-empty-card p {
  max-width: 560px;
  margin: 12px auto 0;
  color: #7b8496;
  font-size: 14px;
  line-height: 1.8;
}

.interview-empty-card .record-primary {
  min-width: 140px;
  margin-top: 24px;
}

.login-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(247, 249, 253, 0.96));
}

.question-grid {
  margin-top: 26px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.question-card {
  min-height: 150px;
  padding: 20px;
  display: grid;
  align-content: start;
  gap: 10px;
  text-align: left;
  cursor: pointer;
}

.question-card:focus-visible {
  outline: 3px solid rgba(255, 90, 42, 0.28);
  outline-offset: 3px;
}

.question-card span {
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 800;
}

.question-card h3 {
  font-size: 16px;
  line-height: 1.55;
}

.question-card p {
  color: #8992a3;
  font-size: 13px;
}

@keyframes heroRise {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes radarEnter {
  from {
    opacity: 0;
    transform: translateX(28px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes floatHalo {
  from {
    transform: translate3d(0, 0, 0) scale(1);
  }
  to {
    transform: translate3d(14px, -18px, 0) scale(1.04);
  }
}

@keyframes orbitDrift {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 960px) {
  .hero-inner {
    grid-template-columns: 1fr;
    padding-top: 42px;
  }

  .ability-radar-card {
    min-height: 0;
  }

  .role-grid,
  .question-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .visual-panel {
    grid-template-columns: 1fr;
  }

  .interview-record-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 860px) {
  .prototype-header {
    height: auto;
    min-height: 58px;
    padding: 10px 18px;
    grid-template-columns: 1fr auto;
    gap: 12px;
  }

  .prototype-nav {
    grid-column: 1 / -1;
    grid-row: 2;
    justify-content: flex-start;
    gap: 20px;
  }
}

@media (max-width: 640px) {
  .hero-section {
    min-height: auto;
    padding: 18px 0 40px;
  }

  .hero-inner {
    padding: 40px 0 0;
  }

  .hero-inner h1,
  .hero-inner h1 strong {
    font-size: 38px;
  }

  .stats {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .ability-radar-card {
    padding: 18px;
    border-radius: 22px;
  }

  .radar-head {
    flex-direction: column;
  }

  .home-radar-chart {
    height: 260px;
  }

  .radar-metrics {
    grid-template-columns: 1fr;
  }

  .stats article {
    border-right: none;
  }

  .role-grid,
  .question-grid {
    grid-template-columns: 1fr;
  }

  .visual-section {
    margin-top: 40px;
    padding: 22px 18px;
    border-radius: 22px;
  }

  .visual-chart,
  .visual-metrics,
  .record-main,
  .record-side-card {
    padding: 18px;
    border-radius: 18px;
  }

  .record-meta {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .prototype-actions {
    gap: 6px;
  }

  .login-link,
  .register-link {
    padding: 0 10px;
  }
}
</style>
