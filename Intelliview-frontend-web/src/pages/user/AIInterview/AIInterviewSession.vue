<template>
  <div class="session-page" :class="themeClass">
    <aside class="interviewer-panel">
      <nav class="left-tabs" aria-label="面试侧栏导航">
        <button type="button" :class="{ active: activeLeftPanel === 'interviewer' }" @click="activeLeftPanel = 'interviewer'">
          <el-icon><Service /></el-icon>
          面试信息
        </button>
        <button type="button" :class="{ active: activeLeftPanel === 'resume' }" @click="activeLeftPanel = 'resume'">
          <el-icon><Document /></el-icon>
          简历展示
        </button>
      </nav>

      <section v-show="activeLeftPanel === 'interviewer'" class="left-panel-section">
        <div class="avatar-stage">
          <AvatarDigitalHumanPlayer
            :session="avatarSession"
            :loading="avatarLoading"
            :error="avatarError"
            @error="handleAvatarPlayerError"
          />
        </div>
        <h1>AI 面试官</h1>
        <p>数智面聘 · {{ currentTechLabel }}</p>
        <span class="avatar-status" :class="{ online: avatarSession?.connected && !avatarError }">
          {{ avatarStatusText }}
        </span>

        <div class="interviewer-action-row">
          <button type="button" class="end-btn" :disabled="ending || !session" @click="confirmEndInterview">
            <el-icon><Close /></el-icon>
            结束面试
          </button>

          <button type="button" class="back-side-btn" @click="goBack">
            <el-icon><Back /></el-icon>
            {{ reportMode ? '返回历史记录' : '返回上一页' }}
          </button>
        </div>
      </section>

      <section v-show="activeLeftPanel === 'resume'" class="left-panel-section resume-panel">
        <div class="resume-head">
          <span class="resume-icon"><el-icon><Document /></el-icon></span>
          <div>
            <h1>简历展示</h1>
            <p>{{ sessionResume?.originalFileName || session?.resumeFileName || '本场面试未选择简历' }}</p>
          </div>
        </div>

        <article v-if="resumeLoading" class="resume-state">
          <span class="loader"></span>
          <strong>正在载入简历...</strong>
        </article>

        <article v-else-if="sessionResume" class="resume-readonly">
          <div class="resume-meta">
            <span>岗位：{{ sessionResume.intentionJob || '-' }}</span>
            <span>类型：{{ sessionResume.recruitmentType || '-' }}</span>
            <span>城市：{{ sessionResume.intentionCity || '-' }}</span>
            <span>薪资：{{ sessionResume.expectedSalary || '-' }}</span>
          </div>
          <div class="resume-summary">
            <strong>简历摘要</strong>
            <p>{{ sessionResume.summary || '暂无摘要' }}</p>
          </div>
          <div class="resume-content">
            <strong>解析内容</strong>
            <pre>{{ sessionResume.content || '暂无可展示的解析内容' }}</pre>
          </div>
        </article>

        <article v-else class="resume-state">
          <el-icon><Document /></el-icon>
          <strong>{{ session?.resumeFileName ? '未找到对应简历' : '暂无简历' }}</strong>
          <p>{{ session?.resumeFileName ? '该简历可能已被删除，当前仅保留文件名。' : '创建面试时未绑定简历。' }}</p>
        </article>

        <button type="button" class="back-side-btn" @click="goBack">
          <el-icon><Back /></el-icon>
          {{ reportMode ? '返回历史记录' : '返回上一页' }}
        </button>
      </section>
    </aside>

    <main class="dialog-panel">
      <header class="dialog-head">
        <div class="head-title">
          <h2>{{ activeMainTab === 'report' ? '面试报告' : '面试对话' }}</h2>
        </div>
        <div class="head-actions">
          <template v-if="activeLeftPanel === 'interviewer'">
            <span class="head-chip">{{ session?.targetPosition || '加载中' }}</span>
            <span class="head-chip">{{ currentTechLabel }}</span>
            <span class="head-chip time">{{ elapsedLabel }}</span>
            <span class="head-chip">第 {{ currentOrder }} / {{ session?.questionCount || 0 }} 题</span>
          </template>
          <button type="button" class="theme-toggle" @click="toggleTheme">
            <el-icon><component :is="isDark ? Sunny : Moon" /></el-icon>
            {{ isDark ? '浅色' : '深色' }}
          </button>
          <span :class="{ done: finished }">{{ activeMainTab === 'report' ? '报告查看中' : (finished ? '已结束' : '面试进行中') }}</span>
        </div>
      </header>

      <nav class="content-tabs" aria-label="面试主内容切换">
        <button type="button" :class="{ active: activeMainTab === 'dialogue' }" @click="switchMainTab('dialogue')">对话页面</button>
        <button
          type="button"
          :class="{ active: activeMainTab === 'report' }"
          :disabled="!canViewReport"
          @click="switchMainTab('report')"
        >
          报告页面
        </button>
      </nav>

      <template v-if="activeMainTab === 'dialogue'">
        <section class="messages" ref="messagesRef">
          <article v-if="loadingSession" class="loading-card">
            <span class="loader"></span>
            <strong>正在载入 AI 面试...</strong>
          </article>

          <article v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <span v-if="message.role === 'ai'" class="bot-icon"><el-icon><Service /></el-icon></span>
            <div :class="message.role === 'ai' ? 'bubble' : 'user-bubble'">
              <strong v-if="message.role === 'ai'">{{ message.title }}</strong>
              <p>{{ message.content }}</p>
            </div>
            <span v-if="message.role === 'user'" class="user-avatar">我</span>
          </article>

          <article v-if="waitingAI" class="message ai">
            <span class="bot-icon"><el-icon><Service /></el-icon></span>
            <div class="typing"><i></i><i></i><i></i><span>AI 面试官正在判断回答...</span></div>
          </article>

          <article v-if="finished && !waitingAI" class="finish-card">
            <strong>面试已结束</strong>
            <p>报告已经生成，可切换到上方“报告页面”查看完整复盘。</p>
          </article>
        </section>

        <footer class="input-shell">
          <div v-if="recording || transcribing" class="voice-live-card" :class="{ recording, transcribing }">
            <div class="voice-live-head">
              <div class="voice-wave" :class="{ active: recording }">
                <i></i>
                <i></i>
                <i></i>
                <i></i>
              </div>
              <div class="voice-live-copy">
                <strong>{{ recording ? '正在语音输入' : '正在整理语音内容' }}</strong>
                <span>{{ recognitionSupported ? '识别内容会实时显示' : '当前浏览器不支持实时识别，停止后会自动转写' }}</span>
              </div>
            </div>
            <p>{{ voiceDisplayText }}</p>
          </div>

          <div class="input-bar">
            <el-input v-model="draft" :disabled="inputDisabled" placeholder="输入你的回答，或点击麦克风语音输入..." @keyup.enter="submitAnswer" />
            <button type="button" class="call-btn" :disabled="loadingSession" @click="openCallView">
              <el-icon><VideoCamera /></el-icon>
            </button>
            <button type="button" class="mic-btn" :class="{ active: recording }" :disabled="inputDisabled || transcribing" @click="toggleRecord">
              <el-icon><Microphone /></el-icon>
            </button>
            <button type="button" class="send-btn" :disabled="inputDisabled || !draft.trim()" @click="submitAnswer">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </footer>
      </template>

      <section v-else class="report-page">
        <div v-if="summaryLoading || ending" class="report-loading-stage">
          <div class="report-loader-orbit">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <strong>{{ ending ? 'AI 正在生成更完整的面试报告...' : '正在载入面试报告...' }}</strong>
          <p>我们正在整理综合评分、优劣势、关键词覆盖和逐题复盘，请稍候片刻。</p>
        </div>

        <template v-else-if="summary">
          <section class="report-hero">
            <div class="report-score-panel">
              <span>综合评分</span>
              <strong>{{ Math.round(summary.overallScore || 0) }}</strong>
              <p>{{ summary.summary }}</p>
            </div>
            <div class="report-overview-grid">
              <article>
                <span>答题完成度</span>
                <strong>{{ Math.round(summary.completionRate || 0) }}%</strong>
              </article>
              <article>
                <span>回答平均分</span>
                <strong>{{ Math.round(summary.averageAnswerScore || 0) }}</strong>
              </article>
              <article>
                <span>表达稳定度</span>
                <strong>{{ Math.round(summary.expressionScore || 0) }}</strong>
              </article>
              <article>
                <span>关键词覆盖</span>
                <strong>{{ Math.round(summary.keywordCoverageScore || 0) }}%</strong>
              </article>
            </div>
          </section>

          <section class="report-meta-row">
            <div><span>岗位</span><strong>{{ session?.targetPosition || '-' }}</strong></div>
            <div><span>技术栈</span><strong>{{ (summary.techStacks || session?.techStacks || []).join(' / ') || '-' }}</strong></div>
            <div><span>答题数</span><strong>{{ summary.answeredQuestions }}/{{ summary.totalQuestions }}</strong></div>
            <div><span>用时</span><strong>{{ formatDuration(summary.durationSeconds || 0) }}</strong></div>
          </section>

          <section class="dimension-panel">
            <article class="radar-card">
              <div class="section-head">
                <h3>能力雷达</h3>
                <p>五个岗位能力维度共同解释综合评分来源。</p>
              </div>
              <div ref="radarChartRef" class="radar-chart" aria-label="能力维度雷达图"></div>
            </article>
            <article class="dimension-card-list">
              <div v-for="item in dimensionItems" :key="item.key" class="dimension-card">
                <div>
                  <span>{{ item.label }}</span>
                  <strong>{{ Math.round(item.score) }}</strong>
                </div>
                <p>{{ item.description }}</p>
                <i :style="{ width: `${Math.max(4, Math.min(100, item.score))}%` }"></i>
              </div>
            </article>
          </section>

          <section class="report-section-grid">
            <article class="report-section-card">
              <h3>优势亮点</h3>
              <ul><li v-for="item in summary.strengths" :key="item">{{ item }}</li></ul>
            </article>
            <article class="report-section-card">
              <h3>当前短板</h3>
              <ul><li v-for="item in summary.weaknesses" :key="item">{{ item }}</li></ul>
            </article>
            <article class="report-section-card">
              <h3>改进建议</h3>
              <ul><li v-for="item in summary.suggestions" :key="item">{{ item }}</li></ul>
            </article>
          </section>

          <section class="question-review-section">
            <div class="section-head">
              <h3>逐题复盘</h3>
              <p>查看每道题的回答得分、简评、关键词覆盖和补强建议。</p>
            </div>

            <article v-for="review in summary.questionReviews || []" :key="`${review.questionId}-${review.questionOrder}`" class="review-card">
              <div class="review-top">
                <div>
                  <span class="review-order">第 {{ review.questionOrder || '-' }} 题 · {{ review.questionType || 'QUESTION' }}</span>
                  <h4>{{ review.questionContent || '题目内容暂无记录' }}</h4>
                </div>
                <div class="review-score">
                  <strong>{{ Math.round(review.score || 0) }}</strong>
                  <span>本题得分</span>
                </div>
              </div>

              <div class="review-meta">
                <span>作答时长：{{ formatDuration(review.duration || 0) }}</span>
                <span>表达信心：{{ review.confidenceLevel || 0 }}/10</span>
              </div>

              <div class="review-answer">
                <span>回答摘要</span>
                <p>{{ review.answerContent || '未作答' }}</p>
              </div>

              <p v-if="review.feedbackSummary" class="review-summary">{{ review.feedbackSummary }}</p>

              <div v-if="review.dimensionScores" class="review-dimension-row">
                <span
                  v-for="item in getReviewDimensionItems(review.dimensionScores)"
                  :key="`${review.questionId}-${item.key}`"
                  :class="{ weak: item.score < 65, strong: item.score >= 80 }"
                >
                  {{ item.label }} {{ Math.round(item.score) }}
                </span>
              </div>

              <div class="review-columns">
                <section>
                  <h5>回答做得好</h5>
                  <ul><li v-for="item in review.strengths || []" :key="item">{{ item }}</li></ul>
                </section>
                <section>
                  <h5>还需要补充</h5>
                  <ul><li v-for="item in review.weaknesses || []" :key="item">{{ item }}</li></ul>
                </section>
                <section>
                  <h5>下一步建议</h5>
                  <ul><li v-for="item in review.suggestions || []" :key="item">{{ item }}</li></ul>
                </section>
              </div>

              <div class="keyword-row">
                <div>
                  <span>命中关键词</span>
                  <p>{{ (review.hitKeywords || []).join('、') || '暂无' }}</p>
                </div>
                <div>
                  <span>缺失关键词</span>
                  <p>{{ (review.missingKeywords || []).join('、') || '暂无' }}</p>
                </div>
              </div>
            </article>
          </section>
        </template>

        <article v-else class="report-empty">
          <strong>当前还没有可展示的报告</strong>
          <p>完成面试后，这里会显示完整的评估结果与逐题复盘。</p>
        </article>
      </section>

      <transition name="call-view">
        <section v-if="callMode" class="call-view">
          <header class="call-view-head">
            <div>
              <span>AI 通话面试</span>
              <h3>{{ session?.targetPosition || '模拟面试通话中' }}</h3>
            </div>
            <div class="call-view-actions">
              <button type="button" class="call-back-btn" @click="closeCallView">返回对话</button>
              <button type="button" class="call-end-btn" :disabled="ending || !session" @click="confirmEndInterview">结束面试</button>
            </div>
          </header>

          <section class="call-view-stage">
            <article class="call-card chat">
              <div class="call-card-head">
                <strong>对话记录</strong>
                <span>{{ voiceStatusText }}</span>
              </div>
              <div class="call-chat-panel" ref="callMessagesRef">
                <article v-for="message in messages" :key="`call-${message.id}`" class="call-chat-item" :class="message.role">
                  <span class="call-chat-role">{{ message.role === 'ai' ? 'AI' : '我' }}</span>
                  <div class="call-chat-bubble">
                    <strong v-if="message.role === 'ai'">{{ message.title }}</strong>
                    <p>{{ message.content }}</p>
                  </div>
                </article>

                <article v-if="waitingAI" class="call-chat-item ai waiting">
                  <span class="call-chat-role">AI</span>
                  <div class="typing"><i></i><i></i><i></i><span>AI 面试官正在判断回答...</span></div>
                </article>

                <div v-if="!messages.length && !loadingSession" class="call-chat-empty">
                  <strong>对话内容准备中</strong>
                  <p>进入面试后，这里会持续展示你和 AI 面试官的实时聊天记录。</p>
                </div>
              </div>
            </article>

            <article class="call-card me">
              <div class="call-card-head">
                <strong>我的画面</strong>
                <span>{{ cameraStatusText }}</span>
              </div>
              <div class="camera-panel" :class="{ active: cameraActive }">
                <video v-show="cameraActive" ref="cameraVideoRef" autoplay muted playsinline></video>
                <div v-if="!cameraActive" class="camera-fallback">
                  <el-icon><VideoCamera /></el-icon>
                  <strong>{{ cameraError || '正在准备摄像头画面' }}</strong>
                  <p>允许浏览器访问摄像头后，这里会显示你的实时人像。</p>
                </div>
              </div>
            </article>
          </section>

          <section class="call-question-card">
            <span>当前题目</span>
            <strong>{{ currentQuestion?.content || '题目同步中，请稍候...' }}</strong>
          </section>

          <footer class="call-footer">
            <div v-if="recording || transcribing" class="voice-live-card call-live-card" :class="{ recording, transcribing }">
              <div class="voice-live-head">
                <div class="voice-wave" :class="{ active: recording }">
                  <i></i>
                  <i></i>
                  <i></i>
                  <i></i>
                </div>
                <div class="voice-live-copy">
                  <strong>{{ recording ? '正在语音输入' : '正在整理语音内容' }}</strong>
                  <span>{{ recognitionSupported ? '识别内容会实时显示' : '当前浏览器不支持实时识别，停止后会自动转写' }}</span>
                </div>
              </div>
              <p>{{ voiceDisplayText }}</p>
            </div>

            <div class="input-bar call-input-bar">
              <el-input v-model="draft" :disabled="inputDisabled" placeholder="输入你的回答，或点击麦克风语音输入..." @keyup.enter="submitAnswer" />
              <button type="button" class="mic-btn" :class="{ active: recording }" :disabled="inputDisabled || transcribing" @click="toggleRecord">
                <el-icon><Microphone /></el-icon>
              </button>
              <button type="button" class="send-btn" :disabled="inputDisabled || !draft.trim()" @click="submitAnswer">
                <el-icon><Promotion /></el-icon>
              </button>
            </div>
          </footer>
        </section>
      </transition>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Close, Document, Microphone, Moon, Promotion, Service, Sunny, VideoCamera } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { GridComponent, RadarComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import aiInterviewApi, {
  type AIInterviewAvatarSession,
  type AIInterviewQuestion,
  type AIInterviewSession,
  type AIInterviewSummary
} from '@/api/aiInterview'
import resumeApi, { type UserResume } from '@/api/resumes'
import AvatarDigitalHumanPlayer from '@/components/AIInterview/AvatarDigitalHumanPlayer.vue'

echarts.use([RadarChart, GridComponent, RadarComponent, TooltipComponent, CanvasRenderer])

interface ChatMessage {
  id: string
  role: 'ai' | 'user'
  title?: string
  content: string
}

interface DimensionItem {
  key: string
  label: string
  score: number
  description: string
}

const DIMENSION_DEFINITIONS: Array<Omit<DimensionItem, 'score'>> = [
  { key: 'technicalDepth', label: '技术深度', description: '核心原理、关键机制、边界条件' },
  { key: 'projectRelevance', label: '项目匹配', description: '项目经历、技术栈和岗位场景结合度' },
  { key: 'problemSolving', label: '问题分析', description: '拆解问题、说明思路、对比方案' },
  { key: 'communicationClarity', label: '表达清晰', description: '结构化表达、逻辑连贯、重点明确' },
  { key: 'jobMatch', label: '岗位匹配', description: '回答贴合目标岗位要求的程度' }
]

const route = useRoute()
const router = useRouter()
const session = ref<AIInterviewSession | null>(null)
const currentQuestion = ref<AIInterviewQuestion | null>(null)
const summary = ref<AIInterviewSummary | null>(null)
const resumes = ref<UserResume[]>([])
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const loadingSession = ref(true)
const resumeLoading = ref(false)
const waitingAI = ref(false)
const ending = ref(false)
const summaryLoading = ref(false)
const recording = ref(false)
const transcribing = ref(false)
const liveTranscript = ref('')
const interimTranscript = ref('')
const avatarLoading = ref(false)
const avatarSpeaking = ref(false)
const avatarError = ref('')
const avatarSession = ref<AIInterviewAvatarSession | null>(null)
const isDark = ref(false)
const activeLeftPanel = ref<'interviewer' | 'resume'>('interviewer')
const activeMainTab = ref<'dialogue' | 'report'>('dialogue')
const cameraVideoRef = ref<HTMLVideoElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)
const callMessagesRef = ref<HTMLElement | null>(null)
const radarChartRef = ref<HTMLElement | null>(null)
const pendingAvatarTexts = ref<string[]>([])
let startedAt = Date.now()
let elapsedTimer: number | undefined
const elapsedSeconds = ref(0)
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
const cameraStream = ref<MediaStream | null>(null)
let speechRecognition: any = null
let keepSpeechRecognitionAlive = false
let speechRecognitionStarted = false
let avatarClosingPromise: Promise<void> | null = null
let radarChart: echarts.ECharts | null = null
let pageExitHandled = false
const cameraError = ref('')
const voiceError = ref('')

