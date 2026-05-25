<template>
  <div class="loading-page">
    <div class="loading-shell">
      <section class="loading-hero">
        <span class="eyebrow">INTELLIVIEW INTERVIEW</span>
        <h1>AI 正在搭建你的模拟面试现场</h1>
        <p>加载页会直接完成岗位理解、题目生成和面试启动，不再只是播放一个占位动画。</p>

        <div class="hero-grid">
          <article>
            <span>目标岗位</span>
            <strong>{{ targetRole }}</strong>
          </article>
          <article>
            <span>面试名称</span>
            <strong>{{ interviewName }}</strong>
          </article>
          <article>
            <span>聚焦技术</span>
            <strong>{{ techLabel }}</strong>
          </article>
        </div>
      </section>

      <section class="progress-card">
        <div class="signal-stage">
          <div class="signal-core">
            <span>{{ progress }}%</span>
            <small>{{ errorMessage ? '异常中断' : '准备中' }}</small>
          </div>
          <div class="signal-ring ring-a"></div>
          <div class="signal-ring ring-b"></div>
          <div class="signal-ring ring-c"></div>
        </div>

        <div class="progress-panel">
          <div class="progress-head">
            <div>
              <span>当前进度</span>
              <strong>{{ errorMessage ? '加载失败' : activeStage.title }}</strong>
            </div>
            <em>{{ errorMessage || activeStage.copy }}</em>
          </div>

          <div class="progress-bar">
            <i :style="{ width: `${progress}%` }"></i>
          </div>

          <div class="stage-list">
            <article
              v-for="(stage, index) in loadingStages"
              :key="stage.title"
              :class="{
                done: progress >= stage.threshold && !errorMessage,
                active: activeStageIndex === index && !errorMessage
              }"
            >
              <b>{{ String(index + 1).padStart(2, '0') }}</b>
              <div>
                <strong>{{ stage.title }}</strong>
                <p>{{ stage.copy }}</p>
              </div>
            </article>
          </div>

          <div v-if="errorMessage" class="error-row">
            <button type="button" class="ghost-btn" @click="goBack">
              返回配置页
            </button>
            <button type="button" class="primary-btn" @click="startPreparing">
              重新尝试
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import aiInterviewApi, { type AIInterviewCreatePayload } from '@/api/aiInterview'

const route = useRoute()
const router = useRouter()
const progress = ref(4)
const errorMessage = ref('')
const preparing = ref(false)

const loadingStages = [
  { threshold: 18, title: '解析岗位画像', copy: '正在提炼岗位关键词与追问方向。' },
  { threshold: 44, title: '匹配简历亮点', copy: '结合简历和技术栈建立提问重点。' },
  { threshold: 74, title: '生成面试题流', copy: '创建开场、主问题与追问节奏。' },
  { threshold: 100, title: '启动面试会话', copy: '正在进入面试对话页面。' }
] as const

const draftKey = computed(() => String(route.query.draft || ''))
const targetRole = computed(() => String(route.query.role || '岗位化模拟面试'))
const interviewName = computed(() => String(route.query.name || '模拟面试'))
const techLabel = computed(() => String(route.query.tech || '技术栈准备中'))
const activeStageIndex = computed(() => {
  const currentIndex = loadingStages.findIndex((stage) => progress.value < stage.threshold)
  return currentIndex === -1 ? loadingStages.length - 1 : currentIndex
})
const activeStage = computed(() => loadingStages[activeStageIndex.value])

let progressTimer: number | undefined

const stopProgressTimer = () => {
  if (progressTimer) {
    window.clearInterval(progressTimer)
    progressTimer = undefined
  }
}

const goBack = () => {
  router.replace('/user/interview/ai/create')
}

const readDraftPayload = () => {
  if (!draftKey.value) return null
  try {
    const raw = sessionStorage.getItem(draftKey.value)
    if (!raw) return null
    const payload = JSON.parse(raw) as AIInterviewCreatePayload
    if (!payload.clientRequestId) {
      payload.clientRequestId = createClientRequestId()
      sessionStorage.setItem(draftKey.value, JSON.stringify(payload))
    }
    return payload
  } catch {
    return null
  }
}

