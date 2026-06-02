<template>
  <div class="avatar-shell" :style="avatarShellStyle">
    <div ref="containerRef" class="avatar-canvas"></div>

    <div v-if="loading" class="avatar-overlay">
      <span class="avatar-brand"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
      <span class="loader"></span>
      <strong>正在连接数字人...</strong>
    </div>

    <div v-else-if="error" class="avatar-overlay error">
      <span class="avatar-brand"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
      <strong>数字人暂不可用</strong>
      <p>{{ error }}</p>
    </div>

    <div v-else-if="!session?.connected" class="avatar-overlay idle">
      <span class="avatar-brand"><img src="/images/shuzhimianpin_logo.png" alt="AI 面试官" /></span>
      <strong>AI 面试官</strong>
      <p>数智面聘数字面试官正在待命</p>
    </div>

    <button v-else-if="resumeNeeded" type="button" class="resume-btn" @click="resumePlayback">
      点击唤醒数字面试官
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import type { AIInterviewAvatarSession } from '@/api/aiInterview'

const props = defineProps<{
  session: AIInterviewAvatarSession | null
  loading?: boolean
  error?: string
}>()

const emit = defineEmits<{
  (event: 'ready'): void
  (event: 'error', message: string): void
}>()

const containerRef = ref<HTMLElement | null>(null)
const resumeNeeded = ref(false)
const sdkLoaded = ref(false)

const avatarShellStyle = computed(() => {
  const width = props.session?.width || 720
  const height = props.session?.height || 1280
  const ratio = width > 0 && height > 0 ? width / height : 720 / 1280
  return {
    '--avatar-ratio': String(ratio),
    '--avatar-max-height': 'max(280px, calc(100vh - 360px))'
  }
})

let RTCPlayerCtor: any = null
let player: any = null
let activeStreamKey = ''
const dynamicImporter = new Function('path', 'return import(path)') as (path: string) => Promise<any>

const buildStreamKey = (session: AIInterviewAvatarSession | null) => {
  if (!session?.connected) return ''
  return [
    session.sid,
    session.server,
    session.auth,
    session.appid,
    session.userId,
    session.roomId,
    session.timeStr
  ].join('|')
}

const loadSdk = async () => {
  if (sdkLoaded.value && RTCPlayerCtor) return
  const module = await dynamicImporter('/vendor/xfyun/rtcplayer.esm.js')
  RTCPlayerCtor = module?.RTCPlayer || module?.default?.RTCPlayer || module?.default
  if (!RTCPlayerCtor) {
    throw new Error('未能加载讯飞 RTCPlayer SDK')
  }
  sdkLoaded.value = true
}

const destroyPlayer = () => {
  resumeNeeded.value = false
  if (!player) return
  try {
    player.off?.('play')
    player.off?.('playing')
    player.off?.('waiting')
    player.off?.('error')
    player.off?.('not-allowed')
    player.stop?.()
    player.destroy?.()
    player.close?.()
  } catch {
    // 忽略第三方播放器内部清理异常
  } finally {
    player = null
  }
}

const initPlayer = async (session: AIInterviewAvatarSession) => {
  if (!containerRef.value || !session.connected) return
  await loadSdk()
  destroyPlayer()

  player = new RTCPlayerCtor()
  player.on?.('playing', () => {
    resumeNeeded.value = false
    emit('ready')
  })
  player.on?.('not-allowed', () => {
    resumeNeeded.value = true
  })
  player.on?.('error', (error: unknown) => {
    const message = error instanceof Error ? error.message : '数字人播放器启动失败'
    emit('error', message)
  })

  player.playerType = session.playerType || 12
  player.stream = {
    sid: session.sid,
    server: session.server,
    auth: session.auth,
    appid: session.appid,
    userId: session.userId,
    roomId: session.roomId,
    timeStr: session.timeStr
  }
  player.videoSize = {
    width: session.width || 720,
    height: session.height || 1280
  }
  player.container = containerRef.value
  player.play?.()
}

const resumePlayback = () => {
  try {
    player?.resume?.()
    resumeNeeded.value = false
  } catch (error) {
    const message = error instanceof Error ? error.message : '恢复数字人播放失败'
    emit('error', message)
  }
}

watch(
  () => buildStreamKey(props.session),
  async (streamKey) => {
    if (!streamKey || streamKey === activeStreamKey) return
    activeStreamKey = streamKey
    try {
      await initPlayer(props.session as AIInterviewAvatarSession)
    } catch (error) {
      const message = error instanceof Error ? error.message : '数字人播放器初始化失败'
      emit('error', message)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<style scoped>
.avatar-shell {
  position: relative;
  width: min(100%, calc(var(--avatar-max-height) * var(--avatar-ratio)));
  max-width: 100%;
  max-height: var(--avatar-max-height);
  aspect-ratio: var(--avatar-ratio);
  padding: 4px;
  border-radius: 28px;
  overflow: hidden;
  isolation: isolate;
  background: transparent;
  box-shadow: none;
  flex: 0 1 auto;
}

.avatar-shell::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(255, 154, 120, 0.55);
  box-shadow:
    0 0 18px rgba(255, 134, 89, 0.22),
    0 0 38px rgba(255, 168, 133, 0.12);
  animation: border-pulse 3.2s ease-in-out infinite;
  pointer-events: none;
  z-index: 2;
}

.avatar-shell::after {
  content: '';
  position: absolute;
  inset: -18%;
  border-radius: inherit;
  background:
    linear-gradient(115deg, rgba(255, 122, 77, 0) 28%, rgba(255, 165, 132, 0.18) 50%, rgba(255, 122, 77, 0) 72%);
  filter: blur(16px);
  pointer-events: none;
  z-index: 0;
  animation: border-sweep 6.5s linear infinite;
}

.avatar-canvas {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.16);
}

.avatar-overlay {
  position: absolute;
  inset: 4px;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  text-align: center;
  color: #2c2f3a;
  border-radius: 24px;
  background: rgba(255, 248, 244, 0.9);
}

.avatar-overlay.error {
  color: #7a3a2d;
}

.avatar-brand {
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  overflow: hidden;
  background: transparent;
  box-shadow:
    0 18px 44px rgba(255, 90, 42, 0.24),
    0 0 0 8px rgba(255, 106, 0, 0.08);
}

.avatar-brand img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay p {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.resume-btn {
  position: absolute;
  left: 50%;
  bottom: 28px;
  z-index: 3;
  transform: translateX(-50%);
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff6a35, #ff875f);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(255, 90, 42, 0.24);
}

.loader {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(255, 90, 42, 0.16);
  border-top-color: #ff5a2a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes border-sweep {
  to {
    transform: rotate(360deg);
  }
}

@keyframes border-pulse {
  0%,
  100% {
    opacity: 0.72;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.01);
  }
}
</style>
