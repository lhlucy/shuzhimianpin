<template>
  <div class="profile-page">
    <aside class="app-sidebar">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="side-nav" aria-label="个人中心导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/profile' }">
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <main class="profile-main">
      <section class="profile-banner">
        <div class="profile-identity">
          <el-avatar :size="68" :src="avatarUrl" class="avatar-ring">{{ userInitial }}</el-avatar>
          <div>
            <h1>{{ displayName }}</h1>
            <p>意向岗位：{{ targetRoleName }}</p>
            <span>{{ profile?.email || '未绑定邮箱' }}　·　{{ profile?.targetCity || '未设置意向城市' }}</span>
          </div>
        </div>

        <div class="banner-stats">
          <article><strong>{{ practiceStats.totalCount }}</strong><span>刷题数</span></article>
          <article><strong>{{ interviewStats.count }}</strong><span>模拟面试</span></article>
          <article><strong>{{ interviewStats.averageScore }}</strong><span>平均分</span></article>
        </div>
      </section>

      <section class="content-grid">
        <div class="left-column">
          <section class="info-card">
            <div class="panel-head">
              <h2>个人信息</h2>
              <button type="button" @click="openEdit">
                <el-icon><EditPen /></el-icon>
                编辑资料
              </button>
            </div>

            <dl class="info-list">
              <div><dt>用户名</dt><dd>{{ profile?.username || '-' }}</dd></div>
              <div><dt>昵称</dt><dd>{{ displayName }}</dd></div>
              <div><dt>邮箱</dt><dd>{{ profile?.email || '-' }}</dd></div>
              <div><dt>手机号</dt><dd>{{ profile?.phone || '-' }}</dd></div>
              <div><dt>意向岗位</dt><dd>{{ targetRoleName }}</dd></div>
              <div><dt>意向城市</dt><dd>{{ profile?.targetCity || '-' }}</dd></div>
              <div><dt>期望薪资</dt><dd>{{ profile?.expectedSalary || '-' }}</dd></div>
              <div><dt>招聘类型</dt><dd>{{ profile?.preferredCompanyType || '-' }}</dd></div>
            </dl>
          </section>

          <section class="visual-card">
            <div class="panel-head compact">
              <h2>训练数据可视化</h2>
              <small>最近 {{ recentScores.length }} 场</small>
            </div>

            <div class="chart-wrap">
              <svg viewBox="0 0 320 150" role="img" aria-label="最近模拟面试得分趋势">
                <polyline class="grid-line" points="0,120 320,120" />
                <polyline class="grid-line" points="0,80 320,80" />
                <polyline class="grid-line" points="0,40 320,40" />
                <polyline class="trend-fill" :points="trendFillPoints" />
                <polyline class="trend-line" :points="trendLinePoints" />
                <circle v-for="point in chartPoints" :key="point.x" :cx="point.x" :cy="point.y" r="4" />
              </svg>
              <div class="chart-labels">
                <span v-for="label in chartLabels" :key="label">{{ label }}</span>
              </div>
            </div>

            <div class="ability-bars">
              <div v-for="item in abilities" :key="item.label">
                <span>{{ item.label }}</span>
                <i><b :style="{ width: `${item.value}%`, background: item.color }"></b></i>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </section>
        </div>

        <section class="resume-card">
          <div class="panel-head">
            <h2>我的简历</h2>
            <el-upload :auto-upload="false" :show-file-list="false" accept=".pdf,.docx,.txt,.md" :on-change="uploadResume">
              <button type="button" class="upload-main" :disabled="resumeUploading">
                <el-icon><Plus /></el-icon>
                {{ resumeUploading ? '解析中' : '上传简历' }}
              </button>
            </el-upload>
          </div>

          <div v-if="resumes.length" class="resume-list">
            <article v-for="resume in resumes" :key="resume.id" class="resume-row">
              <div class="file-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="resume-body">
                <h3>{{ resume.originalFileName }}</h3>
                <p>{{ resume.summary || '已上传，暂无可展示摘要' }}</p>
                <div class="resume-tags">
                  <span>岗位：{{ resume.intentionJob || '-' }}</span>
                  <span>类型：{{ resume.recruitmentType || '-' }}</span>
                  <span>城市：{{ resume.intentionCity || '-' }}</span>
                  <span>薪资：{{ resume.expectedSalary || '-' }}</span>
                </div>
              </div>
              <div class="file-actions">
                <a :href="assetUrl(resume.fileUrl)" target="_blank" rel="noreferrer" title="查看简历">
                  <el-icon><Download /></el-icon>
                </a>
                <button type="button" title="删除简历" @click="removeResume(resume.id)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </article>
          </div>

          <div v-else class="upload-zone">
            <el-icon><Upload /></el-icon>
            <strong>暂无简历</strong>
            <span>上传后会解析意向岗位、招聘类型、意向城市和期望薪资</span>
          </div>
        </section>
      </section>
    </main>

    <el-dialog v-model="editVisible" title="编辑个人信息" width="620px">
      <el-form label-width="88px" class="edit-form">
        <el-form-item label="头像">
          <div class="avatar-edit">
            <el-avatar :size="54" :src="assetUrl(editForm.avatar)">{{ userInitial }}</el-avatar>
            <el-upload :auto-upload="false" :show-file-list="false" accept=".jpg,.jpeg,.png,.webp,.gif" :on-change="uploadAvatar">
              <el-button :loading="avatarUploading">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="意向岗位">
          <el-select v-model="editForm.targetJobRoleId" clearable filterable placeholder="选择意向岗位">
            <el-option v-for="role in jobRoles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向城市"><el-input v-model="editForm.targetCity" placeholder="如：杭州、上海" /></el-form-item>
        <el-form-item label="招聘类型">
          <el-select v-model="editForm.preferredCompanyType" clearable placeholder="选择招聘类型">
            <el-option label="校招" value="校招" />
            <el-option label="社招" value="社招" />
            <el-option label="实习" value="实习" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望薪资"><el-input v-model="editForm.expectedSalary" placeholder="如：15k-20k、面议" /></el-form-item>
        <el-form-item label="个人简介"><el-input v-model="editForm.bio" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Clock,
  Collection,
  Delete,
  Document,
  Download,
  EditPen,
  House,
  Monitor,
  Plus,
  Star,
  Upload,
  User
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import aiInterviewApi, { type AIInterviewHistoryItem } from '@/api/aiInterview'
import jobRoleApi, { type JobRoleOption } from '@/api/jobRoles'
import resumeApi, { type UserResume } from '@/api/resumes'
import userApi, { emptyPracticeStats } from '@/api/user'
import { authUser, getAvatarInitial, getDisplayName, loadCurrentUser, refreshAuthState, type AuthUser } from '@/utils/auth'

