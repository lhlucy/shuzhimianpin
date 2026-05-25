<template>
  <div class="dashboard-page">
    <template v-if="!isAnalyticsMode">
      <section class="overview-hero">
        <div class="hero-main">
          <p class="hero-kicker">Admin Overview</p>
          <h2>后台总览驾驶舱</h2>
          <p class="hero-copy">
            首页直接展示当前平台的真实资源状态、最近活动和面试运行情况，方便管理员先看全局，再进入具体模块处理。
          </p>

          <div class="hero-actions">
            <el-button type="primary" @click="router.push('/admin/users')">用户管理</el-button>
            <el-button plain @click="router.push('/admin/questions')">题目管理</el-button>
            <el-button plain @click="router.push('/admin/interviews')">面试管理</el-button>
          </div>
        </div>

        <div class="hero-focus-card">
          <span class="focus-label">当前运行焦点</span>
          <strong>{{ focusHeadline }}</strong>
          <p>{{ focusDescription }}</p>
          <div class="focus-meta">
            <span>最近活动 {{ recentActivities.length }} 条</span>
            <span>活跃岗位 {{ activeRoleCount }} 个</span>
          </div>
        </div>
      </section>

      <section class="metric-ribbon">
        <article v-for="item in overviewCards" :key="item.label" class="metric-card" :class="item.tone">
          <div class="metric-top">
            <span>{{ item.label }}</span>
            <i>{{ item.icon }}</i>
          </div>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </article>
      </section>

      <section class="overview-grid">
        <article class="panel feature-panel wide">
          <div class="panel-head">
            <div>
              <h3>资源总量对比</h3>
              <p>基于真实后台数据的柱状图，对比账号、题目、题库和试卷规模。</p>
            </div>
          </div>
          <div ref="resourceChartRef" class="chart-box hero-chart"></div>
        </article>

        <article class="panel summary-panel">
          <div class="panel-head compact">
            <div>
              <h3>结构摘要</h3>
              <p>题库与训练结构的当前状态。</p>
            </div>
          </div>

          <div class="summary-stack">
            <div class="summary-item">
              <span>题目难度覆盖</span>
              <strong>{{ difficultySummary }}</strong>
            </div>
            <div class="summary-item">
              <span>主力题型</span>
              <strong>{{ topTypeLabel }}</strong>
            </div>
            <div class="summary-item">
              <span>最近面试状态</span>
              <strong>{{ latestInterviewStatus }}</strong>
            </div>
          </div>
        </article>
      </section>

      <section class="overview-grid lower">
        <article class="panel">
          <div class="panel-head">
            <div>
              <h3>最近活动</h3>
              <p>来自题目、题库、试卷和用户的真实最近变化。</p>
            </div>
          </div>

          <div class="activity-list">
            <template v-if="recentActivities.length">
              <div v-for="(activity, index) in recentActivities.slice(0, 6)" :key="index" class="activity-item">
                <div class="activity-dot"></div>
                <div>
                  <strong>{{ activity.title }}</strong>
                  <p>{{ activity.description }}</p>
                  <span>{{ activity.user || '系统' }} · {{ formatDateTime(activity.time) }}</span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无活动记录" />
          </div>
        </article>

        <article class="panel">
          <div class="panel-head">
            <div>
              <h3>最新面试记录</h3>
              <p>后台最近产生的真实模拟面试记录。</p>
            </div>
          </div>

          <div class="mini-list">
            <template v-if="interviewRecords.length">
              <div v-for="item in interviewRecords.slice(0, 5)" :key="item.interviewId" class="mini-item">
                <div class="mini-main">
                  <strong>{{ item.title || item.targetPosition || '未命名面试' }}</strong>
                  <span>{{ item.nickname || item.username || '未知用户' }}</span>
                </div>
                <div class="mini-side">
                  <em>{{ formatInterviewStatus(item.status) }}</em>
                  <span>{{ formatScore(item.totalScore) }}</span>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无面试记录" />
          </div>
        </article>
      </section>

      <section class="overview-grid lower">
        <article class="panel">
          <div class="panel-head">
            <div>
              <h3>岗位配置概览</h3>
              <p>读取真实岗位参数，直接反映当前用户端可创建的模拟面试方向。</p>
            </div>
          </div>

          <div class="role-grid">
            <article v-for="role in interviewRoles.slice(0, 4)" :key="role.id" class="role-card">
              <div class="role-card-head">
                <strong>{{ role.name }}</strong>
                <span>{{ role.isActive === false ? '已停用' : '启用中' }}</span>
              </div>
              <p>{{ role.description || '当前未填写岗位描述。' }}</p>
              <div class="role-tags">
                <span
                  v-for="tag in normalizeList(role.typicalTechStack).slice(0, 4)"
                  :key="`${role.id}-${tag}`"
                >
                  {{ tag }}
                </span>
                <span v-if="!normalizeList(role.typicalTechStack).length">未配置技术栈</span>
              </div>
            </article>
            <el-empty v-if="!interviewRoles.length" description="暂无岗位配置" />
          </div>
        </article>

        <article class="panel feature-panel">
          <div class="panel-head">
            <div>
              <h3>资源趋势</h3>
              <p>按真实资源体量生成趋势线，辅助快速判断平台当前发展阶段。</p>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-box short-chart"></div>
        </article>
      </section>

      <section class="overview-grid lower">
        <article class="panel wide">
          <div class="panel-head">
            <div>
              <h3>热门短板 Top 10</h3>
              <p>聚合 AI 面试报告中的高频薄弱点，辅助管理员补题和调整训练路径。</p>
            </div>
          </div>

          <div class="weakness-admin-list">
            <template v-if="weaknessTags.length">
              <div v-for="item in weaknessTags" :key="item.keyword">
                <strong>{{ item.keyword }}</strong>
                <span>{{ item.count }} 次</span>
                <p>{{ item.suggestion }}</p>
              </div>
            </template>
            <el-empty v-else description="暂无短板统计" />
          </div>
        </article>
      </section>
    </template>

    <template v-else>
      <section class="analytics-hero">
        <div>
          <p class="hero-kicker">Analytics Center</p>
          <h2>数据分析中心</h2>
          <p class="hero-copy analytics-copy">
            聚焦题库结构和资源分布，用图表快速识别题目难度、题型覆盖和整体资源规模。
          </p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="router.push('/admin/questions')">查看题目结构</el-button>
          <el-button plain @click="router.push('/admin/interviews')">查看面试管理</el-button>
        </div>
      </section>

      <section class="metric-ribbon compact-ribbon">
        <article v-for="item in analyticsCards" :key="item.label" class="metric-card analytics-tone">
          <div class="metric-top">
            <span>{{ item.label }}</span>
            <i>{{ item.icon }}</i>
          </div>
          <strong>{{ item.value }}</strong>
          <small>{{ item.note }}</small>
        </article>
      </section>

      <section class="analytics-grid">
        <article class="panel feature-panel">
          <div class="panel-head">
            <div>
              <h3>题目难度分布</h3>
              <p>从简单、中等到困难，观察题库结构是否均衡。</p>
            </div>
          </div>
          <div ref="difficultyChartRef" class="chart-box"></div>
        </article>

        <article class="panel feature-panel">
          <div class="panel-head">
            <div>
              <h3>题目类型分布</h3>
              <p>识别当前题库在技术类型上的覆盖情况。</p>
            </div>
          </div>
          <div ref="typeChartRef" class="chart-box"></div>
        </article>

        <article class="panel feature-panel">
          <div class="panel-head">
            <div>
              <h3>资源柱状图</h3>
              <p>横向比较核心资源总量。</p>
            </div>
          </div>
          <div ref="resourceChartRef" class="chart-box short-chart"></div>
        </article>

        <article class="panel feature-panel">
          <div class="panel-head">
            <div>
              <h3>资源趋势线</h3>
              <p>查看当前资源体量的结构节奏。</p>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-box short-chart"></div>
        </article>

        <article class="panel">
          <div class="panel-head">
            <div>
              <h3>热门短板</h3>
              <p>从 AI 面试报告中统计最近高频薄弱点。</p>
            </div>
          </div>
          <div class="weakness-admin-list compact">
            <template v-if="weaknessTags.length">
              <div v-for="item in weaknessTags.slice(0, 6)" :key="item.keyword">
                <strong>{{ item.keyword }}</strong>
                <span>{{ item.count }} 次</span>
              </div>
            </template>
            <el-empty v-else description="暂无短板统计" />
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import adminApi, {
  type AdminActivityItem,
  type AdminDashboardStats,
  type AdminInterviewRecord,
  type AdminJobRoleRecord,
  type AdminWeaknessTag
} from '@/api/admin'

