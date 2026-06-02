<template>
  <div class="interview-create-page">
    <aside class="app-sidebar">
      <router-link to="/user" class="brand">
        <span class="brand-logo"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="side-nav" aria-label="模拟面试导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/interview/ai/create' }">
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <main class="create-main">
      <div class="create-shell">
        <section class="create-hero">
          <span class="eyebrow">AI INTERVIEW</span>
          <h1>开始模拟面试</h1>
          <p>选择岗位和面试模式后，进入一场更贴近真实场景的岗位化模拟面试。</p>

          <div class="stage-preview">
            <div class="stage-head">
              <span></span>
              <strong>{{ setupTitle }}</strong>
            </div>
            <div class="stage-dialog">
              <i><img src="/images/shuzhimianpin_logo.png" alt="" /></i>
              <p>{{ previewQuestion }}</p>
            </div>
            <div class="stage-tags">
              <template v-if="form.interviewMode === 'RESUME'">
                <span>简历项目</span>
                <span>技能关键词</span>
                <span>经历追问</span>
              </template>
              <template v-else>
                <span v-for="tech in previewTechStacks.slice(0, 4)" :key="tech">{{ tech }}</span>
                <span v-if="!previewTechStacks.length">待选择技术栈</span>
              </template>
            </div>
          </div>

          <div class="hero-metrics">
            <article><strong>{{ roleBanks.length || 0 }}</strong><span>岗位方向</span></article>
            <article><strong>{{ currentTechOptions.length }}</strong><span>可选技术</span></article>
            <article><strong>{{ resumes.length }}</strong><span>已存简历</span></article>
          </div>
        </section>

        <section class="setup-card">
          <div class="card-head">
            <div>
              <span>配置面试</span>
              <h2>{{ setupTitle }}</h2>
            </div>
            <small>{{ techSummary }}</small>
          </div>

          <div class="mode-switch">
            <button
              type="button"
              :class="{ active: form.interviewMode === 'CUSTOM' }"
              @click="form.interviewMode = 'CUSTOM'"
            >
              <strong>自定义面试</strong>
              <span>以选择岗位和技术栈为主，简历只用于追问素材</span>
            </button>
            <button
              type="button"
              :class="{ active: form.interviewMode === 'RESUME' }"
              @click="form.interviewMode = 'RESUME'"
            >
              <strong>按简历面试</strong>
              <span>只选择简历，系统根据简历意向与经历生成问题</span>
            </button>
          </div>

          <div v-if="form.interviewMode === 'CUSTOM'" class="field">
            <strong>目标岗位</strong>
            <div class="pill-row">
              <button
                v-for="role in roleBanks"
                :key="role.code"
                type="button"
                :class="{ active: form.role === role.code }"
                @click="selectRole(role.code)"
              >
                {{ role.name }}
              </button>
            </div>
          </div>

          <div class="field">
            <strong>面试名称</strong>
            <el-input
              v-model="form.interviewName"
              maxlength="100"
              show-word-limit
              placeholder="例如：字节前端一面模拟、Java 后端查漏补缺"
            />
          </div>

          <div v-if="form.interviewMode === 'CUSTOM'" class="field">
            <strong>技术选择</strong>
            <div class="tech-grid">
              <button
                v-for="tech in currentTechOptions"
                :key="tech"
                type="button"
                :class="{ active: form.techStacks.includes(tech) }"
                @click="toggleTech(tech)"
              >
                <el-icon><Connection /></el-icon>
                {{ tech }}
              </button>
            </div>
          </div>

          <div class="resume-row">
            <div>
              <strong>{{ form.interviewMode === 'RESUME' ? '简历（必选）' : '简历（可选）' }}</strong>
              <p>{{ resumeModeHint }}</p>
            </div>
            <div class="resume-actions">
              <el-select v-model="selectedResumeId" clearable placeholder="选择已有简历" class="resume-select">
                <el-option
                  v-for="resume in resumes"
                  :key="resume.id"
                  :label="resume.originalFileName"
                  :value="resume.id"
                />
              </el-select>
              <el-upload :auto-upload="false" :show-file-list="false" accept=".pdf,.docx,.txt,.md" :on-change="handleResume">
                <el-button class="upload-btn" :loading="resumeUploading">
                  <el-icon><Upload /></el-icon>
                  上传新简历
                </el-button>
              </el-upload>
            </div>
          </div>

          <div v-if="selectedResume" class="resume-preview">
            <strong>{{ selectedResume.originalFileName }}</strong>
            <p>{{ selectedResume.summary || '已选择简历，将用于面试评估。' }}</p>
            <div>
              <span>岗位：{{ selectedResume.intentionJob || '-' }}</span>
              <span>类型：{{ selectedResume.recruitmentType || '-' }}</span>
              <span>城市：{{ selectedResume.intentionCity || '-' }}</span>
              <span>薪资：{{ selectedResume.expectedSalary || '-' }}</span>
            </div>
          </div>

          <div class="start-row">
            <el-button type="primary" class="start-btn" @click="startInterview">
              <el-icon><VideoPlay /></el-icon>
              开始面试
            </el-button>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Collection, Connection, House, Monitor, Star, TrendCharts, Upload, User, VideoPlay } from '@element-plus/icons-vue'