const interviewId = computed(() => Number(route.params.id))
const reportMode = computed(() => route.query.report === '1')
const finished = computed(() => session.value?.status === 'COMPLETED')
const inputDisabled = computed(() => loadingSession.value || waitingAI.value || finished.value || activeMainTab.value === 'report' || !currentQuestion.value)
const canViewReport = computed(() => finished.value)
const themeClass = computed(() => (isDark.value ? 'theme-dark' : 'theme-light'))
const callMode = computed(() => route.query.view === 'call')
const cameraActive = computed(() => Boolean(cameraStream.value))
const cameraStatusText = computed(() => {
  if (cameraActive.value) return '摄像头已接通'
  if (cameraError.value) return cameraError.value
  return '等待授权'
})
const voiceStatusText = computed(() => {
  if (recording.value) return '语音识别进行中'
  if (transcribing.value) return '正在整理语音内容'
  if (voiceError.value) return voiceError.value
  return recognitionSupported.value ? '进入视频后会自动识别语音' : '当前浏览器仅支持录音后转写'
})
const currentTechLabel = computed(() => session.value?.techStacks?.slice(0, 2).join(' / ') || session.value?.targetPosition || 'AI 面试')
const currentOrder = computed(() => currentQuestion.value?.questionOrder || Math.min((session.value?.answeredCount || 0) + 1, session.value?.questionCount || 0))
const avatarStatusText = computed(() => {
  if (avatarLoading.value) return '数字人连接中'
  if (avatarError.value) return avatarError.value
  if (avatarSession.value?.connected) return '数字人在线'
  return avatarSession.value?.message || '数字人未启用'
})
const sessionResume = computed(() => {
  const fileName = session.value?.resumeFileName?.trim()
  if (!fileName) return null
  return resumes.value.find((resume) => resume.originalFileName === fileName || resume.fileName === fileName) || null
})
const elapsedLabel = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60)
  const seconds = elapsedSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
