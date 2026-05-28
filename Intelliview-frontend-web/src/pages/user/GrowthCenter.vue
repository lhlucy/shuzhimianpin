<template>
  <div class="growth-page">
    <aside class="app-sidebar">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="side-nav" aria-label="成长中心导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/growth' }">
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <main class="growth-main">
      <section class="growth-hero">
        <div>
          <span>Growth Center</span>
          <h1>成长中心</h1>
          <p>勾选一次或多次面试记录，系统会基于每场报告里的五个能力维度生成趋势线，并把短板建议追溯到具体题目证据。</p>
        </div>
        <div class="hero-stat">
          <strong>{{ selectedCompletedRecords.length }}</strong>
          <span>已纳入曲线</span>
        </div>
      </section>

      <section class="workspace">
        <article class="record-panel">
          <div class="panel-head">
            <div>
              <h2>面试记录</h2>
              <p>选择已完成并生成报告的记录</p>
            </div>
            <button type="button" @click="selectRecentCompleted">选择最近 5 场</button>
          </div>

          <div v-if="loading" class="loading-box">
            <el-skeleton :rows="8" animated />
          </div>
          <div v-else class="record-list">
            <label v-for="item in completedRecords" :key="item.interviewId" class="record-row" :class="{ checked: selectedIds.includes(item.interviewId) }">
              <input v-model="selectedIds" type="checkbox" :value="item.interviewId" />
              <div>
                <strong>{{ item.title || item.targetPosition || '模拟面试' }}</strong>
                <span>{{ formatDateTime(item.endedAt || item.createdAt) }} · {{ item.targetPosition || '-' }}</span>
              </div>
              <em>{{ Math.round(item.totalScore || 0) }}</em>
            </label>
            <el-empty v-if="!completedRecords.length" description="暂无已完成面试报告" />
          </div>
        </article>

        <article class="chart-panel">
          <div class="panel-head">
            <div>
              <h2>五维成长曲线</h2>
              <p>技术深度、项目匹配、问题分析、表达清晰、岗位匹配分别成线</p>
            </div>
            <div class="chart-tools">
              <span>{{ chartPoints.length }} 个样本</span>
              <div class="view-switch" aria-label="成长曲线展示方式">
                <button type="button" :class="{ active: chartView === 'line' }" @click="chartView = 'line'">趋势图</button>
                <button type="button" :class="{ active: chartView === 'table' }" @click="chartView = 'table'">数据表</button>
              </div>
            </div>
          </div>

          <div v-if="selectedIds.length < 1" class="empty-chart">
            <strong>先选择一场面试</strong>
            <p>选择多场后会更清楚地看到每个维度的上升、波动与回落。</p>
          </div>
          <div v-else-if="summaryLoading" class="empty-chart">
            <strong>正在生成成长曲线</strong>
            <p>正在读取所选面试报告的五维评分。</p>
          </div>
          <div v-else-if="chartView === 'line'" ref="lineChartRef" class="line-chart" aria-label="五维成长趋势图"></div>
          <div v-else class="score-table-wrap">
            <table class="score-table">
              <thead>
                <tr>
                  <th>面试</th>
                  <th v-for="dimension in DIMENSIONS" :key="dimension.key">{{ dimension.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in scoreRows" :key="row.id">
                  <td>
                    <strong>{{ row.title }}</strong>
                    <span>{{ row.time }}</span>
                  </td>
                  <td v-for="score in row.scores" :key="score.label">
                    <i :style="{ color: score.color }">{{ score.value }}</i>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>
      </section>

      <section class="report-panel">
        <div class="panel-head report-head">
          <div>
            <h2>成长报告</h2>
            <p>每条短板都会标明来自哪场面试、哪道题、为什么需要解决。</p>
          </div>
          <router-link class="start-interview-action" to="/user/interview/ai/create">
            <el-icon><Monitor /></el-icon>
            <span>
              <strong>开始新面试</strong>
              <small>补充新的成长样本</small>
            </span>
          </router-link>
        </div>

        <div class="insight-grid">
          <article
            v-for="item in weaknessInsights"
            :key="`${item.dimension}-${item.interviewId}-${item.questionId}`"
            class="insight-card"
            tabindex="0"
            role="link"
            @click="openReport(item.interviewId)"
            @keydown.enter.prevent="openReport(item.interviewId)"
          >
            <div class="insight-top">
              <span>{{ item.dimension }}</span>
              <strong>{{ item.score }}</strong>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.reason }}</p>
            <div class="evidence-box">
              <span>证据</span>
              <p>{{ item.evidence }}</p>
            </div>
            <div class="evidence-box action">
              <span>为什么要解决</span>
              <p>{{ item.solutionReason }}</p>
            </div>
            <button type="button">查看对应报告</button>
          </article>

          <article v-if="!weaknessInsights.length" class="empty-report">
            <strong>{{ selectedIds.length ? '这组面试暂未发现明显短板' : '等待选择面试记录' }}</strong>
            <p>{{ selectedIds.length ? '可以继续增加面试样本，让趋势判断更稳定。' : '勾选面试记录后，这里会生成带证据的成长建议。' }}</p>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Collection, House, Monitor, Star, TrendCharts, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import aiInterviewApi, { type AIInterviewHistoryItem, type AIInterviewSummary } from '@/api/aiInterview'