import type { AIInterviewCreatePayload } from '@/api/aiInterview'
import jobRoleApi, { type JobRoleConfig, type JobRoleOption } from '@/api/jobRoles'
import resumeApi, { type UserResume } from '@/api/resumes'

const router = useRouter()
const roleBanks = ref<JobRoleOption[]>([])
const currentRoleConfig = ref<JobRoleConfig | null>(null)
const resumes = ref<UserResume[]>([])
const selectedResumeId = ref<number | null>(null)
const resumeUploading = ref(false)
const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '成长中心', path: '/user/growth', icon: TrendCharts },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const form = reactive({
  interviewMode: 'CUSTOM' as 'CUSTOM' | 'RESUME',
  role: '',
  techStacks: [] as string[],
  interviewName: ''
})

const currentTechOptions = computed(() => {
  const role = roleBanks.value.find((item) => item.code === form.role)
  const config = currentRoleConfig.value?.role
  if (config?.typicalTechStack?.length) {
    return config.typicalTechStack
  }
  if (config?.interviewFocus?.length) {
    return config.interviewFocus
  }
  return role?.typicalTechStack || role?.interviewFocus || []
})
const selectedResume = computed(() => resumes.value.find((resume) => resume.id === selectedResumeId.value) || null)
const selectedRole = computed(() => roleBanks.value.find((item) => item.code === form.role))
const selectedRoleName = computed(() => selectedRole.value?.name || '岗位化模拟面试')
const setupTitle = computed(() => form.interviewMode === 'RESUME' ? '按简历生成面试' : selectedRoleName.value)
const roleDefaultTechStacks = computed(() => currentTechOptions.value.slice(0, 3))
const effectiveTechStacks = computed(() => form.interviewMode === 'RESUME' ? roleDefaultTechStacks.value : form.techStacks)
const previewTechStacks = computed(() => effectiveTechStacks.value.length ? effectiveTechStacks.value : currentTechOptions.value.slice(0, 4))
const techSummary = computed(() => form.interviewMode === 'RESUME'
  ? '简历驱动'
  : (form.techStacks.length ? `${form.techStacks.length} 项技术已选` : '请选择技术栈'))
const resumeModeHint = computed(() => form.interviewMode === 'CUSTOM'
  ? '当前以选择岗位为主，简历会作为项目经历和追问素材'
  : '当前只需选择简历，系统会围绕简历意向、技能和项目经历追问')