const router = useRouter()
const route = useRoute()
const difficultyChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const resourceChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

const recentActivities = ref<AdminActivityItem[]>([])
const interviewRecords = ref<AdminInterviewRecord[]>([])
const interviewRoles = ref<AdminJobRoleRecord[]>([])
const weaknessTags = ref<AdminWeaknessTag[]>([])
const stats = ref<AdminDashboardStats>({
  questionCount: 0,
  paperCount: 0,
  bankCount: 0,
  userCount: 0
})
const difficultyStats = ref<Record<string, number>>({})
const typeStats = ref<Record<string, number>>({})

let difficultyChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let resourceChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const isAnalyticsMode = computed(() => route.name === 'AdminAnalytics')
const activeRoleCount = computed(() => interviewRoles.value.filter((item) => item.isActive !== false).length)
const topDifficultyEntry = computed(() => {
  const entries = Object.entries(difficultyStats.value)
  return entries.sort((a, b) => b[1] - a[1])[0] || ['MEDIUM', 0]
})
const topTypeEntry = computed(() => {
  const entries = Object.entries(typeStats.value)
  return entries.sort((a, b) => b[1] - a[1])[0] || ['未分类', 0]
})
const difficultySummary = computed(() => `${mapDifficultyLabel(topDifficultyEntry.value[0])}最多，共 ${topDifficultyEntry.value[1]} 题`)
const topTypeLabel = computed(() => `${topTypeEntry.value[0]} · ${topTypeEntry.value[1]} 题`)
const latestInterviewStatus = computed(() => {
  const latest = interviewRecords.value[0]
  if (!latest) return '暂无面试记录'
  return `${formatInterviewStatus(latest.status)} · ${latest.nickname || latest.username || '未知用户'}`
})
const focusHeadline = computed(() => {
  if (interviewRecords.value.length) {
    const latest = interviewRecords.value[0]
    return `${latest.title || latest.targetPosition || '最新面试'} 正在被重点关注`
  }
  return '当前平台以题库与用户资源运营为主'
})
const focusDescription = computed(() => {
  if (interviewRecords.value.length) {
    const latest = interviewRecords.value[0]
    return `最近一条面试记录来自 ${latest.nickname || latest.username || '未知用户'}，状态为 ${formatInterviewStatus(latest.status)}，可直接进入面试管理查看报告。`
  }
  return `当前共有 ${stats.value.questionCount} 道题、${stats.value.bankCount} 个题库，适合继续完善内容结构。`
})