const recognitionSupported = computed(() => {
  if (typeof window === 'undefined') return false
  const speechApi = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  return Boolean(speechApi)
})
const voiceDisplayText = computed(() => {
  const merged = [liveTranscript.value, interimTranscript.value].filter(Boolean).join('').trim()
  if (merged) return merged
  return recording.value ? '正在聆听，请开始说话...' : '语音已结束，正在生成文字...'
})
const dimensionItems = computed(() => getReviewDimensionItems(summary.value?.dimensionScores))

const formatDuration = (seconds?: number) => {
  const totalSeconds = Number(seconds || 0)
  if (totalSeconds <= 0) return '0 秒'
  if (totalSeconds < 60) return `${totalSeconds} 秒`
  const minutes = Math.floor(totalSeconds / 60)
  const remainSeconds = totalSeconds % 60
  return remainSeconds ? `${minutes} 分 ${remainSeconds} 秒` : `${minutes} 分钟`
}

const normalizeDimensionScore = (scores: Record<string, number> | undefined, key: string) => {
  const fallback = summary.value?.overallScore || 0
  const score = Number(scores?.[key] ?? fallback)
  return Number.isFinite(score) ? Math.max(0, Math.min(100, score)) : 0
}

const getReviewDimensionItems = (scores?: Record<string, number>): DimensionItem[] => {
  return DIMENSION_DEFINITIONS.map((item) => ({
    ...item,
    score: normalizeDimensionScore(scores, item.key)
  }))
}

const renderRadarChart = async () => {
  await nextTick()
  if (!radarChartRef.value || !summary.value) return
  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }
  const items = dimensionItems.value
  radarChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: () => items.map((item) => `${item.label}: ${Math.round(item.score)}`).join('<br/>')
    },
    radar: {
      radius: '68%',
      center: ['50%', '52%'],
      indicator: items.map((item) => ({ name: item.label, max: 100 })),
      splitNumber: 4,
      axisName: {
        color: isDark.value ? '#dbe7ff' : '#435064',
        fontWeight: 800
      },
      splitLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.16)' : 'rgba(67, 80, 100, 0.14)' } },
      splitArea: { areaStyle: { color: ['rgba(255, 90, 42, 0.04)', 'rgba(65, 110, 230, 0.04)'] } },
      axisLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.18)' : 'rgba(67, 80, 100, 0.16)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: items.map((item) => Number(item.score.toFixed(1))),
        name: '能力维度',
        areaStyle: { color: 'rgba(255, 90, 42, 0.22)' },
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

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
  if (callMessagesRef.value) {
    callMessagesRef.value.scrollTop = callMessagesRef.value.scrollHeight
  }
}

const pushMessage = (role: 'ai' | 'user', content: string, title = role === 'ai' ? '面试官' : '') => {
  messages.value.push({ id: `${Date.now()}-${messages.value.length}`, role, title, content })
  if (role === 'ai') {
    enqueueAvatarSpeech(content)
  }
  scrollToBottom()
}