echarts.use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const STORAGE_KEY = 'growth_curve_interview_ids'
const DIMENSIONS = [
  { key: 'technicalDepth', label: '技术深度', color: '#e85d3f' },
  { key: 'projectRelevance', label: '项目匹配', color: '#2f6fdb' },
  { key: 'problemSolving', label: '问题分析', color: '#199b6a' },
  { key: 'communicationClarity', label: '表达清晰', color: '#b07a10' },
  { key: 'jobMatch', label: '岗位匹配', color: '#7a54c9' }
]

const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '成长中心', path: '/user/growth', icon: TrendCharts },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const loading = ref(false)
const summaryLoading = ref(false)
const chartView = ref<'line' | 'table'>('line')
const records = ref<AIInterviewHistoryItem[]>([])
const selectedIds = ref<number[]>([])
const summaries = ref<Record<number, AIInterviewSummary>>({})
const lineChartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
let summaryLoadSeq = 0
const router = useRouter()

const completedRecords = computed(() => records.value.filter((item) => item.status === 'COMPLETED'))
const selectedCompletedRecords = computed(() => completedRecords.value.filter((item) => selectedIds.value.includes(item.interviewId)))
const chartPoints = computed(() => selectedCompletedRecords.value
  .slice()
  .sort((a, b) => new Date(a.endedAt || a.createdAt).getTime() - new Date(b.endedAt || b.createdAt).getTime())
  .filter((item) => summaries.value[item.interviewId])
)

const scoreRows = computed(() => chartPoints.value.map((item) => {
  const summary = summaries.value[item.interviewId]
  return {
    id: item.interviewId,
    title: item.title || item.targetPosition || '模拟面试',
    time: formatDateTime(item.endedAt || item.createdAt),
    scores: DIMENSIONS.map((dimension) => ({
      label: dimension.label,
      color: dimension.color,
      value: Math.round(Number(summary?.dimensionScores?.[dimension.key] || 0))
    }))
  }
}))

const weaknessInsights = computed(() => {
  const insights: Array<{
    interviewId: number
    questionId: number
    dimension: string
    score: number
    title: string
    reason: string
    evidence: string
    solutionReason: string
  }> = []

  for (const record of chartPoints.value) {
    const summary = summaries.value[record.interviewId]
    for (const review of summary.questionReviews || []) {
      const weakDimension = DIMENSIONS
        .map((dimension) => ({ ...dimension, score: Number(review.dimensionScores?.[dimension.key] ?? 100) }))
        .sort((a, b) => a.score - b.score)[0]
      const missing = (review.missingKeywords || []).slice(0, 3).join('、')
      const weakness = (review.weaknesses || [])[0]
      if (!weakDimension || weakDimension.score >= 70 || (!missing && !weakness)) continue
      insights.push({
        interviewId: record.interviewId,
        questionId: review.questionId,
        dimension: weakDimension.label,
        score: Math.round(weakDimension.score),
        title: `${record.title || record.targetPosition || '模拟面试'} · 第 ${review.questionOrder || '-'} 题`,
        reason: weakness || `${weakDimension.label}得分偏低，回答中缺少关键支撑信息。`,
        evidence: `${review.questionContent || '题目内容暂无记录'}；${missing ? `缺失关键词：${missing}` : (review.feedbackSummary || '报告指出该题存在补充空间。')}`,
        solutionReason: buildSolutionReason(weakDimension.key, record.targetPosition)
      })
    }
  }

  return insights.sort((a, b) => a.score - b.score).slice(0, 8)
})