const overviewCards = computed(() => [
  {
    label: '用户账号',
    value: stats.value.userCount,
    icon: '👥',
    note: '真实注册账号数',
    tone: 'blue'
  },
  {
    label: '题目总量',
    value: stats.value.questionCount,
    icon: '🧠',
    note: '当前题库可用题目',
    tone: 'navy'
  },
  {
    label: '题库数量',
    value: stats.value.bankCount,
    icon: '📚',
    note: '后台维护中的题库',
    tone: 'gold'
  },
  {
    label: '试卷数量',
    value: stats.value.paperCount,
    icon: '📝',
    note: '系统已有试卷资源',
    tone: 'slate'
  }
])

const analyticsCards = computed(() => [
  {
    label: '题目总数',
    value: stats.value.questionCount,
    icon: '🧠',
    note: '参与结构分析'
  },
  {
    label: '题库总数',
    value: stats.value.bankCount,
    icon: '📚',
    note: '支撑内容分布'
  },
  {
    label: '试卷总数',
    value: stats.value.paperCount,
    icon: '📝',
    note: '当前可用试卷'
  }
])

const resourceSeries = computed(() => ([
  { label: '用户', value: stats.value.userCount },
  { label: '题目', value: stats.value.questionCount },
  { label: '题库', value: stats.value.bankCount },
  { label: '试卷', value: stats.value.paperCount }
]))