const enqueueAvatarSpeech = (content: string) => {
  const text = content.trim()
  if (!text) return
  pendingAvatarTexts.value.push(text)
  flushAvatarQueue()
}

const flushAvatarQueue = async () => {
  if (avatarLoading.value || avatarSpeaking.value || !avatarSession.value?.connected || !pendingAvatarTexts.value.length) {
    return
  }
  avatarSpeaking.value = true
  try {
    while (pendingAvatarTexts.value.length && avatarSession.value?.connected) {
      const text = pendingAvatarTexts.value.shift()
      if (!text) continue
      const spoken = await aiInterviewApi.speakAvatar(interviewId.value, { text })
      if (!spoken) {
        avatarSession.value = null
        avatarError.value = '数字人播报失败，已切换为文字 / 语音面试'
        pendingAvatarTexts.value = []
        break
      }
    }
  } catch (error: any) {
    avatarError.value = error?.message || '数字人播报失败'
  } finally {
    avatarSpeaking.value = false
  }
}

const initAvatarSession = async () => {
  avatarLoading.value = true
  avatarError.value = ''
  try {
    const data = await aiInterviewApi.initAvatarSession(interviewId.value)
    avatarSession.value = data
    if (!data.enabled || !data.connected) {
      avatarError.value = data.message || '数字人暂未启用，已切换为文字 / 语音面试'
      return
    }
    await flushAvatarQueue()
  } catch (error: any) {
    avatarSession.value = null
    avatarError.value = error?.message || '数字人连接失败，已切换为文字 / 语音面试'
  } finally {
    avatarLoading.value = false
  }
}

const handleAvatarPlayerError = (message: string) => {
  avatarError.value = message
}

const closeAvatarSession = async () => {
  if (!avatarSession.value?.connected) return
  if (avatarClosingPromise) {
    await avatarClosingPromise
    return
  }
  avatarClosingPromise = aiInterviewApi.stopAvatarSession(interviewId.value)
    .catch(() => undefined)
    .then(() => {
      avatarSession.value = null
      pendingAvatarTexts.value = []
    })
    .finally(() => {
      avatarClosingPromise = null
    })
  await avatarClosingPromise
}

const handlePageExit = () => {
  if (pageExitHandled || !avatarSession.value?.connected) return
  pageExitHandled = true
  aiInterviewApi.stopAvatarSessionOnPageExit(interviewId.value)
  avatarSession.value = null
  pendingAvatarTexts.value = []
}

const goBack = async () => {
  if (!reportMode.value && !finished.value && session.value) {
    try {
      await ElMessageBox.confirm(
        '面试还未结束，是否需要退出当前页面？退出后将关闭本场数字人会话。',
        '退出确认',
        { confirmButtonText: '确认退出', cancelButtonText: '继续面试', type: 'warning' }
      )
    } catch {
      return
    }
  }
  await closeAvatarSession()
  if (reportMode.value) {
    router.push('/user/history')
    return
  }
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/user/interview/ai/create')
}

const switchMainTab = async (tab: 'dialogue' | 'report') => {
  if (tab === 'report') {
    if (!canViewReport.value) {
      ElMessage.info('请先完成面试，再查看完整报告')
      return
    }
    activeMainTab.value = 'report'
    if (!summary.value && finished.value) {
      await openSummary()
    }
    return
  }
  activeMainTab.value = 'dialogue'
}

const loadInterview = async () => {
  loadingSession.value = true
  try {
    let data = await aiInterviewApi.getInterview(interviewId.value)
    if (!reportMode.value && data.status !== 'IN_PROGRESS' && data.status !== 'COMPLETED') {
      data = await aiInterviewApi.startInterview(interviewId.value)
    }
    session.value = data
    currentQuestion.value = data.currentQuestion || null
    startedAt = Date.now()
    messages.value = []
    pendingAvatarTexts.value = []
    initAvatarSession()
    if (data.openingMessage) {
      pushMessage('ai', data.openingMessage, '开场')
    }
    if (currentQuestion.value) {
      pushMessage('ai', currentQuestion.value.content, `第 ${currentQuestion.value.questionOrder} 题`)
    }
    if (reportMode.value) {
      if (data.status !== 'COMPLETED') {
        ElMessage.warning('该场面试还没有生成最终报告，已为你切回继续面试页面')
        router.replace(`/user/interview/ai/session/${interviewId.value}`)
        return
      }
      activeMainTab.value = 'report'
      await openSummary()
      return
    }
    if (data.status === 'COMPLETED') {
      await openSummary()
      return
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '面试载入失败')
  } finally {
    loadingSession.value = false
  }
}

const loadResumes = async () => {
  resumeLoading.value = true
  try {
    resumes.value = await resumeApi.listResumes()
  } catch (error: any) {
    ElMessage.error(error?.message || '简历载入失败')
  } finally {
    resumeLoading.value = false
  }
}

const submitAnswer = async () => {
  const content = draft.value.trim()
  if (!content || !currentQuestion.value || !session.value) return
  const questionId = currentQuestion.value.questionId
  draft.value = ''
  pushMessage('user', content)
  waitingAI.value = true
  try {
    const result = await aiInterviewApi.submitAnswer(session.value.interviewId, questionId, {
      content,
      inputMode: 'TEXT',
      duration: elapsedSeconds.value
    })
    if (result.interviewerReply) {
      pushMessage('ai', result.interviewerReply)
    }
    session.value.answeredCount += 1
    if (result.interviewCompleted || result.nextAction === 'END') {
      session.value.status = 'COMPLETED'
      currentQuestion.value = null
      await closeAvatarSession()
      activeMainTab.value = 'report'
      await openSummary()
      return
    }
    currentQuestion.value = result.nextQuestion || await aiInterviewApi.getNextQuestion(session.value.interviewId)
  } catch (error: any) {
    ElMessage.error(error?.message || '提交回答失败，请稍后重试')
  } finally {
    waitingAI.value = false
  }
}

const confirmEndInterview = async () => {
  if (!session.value || finished.value) return
  const remaining = Math.max(0, (session.value.questionCount || 0) - (session.value.answeredCount || 0))
  try {
    await ElMessageBox.confirm(
      remaining > 0 ? `还有 ${remaining} 道题没有回答完，是否结束并生成当前进度的面试报告？` : '是否结束面试并生成报告？',
      '结束面试确认',
      { confirmButtonText: '结束面试', cancelButtonText: '继续作答', type: remaining > 0 ? 'warning' : 'success' }
    )
    ending.value = true
    activeMainTab.value = 'report'
    summaryLoading.value = true
    summary.value = await aiInterviewApi.endInterview(session.value.interviewId)
    session.value.status = 'COMPLETED'
    currentQuestion.value = null
    await closeAvatarSession()
  } catch {
    activeMainTab.value = 'dialogue'
  } finally {
    ending.value = false
    summaryLoading.value = false
  }
}

const openSummary = async () => {
  if (!session.value) return
  summaryLoading.value = true
  try {
    summary.value = await aiInterviewApi.getSummary(session.value.interviewId)
  } catch {
    summary.value = await aiInterviewApi.endInterview(session.value.interviewId)
  } finally {
    summaryLoading.value = false
  }
}

const toggleTheme = () => {
  isDark.value = !isDark.value
}

const syncCameraVideo = async () => {
  await nextTick()
  if (!cameraVideoRef.value || !cameraStream.value) return
  cameraVideoRef.value.srcObject = cameraStream.value
  try {
    await cameraVideoRef.value.play()
  } catch {
    // Ignore autoplay failures while video element is mounting.
  }
}

const stopCameraPreview = () => {
  cameraStream.value?.getTracks().forEach((track) => track.stop())
  cameraStream.value = null
  if (cameraVideoRef.value) {
    cameraVideoRef.value.srcObject = null
  }
}

const startCameraPreview = async () => {
  if (cameraStream.value) {
    await syncCameraVideo()
    return
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    cameraError.value = '当前浏览器不支持摄像头调用'
    return
  }
  try {
    cameraError.value = ''
    cameraStream.value = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'user',
        width: { ideal: 960 },
        height: { ideal: 540 }
      },
      audio: false
    })
    await syncCameraVideo()
  } catch (error: any) {
    cameraError.value = error?.message || '摄像头调用失败'
  }
}