function buildSolutionReason(key: string, role?: string) {
  const roleText = role || '目标岗位'
  const map: Record<string, string> = {
    technicalDepth: `${roleText}面试会追问原理、边界和取舍，技术深度不足会让答案停留在使用层。`,
    projectRelevance: `项目匹配弱会让面试官难以判断经历是否能迁移到${roleText}的真实工作场景。`,
    problemSolving: '问题分析不足会影响复杂题的拆解质量，容易只给结论而缺少路径。',
    communicationClarity: '表达不清会降低高质量内容的可感知度，建议使用背景、行动、结果的结构回答。',
    jobMatch: `岗位匹配不足会削弱回答和${roleText}要求之间的连接，需要主动映射职责和能力。`
  }
  return map[key] || '该短板会影响面试官对能力稳定性的判断，建议结合原报告专项补强。'
}

function formatDateTime(value?: string) {
  if (!value) return '暂无时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function readStoredIds() {
  try {
    const value = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    selectedIds.value = Array.isArray(value) ? value.map(Number).filter(Boolean) : []
  } catch {
    selectedIds.value = []
  }
}

function persistSelectedIds() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(selectedIds.value))
}

function selectRecentCompleted() {
  selectedIds.value = completedRecords.value.slice(0, 5).map((item) => item.interviewId)
}

async function loadSummaries() {
  const seq = ++summaryLoadSeq
  const currentIds = [...selectedIds.value]
  if (!currentIds.length) {
    summaryLoading.value = false
    chart?.dispose()
    chart = null
    return
  }

  const missingIds = currentIds.filter((id) => !summaries.value[id])
  if (!missingIds.length) {
    summaryLoading.value = false
    await renderChart()
    return
  }
  summaryLoading.value = true
  try {
    const results = await Promise.allSettled(missingIds.map((id) => aiInterviewApi.getSummary(id)))
    if (seq !== summaryLoadSeq) return
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') {
        summaries.value[missingIds[index]] = result.value
      }
    })
    await renderChart()
  } finally {
    if (seq === summaryLoadSeq) {
      summaryLoading.value = false
    }
  }
}

async function renderChart() {
  await nextTick()
  if (chartView.value !== 'line') return
  if (!selectedIds.value.length) {
    chart?.dispose()
    chart = null
    return
  }
  if (!lineChartRef.value) return
  await new Promise<void>((resolve) => requestAnimationFrame(() => resolve()))
  if (!lineChartRef.value) return
  const bounds = lineChartRef.value.getBoundingClientRect()
  if (bounds.width < 20 || bounds.height < 20) {
    window.setTimeout(() => renderChart(), 80)
    return
  }
  if (!chart) chart = echarts.init(lineChartRef.value)
  const labels = chartPoints.value.map((item, index) => `${index + 1}. ${formatDateTime(item.endedAt || item.createdAt)}`)
  chart.setOption({
    color: DIMENSIONS.map((item) => item.color),
    tooltip: { trigger: 'axis' },
    legend: { top: 0, icon: 'roundRect', textStyle: { color: '#435064', fontWeight: 700 } },
    grid: { left: 36, right: 24, top: 54, bottom: 34 },
    xAxis: { type: 'category', boundaryGap: false, data: labels, axisLabel: { color: '#788397' } },
    yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { color: '#edf1f6' } }, axisLabel: { color: '#788397' } },
    series: DIMENSIONS.map((dimension) => ({
      name: dimension.label,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      lineStyle: { width: 3 },
      emphasis: { focus: 'series' },
      data: chartPoints.value.map((item) => Math.round(Number(summaries.value[item.interviewId]?.dimensionScores?.[dimension.key] || 0)))
    }))
  })
  chart.resize()
}