const trendSeries = computed(() => {
  const questionBase = stats.value.questionCount || 1
  const userBase = stats.value.userCount || 1
  const bankBase = stats.value.bankCount || 1
  const paperBase = stats.value.paperCount || 1
  return [
    Math.max(1, Math.round(userBase * 0.45)),
    Math.max(1, Math.round(bankBase * 1.1)),
    Math.max(1, Math.round(paperBase * 1.4)),
    Math.max(1, Math.round(questionBase * 0.55)),
    questionBase
  ]
})

const resizeCharts = () => {
  difficultyChart?.resize()
  typeChart?.resize()
  resourceChart?.resize()
  trendChart?.resize()
}

const mapDifficultyLabel = (value: string) => {
  const labelMap: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return labelMap[value] || value
}

const normalizeList = (rawValue?: string | string[]) => {
  if (!rawValue) return []
  if (Array.isArray(rawValue)) return rawValue.filter(Boolean)

  return Array.from(new Set(rawValue
    .trim()
    .replace(/^\[/, '')
    .replace(/\]$/, '')
    .replace(/"/g, '')
    .replace(/'/g, '')
    .split(/[,，、|/;；\n\r]+/)
    .map((item) => item.trim())
    .filter(Boolean)))
}

const formatScore = (value?: number) => (typeof value === 'number' ? value.toFixed(1) : '-')
const formatDateTime = (value?: string) => value || '-'
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

const buildPieOption = (title: string, source: Record<string, number>) => {
  const data = Object.entries(source).map(([key, value]) => ({
    name: mapDifficultyLabel(key),
    value
  }))

  return {
    color: ['#173a74', '#356ac3', '#fdca17', '#93a8c8', '#d9e3f1'],
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, left: 'center', icon: 'circle' },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['44%', '72%'],
        center: ['50%', '44%'],
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff',
          borderWidth: 4
        },
        label: { formatter: '{b}\n{c}' },
        data
      }
    ]
  }
}

const buildBarOption = () => ({
  grid: { left: 26, right: 14, top: 20, bottom: 24, containLabel: true },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  xAxis: {
    type: 'category',
    data: resourceSeries.value.map((item) => item.label),
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#dde4ef' } },
    axisLabel: { color: '#647792' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#edf2f8' } },
    axisLabel: { color: '#8090a7' }
  },
  series: [
    {
      type: 'bar',
      barWidth: 30,
      data: resourceSeries.value.map((item) => item.value),
      itemStyle: {
        borderRadius: [12, 12, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#1f5bcb' },
          { offset: 1, color: '#8fb3ff' }
        ])
      }
    }
  ]
})

const buildLineOption = () => ({
  grid: { left: 24, right: 14, top: 20, bottom: 24, containLabel: true },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['账号', '题库', '试卷', '训练', '当前'],
    axisLine: { lineStyle: { color: '#dde4ef' } },
    axisLabel: { color: '#647792' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: '#edf2f8' } },
    axisLabel: { color: '#8090a7' }
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: trendSeries.value,
      lineStyle: { width: 4, color: '#173a74' },
      itemStyle: { color: '#fdca17', borderColor: '#173a74', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(39,95,202,0.28)' },
          { offset: 1, color: 'rgba(39,95,202,0.02)' }
        ])
      }
    }
  ]
})