const openCallView = async () => {
  voiceError.value = ''
  const nextQuery = { ...route.query, view: 'call' }
  await router.replace({ path: route.path, query: nextQuery }).catch(() => undefined)
  await Promise.allSettled([
    startCameraPreview(),
    !finished.value && !loadingSession.value ? startVoiceCapture() : Promise.resolve()
  ])
}

const closeCallView = () => {
  const nextQuery = { ...route.query }
  delete nextQuery.view
  router.replace({ path: route.path, query: nextQuery }).catch(() => undefined)
}

const syncDraftWithTranscript = () => {
  const merged = [liveTranscript.value, interimTranscript.value].filter(Boolean).join('').trim()
  if (merged) {
    draft.value = merged
  }
}

const buildRecognitionLanguage = () => {
  const language = (session.value?.interviewLanguage || '').toLowerCase()
  if (language.startsWith('en')) return 'en-US'
  if (language.startsWith('ja')) return 'ja-JP'
  return 'zh-CN'
}

const ensureSpeechRecognition = () => {
  if (speechRecognition || typeof window === 'undefined') return speechRecognition
  const SpeechRecognitionCtor = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
  if (!SpeechRecognitionCtor) return null

  speechRecognition = new SpeechRecognitionCtor()
  speechRecognition.continuous = true
  speechRecognition.interimResults = true
  speechRecognition.lang = buildRecognitionLanguage()

  speechRecognition.onstart = () => {
    speechRecognitionStarted = true
  }

  speechRecognition.onresult = (event: any) => {
    let finalChunk = ''
    let interimChunk = ''

    for (let index = event.resultIndex; index < event.results.length; index += 1) {
      const transcript = event.results[index]?.[0]?.transcript || ''
      if (event.results[index].isFinal) {
        finalChunk += transcript
      } else {
        interimChunk += transcript
      }
    }

    if (finalChunk.trim()) {
      liveTranscript.value = `${liveTranscript.value}${finalChunk}`.trim()
    }
    interimTranscript.value = interimChunk.trim()
    syncDraftWithTranscript()
  }

  speechRecognition.onerror = (event: any) => {
    if (event?.error === 'aborted' || event?.error === 'no-speech') return
    ElMessage.warning('实时语音识别暂时不可用，停止录音后会自动转写')
  }

  speechRecognition.onend = () => {
    speechRecognitionStarted = false
    if (recording.value && keepSpeechRecognitionAlive) {
      try {
        speechRecognition.lang = buildRecognitionLanguage()
        speechRecognition.start()
      } catch {
        // Ignore restart failures and keep recorder working.
      }
    }
  }

  return speechRecognition
}

const startSpeechRecognition = () => {
  const recognition = ensureSpeechRecognition()
  if (!recognition) return
  keepSpeechRecognitionAlive = true
  recognition.lang = buildRecognitionLanguage()
  try {
    recognition.start()
  } catch {
    // Ignore duplicate start calls while recognition is already active.
  }
}

const stopSpeechRecognition = () => {
  keepSpeechRecognitionAlive = false
  if (!speechRecognition || !speechRecognitionStarted) return
  try {
    speechRecognition.stop()
  } catch {
    // Ignore stop failures during teardown.
  }
}

const stopVoiceCapture = () => {
  stopSpeechRecognition()
  if (mediaRecorder && recording.value) {
    mediaRecorder.stop()
  }
  recording.value = false
}

const startVoiceCapture = async (silent = false) => {
  if (recording.value || transcribing.value) return
  voiceError.value = ''
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    audioChunks = []
    liveTranscript.value = draft.value.trim()
    interimTranscript.value = ''
    mediaRecorder = new MediaRecorder(stream)
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) audioChunks.push(event.data)
    }
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach((track) => track.stop())
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const file = new File([blob], 'answer.webm', { type: 'audio/webm' })
      transcribing.value = true
      try {
        const result = await aiInterviewApi.transcribeAudio(file, session.value?.interviewLanguage)
        const transcript = (result.transcript || '').trim()
        if (transcript) {
          liveTranscript.value = transcript
          draft.value = transcript
        }
      } catch (error: any) {
        ElMessage.error(error?.message || '语音识别失败')
      } finally {
        interimTranscript.value = ''
        transcribing.value = false
      }
    }
    mediaRecorder.start()
    startSpeechRecognition()
    recording.value = true
  } catch (error: any) {
    voiceError.value = error?.message || '麦克风权限未开启'
    if (!silent) {
      ElMessage.error('无法访问麦克风，请检查浏览器权限')
    }
  }
}

const toggleRecord = async () => {
  if (recording.value) {
    stopVoiceCapture()
    return
  }
  await startVoiceCapture()
}

watch(
  callMode,
  (enabled) => {
    if (enabled) {
      return
    }
    stopCameraPreview()
    if (recording.value) {
      stopVoiceCapture()
    }
  },
  { immediate: true }
)

watch(
  [activeMainTab, finished],
  ([tab, interviewFinished]) => {
    if ((tab === 'report' || interviewFinished) && callMode.value) {
      closeCallView()
    }
  }
)

watch(
  loadingSession,
  async (loading) => {
    if (!loading && callMode.value && !recording.value && !finished.value) {
      await startVoiceCapture()
    }
  }
)

watch(
  [summary, activeMainTab, isDark],
  () => {
    if (activeMainTab.value === 'report' && summary.value) {
      renderRadarChart()
    }
  },
  { deep: true }
)

onMounted(() => {
  activeMainTab.value = reportMode.value ? 'report' : 'dialogue'
  loadInterview()
  loadResumes()
  window.addEventListener('pagehide', handlePageExit)
  window.addEventListener('beforeunload', handlePageExit)
  window.addEventListener('resize', resizeRadarChart)
  elapsedTimer = window.setInterval(() => {
    elapsedSeconds.value = Math.floor((Date.now() - startedAt) / 1000)
  }, 1000)
})

onBeforeRouteLeave(async () => {
  stopCameraPreview()
  await closeAvatarSession()
})

onUnmounted(() => {
  window.removeEventListener('pagehide', handlePageExit)
  window.removeEventListener('beforeunload', handlePageExit)
  window.removeEventListener('resize', resizeRadarChart)
  if (elapsedTimer) window.clearInterval(elapsedTimer)
  stopCameraPreview()
  stopSpeechRecognition()
  radarChart?.dispose()
  radarChart = null
  if (mediaRecorder && recording.value) mediaRecorder.stop()
  void closeAvatarSession()
})
</script>

<style scoped>
.session-page {
  --page-bg: #fff7f3;
  --panel-bg: #ffffff;
  --side-bg: #ffffff;
  --card-bg: #fff2ec;
  --card-border: #ffd8c8;
  --primary-text: #242733;
  --muted-text: #7a8496;
  --soft-text: #96a0af;
  --accent: #ff5a2a;
  --accent-soft: #fff1eb;
  --bot-icon-bg: #fff1eb;
  --bot-bubble-bg: #ffffff;
  --user-bubble-bg: #ff5a2a;
  --input-bg: #ffffff;
  --input-border: #f1d9ce;
  --success: #24a865;
  --success-bg: #eafaf1;
  --report-card-bg: #fbfcfe;
  --report-card-border: #e7ebf3;
  --report-divider: rgba(36, 39, 51, 0.08);
  --report-shadow: 0 10px 24px rgba(27, 36, 56, 0.035);
  --report-muted-bg: #ffffff;
  --report-text: #4f5968;
  --report-hero-shadow: 0 24px 48px rgba(255, 90, 42, 0.22);
  height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 38.2%) minmax(0, 61.8%);
  overflow: hidden;
  background: var(--page-bg);
  color: var(--primary-text);
}