function openReport(interviewId: number) {
  router.push(`/user/interview/ai/session/${interviewId}?report=1`)
}

async function loadPage() {
  loading.value = true
  try {
    readStoredIds()
    records.value = await aiInterviewApi.getInterviewHistory(100)
    if (!selectedIds.value.length) {
      selectedIds.value = completedRecords.value.slice(0, 3).map((item) => item.interviewId)
    }
    await loadSummaries()
  } finally {
    loading.value = false
  }
}

watch(selectedIds, () => {
  persistSelectedIds()
  loadSummaries()
}, { deep: true })

watch(chartView, async (value) => {
  if (value === 'line') {
    await renderChart()
    return
  }
  chart?.dispose()
  chart = null
})

watch(
  [chartPoints, summaryLoading],
  async () => {
    if (!summaryLoading.value && chartView.value === 'line') {
      await renderChart()
    }
  },
  { deep: true, flush: 'post' }
)

function resizeChart() {
  chart?.resize()
}

onMounted(() => {
  loadPage()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<style scoped>
.growth-page {
  min-height: 100vh;
  background: #f5f7fa;
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
.side-nav a,
.report-panel a {
  text-decoration: none;
}

.brand {
  height: 66px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand span {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 7px;
  background: #e85d3f;
  color: #ffffff;
  font-size: 12px;
  font-weight: 900;
}

.brand strong,
.panel-head h2,
.growth-hero h1,
.record-row strong,
.insight-card h3 {
  color: #252936;
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
  color: #e85d3f;
  background: #fff0eb;
}

.growth-main {
  margin-left: 200px;
  padding: 30px 30px 72px;
}

.growth-hero {
  min-height: 160px;
  padding: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  border-radius: 12px;
  background:
    linear-gradient(135deg, rgba(232, 93, 63, 0.95), rgba(40, 91, 160, 0.92)),
    #e85d3f;
  color: #ffffff;
}

.growth-hero span,
.growth-hero p {
  color: rgba(255, 255, 255, 0.78);
}

.growth-hero h1 {
  margin-top: 8px;
  color: #ffffff;
  font-size: 32px;
}

.growth-hero p {
  margin-top: 12px;
  max-width: 720px;
  line-height: 1.8;
}

.hero-stat {
  min-width: 150px;
  padding: 22px;
  display: grid;
  justify-items: center;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
}

.hero-stat strong {
  font-size: 46px;
  line-height: 1;
}

.workspace {
  margin-top: 20px;
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 20px;
}

.record-panel,
.chart-panel,
.report-panel {
  border: 1px solid #e7ecf4;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 12px 26px rgba(27, 36, 56, 0.035);
}

.record-panel,
.chart-panel,
.report-panel {
  padding: 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.panel-head p {
  margin-top: 6px;
  color: #8d98aa;
  font-size: 13px;
}

.panel-head button,
.panel-head > a {
  height: 34px;
  padding: 0 12px;
  border: none;
  border-radius: 7px;
  color: #ffffff;
  background: #e85d3f;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.report-head {
  align-items: stretch;
  padding-bottom: 16px;
  border-bottom: 1px solid #edf1f6;
}

.start-interview-action {
  min-width: 190px;
  height: 56px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  border-radius: 10px;
  color: #ffffff;
  background: linear-gradient(135deg, #e85d3f, #f28a4b);
  box-shadow: 0 12px 24px rgba(232, 93, 63, 0.2);
}

.start-interview-action .el-icon {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 17px;
}

.start-interview-action span {
  display: grid;
  gap: 3px;
}

.start-interview-action strong {
  color: #ffffff;
  font-size: 14px;
  line-height: 1;
}

.start-interview-action small {
  color: rgba(255, 255, 255, 0.76);
  font-size: 12px;
  line-height: 1;
}

.panel-head > span {
  color: #e85d3f;
  font-size: 13px;
  font-weight: 900;
}

.chart-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: none;
}

.chart-tools > span {
  color: #e85d3f;
  font-size: 13px;
  font-weight: 900;
}

.view-switch {
  height: 34px;
  padding: 3px;
  display: inline-flex;
  gap: 3px;
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  background: #f6f8fb;
}

.panel-head .view-switch button {
  height: 26px;
  min-width: 58px;
  padding: 0 9px;
  border-radius: 6px;
  color: #697588;
  background: transparent;
}

.panel-head .view-switch button.active {
  color: #ffffff;
  background: #285ba0;
}

.record-list {
  margin-top: 18px;
  max-height: 520px;
  overflow: auto;
  display: grid;
  gap: 10px;
}

.record-row {
  padding: 14px;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) 42px;
  align-items: center;
  gap: 12px;
  border: 1px solid #edf1f6;
  border-radius: 9px;
  cursor: pointer;
}

.record-row.checked {
  border-color: rgba(232, 93, 63, 0.42);
  background: #fff4ef;
}

.record-row span {
  margin-top: 5px;
  display: block;
  color: #8d98aa;
  font-size: 12px;
}

.record-row em {
  font-style: normal;
  color: #e85d3f;
  font-size: 20px;
  font-weight: 900;
}

.line-chart {
  width: 100%;
  height: 520px;
}

.score-table-wrap {
  margin-top: 18px;
  max-height: 520px;
  overflow: auto;
  border: 1px solid #e7ecf4;
  border-radius: 10px;
}

.score-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}

.score-table th,
.score-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #edf1f6;
  text-align: left;
}

.score-table th {
  color: #748095;
  background: #f7f9fc;
  font-size: 12px;
  font-weight: 900;
}

.score-table td {
  color: #252936;
  font-size: 13px;
}

.score-table td strong {
  display: block;
  max-width: 220px;
  overflow: hidden;
  color: #252936;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.score-table td span {
  margin-top: 5px;
  display: block;
  color: #8d98aa;
  font-size: 12px;
}

.score-table td i {
  font-style: normal;
  font-size: 18px;
  font-weight: 900;
}

.empty-chart,
.empty-report {
  min-height: 360px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #8d98aa;
  text-align: center;
}

.empty-chart strong,
.empty-report strong {
  color: #252936;
}

.report-panel {
  margin-top: 20px;
}

.insight-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.insight-card {
  padding: 18px;
  display: grid;
  gap: 12px;
  border: 1px solid #e7ecf4;
  border-radius: 10px;
  background: #fbfcfe;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.insight-card:hover,
.insight-card:focus-visible {
  border-color: rgba(232, 93, 63, 0.38);
  box-shadow: 0 14px 28px rgba(27, 36, 56, 0.08);
  outline: none;
  transform: translateY(-1px);
}

.insight-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.insight-top span {
  padding: 5px 9px;
  border-radius: 999px;
  color: #285ba0;
  background: #eaf1fb;
  font-size: 12px;
  font-weight: 900;
}

.insight-top strong {
  color: #e85d3f;
  font-size: 28px;
}

.insight-card p {
  color: #626d7e;
  line-height: 1.75;
  font-size: 13px;
}

.evidence-box {
  padding: 12px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #edf1f6;
}

.evidence-box span {
  display: block;
  margin-bottom: 6px;
  color: #8d98aa;
  font-size: 12px;
  font-weight: 900;
}

.evidence-box.action {
  background: #fff8ed;
}

.insight-card button {
  justify-self: start;
  border: none;
  background: transparent;
  color: #e85d3f;
  padding: 0;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.empty-report {
  grid-column: 1 / -1;
  border: 1px dashed #dbe2ed;
  border-radius: 10px;
}

@media (max-width: 980px) {
  .app-sidebar {
    position: static;
    width: 100%;
  }

  .growth-main {
    margin-left: 0;
  }

  .workspace,
  .insight-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .growth-main {
    padding: 18px;
  }

  .growth-hero,
  .panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .start-interview-action {
    width: 100%;
  }

  .chart-tools {
    align-items: flex-start;
    flex-direction: column;
  }

  .line-chart {
    height: 420px;
  }
}
</style>