const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const profile = ref<AuthUser | null>(authUser.value)
const practiceStats = ref({ ...emptyPracticeStats })
const interviewHistory = ref<AIInterviewHistoryItem[]>([])
const resumes = ref<UserResume[]>([])
const jobRoles = ref<JobRoleOption[]>([])
const editVisible = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const resumeUploading = ref(false)
const editForm = reactive<Partial<AuthUser>>({})
const AVATAR_MAX_SIZE = 2 * 1024 * 1024
const AVATAR_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']

const displayName = computed(() => getDisplayName(profile.value || authUser.value))
const userInitial = computed(() => getAvatarInitial(profile.value || authUser.value))
const avatarUrl = computed(() => assetUrl(profile.value?.avatar))
const targetRoleName = computed(() => jobRoles.value.find((role) => role.id === profile.value?.targetJobRoleId)?.name || '暂未设置')
const scoredInterviews = computed(() => interviewHistory.value.filter((item) => typeof item.totalScore === 'number'))
const interviewStats = computed(() => {
  const scores = scoredInterviews.value.map((item) => Number(item.totalScore || 0))
  const average = scores.length ? Math.round(scores.reduce((sum, score) => sum + score, 0) / scores.length) : 0
  return { count: interviewHistory.value.length, averageScore: average }
})
const recentScores = computed(() => {
  const scores = scoredInterviews.value.slice(0, 6).reverse().map((item) => Number(item.totalScore || 0))
  return scores.length ? scores : [0]
})
const chartPoints = computed(() => {
  const scores = recentScores.value
  const step = scores.length > 1 ? 320 / (scores.length - 1) : 320
  return scores.map((score, index) => ({ x: Math.round(index * step), y: Math.max(18, Math.min(132, 132 - score * 1.05)) }))
})
const trendLinePoints = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))
const trendFillPoints = computed(() => `${trendLinePoints.value} 320,150 0,150`)
const chartLabels = computed(() => recentScores.value.map((_, index) => `第${index + 1}场`))
const abilities = computed(() => [
  { label: '完成率', value: Math.round(practiceStats.value.completionRate || 0), color: '#ff5a2a' },
  { label: '查看答案', value: percent(practiceStats.value.viewAnswerCount, practiceStats.value.totalCount), color: '#416ee6' },
  { label: '面试均分', value: interviewStats.value.averageScore, color: '#26b96d' },
  { label: '等级经验', value: Math.min(100, Number(profile.value?.experience || 0)), color: '#8b48e8' }
])