.theme-dark {
  --page-bg: #101b2f;
  --panel-bg: #111c30;
  --side-bg: #142441;
  --card-bg: rgba(255, 255, 255, 0.07);
  --card-border: rgba(151, 170, 204, 0.16);
  --primary-text: #eef4ff;
  --muted-text: #7f8fa9;
  --soft-text: #8797b1;
  --accent: #ff6a35;
  --accent-soft: rgba(255, 90, 42, 0.12);
  --bot-icon-bg: #203860;
  --bot-bubble-bg: #1c3765;
  --input-bg: #1c3765;
  --input-border: transparent;
  --success: #43d783;
  --success-bg: rgba(67, 215, 131, 0.12);
  --report-card-bg: rgba(255, 255, 255, 0.04);
  --report-card-border: rgba(148, 170, 204, 0.16);
  --report-divider: rgba(219, 229, 245, 0.12);
  --report-shadow: 0 14px 28px rgba(2, 6, 23, 0.24);
  --report-muted-bg: rgba(255, 255, 255, 0.03);
  --report-text: #c3d0e5;
  --report-hero-shadow: 0 24px 48px rgba(0, 0, 0, 0.22);
}

.interviewer-panel {
  min-height: 0;
  padding: 24px 30px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid var(--card-border);
  background: var(--side-bg);
}

.left-tabs {
  width: 100%;
  padding: 4px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: var(--card-bg);
}

.left-tabs button {
  min-width: 0;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 8px;
  color: var(--muted-text);
  background: transparent;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.left-tabs button.active {
  color: #ffffff;
  background: var(--accent);
}

.left-panel-section {
  width: 100%;
  min-height: 0;
  margin-top: 28px;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
}

.avatar-stage {
  width: 100%;
  min-height: 0;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.interviewer-action-row {
  width: 100%;
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.avatar-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.avatar-status.online {
  color: #ffffff;
  background: linear-gradient(135deg, #ff6a35, #ff8a63);
}

.interviewer-panel h1 {
  margin-top: 18px;
  font-size: 20px;
  font-weight: 900;
}

.interviewer-panel p {
  margin-top: 10px;
  color: var(--muted-text);
  font-size: 13px;
}

.end-btn {
  width: 100%;
  height: 38px;
  border: 1px solid rgba(255, 90, 42, 0.45);
  border-radius: 8px;
  color: #ff4f4f;
  background: var(--accent-soft);
  font-weight: 900;
  cursor: pointer;
}

.end-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.resume-panel {
  margin-top: 22px;
  align-items: stretch;
  overflow: hidden;
}

.resume-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resume-icon {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 22px;
}

.resume-head h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
}

.resume-head p {
  max-width: 244px;
  margin: 7px 0 0;
  color: var(--muted-text);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-readonly {
  min-height: 0;
  margin-top: 18px;
  padding-right: 4px;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
  overflow: auto;
}

.resume-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.resume-meta span {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid var(--card-border);
  border-radius: 8px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-summary,
.resume-content,
.resume-state {
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: var(--card-bg);
}

.resume-summary,
.resume-content {
  padding: 14px;
}

.resume-summary strong,
.resume-content strong {
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.resume-summary p {
  margin: 8px 0 0;
  color: var(--muted-text);
  font-size: 13px;
  line-height: 1.8;
}

.resume-content {
  min-height: 0;
  flex: 1;
}

.resume-content pre {
  margin: 10px 0 0;
  color: var(--muted-text);
  font-family: inherit;
  font-size: 12px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.resume-state {
  min-height: 180px;
  margin-top: 18px;
  padding: 22px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: var(--muted-text);
  text-align: center;
}

.resume-state > .el-icon {
  color: var(--accent);
  font-size: 28px;
}

.resume-state strong {
  color: var(--primary-text);
  font-size: 14px;
}

.resume-state p {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
}

.dialog-panel {
  position: relative;
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: 52px auto minmax(0, 1fr) auto;
  background: var(--panel-bg);
}

.call-stage-panel {
  position: absolute;
  inset: 94px 0 0;
  z-index: 8;
  padding: 26px;
  display: grid;
  place-items: center;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.17), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.14), transparent 24%),
    linear-gradient(180deg, rgba(255, 248, 244, 0.94), rgba(255, 255, 255, 0.98));
}

.theme-dark .call-stage-panel {
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.16), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.08), transparent 24%),
    linear-gradient(180deg, rgba(12, 24, 42, 0.96), rgba(17, 28, 48, 0.98));
}

.call-stage-backdrop span {
  position: absolute;
  border-radius: 50%;
  filter: blur(18px);
  opacity: 0.8;
}

.call-stage-backdrop span:nth-child(1) {
  width: 220px;
  height: 220px;
  top: 42px;
  left: 42px;
  background: rgba(255, 90, 42, 0.14);
}

.call-stage-backdrop span:nth-child(2) {
  width: 160px;
  height: 160px;
  right: 92px;
  top: 86px;
  background: rgba(255, 170, 78, 0.18);
}

.call-stage-backdrop span:nth-child(3) {
  width: 180px;
  height: 180px;
  right: 48px;
  bottom: 46px;
  background: rgba(255, 124, 71, 0.12);
}

.call-stage-card {
  position: relative;
  z-index: 1;
  width: min(760px, 100%);
  padding: 36px;
  display: grid;
  justify-items: center;
  gap: 22px;
  border: 1px solid rgba(255, 164, 133, 0.28);
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 26px 72px rgba(255, 90, 42, 0.16);
  text-align: center;
  backdrop-filter: blur(16px);
}

.theme-dark .call-stage-card {
  border-color: rgba(255, 164, 133, 0.16);
  background: rgba(20, 36, 65, 0.74);
  box-shadow: 0 26px 72px rgba(0, 0, 0, 0.26);
}

.call-stage-avatar {
  position: relative;
  width: 168px;
  height: 168px;
  display: grid;
  place-items: center;
}

.call-avatar-core {
  position: relative;
  z-index: 2;
  width: 104px;
  height: 104px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff854d 60%, #ffb173 100%);
  box-shadow: 0 18px 38px rgba(255, 90, 42, 0.24);
  font-size: 40px;
}

.orbit {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 90, 42, 0.18);
  border-radius: 50%;
  animation: orbitPulse 2.5s ease-out infinite;
}

.orbit-b {
  inset: 18px;
  animation-delay: 0.25s;
}

.orbit-c {
  inset: 36px;
  animation-delay: 0.5s;
}

.call-stage-copy {
  display: grid;
  gap: 10px;
}

.call-stage-tag {
  width: fit-content;
  justify-self: center;
  padding: 6px 11px;
  border-radius: 999px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 11px;
  font-weight: 900;
}

.call-stage-copy h3 {
  font-size: 32px;
  line-height: 1.15;
  font-weight: 900;
}

.call-stage-copy p {
  max-width: 540px;
  color: var(--muted-text);
  line-height: 1.85;
}

.call-stage-meta {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.call-stage-meta article {
  padding: 16px;
  display: grid;
  gap: 8px;
  border-radius: 18px;
  border: 1px solid var(--card-border);
  background: color-mix(in srgb, var(--card-bg) 80%, var(--panel-bg) 20%);
  text-align: left;
}

.call-stage-meta span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 800;
}

.call-stage-meta strong {
  color: var(--primary-text);
  font-size: 15px;
  line-height: 1.6;
  font-weight: 900;
}

.call-stage-timeline {
  display: flex;
  align-items: center;
  gap: 10px;
}

.call-stage-timeline i {
  width: 54px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 90, 42, 0.14);
  transition: background 0.25s ease, transform 0.25s ease;
}

.call-stage-timeline i.done {
  background: linear-gradient(90deg, #ff5a2a, #ffb173);
  transform: scaleX(1.06);
}

.call-action-btn {
  min-width: 186px;
  height: 52px;
  padding: 0 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff7f48 60%, #ffb173 100%);
  box-shadow: 0 18px 34px rgba(255, 90, 42, 0.22);
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, opacity 0.25s ease;
}

.call-action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 22px 40px rgba(255, 90, 42, 0.26);
}

.call-action-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.dialog-head {
  padding: 0 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--card-border);
}

.head-title {
  display: grid;
}