const previewQuestion = computed(() => {
  if (form.interviewMode === 'RESUME') {
    return '请先介绍一下简历中最能体现你能力的一段项目经历，我会基于项目继续追问。'
  }
  const focus = currentRoleConfig.value?.role?.interviewFocus || selectedRole.value?.interviewFocus || []
  if (focus.length) {
    return `请结合你的项目经历，围绕 ${focus[0]} 说说一次你解决技术问题的过程。`
  }
  return '请结合你的项目经历，说明一次你解决复杂技术问题的过程。'
})
const LOADING_DRAFT_PREFIX = 'intelliview-ai-interview-draft:'

const selectRole = async (role: string) => {
  form.role = role
  try {
    currentRoleConfig.value = await jobRoleApi.getJobRoleConfig(role)
  } catch (error) {
    console.error('加载岗位配置失败:', error)
    currentRoleConfig.value = null
  }

  form.techStacks = currentTechOptions.value.slice(0, 3)
  if (!form.interviewName.trim() || form.interviewName.includes('模拟面试')) {
    const roleName = roleBanks.value.find((item) => item.code === role)?.name || '岗位'
    form.interviewName = `${roleName}模拟面试`
  }
}

const toggleTech = (tech: string) => {
  if (form.techStacks.includes(tech)) {
    form.techStacks = form.techStacks.filter((item) => item !== tech)
    return
  }
  form.techStacks.push(tech)
}

const handleResume = async (file: any) => {
  const raw = file.raw as File | undefined
  if (!raw) return
  resumeUploading.value = true
  try {
    const resume = await resumeApi.uploadResume(raw)
    if (resume) {
      resumes.value = [resume, ...resumes.value.filter((item) => item.id !== resume.id)]
      selectedResumeId.value = resume.id
      ElMessage.success('简历上传并解析完成')
    }
  } finally {
    resumeUploading.value = false
  }
}

watch(
  roleBanks,
  async (roles) => {
    if (!roles.length) return
    if (!roles.some((role) => role.code === form.role)) {
      await selectRole(roles[0].code)
    }
  },
  { immediate: true }
)

const startInterview = () => {
  const role = selectedRole.value
  const interviewName = form.interviewName.trim()
  if (!interviewName) {
    ElMessage.warning('请先填写面试名称')
    return
  }
  if (form.interviewMode === 'CUSTOM' && !role) {
    ElMessage.warning('请先选择目标岗位')
    return
  }
  if (form.interviewMode === 'CUSTOM' && !form.techStacks.length) {
    ElMessage.warning('请至少选择一个技术栈')
    return
  }
  if (form.interviewMode === 'RESUME' && !selectedResume.value) {
    ElMessage.warning('按简历面试请先选择或上传简历')
    return
  }

  const payload: AIInterviewCreatePayload = {
    interviewMode: form.interviewMode,
    interviewName,
    jobRoleId: role?.id,
    targetPosition: form.interviewMode === 'RESUME'
      ? (selectedResume.value?.intentionJob || selectedRoleName.value)
      : selectedRoleName.value,
    interviewLanguage: 'zh-CN',
    techStacks: effectiveTechStacks.value,
    resumeFileName: selectedResume.value?.originalFileName,
    resumeContent: selectedResume.value?.content,
    voiceEnabled: true
  }
  const draftKey = `${LOADING_DRAFT_PREFIX}${Date.now()}`

  try {
    sessionStorage.setItem(draftKey, JSON.stringify(payload))
    router.push({
      name: 'AIInterviewLoading',
      query: {
        draft: draftKey,
        role: selectedRoleName.value,
        name: interviewName,
        tech: effectiveTechStacks.value.join(' / ')
      }
    })
  } catch {
    ElMessage.error('暂时无法创建加载任务，请稍后重试')
  }
}

onMounted(async () => {
  try {
    const [resumeList, roleList] = await Promise.all([
      resumeApi.listResumes(),
      jobRoleApi.listJobRoles()
    ])
    resumes.value = resumeList
    roleBanks.value = roleList
    if (roleList.length && !form.role) {
      await selectRole(roleList[0].code)
    }
  } catch (error) {
    console.error('加载面试创建页数据失败:', error)
    ElMessage.error('面试配置加载失败，请稍后重试')
  }
})
</script>