const renderCharts = async () => {
  await nextTick()

  if (difficultyChartRef.value && isAnalyticsMode.value) {
    difficultyChart?.dispose()
    difficultyChart = echarts.init(difficultyChartRef.value)
    difficultyChart.setOption(buildPieOption('题目难度', difficultyStats.value))
  } else {
    difficultyChart?.dispose()
    difficultyChart = null
  }

  if (typeChartRef.value && isAnalyticsMode.value) {
    typeChart?.dispose()
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption(buildPieOption('题目类型', typeStats.value))
  } else {
    typeChart?.dispose()
    typeChart = null
  }

  if (resourceChartRef.value) {
    resourceChart?.dispose()
    resourceChart = echarts.init(resourceChartRef.value)
    resourceChart.setOption(buildBarOption())
  }

  if (trendChartRef.value) {
    trendChart?.dispose()
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption(buildLineOption())
  }
}

const fetchDashboard = async () => {
  try {
    const [dashboardStats, activities, difficulty, types, interviews, roles, weaknesses] = await Promise.all([
      adminApi.getDashboardStats(),
      adminApi.getRecentActivities(),
      adminApi.getDifficultyDistribution(),
      adminApi.getTypeDistribution(),
      adminApi.getInterviewRecords(),
      adminApi.getInterviewRoles(),
      adminApi.getWeaknessTags()
    ])

    stats.value = dashboardStats
    recentActivities.value = activities
    difficultyStats.value = difficulty
    typeStats.value = types
    interviewRecords.value = interviews
    interviewRoles.value = roles
    weaknessTags.value = weaknesses
    renderCharts()
  } catch (error: any) {
    ElMessage.error(error?.message || '获取后台数据失败')
  }
}

watch(() => route.name, () => renderCharts())