.dialog-head h2 {
  font-size: 15px;
  font-weight: 900;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.theme-toggle {
  height: 28px;
  padding: 0 11px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1px solid var(--card-border);
  border-radius: 999px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.back-side-btn {
  width: 100%;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid var(--card-border);
  border-radius: 12px;
  color: var(--muted-text);
  background: transparent;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.back-side-btn:hover {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-soft);
}

.dialog-head span {
  height: 22px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--success);
  background: var(--success-bg);
  font-size: 12px;
  font-weight: 900;
}

.dialog-head span.done {
  color: var(--muted-text);
  background: var(--card-bg);
}

.head-chip {
  max-width: 220px;
  height: 28px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.head-chip.time {
  color: var(--accent);
}

.content-tabs {
  padding: 8px 22px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--report-divider);
}

.content-tabs button {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  color: var(--muted-text);
  background: transparent;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.content-tabs button.active {
  color: #ffffff;
  background: var(--accent);
}

.content-tabs button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.messages {
  min-height: 0;
  overflow: auto;
  padding: 18px 18px 22px;
}

.message {
  max-width: 910px;
  margin: 0 auto 26px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message.user {
  justify-content: flex-end;
}

.bot-icon,
.user-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
}

.bot-icon {
  color: var(--accent);
  background: var(--bot-icon-bg);
}

.user-avatar {
  color: #ffffff;
  background: var(--accent);
  font-weight: 900;
}

.bubble,
.user-bubble,
.typing,
.finish-card,
.loading-card {
  border-radius: 10px;
  line-height: 1.8;
}

.bubble {
  max-width: 640px;
  padding: 15px 18px;
  color: var(--primary-text);
  background: var(--bot-bubble-bg);
  border: 1px solid var(--card-border);
}

.bubble strong {
  color: var(--accent);
  font-size: 12px;
}

.bubble p,
.user-bubble p {
  margin-top: 6px;
  font-size: 14px;
  white-space: pre-wrap;
}

.user-bubble {
  max-width: 520px;
  padding: 12px 18px;
  color: #ffffff;
  background: var(--user-bubble-bg);
}

.typing {
  padding: 10px 16px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--soft-text);
  background: var(--bot-bubble-bg);
  border: 1px solid var(--card-border);
}

.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
  animation: pulse 1s infinite ease-in-out;
}

.typing i:nth-child(2) { animation-delay: 0.15s; }
.typing i:nth-child(3) { animation-delay: 0.3s; }

.finish-card,
.loading-card {
  max-width: 620px;
  margin: 0 auto;
  padding: 18px;
  color: var(--primary-text);
  background: var(--success-bg);
  border: 1px solid rgba(67, 215, 131, 0.25);
}

.loading-card {
  display: grid;
  justify-items: center;
  gap: 10px;
  background: var(--card-bg);
}

.finish-card strong {
  color: var(--success);
}

.input-shell {
  padding: 10px 18px;
  display: grid;
  gap: 10px;
  border-top: 1px solid var(--card-border);
}

.voice-live-card {
  padding: 12px 14px;
  border: 1px solid var(--card-border);
  border-radius: 14px;
  background: color-mix(in srgb, var(--accent-soft) 72%, var(--panel-bg) 28%);
}

.voice-live-card.recording {
  box-shadow: 0 10px 24px rgba(255, 90, 42, 0.12);
}

.voice-live-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.voice-live-copy {
  display: grid;
  gap: 2px;
}

.voice-live-copy strong {
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.voice-live-copy span {
  color: var(--muted-text);
  font-size: 12px;
}

.voice-live-card p {
  margin-top: 10px;
  color: var(--primary-text);
  line-height: 1.7;
  font-size: 13px;
  white-space: pre-wrap;
}

.voice-wave {
  width: 44px;
  height: 28px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
}

.voice-wave i {
  width: 4px;
  height: 8px;
  border-radius: 999px;
  background: var(--accent);
  opacity: 0.5;
}

.voice-wave.active i {
  animation: voiceWave 1s ease-in-out infinite;
}

.voice-wave.active i:nth-child(2) {
  animation-delay: 0.12s;
}

.voice-wave.active i:nth-child(3) {
  animation-delay: 0.24s;
}

.voice-wave.active i:nth-child(4) {
  animation-delay: 0.36s;
}

.input-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 38px 38px 38px;
  align-items: center;
  gap: 8px;
}

:deep(.input-bar .el-input__wrapper) {
  height: 44px;
  border-radius: 9px;
  background: var(--input-bg);
  box-shadow: inset 0 0 0 1px var(--input-border);
}

:deep(.input-bar .el-input__inner) {
  color: var(--primary-text);
}

.call-btn,
.mic-btn,
.send-btn {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 9px;
  color: #ffffff;
  cursor: pointer;
}

.call-btn {
  color: var(--accent);
  background: var(--accent-soft);
  border: 1px solid var(--card-border);
}

.mic-btn {
  background: var(--accent);
}

.mic-btn.active {
  background: #ff3b3b;
}

.send-btn {
  background: var(--primary-text);
}

.theme-dark .send-btn {
  background: #294a7e;
}

.call-btn:disabled,
.mic-btn:disabled,
.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.call-view {
  position: absolute;
  inset: 0;
  z-index: 9;
  padding: 20px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  gap: 16px;
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.1), transparent 24%),
    linear-gradient(180deg, rgba(255, 248, 244, 0.98), rgba(255, 255, 255, 0.98));
}

.theme-dark .call-view {
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.08), transparent 24%),
    linear-gradient(180deg, rgba(14, 26, 45, 0.98), rgba(17, 28, 48, 0.98));
}

.call-view-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.call-view-head span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 900;
}

.call-view-head h3 {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 900;
}

.call-view-actions {
  display: flex;
  gap: 10px;
}