const createClientRequestId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `interview-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const finishLoading = (interviewId: number) => {
  progress.value = 100
  window.setTimeout(() => {
    router.replace({
      name: 'AIInterviewSession',
      params: { id: interviewId }
    })
  }, 420)
}

const startProgressAnimation = () => {
  stopProgressTimer()
  progress.value = 6
  progressTimer = window.setInterval(() => {
    if (errorMessage.value) {
      stopProgressTimer()
      return
    }

    const current = progress.value
    if (current >= 88) return
    if (current < 20) {
      progress.value = Math.min(88, current + 5)
      return
    }
    if (current < 48) {
      progress.value = Math.min(88, current + 3)
      return
    }
    if (current < 72) {
      progress.value = Math.min(88, current + 2)
      return
    }
    progress.value = Math.min(88, current + 1)
  }, 180)
}

const startPreparing = async () => {
  if (preparing.value) return

  const payload = readDraftPayload()
  if (!payload) {
    errorMessage.value = '未找到本次面试配置，请重新创建。'
    progress.value = 0
    ElMessage.warning('面试配置已失效，请重新开始')
    return
  }

  preparing.value = true
  errorMessage.value = ''
  startProgressAnimation()

  try {
    progress.value = Math.max(progress.value, 16)
    const session = await aiInterviewApi.createInterview(payload)
    progress.value = Math.max(progress.value, 62)
    await aiInterviewApi.startInterview(session.interviewId)
    sessionStorage.removeItem(draftKey.value)
    stopProgressTimer()
    finishLoading(session.interviewId)
  } catch (error: any) {
    stopProgressTimer()
    errorMessage.value = error?.message || 'AI 面试创建失败，请稍后重试'
    progress.value = Math.min(progress.value, 82)
    ElMessage.error(errorMessage.value)
  } finally {
    preparing.value = false
  }
}

onMounted(() => {
  startPreparing()
})

onUnmounted(() => {
  stopProgressTimer()
})
</script>

<style scoped>
.loading-page {
  min-height: 100vh;
  padding: 32px;
  display: grid;
  place-items: center;
  background:
    radial-gradient(circle at top left, rgba(255, 128, 77, 0.22), transparent 34%),
    radial-gradient(circle at bottom right, rgba(255, 196, 120, 0.16), transparent 30%),
    linear-gradient(145deg, #fff7f2 0%, #fff1ea 42%, #f8f9fc 100%);
  color: #232634;
  overflow: hidden;
}

.loading-page::before,
.loading-page::after {
  content: "";
  position: fixed;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(24px);
}

.loading-page::before {
  width: 280px;
  height: 280px;
  top: -72px;
  right: -52px;
  background: rgba(255, 90, 42, 0.16);
}

.loading-page::after {
  width: 220px;
  height: 220px;
  bottom: -48px;
  left: -36px;
  background: rgba(255, 180, 108, 0.14);
}

.loading-shell {
  width: min(1160px, 100%);
  display: grid;
  grid-template-columns: minmax(320px, 0.94fr) minmax(420px, 1.06fr);
  gap: 28px;
  align-items: center;
}

.loading-hero,
.progress-card {
  position: relative;
  border: 1px solid rgba(255, 161, 128, 0.32);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  box-shadow: 0 28px 70px rgba(255, 90, 42, 0.12);
}

.loading-hero {
  padding: 38px;
  display: grid;
  gap: 18px;
}

.eyebrow {
  width: fit-content;
  padding: 7px 12px;
  border-radius: 999px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 11px;
  font-weight: 900;
}

.loading-hero h1 {
  font-size: 42px;
  line-height: 1.1;
  font-weight: 900;
}

.loading-hero p {
  max-width: 460px;
  color: #697386;
  line-height: 1.9;
}

.hero-grid {
  margin-top: 6px;
  display: grid;
  gap: 12px;
}

.hero-grid article {
  padding: 16px 18px;
  display: grid;
  gap: 8px;
  border-radius: 18px;
  border: 1px solid rgba(255, 176, 142, 0.26);
  background: linear-gradient(135deg, rgba(255, 241, 235, 0.9), rgba(255, 255, 255, 0.84));
}

.hero-grid span {
  color: #8b93a4;
  font-size: 12px;
  font-weight: 800;
}

.hero-grid strong {
  font-size: 18px;
  line-height: 1.5;
}

.progress-card {
  padding: 32px;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 26px;
  align-items: center;
}

.signal-stage {
  position: relative;
  width: 220px;
  height: 220px;
  margin: 0 auto;
  display: grid;
  place-items: center;
}

.signal-core {
  position: relative;
  z-index: 2;
  width: 132px;
  height: 132px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 6px;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff7d47 60%, #ffb071 100%);
  box-shadow: 0 18px 38px rgba(255, 90, 42, 0.28);
}

.signal-core span {
  font-size: 34px;
  line-height: 1;
  font-weight: 900;
}

.signal-core small {
  font-size: 12px;
  letter-spacing: 0.04em;
}

.signal-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1px solid rgba(255, 90, 42, 0.16);
}

.ring-a {
  animation: pulseRing 2.6s ease-out infinite;
}

.ring-b {
  inset: 18px;
  animation: pulseRing 2.6s ease-out infinite 0.35s;
}

.ring-c {
  inset: 36px;
  animation: pulseRing 2.6s ease-out infinite 0.7s;
}

.progress-panel {
  display: grid;
  gap: 18px;
}

.progress-head {
  display: grid;
  gap: 10px;
}

.progress-head span {
  color: #8c95a6;
  font-size: 12px;
  font-weight: 900;
}

.progress-head strong {
  display: block;
  margin-top: 6px;
  font-size: 28px;
  font-weight: 900;
}

.progress-head em {
  color: #697386;
  font-style: normal;
  line-height: 1.8;
}

.progress-bar {
  height: 12px;
  padding: 2px;
  border-radius: 999px;
  background: #ffe4d8;
}

.progress-bar i {
  height: 100%;
  display: block;
  border-radius: inherit;
  background: linear-gradient(90deg, #ff5a2a 0%, #ff874f 56%, #ffbe7b 100%);
  transition: width 0.25s ease;
}

.stage-list {
  display: grid;
  gap: 12px;
}

.stage-list article {
  padding: 14px 16px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  border-radius: 18px;
  border: 1px solid #eef1f6;
  background: #ffffff;
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.stage-list article.active {
  transform: translateX(6px);
  border-color: rgba(255, 90, 42, 0.32);
  box-shadow: 0 14px 28px rgba(255, 90, 42, 0.1);
}

.stage-list article.done {
  background: linear-gradient(135deg, rgba(255, 241, 235, 0.82), rgba(255, 255, 255, 0.98));
}

.stage-list b {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 13px;
  font-weight: 900;
}

.stage-list strong {
  font-size: 15px;
  font-weight: 900;
}

.stage-list p {
  margin-top: 6px;
  color: #768094;
  font-size: 13px;
  line-height: 1.7;
}

.error-row {
  display: flex;
  gap: 12px;
}

.ghost-btn,
.primary-btn {
  height: 44px;
  padding: 0 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
}

.ghost-btn {
  border: 1px solid #f0c7b4;
  color: #94604d;
  background: #fff8f4;
}

.primary-btn {
  border: none;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff854d 60%, #ffb173 100%);
  box-shadow: 0 12px 28px rgba(255, 90, 42, 0.18);
}

@keyframes pulseRing {
  0% {
    transform: scale(0.88);
    opacity: 0.88;
  }
  100% {
    transform: scale(1.08);
    opacity: 0;
  }
}

@media (max-width: 960px) {
  .loading-page {
    padding: 20px;
  }

  .loading-shell,
  .progress-card {
    grid-template-columns: 1fr;
  }

  .loading-hero h1 {
    font-size: 34px;
  }
}

@media (max-width: 640px) {
  .loading-hero,
  .progress-card {
    padding: 22px;
    border-radius: 22px;
  }

  .signal-stage {
    width: 184px;
    height: 184px;
  }

  .signal-core {
    width: 116px;
    height: 116px;
  }

  .signal-core span {
    font-size: 28px;
  }

  .error-row {
    flex-direction: column;
  }
}
</style>