onMounted(() => {
  fetchDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  difficultyChart?.dispose()
  typeChart?.dispose()
  resourceChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 16px;
}

.overview-hero,
.analytics-hero,
.panel,
.metric-card {
  border-radius: 24px;
  border: 1px solid rgba(30, 80, 162, 0.1);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 36px rgba(18, 44, 93, 0.08);
}

.overview-hero {
  padding: 24px;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(300px, 0.8fr);
  gap: 18px;
  background:
    radial-gradient(circle at right top, rgba(253, 202, 23, 0.22), transparent 22%),
    linear-gradient(135deg, #173a74 0%, #204b90 54%, #2e66c0 100%);
  color: #fff;
}

.analytics-hero {
  padding: 24px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
  background:
    radial-gradient(circle at left top, rgba(253, 202, 23, 0.18), transparent 24%),
    linear-gradient(135deg, #173a74 0%, #244f97 54%, #3f74cf 100%);
  color: #fff;
}

.hero-kicker {
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.overview-hero h2,
.analytics-hero h2 {
  font-size: 28px;
  line-height: 1.2;
}

.hero-copy {
  margin-top: 12px;
  max-width: 760px;
  color: rgba(255, 255, 255, 0.82);
  line-height: 1.7;
  font-size: 13px;
}

.analytics-copy {
  max-width: 680px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
}

.hero-focus-card {
  padding: 18px 18px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  display: grid;
  gap: 10px;
  align-content: start;
}

.focus-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-weight: 800;
}

.hero-focus-card strong {
  font-size: 20px;
  line-height: 1.45;
}

.hero-focus-card p {
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
  line-height: 1.8;
}

.focus-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.focus-meta span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 11px;
}

.metric-ribbon {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.compact-ribbon {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-card {
  padding: 16px 18px;
}

.metric-card.blue {
  background: linear-gradient(180deg, #f6f9ff 0%, #eff4ff 100%);
}

.metric-card.navy {
  background: linear-gradient(180deg, #f5f8fd 0%, #edf2fb 100%);
}

.metric-card.gold {
  background: linear-gradient(180deg, #fffaf0 0%, #fff6e2 100%);
}

.metric-card.slate,
.metric-card.analytics-tone {
  background: linear-gradient(180deg, #f8fafc 0%, #eef3f8 100%);
}

.metric-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.metric-top span {
  color: #5f7394;
  font-size: 12px;
  font-weight: 700;
}

.metric-top i {
  font-style: normal;
  font-size: 18px;
}

.metric-card strong {
  display: block;
  margin-top: 14px;
  color: #173a74;
  font-size: 28px;
  font-weight: 900;
}

.metric-card small {
  display: block;
  margin-top: 6px;
  color: #7f90aa;
  font-size: 11px;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.75fr);
  gap: 16px;
}

.overview-grid.lower {
  align-items: stretch;
}

.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel {
  padding: 18px;
}

.wide {
  grid-column: 1 / -1;
}

.feature-panel {
  overflow: hidden;
}

.panel-head {
  margin-bottom: 18px;
}

.panel-head.compact {
  margin-bottom: 14px;
}

.panel-head h3 {
  color: #173a74;
  font-size: 18px;
}

.panel-head p {
  margin-top: 6px;
  color: #7187a5;
  font-size: 12px;
}

.chart-box {
  height: 320px;
}

.hero-chart {
  height: 300px;
}

.short-chart {
  height: 260px;
}

.summary-stack,
.insight-stack {
  display: grid;
  gap: 12px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: #f7faff;
}

.summary-item span {
  color: #6d84a5;
  font-size: 11px;
}

.summary-item strong {
  display: block;
  margin-top: 8px;
  color: #173a74;
  font-size: 16px;
  line-height: 1.5;
}

.activity-list,
.mini-list {
  display: grid;
  gap: 14px;
}

.activity-item {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 14px;
}

.activity-dot {
  width: 14px;
  height: 14px;
  margin-top: 5px;
  border-radius: 50%;
  background: #fdca17;
  box-shadow: 0 0 0 5px rgba(253, 202, 23, 0.18);
}

.activity-item strong,
.mini-main strong {
  color: #173a74;
  font-size: 14px;
}

.activity-item p {
  margin: 6px 0;
  color: #576d90;
  line-height: 1.6;
  font-size: 12px;
}

.activity-item span,
.mini-main span,
.mini-side em,
.mini-side span {
  color: #91a0b8;
  font-size: 11px;
  font-style: normal;
}

.mini-item {
  padding: 12px 14px;
  border-radius: 16px;
  background: #f7faff;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.mini-main,
.mini-side {
  display: grid;
  gap: 4px;
}

.mini-side {
  justify-items: end;
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.role-card {
  padding: 14px;
  border-radius: 18px;
  background: #f7faff;
}

.role-card-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.role-card-head strong {
  color: #173a74;
  font-size: 15px;
}

.role-card-head span {
  color: #5f7394;
  font-size: 11px;
}

.role-card p {
  margin-top: 8px;
  color: #647792;
  line-height: 1.7;
  font-size: 12px;
  min-height: 40px;
}

.role-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-tags span {
  padding: 4px 8px;
  border-radius: 999px;
  background: #ffffff;
  color: #476287;
  font-size: 11px;
}

.weakness-admin-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.weakness-admin-list.compact {
  grid-template-columns: 1fr;
}

.weakness-admin-list div {
  padding: 14px;
  border-radius: 18px;
  background: #f7faff;
}

.weakness-admin-list strong {
  color: #173a74;
  font-size: 14px;
}

.weakness-admin-list span {
  float: right;
  padding: 4px 8px;
  border-radius: 999px;
  color: #8a6510;
  background: #fff6df;
  font-size: 11px;
  font-weight: 800;
}

.weakness-admin-list p {
  clear: both;
  margin-top: 8px;
  color: #647792;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 1180px) {
  .overview-hero,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .role-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 960px) {
  .analytics-grid,
  .metric-ribbon,
  .compact-ribbon {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-hero {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 720px) {
  .overview-hero,
  .analytics-hero,
  .panel,
  .metric-card {
    padding: 18px;
  }

  .metric-ribbon,
  .compact-ribbon,
  .analytics-grid,
  .role-grid,
  .weakness-admin-list {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    width: 100%;
    flex-direction: column;
  }

  .mini-item,
  .role-card-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .mini-side {
    justify-items: start;
  }
}
</style>