.call-back-btn,
.call-end-btn {
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.call-back-btn {
  border: 1px solid var(--card-border);
  color: var(--muted-text);
  background: var(--panel-bg);
}

.call-end-btn {
  border: none;
  color: #ffffff;
  background: #ff5a5a;
}

.call-view-stage {
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.call-card,
.call-question-card {
  border: 1px solid var(--card-border);
  border-radius: 24px;
  background: color-mix(in srgb, var(--panel-bg) 82%, var(--card-bg) 18%);
  box-shadow: 0 16px 36px rgba(27, 36, 56, 0.08);
}

.call-card {
  min-height: 0;
  padding: 16px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 14px;
}

.call-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.call-card-head strong {
  font-size: 16px;
  font-weight: 900;
}

.call-card-head span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 800;
}

.call-avatar-panel,
.camera-panel {
  min-height: 0;
  border-radius: 18px;
  overflow: hidden;
  background: #0f172a;
}

.call-avatar-panel {
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-panel {
  position: relative;
  display: grid;
  place-items: center;
}

.call-chat-panel {
  min-height: 0;
  padding: 8px;
  display: grid;
  align-content: start;
  gap: 12px;
  overflow: auto;
  border-radius: 18px;
  background: color-mix(in srgb, var(--panel-bg) 84%, var(--card-bg) 16%);
}

.call-chat-item {
  display: grid;
  justify-items: start;
  gap: 8px;
}

.call-chat-item.user {
  justify-items: end;
}

.call-chat-role {
  padding: 4px 9px;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 11px;
  font-weight: 900;
}

.call-chat-bubble {
  max-width: min(100%, 420px);
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid var(--card-border);
  background: var(--bot-bubble-bg);
}

.call-chat-item.user .call-chat-bubble {
  color: #ffffff;
  background: var(--user-bubble-bg);
}

.call-chat-bubble strong {
  color: var(--accent);
  font-size: 12px;
}

.call-chat-item.user .call-chat-bubble strong {
  color: rgba(255, 255, 255, 0.86);
}

.call-chat-bubble p {
  margin-top: 6px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.call-chat-item.waiting {
  justify-items: start;
}

.call-chat-empty {
  min-height: 160px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: var(--muted-text);
  text-align: center;
}

.call-chat-empty strong {
  color: var(--primary-text);
  font-size: 15px;
}

.camera-panel video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.camera-fallback {
  padding: 24px;
  display: grid;
  justify-items: center;
  gap: 10px;
  color: #ffffff;
  text-align: center;
}

.camera-fallback .el-icon {
  font-size: 34px;
  color: #ffb173;
}

.camera-fallback strong {
  font-size: 16px;
  font-weight: 900;
}

.camera-fallback p {
  max-width: 280px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.7;
}

.call-question-card {
  padding: 18px 20px;
  display: grid;
  gap: 8px;
}

.call-question-card span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 900;
}

.call-question-card strong {
  color: var(--primary-text);
  font-size: 16px;
  line-height: 1.8;
}

.call-footer {
  display: grid;
  gap: 10px;
}

.call-live-card {
  margin-bottom: 2px;
}

.call-input-bar {
  padding: 0;
}

.loader {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 3px solid rgba(255, 90, 42, 0.22);
  border-top-color: var(--accent);
  animation: spin 0.8s linear infinite;
}

.report-page {
  min-height: 0;
  overflow: auto;
  padding: 22px;
  display: grid;
  align-content: start;
  gap: 18px;
}

.report-loading-stage {
  min-height: 100%;
  display: grid;
  place-items: center;
  align-content: center;
  justify-items: center;
  gap: 14px;
  text-align: center;
  color: var(--muted-text);
}

.report-loading-stage strong {
  color: var(--primary-text);
  font-size: 18px;
  font-weight: 900;
}

.report-loading-stage p {
  max-width: 420px;
  line-height: 1.8;
}

.report-loader-orbit {
  width: 98px;
  height: 98px;
  position: relative;
}

.report-loader-orbit span {
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.report-loader-orbit span:nth-child(2) {
  inset: 10px;
  border-top-color: #416ee6;
  animation-duration: 1.3s;
}

.report-loader-orbit span:nth-child(3) {
  inset: 22px;
  border-top-color: #26b96d;
  animation-duration: 1.6s;
}

.report-hero {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.report-score-panel {
  padding: 24px;
  display: grid;
  gap: 8px;
  border-radius: 18px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff7a45 56%, #ff9c6b 100%);
  box-shadow: var(--report-hero-shadow);
}

.report-score-panel span {
  font-size: 12px;
  opacity: 0.82;
}

.report-score-panel strong {
  font-size: 58px;
  line-height: 1;
  font-weight: 900;
}

.report-score-panel p {
  max-width: 420px;
  line-height: 1.8;
  opacity: 0.96;
}

.report-overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.report-overview-grid article,
.report-meta-row div,
.report-section-card,
.review-card {
  border: 1px solid var(--report-card-border);
  background: var(--report-card-bg);
  box-shadow: var(--report-shadow);
}

.report-overview-grid article {
  min-height: 122px;
  padding: 20px;
  display: grid;
  align-content: space-between;
  border-radius: 16px;
}

.report-overview-grid span,
.report-meta-row span,
.section-head p,
.review-order,
.review-meta span,
.review-answer span,
.keyword-row span {
  color: var(--muted-text);
  font-size: 12px;
}

.report-overview-grid strong {
  color: var(--primary-text);
  font-size: 28px;
  font-weight: 900;
}

.report-meta-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.report-meta-row div {
  padding: 16px 18px;
  display: grid;
  gap: 8px;
  border-radius: 14px;
  position: relative;
}

.report-meta-row div::after,
.report-section-card::after,
.review-card::after {
  content: "";
  position: absolute;
  left: 18px;
  right: 18px;
  top: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, var(--report-divider) 18%, var(--report-divider) 82%, transparent 100%);
}

.report-meta-row strong {
  color: var(--primary-text);
  font-size: 14px;
  font-weight: 800;
}

.dimension-panel {
  display: grid;
  grid-template-columns: minmax(300px, 0.94fr) minmax(0, 1.06fr);
  gap: 14px;
}

.radar-card,
.dimension-card {
  border: 1px solid var(--report-card-border);
  background: var(--report-card-bg);
  box-shadow: var(--report-shadow);
}

.radar-card {
  min-height: 360px;
  padding: 18px;
  display: grid;
  grid-template-rows: auto minmax(260px, 1fr);
  gap: 12px;
  border-radius: 16px;
}

.radar-chart {
  width: 100%;
  min-height: 282px;
}

.dimension-card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.dimension-card {
  min-height: 124px;
  padding: 16px;
  display: grid;
  align-content: space-between;
  gap: 10px;
  border-radius: 14px;
  overflow: hidden;
}

.dimension-card div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dimension-card span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 900;
}

.dimension-card strong {
  color: var(--accent);
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}

.dimension-card p {
  min-height: 38px;
  color: var(--report-text);
  font-size: 13px;
  line-height: 1.55;
}

.dimension-card i {
  height: 6px;
  display: block;
  border-radius: 999px;
  background: linear-gradient(90deg, #ff5a2a, #416ee6);
}

.report-section-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.report-section-card {
  padding: 18px;
  border-radius: 16px;
  position: relative;
}

.report-section-card h3,
.section-head h3,
.review-top h4,
.review-columns h5 {
  color: var(--primary-text);
  font-weight: 900;
}

.report-section-card h3,
.section-head h3 {
  font-size: 16px;
}

.report-section-card ul,
.review-columns ul {
  margin: 12px 0 0;
  padding-left: 18px;
  color: #5d6676;
  line-height: 1.8;
}

.question-review-section {
  display: grid;
  gap: 14px;
}

.section-head {
  display: grid;
  gap: 6px;
}

.review-card {
  padding: 20px;
  display: grid;
  gap: 16px;
  border-radius: 16px;
  position: relative;
}

.review-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.review-top h4 {
  margin-top: 6px;
  font-size: 17px;
  line-height: 1.6;
}

.review-score {
  min-width: 92px;
  padding: 12px;
  display: grid;
  justify-items: center;
  gap: 4px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--accent-soft) 78%, var(--panel-bg) 22%);
  border: 1px solid var(--report-card-border);
}

.review-score strong {
  color: var(--accent);
  font-size: 32px;
  line-height: 1;
  font-weight: 900;
}

.review-score span {
  color: var(--muted-text);
  font-size: 12px;
}

.review-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.review-answer {
  display: grid;
  gap: 8px;
}

.review-answer p,
.review-summary {
  color: var(--report-text);
  line-height: 1.8;
  white-space: pre-wrap;
}

.review-dimension-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-dimension-row span {
  min-height: 28px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
  font-size: 12px;
  font-weight: 900;
}

.review-dimension-row span.strong {
  color: #168052;
  background: rgba(36, 168, 101, 0.1);
}

.review-dimension-row span.weak {
  color: #d3462f;
  background: rgba(211, 70, 47, 0.1);
}

.review-columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.review-columns section {
  min-width: 0;
  padding: 14px;
  border-radius: 14px;
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
}

.keyword-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.keyword-row div {
  padding: 14px;
  border-radius: 14px;
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
}

.keyword-row p {
  margin-top: 8px;
  color: var(--report-text);
  line-height: 1.8;
}

.report-empty {
  min-height: 320px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  text-align: center;
  border: 1px solid var(--report-card-border);
  border-radius: 18px;
  color: var(--muted-text);
  background: var(--report-card-bg);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.45; }
  40% { transform: scale(1); opacity: 1; }
}

@keyframes voiceWave {
  0%, 100% {
    height: 8px;
    opacity: 0.45;
  }
  50% {
    height: 24px;
    opacity: 1;
  }
}

@keyframes orbitPulse {
  0% {
    transform: scale(0.9);
    opacity: 0.84;
  }
  100% {
    transform: scale(1.08);
    opacity: 0;
  }
}

.call-stage-enter-active,
.call-stage-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.call-stage-enter-from,
.call-stage-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

.call-view-enter-active,
.call-view-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.call-view-enter-from,
.call-view-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 900px) {
  .session-page {
    height: auto;
    min-height: 100vh;
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .interviewer-panel {
    padding: 36px 22px;
  }

  .dialog-panel {
    min-height: 680px;
  }

  .dialog-head {
    height: auto;
    padding: 14px 18px;
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .head-actions {
    width: 100%;
    justify-content: space-between;
  }

  .content-tabs,
  .report-page {
    padding-left: 18px;
    padding-right: 18px;
  }

  .call-view {
    padding: 14px;
    grid-template-rows: auto minmax(0, auto) auto auto;
  }

  .call-view-head,
  .call-view-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .call-view-stage {
    grid-template-columns: 1fr;
  }

  .report-hero,
  .report-meta-row,
  .dimension-panel,
  .dimension-card-list,
  .report-section-grid,
  .review-columns,
  .keyword-row {
    grid-template-columns: 1fr;
  }

  .review-top {
    flex-direction: column;
  }
}
</style>