<style scoped>
.interview-create-page {
  min-height: 100vh;
  background:
    linear-gradient(135deg, rgba(255, 90, 42, 0.08) 0%, rgba(255, 255, 255, 0) 34%),
    linear-gradient(180deg, #fff8f4 0%, #f7f8fb 52%, #ffffff 100%);
  color: #242733;
}

.interview-create-page::before {
  content: "";
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(255, 90, 42, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 90, 42, 0.06) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(90deg, transparent 0, #000 20%, #000 78%, transparent 100%);
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

.create-main {
  min-height: 100vh;
  margin-left: 200px;
  padding: 54px 36px;
}

.create-shell {
  width: min(1180px, 100%);
  min-height: calc(100vh - 108px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(320px, 0.92fr) minmax(520px, 1.08fr);
  align-items: center;
  gap: 34px;
}

.create-hero {
  position: relative;
  display: grid;
  gap: 18px;
}

.eyebrow {
  width: fit-content;
  padding: 6px 10px;
  border: 1px solid #ffd9ca;
  border-radius: 999px;
  color: #ff5a2a;
  background: rgba(255, 255, 255, 0.72);
  font-size: 11px;
  font-weight: 900;
  letter-spacing: 0;
}

.create-hero h1 {
  color: #242733;
  font-size: 42px;
  line-height: 1.15;
  font-weight: 900;
}

.create-hero p {
  max-width: 460px;
  color: #687286;
  font-size: 15px;
  line-height: 1.8;
}

.stage-preview {
  width: min(440px, 100%);
  margin-top: 8px;
  padding: 18px;
  display: grid;
  gap: 14px;
  border: 1px solid rgba(255, 151, 106, 0.34);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 22px 52px rgba(255, 90, 42, 0.14);
  backdrop-filter: blur(14px);
}

.stage-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.stage-head span {
  width: 42px;
  height: 8px;
  border-radius: 999px;
  background: #ff5a2a;
}

.stage-head strong {
  color: #242733;
  font-size: 13px;
  font-weight: 900;
}

.stage-dialog {
  min-height: 96px;
  padding: 14px;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  border-radius: 12px;
  background: #fff5ef;
}

.stage-dialog i {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  overflow: hidden;
  background: transparent;
  font-style: normal;
}

.stage-dialog i img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.stage-dialog p {
  max-width: none;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #4a5362;
  font-size: 13px;
  line-height: 1.7;
}

.stage-tags,
.hero-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stage-tags span {
  padding: 5px 9px;
  border-radius: 999px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
}

.hero-metrics {
  width: min(440px, 100%);
  margin-top: 4px;
}

.hero-metrics article {
  min-width: 118px;
  padding: 12px 14px;
  display: grid;
  gap: 4px;
  border: 1px solid rgba(255, 151, 106, 0.22);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.66);
}

.hero-metrics strong {
  color: #242733;
  font-size: 22px;
  line-height: 1;
}

.hero-metrics span {
  color: #8892a3;
  font-size: 12px;
  font-weight: 800;
}

.setup-card {
  position: relative;
  width: 100%;
  padding: 24px;
  display: grid;
  gap: 20px;
  border: 1px solid rgba(232, 237, 245, 0.9);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(27, 36, 56, 0.09);
}

.card-head {
  padding-bottom: 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  border-bottom: 1px solid #edf0f5;
}

.card-head span,
.card-head small {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 900;
}

.card-head h2 {
  margin-top: 5px;
  color: #242733;
  font-size: 20px;
  font-weight: 900;
}

.card-head small {
  padding: 6px 10px;
  border-radius: 999px;
  color: #ff5a2a;
  background: #fff1eb;
  white-space: nowrap;
}

.field {
  display: grid;
  gap: 8px;
}

.field-tip {
  color: #8a95a8;
  font-size: 12px;
  line-height: 1.6;
}

.mode-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.mode-switch button {
  min-height: 76px;
  padding: 14px;
  display: grid;
  align-content: center;
  gap: 6px;
  border: 1px solid #e5e9f0;
  border-radius: 12px;
  background: #ffffff;
  color: #687286;
  cursor: pointer;
  text-align: left;
}

.mode-switch button.active {
  border-color: #ff5a2a;
  background: #fff5ef;
  box-shadow: inset 0 0 0 1px #ff5a2a;
}

.mode-switch strong {
  color: #242733;
  font-size: 14px;
  font-weight: 900;
}

.mode-switch span {
  color: #7c8799;
  font-size: 12px;
  line-height: 1.5;
}

.field strong,
.resume-row strong {
  color: #252936;
  font-size: 14px;
  font-weight: 900;
}

.pill-row,
.tech-grid {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  max-height: 154px;
  overflow: auto;
  padding-right: 4px;
}

.pill-row button,
.tech-grid button {
  border: 1px solid #e5e9f0;
  color: #687286;
  background: #ffffff;
  cursor: pointer;
  font-weight: 800;
}

.pill-row button {
  height: 32px;
  padding: 0 14px;
  border-radius: 7px;
  font-size: 13px;
}

.tech-grid button {
  width: auto;
  min-width: 0;
  height: 32px;
  padding: 0 10px;
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border-radius: 7px;
  font-size: 12px;
}

.tech-grid button .el-icon {
  font-size: 13px;
}

.pill-row button.active,
.tech-grid button.active {
  border-color: #ff5a2a;
  color: #ff5a2a;
  background: #fff1eb;
  box-shadow: inset 0 0 0 1px #ff5a2a;
}

.resume-row {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  border: 1px solid #edf0f5;
  border-radius: 12px;
  background: #fbfcfe;
}

.resume-actions {
  min-width: 310px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.resume-select {
  width: 180px;
}

.resume-preview {
  padding: 14px;
  display: grid;
  gap: 8px;
  border: 1px solid #e8edf5;
  border-radius: 9px;
  background: #fbfcfe;
}

.resume-preview strong {
  color: #252936;
  font-size: 13px;
  font-weight: 900;
}

.resume-preview p {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #7b8495;
  font-size: 12px;
  line-height: 1.6;
}

.resume-preview div {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.resume-preview span {
  padding: 4px 8px;
  border-radius: 6px;
  color: #5f6879;
  background: #eef2f7;
  font-size: 12px;
  font-weight: 800;
}

.resume-row p {
  margin-top: 6px;
  color: #8f98aa;
  font-size: 12px;
}

:deep(.upload-btn.el-button) {
  height: 34px;
  border-radius: 7px;
  color: #687286;
  background: #f8f9fb;
  border-color: #e6eaf1;
  font-weight: 800;
}

.start-row {
  display: grid;
}

:deep(.start-btn.el-button--primary) {
  height: 48px;
  border: none;
  border-radius: 10px;
  background: #ff5a2a;
  font-size: 15px;
  font-weight: 900;
  box-shadow: 0 14px 24px rgba(255, 90, 42, 0.24);
}

@media (max-width: 1080px) {
  .create-shell {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .create-hero {
    justify-items: start;
  }
}

@media (max-width: 900px) {
  .app-sidebar {
    position: static;
    width: 100%;
  }

  .create-main {
    margin-left: 0;
    align-content: start;
    padding: 32px 18px;
  }

  .create-shell {
    min-height: auto;
  }
}

@media (max-width: 640px) {
  .create-hero h1 {
    font-size: 32px;
  }

  .setup-card {
    padding: 18px;
  }

  .card-head,
  .resume-row {
    align-items: stretch;
    flex-direction: column;
  }

  .resume-row {
    padding: 14px;
  }

  .resume-actions {
    min-width: 0;
    align-items: stretch;
    flex-direction: column;
  }

  .resume-select {
    width: 100%;
  }
}
</style>