function assetUrl(value?: string) {
  if (!value) return ''
  if (/^https?:\/\//.test(value)) return value
  return `http://localhost:8080${value}`
}

function percent(value: number, total: number) {
  return total > 0 ? Math.round((value / total) * 100) : 0
}

function openEdit() {
  Object.assign(editForm, {
    nickname: profile.value?.nickname || '',
    email: profile.value?.email || '',
    phone: profile.value?.phone || '',
    bio: profile.value?.bio || '',
    avatar: profile.value?.avatar || '',
    targetJobRoleId: profile.value?.targetJobRoleId,
    preferredCompanyType: profile.value?.preferredCompanyType || '',
    targetCity: profile.value?.targetCity || '',
    expectedSalary: profile.value?.expectedSalary || ''
  })
  editVisible.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    const updated = await userApi.updateProfile(editForm)
    if (updated) {
      profile.value = updated
      localStorage.setItem('currentUser', JSON.stringify(updated))
      refreshAuthState()
    }
    editVisible.value = false
    ElMessage.success('个人信息已更新')
  } finally {
    saving.value = false
  }
}

async function uploadAvatar(file: any) {
  const raw = file.raw as File | undefined
  if (!raw) return

  if (!AVATAR_ALLOWED_TYPES.includes(raw.type)) {
    ElMessage.warning('头像仅支持 JPG、PNG、WEBP、GIF 格式')
    return
  }

  if (raw.size > AVATAR_MAX_SIZE) {
    ElMessage.warning('头像大小不能超过 2MB，请压缩后再上传')
    return
  }

  avatarUploading.value = true
  try {
    const updated = await userApi.uploadAvatar(raw)
    if (updated) {
      profile.value = updated
      editForm.avatar = updated.avatar
      localStorage.setItem('currentUser', JSON.stringify(updated))
      refreshAuthState()
      await loadCurrentUser().catch(() => null)
    }
    ElMessage.success('头像已更新')
  } catch (error: any) {
    console.error('头像上传失败:', error)
    ElMessage.error(error?.message || '头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

async function uploadResume(file: any) {
  const raw = file.raw as File | undefined
  if (!raw) return
  resumeUploading.value = true
  try {
    const resume = await resumeApi.uploadResume(raw)
    if (resume) {
      resumes.value = [resume, ...resumes.value.filter((item) => item.id !== resume.id)]
      ElMessage.success('简历上传并解析完成')
    }
  } finally {
    resumeUploading.value = false
  }
}

async function removeResume(id: number) {
  await ElMessageBox.confirm('确定删除这份简历吗？', '删除简历', { type: 'warning' })
  await resumeApi.deleteResume(id)
  resumes.value = resumes.value.filter((item) => item.id !== id)
  ElMessage.success('简历已删除')
}

onMounted(async () => {
  refreshAuthState()
  const [currentProfile, stats, history, resumeList, roles] = await Promise.allSettled([
    loadCurrentUser(),
    userApi.getPracticeStats(),
    aiInterviewApi.getInterviewHistory(20),
    resumeApi.listResumes(),
    jobRoleApi.listJobRoles()
  ])

  if (currentProfile.status === 'fulfilled') profile.value = currentProfile.value
  if (stats.status === 'fulfilled') practiceStats.value = stats.value
  if (history.status === 'fulfilled') interviewHistory.value = history.value
  if (resumeList.status === 'fulfilled') resumes.value = resumeList.value
  if (roles.status === 'fulfilled') jobRoles.value = roles.value
})
</script>

<style scoped>
.profile-page {
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
.side-nav a,
.file-actions a {
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
  background: #ff5a2a;
  color: #ffffff;
  font-size: 12px;
  font-weight: 900;
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

.profile-main {
  margin-left: 200px;
  padding: 30px 30px 70px;
}

.profile-banner {
  min-height: 118px;
  padding: 26px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ff6430, #f24b1b);
  color: #ffffff;
}

.profile-identity {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-ring {
  flex: none;
  border: 2px solid rgba(255, 255, 255, 0.45);
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 28px;
  font-weight: 900;
}

.profile-identity h1 {
  font-size: 22px;
  font-weight: 900;
}

.profile-identity p,
.profile-identity span {
  margin-top: 6px;
  display: block;
  color: rgba(255, 255, 255, 0.78);
  font-size: 13px;
}

.banner-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(70px, 1fr));
  gap: 18px;
}

.banner-stats article {
  min-width: 0;
  display: grid;
  justify-items: center;
  gap: 3px;
  border-left: 1px solid rgba(255, 255, 255, 0.24);
  padding-left: 18px;
}

.banner-stats article:first-child {
  border-left: none;
}

.banner-stats strong {
  font-size: 28px;
  line-height: 1;
}

.banner-stats span {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.content-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: 350px minmax(0, 1fr);
  gap: 20px;
}

.left-column {
  display: grid;
  gap: 16px;
  align-content: start;
}

.info-card,
.visual-card,
.resume-card {
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(27, 36, 56, 0.025);
}

.info-card,
.visual-card {
  padding: 18px;
}

.resume-card {
  min-height: 628px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #edf0f5;
  padding-bottom: 12px;
}

.resume-card .panel-head {
  padding: 18px 20px 12px;
}

.panel-head.compact {
  border-bottom: none;
  padding-bottom: 0;
}

.panel-head h2 {
  color: #242733;
  font-size: 15px;
  font-weight: 900;
}

.panel-head small {
  color: #99a3b6;
  font-weight: 800;
}

.panel-head button,
.upload-main {
  height: 30px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: none;
  border-radius: 6px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.upload-main {
  height: 32px;
  color: #ffffff;
  background: #ff5a2a;
}

.upload-main:disabled {
  opacity: 0.65;
  cursor: progress;
}

.info-list {
  margin-top: 12px;
  display: grid;
  gap: 15px;
}

.info-list div {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.info-list dt {
  flex: none;
  color: #99a3b6;
  font-size: 13px;
}

.info-list dd {
  min-width: 0;
  margin: 0;
  color: #242733;
  font-size: 13px;
  font-weight: 900;
  text-align: right;
  overflow-wrap: anywhere;
}

.chart-wrap {
  margin-top: 14px;
  padding: 8px 4px 0;
}

.chart-wrap svg {
  width: 100%;
  height: auto;
  overflow: visible;
}

.grid-line {
  fill: none;
  stroke: #eef1f6;
  stroke-width: 1;
}

.trend-fill {
  fill: rgba(255, 90, 42, 0.1);
  stroke: none;
}

.trend-line {
  fill: none;
  stroke: #ff5a2a;
  stroke-width: 4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.chart-wrap circle {
  fill: #ffffff;
  stroke: #ff5a2a;
  stroke-width: 3;
}

.chart-labels {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  color: #a0aabd;
  font-size: 11px;
}

.ability-bars {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.ability-bars div {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) 28px;
  align-items: center;
  gap: 10px;
  color: #687286;
  font-size: 12px;
}

.ability-bars i {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: #eef1f6;
}

.ability-bars b {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.ability-bars strong {
  color: #242733;
}

.resume-list {
  max-height: 578px;
  overflow: auto;
}

.resume-row {
  min-height: 118px;
  padding: 16px 20px;
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr) 62px;
  align-items: start;
  gap: 12px;
  border-bottom: 1px solid #edf0f5;
}

.file-icon {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #ff5a2a;
  background: #fff1eb;
}

.resume-body h3 {
  color: #242733;
  font-size: 14px;
  font-weight: 900;
  overflow-wrap: anywhere;
}

.resume-body p {
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #7f899b;
  font-size: 12px;
  line-height: 1.6;
}

.resume-tags {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.resume-tags span {
  padding: 4px 8px;
  border-radius: 6px;
  color: #5f6879;
  background: #f3f5f8;
  font-size: 12px;
  font-weight: 800;
}

.file-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  color: #a2acbf;
}

.file-actions a,
.file-actions button {
  border: 0;
  background: transparent;
  color: #a2acbf;
  cursor: pointer;
}

.upload-zone {
  min-height: 552px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 8px;
  color: #a2acbf;
  background: #fbfcfe;
}

.upload-zone strong {
  color: #8b96aa;
  font-size: 13px;
}

.upload-zone span {
  font-size: 12px;
}

.avatar-edit {
  display: flex;
  align-items: center;
  gap: 14px;
}

.edit-form :deep(.el-select) {
  width: 100%;
}

@media (max-width: 980px) {
  .app-sidebar {
    position: static;
    width: 100%;
  }

  .profile-main {
    margin-left: 0;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .profile-banner,
  .profile-identity,
  .info-list div,
  .resume-row {
    align-items: flex-start;
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .banner-stats {
    width: 100%;
  }
}
</style>
