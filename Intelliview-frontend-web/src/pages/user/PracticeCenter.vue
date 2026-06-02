<template>
  <div class="practice-page" :class="{ practicing: isPracticing }">
    <template v-if="!isPracticing">
      <aside class="app-sidebar">
        <router-link to="/user" class="brand">
          <span class="brand-logo"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
          <strong>数智面聘</strong>
        </router-link>

        <nav class="side-nav" aria-label="岗位刷题导航">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/practice' }">
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </router-link>
        </nav>

        <div class="user-card">
          <span>{{ userInitial }}</span>
          <div>
            <strong>{{ displayName }}</strong>
            <small>已完成 {{ practiceStats.completedCount }} / {{ practiceStats.totalCount }} 道</small>
          </div>
        </div>
      </aside>

      <main class="role-page">
        <section class="role-hero">
          <h1>选择练习岗位</h1>
          <p>选择你目标岗位，进入对应题库开始练习</p>
          <el-input v-model="searchText" class="role-search" placeholder="搜索岗位名称...">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </section>

        <div class="category-tabs">
          <button
            v-for="category in categories"
            :key="category"
            type="button"
            :class="{ active: activeCategory === category }"
            @click="activeCategory = category"
          >
            {{ category }}
          </button>
        </div>

        <section class="role-grid" aria-label="岗位列表">
          <article v-for="role in filteredRoles" :key="role.code" class="role-card" @click="enterRole(role.code)">
            <div class="role-card-top">
              <div class="role-mark" :style="{ color: role.color, background: `${role.color}14` }">
                {{ role.shortName }}
              </div>
              <div>
                <h2>{{ role.name }}</h2>
                <span>{{ role.category }}</span>
              </div>
              <el-icon class="arrow"><Right /></el-icon>
            </div>

            <div class="progress-track">
              <i :style="{ width: `${role.progress}%`, background: role.color }"></i>
            </div>

            <div class="role-meta">
              <span>已完成 {{ role.progress }}%</span>
              <span>{{ role.done }} / {{ role.total }} 题</span>
              <button type="button" :class="{ start: role.progress === 0 }">
                {{ role.progress > 0 ? '继续刷题' : '开始刷题' }}
              </button>
            </div>
          </article>
        </section>
      </main>
    </template>

    <template v-else>
      <main class="question-workspace">
        <aside class="question-sidebar">
          <div class="question-search">
            <button type="button" class="back-link" @click="backToRoles">
              <el-icon><ArrowLeft /></el-icon>
              岗位刷题
            </button>
            <strong>{{ currentRole.name }}</strong>
            <el-input v-model="questionSearch" placeholder="搜索题目...">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="question-tabs">
            <button type="button" :class="{ active: questionTab === 'all' }" @click="questionTab = 'all'">
              全部({{ currentMeta.total }})
            </button>
            <button type="button" :class="{ active: questionTab === 'todo' }" @click="questionTab = 'todo'">
              未完成({{ currentMeta.total - currentMeta.done }})
            </button>
          </div>

          <div class="question-list">
            <button
              v-for="question in filteredQuestions"
              :key="question.id"
              type="button"
              :class="['question-row', { active: question.id === activeQuestion.id }]"
              @click="setQuestion(question.id)"
            >
              <span :class="['question-no', { done: question.done }]">
                {{ getQuestionNo(question.id) }}
              </span>
              <span class="question-copy">
                <strong>{{ compactTitle(question.title) }}</strong>
                <small>{{ question.category }} · {{ question.difficulty }}</small>
              </span>
              <span class="question-actions">
                <button
                  type="button"
                  class="favorite-toggle"
                  :class="{ active: favoriteQuestionIds.has(question.id) }"
                  @click.stop="toggleFavorite(question.id)"
                >
                  <el-icon>
                    <StarFilled v-if="favoriteQuestionIds.has(question.id)" />
                    <Star v-else />
                  </el-icon>
                </button>
              </span>
            </button>
          </div>
        </aside>

        <section class="question-main">
          <header class="question-topbar">
            <div class="crumb">
              <button type="button" @click="backToRoles">
                <el-icon><ArrowLeft /></el-icon>
              </button>
              <span>岗位刷题</span>
              <em>›</em>
              <strong>{{ currentRole.name }}</strong>
            </div>
            <div class="top-actions">
              <span>{{ currentMeta.done }} / {{ currentMeta.total }} 已完成</span>
              <el-icon><Setting /></el-icon>
            </div>
          </header>

          <article class="question-content">
            <div class="tag-row">
              <span class="question-index">第 {{ activeQuestionNo }} 题</span>
              <span>{{ activeQuestion.difficulty }}</span>
              <span>高频考题</span>
            </div>

            <h1>{{ activeQuestion.title }}</h1>
            <p class="question-intro">{{ activeQuestion.detail }}</p>

            <div class="topic-tags">
              <span v-for="point in activeQuestion.points.slice(0, 3)" :key="point">{{ pointLabel(point) }}</span>
            </div>

            <section class="answer-card">
              <div class="answer-head">
                <h2>参考答案</h2>
                <button type="button" @click="toggleAnswerVisible">
                  <el-icon><Hide /></el-icon>
                  {{ answerVisible ? '隐藏答案' : '显示答案' }}
                </button>
              </div>

              <div v-if="answerVisible" class="answer-body">
                <h3>{{ shortAnswerTitle }}</h3>
                <div class="answer-markdown markdown-preview" v-html="formattedAnswerHtml"></div>
              </div>
              <div v-else class="answer-mask">答案已隐藏</div>
            </section>
          </article>

          <footer class="question-footer">
            <button type="button" :disabled="questionIndex === 0" @click="goPrev">
              <el-icon><ArrowLeft /></el-icon>
              上一题
            </button>
            <span>{{ activeQuestionNo }} / {{ questions.length }}</span>
            <button type="button" class="next-btn" :disabled="questionIndex === questions.length - 1" @click="goNext">
              下一题
              <el-icon><Right /></el-icon>
            </button>
          </footer>
        </section>
      </main>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DOMPurify from 'dompurify'
import MarkdownIt from 'markdown-it'
import {
  ArrowLeft,
  Clock,
  Collection,
  Hide,
  House,
  Monitor,
  Right,
  Search,
  Setting,
  Star,
  StarFilled,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { useRoleBanks } from '@/data/interviewData'
import userApi, { emptyPracticeStats } from '@/api/user'
import service from '@/utils/axios'
import { getAvatarInitial, getDisplayName, isAuthenticated, loadCurrentUser, refreshAuthState } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const { roleBanks } = useRoleBanks()
const practiceStats = ref({ ...emptyPracticeStats })
const completedQuestionIds = ref<Set<number>>(new Set())
const favoriteQuestionIds = ref<Set<number>>(new Set())
const viewedQuestionIds = ref<Set<number>>(new Set())
const detailLoadingId = ref<number | null>(null)
const questionDetails = ref<Record<number, { questionText?: string; answerText?: string }>>({})
const displayName = computed(() => getDisplayName())
const userInitial = computed(() => getAvatarInitial())
const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '成长中心', path: '/user/growth', icon: TrendCharts },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const categories = ['全部', '后端开发', '前端开发', '算法/AI', '数据岗位', '云原生/运维', '安全/测试', '移动/嵌入式', '产品岗位']
const searchText = ref('')
const questionSearch = ref('')
const activeCategory = ref('全部')
const questionTab = ref<'all' | 'todo'>('all')
const answerVisible = ref(false)
const activeQuestionId = ref<number | null>(null)

const shortNameMap: Record<string, string> = {
  java_backend: 'Java',
  frontend: 'FE',
  python: 'Py',
  algorithm: 'AI',
  ai_large_model: '大模',
  mlops_engineer: 'ML',
  data_engineer: '数据',
  data_analyst: '分析',
  bi_engineer: 'BI',
  cloud_native: '云',
  devops_sre: 'SRE',
  cybersecurity: '安全',
  fullstack: '全栈',
  go_backend: 'Go',
  c_cpp: 'C++',
  mobile: '移动',
  qa_test: '测试',
  database_dba: 'DBA',
  embedded_iot: 'IoT',
  product_tech: '产品'
}

const groupMap: Record<string, string> = {
  java_backend: '后端开发',
  frontend: '前端开发',
  python: '后端开发',
  algorithm: '算法/AI',
  ai_large_model: '算法/AI',
  mlops_engineer: '算法/AI',
  data_engineer: '数据岗位',
  data_analyst: '数据岗位',
  bi_engineer: '数据岗位',
  cloud_native: '云原生/运维',
  devops_sre: '云原生/运维',
  cybersecurity: '安全/测试',
  qa_test: '安全/测试',
  fullstack: '后端开发',
  go_backend: '后端开发',
  c_cpp: '后端开发',
  mobile: '移动/嵌入式',
  embedded_iot: '移动/嵌入式',
  database_dba: '数据岗位',
  product_tech: '产品岗位'
}

const roleCards = computed(() =>
  roleBanks.value.map((role) => {
    const questions = role.questions.map((question) => ({
      ...question,
      done: completedQuestionIds.value.has(question.id)
    }))
    const total = questions.length
    const done = questions.filter((question) => question.done).length

    return {
      ...role,
      questions,
      shortName: shortNameMap[role.code] || role.name.slice(0, 2),
      category: groupMap[role.code] || '综合岗位',
      group: groupMap[role.code] || '全部',
      progress: total > 0 ? Math.round((done / total) * 100) : 0,
      done,
      total
    }
  })
)

const normalizeSearchText = (value?: string) => (value || '')
  .toString()
  .trim()
  .toLowerCase()
  .replace(/[\s/_-]+/g, '')

const roleSearchCorpus = (role: any) => [
  role.name,
  role.subtitle,
  role.category,
  role.group,
  role.code,
  role.shortName,
  role.hotQuestion,
  role.heat,
  ...(role.focus || []),
  ...(role.questions || []).slice(0, 10).flatMap((question: any) => [question.title, question.category, question.difficulty])
].map(normalizeSearchText).join('|')

const filteredRoles = computed(() => {
  const keyword = normalizeSearchText(searchText.value)
  return roleCards.value.filter((role) => {
    const matchCategory = activeCategory.value === '全部' || role.group === activeCategory.value
    const matchKeyword = !keyword || roleSearchCorpus(role).includes(keyword)
    return matchCategory && matchKeyword
  })
})

const routeRole = computed(() => (typeof route.query.role === 'string' ? route.query.role : ''))
const isPracticing = computed(() => roleBanks.value.some((role) => role.code === routeRole.value))
const currentRole = computed(() => roleCards.value.find((role) => role.code === routeRole.value) || roleCards.value[0] || {
  code: 'java_backend',
  name: '岗位刷题',
  subtitle: '',
  color: '#ff5a2a',
  icon: Monitor,
  hotQuestion: '',
  heat: '',
  focus: [],
  questions: [],
  shortName: 'AI',
  category: '综合岗位',
  group: '全部',
  progress: 0,
  done: 0,
  total: 0
})
const currentMeta = computed(() => ({
  total: currentRole.value.total,
  done: currentRole.value.done
}))
const questions = computed(() => currentRole.value?.questions || [])
const filteredQuestions = computed(() => {
  const keyword = normalizeSearchText(questionSearch.value)
  return questions.value.filter((question) => {
    const matchTab = questionTab.value === 'all' || !question.done
    const matchKeyword = !keyword || [
      question.title,
      question.category,
      question.difficulty,
      question.detail,
      ...(question.points || [])
    ].map(normalizeSearchText).join('|').includes(keyword)
    return matchTab && matchKeyword
  })
})
const renderMarkdown = (content?: string, hiddenTitles: string[] = []) => {
  if (!content) return ''

  const normalizedContent = hiddenTitles.reduce((result, title) => {
    const escapedTitle = title.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    return result
      .replace(new RegExp(`^#\\s*${escapedTitle}\\s*$`, 'gim'), '')
      .replace(new RegExp(`^##\\s*${escapedTitle}\\s*$`, 'gim'), '')
      .replace(new RegExp(`^###\\s*${escapedTitle}\\s*$`, 'gim'), '')
  }, content).trim()

  return DOMPurify.sanitize(markdown.render(normalizedContent))
}

const activeQuestion = computed(() => {
  const source = filteredQuestions.value.length ? filteredQuestions.value : questions.value
  const question = source.find((item) => item.id === activeQuestionId.value) || source[0]
  if (question) {
    const detail = questionDetails.value[question.id]
    return {
      ...question,
      detail: detail?.questionText || question.detail,
      answerMarkdown: detail?.answerText || question.answer.join('\n\n'),
      answer: detail?.answerText
        ? detail.answerText.split(/\n+/).map((item) => item.trim()).filter(Boolean)
        : question.answer
    }
  }

  return {
    id: 0,
    title: '暂无题目',
    difficulty: '中等',
    category: '真实题库',
    done: false,
    detail: '当前岗位暂未查询到可练习题目。',
    points: [],
    answerMarkdown: '请先在后台导入或创建该岗位的题目。',
    answer: ['请先在后台导入或创建该岗位的题目。']
  }
})
const questionIndex = computed(() => questions.value.findIndex((question) => question.id === activeQuestion.value.id))
const activeQuestionNo = computed(() => Math.max(questionIndex.value + 1, 1))
const shortAnswerTitle = computed(() => compactTitle(activeQuestion.value.title).replace(/[？?。.]$/, ''))
const formattedAnswerHtml = computed(() => renderMarkdown(activeQuestion.value.answerMarkdown, ['回答重点', '参考答案', '答案']))

const enterRole = (code: string) => {
  router.push({ path: '/user/practice', query: { role: code } })
}

const backToRoles = () => {
  router.push('/user/practice')
}

const setQuestion = (id: number) => {
  activeQuestionId.value = id
  answerVisible.value = false
  loadQuestionDetail(id)
}

const toggleAnswerVisible = () => {
  answerVisible.value = !answerVisible.value
}

const compactTitle = (title: string) => title.replace(/^请详述\s*/, '').replace(/^如何向面试官解释\s*/, '如何解释 ')
const pointLabel = (text: string) => text.split('、')[0].replace(/[，。,.].*$/, '').slice(0, 8)
const getQuestionNo = (id: number) => {
  const index = questions.value.findIndex((question) => question.id === id)
  return index >= 0 ? index + 1 : 1
}

const goPrev = () => {
  if (questionIndex.value <= 0) return
  setQuestion(questions.value[questionIndex.value - 1].id)
}

const goNext = () => {
  if (questionIndex.value >= questions.value.length - 1) return
  setQuestion(questions.value[questionIndex.value + 1].id)
}

watch(
  () => [route.query.role, route.query.question],
  () => {
    const queryQuestionId = Number(route.query.question)
    const targetQuestion = currentRole.value.questions.find((item) => item.id === queryQuestionId)
    const firstQuestion = targetQuestion || currentRole.value.questions[0]
    activeQuestionId.value = firstQuestion?.id || null
    questionSearch.value = ''
    questionTab.value = 'all'
    answerVisible.value = false
  },
  { immediate: true }
)

watch(
  () => activeQuestion.value.id,
  (id) => {
    if (id) loadQuestionDetail(id)
  },
  { immediate: true }
)

watch(
  () => questions.value.map((item) => item.id).join(','),
  () => {
    if (activeQuestionId.value || !questions.value.length) return
    const queryQuestionId = Number(route.query.question)
    const targetQuestion = questions.value.find((item) => item.id === queryQuestionId)
    activeQuestionId.value = (targetQuestion || questions.value[0])?.id || null
  }
)

const loadUserPracticeData = async () => {
  if (!isAuthenticated.value) return
  const [statsResult, completedResult] = await Promise.allSettled([
    userApi.getPracticeStats(),
    userApi.getCompletedQuestionIds()
  ])

  if (statsResult.status === 'fulfilled') {
    practiceStats.value = statsResult.value
  }
  if (completedResult.status === 'fulfilled') {
    completedQuestionIds.value = new Set(completedResult.value)
  }
}

const loadFavorites = async () => {
  if (!isAuthenticated.value) return
  try {
    const response = await service.get('/api/questions/favorites', {
      params: { page: 0, size: 200 }
    }) as any
    const records = response?.data?.records || response?.records || []
    favoriteQuestionIds.value = new Set(
      records
        .map((item: any) => Number(item.id))
        .filter((id: number) => Number.isFinite(id) && id > 0)
    )
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  }
}

const loadQuestionDetail = async (id: number) => {
  if (!id || questionDetails.value[id] || detailLoadingId.value === id) return
  detailLoadingId.value = id
  try {
    const response = await service.get(`/api/questions/detail/${id}`)
    const detail = response.data || {}
    questionDetails.value = {
      ...questionDetails.value,
      [id]: {
        questionText: detail.questionText || detail.description,
        answerText: detail.answerText
      }
    }
  } finally {
    detailLoadingId.value = null
  }
}

const toggleFavorite = async (questionId: number) => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    return
  }

  const favorited = favoriteQuestionIds.value.has(questionId)

  try {
    if (favorited) {
      await service.delete(`/api/favorites/${questionId}`, { data: {} })
    } else {
      await service.post(`/api/favorites/${questionId}`, {})
    }

    const next = new Set(favoriteQuestionIds.value)
    if (favorited) {
      next.delete(questionId)
    } else {
      next.add(questionId)
    }
    favoriteQuestionIds.value = next
    ElMessage.success(favorited ? '已取消收藏' : '收藏成功')
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    ElMessage.error(error?.message || '收藏操作失败，请稍后重试')
  }
}

watch(answerVisible, async (visible) => {
  if (!visible || !isAuthenticated.value || !activeQuestion.value.id) return
  if (viewedQuestionIds.value.has(activeQuestion.value.id)) return

  viewedQuestionIds.value = new Set(viewedQuestionIds.value).add(activeQuestion.value.id)
  await userApi.recordPractice(activeQuestion.value.id, true, 1000).catch(() => null)
  await loadUserPracticeData()
})

onMounted(async () => {
  refreshAuthState()
  if (isAuthenticated.value) {
    await loadCurrentUser().catch(() => refreshAuthState())
  }
  await Promise.all([loadUserPracticeData(), loadFavorites()])
})
</script>

<style scoped>
.practice-page {
  min-height: 100vh;
  background: #f6f7f9;
  color: #242733;
}

.practice-page.practicing {
  height: 100vh;
  min-height: 0;
  overflow: hidden;
}

.app-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  width: 200px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #edf0f5;
  background: #ffffff;
}

.brand,
.side-nav a,
.back-link {
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

.user-card {
  margin-top: auto;
  padding: 14px 14px 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8f9fb;
}

.user-card > span {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #ffffff;
  background: #ff6a35;
  font-weight: 900;
}

.user-card div {
  display: grid;
  gap: 2px;
}

.user-card strong {
  font-size: 13px;
}

.user-card small {
  color: #9aa3b6;
  font-size: 11px;
}

.role-page {
  margin-left: 200px;
  padding: 38px 38px 64px;
  max-width: 1680px;
}

.role-hero h1 {
  font-size: 30px;
  line-height: 1.2;
  font-weight: 900;
}

.role-hero p {
  margin-top: 28px;
  color: #687286;
  font-size: 14px;
}

.role-search {
  width: min(366px, 100%);
  margin-top: 28px;
}

:deep(.role-search .el-input__wrapper),
:deep(.question-search .el-input__wrapper) {
  height: 40px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e7ebf2 inset;
}

.category-tabs {
  margin-top: 30px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.category-tabs button {
  height: 34px;
  padding: 0 17px;
  border: 1px solid #e5e9f0;
  border-radius: 6px;
  color: #677287;
  background: #ffffff;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.category-tabs button.active {
  color: #ffffff;
  border-color: #ff5a2a;
  background: #ff5a2a;
}

.role-grid {
  margin-top: 30px;
  display: grid;
  grid-template-columns: repeat(4, minmax(210px, 1fr));
  gap: 18px;
}

.role-card {
  min-height: 156px;
  padding: 22px;
  display: grid;
  gap: 20px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(27, 36, 56, 0.035);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.role-card:hover {
  transform: translateY(-4px);
  border-color: #ffd0c1;
  box-shadow: 0 18px 34px rgba(255, 90, 42, 0.08);
}

.role-card-top {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 18px;
  align-items: center;
  gap: 14px;
}

.role-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 900;
}

.role-card h2 {
  color: #242733;
  font-size: 16px;
  font-weight: 900;
  line-height: 1.35;
}

.role-card-top span {
  display: block;
  margin-top: 4px;
  color: #9aa3b6;
  font-size: 12px;
}

.arrow {
  color: #a4aec2;
}

.progress-track {
  height: 4px;
  border-radius: 999px;
  overflow: hidden;
  background: #eef1f6;
}

.progress-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.role-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #99a3b5;
  font-size: 12px;
}

.role-meta button {
  margin-left: auto;
  height: 30px;
  padding: 0 12px;
  border: none;
  border-radius: 6px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.role-meta button.start {
  color: #16a35f;
  background: #eafaf0;
}

.question-workspace {
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-columns: 330px minmax(0, 1fr);
  overflow: hidden;
  background: #ffffff;
}

.question-sidebar {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid #e8edf5;
  background: #fbfcfe;
}

.question-search {
  padding: 16px 14px 12px;
  display: grid;
  gap: 12px;
  border-bottom: 1px solid #eef1f6;
}

.back-link {
  width: fit-content;
  border: none;
  background: transparent;
  color: #9aa3b6;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.question-search strong {
  color: #242733;
  font-size: 16px;
  font-weight: 900;
}

.question-tabs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  border-bottom: 1px solid #e8edf5;
}

.question-tabs button {
  height: 40px;
  border: none;
  background: #ffffff;
  color: #6f798d;
  font-weight: 900;
  cursor: pointer;
}

.question-tabs button.active {
  color: #242733;
  box-shadow: inset 0 -3px 0 #ff5a2a;
}

.question-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  scrollbar-gutter: stable;
}

.question-row {
  width: 100%;
  min-height: 58px;
  padding: 12px 14px;
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr) 34px;
  gap: 10px;
  border: none;
  border-bottom: 1px solid #eef1f6;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.question-row.active {
  background: #fff2ec;
  box-shadow: inset 3px 0 0 #ff5a2a;
}

.question-no {
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 5px;
  color: #99a3b6;
  background: #f0f3f8;
  font-size: 12px;
  font-weight: 900;
}

.question-no.done {
  color: #16a35f;
  background: #eafaf0;
}

.question-copy {
  min-width: 0;
  display: grid;
  gap: 5px;
}

.question-copy strong {
  color: #313541;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.question-copy small {
  color: #9aa3b6;
  font-size: 12px;
}

.question-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.favorite-toggle {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  color: #a4aec2;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.favorite-toggle:hover {
  color: #ffb547;
  background: #fff7e8;
}

.favorite-toggle.active {
  color: #f4b34d;
  background: #fff7e8;
}

.question-main {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: 52px minmax(0, 1fr) 64px;
  overflow: hidden;
}

.question-topbar {
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8edf5;
}

.crumb,
.top-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.crumb button {
  border: none;
  background: transparent;
  color: #8d96a9;
  cursor: pointer;
}

.crumb span,
.crumb em {
  color: #9aa3b6;
  font-style: normal;
}

.crumb strong {
  color: #252936;
}

.top-actions {
  color: #ff5a2a;
  font-size: 13px;
  font-weight: 900;
}

.question-content {
  min-height: 0;
  overflow: auto;
  padding: 72px 38px 32px;
  display: flex;
  flex-direction: column;
  scrollbar-gutter: stable;
}

.tag-row,
.topic-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.tag-row span {
  height: 24px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 5px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
}

.tag-row .question-index {
  color: #ffffff;
  background: #ff5a2a;
}

.question-content h1 {
  margin-top: 34px;
  color: #242733;
  font-size: 25px;
  line-height: 1.45;
  font-weight: 900;
}

.question-intro {
  margin-top: 18px;
  color: #687286;
  line-height: 1.8;
  font-size: 15px;
}

.topic-tags {
  margin-top: 16px;
}

.topic-tags span {
  height: 26px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 5px;
  color: #687286;
  background: #f2f4f8;
  font-size: 12px;
  font-weight: 800;
}

.answer-card {
  flex: 1;
  margin-top: 52px;
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

.answer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.answer-head h2 {
  color: #242733;
  font-size: 17px;
  font-weight: 900;
}

.answer-head button {
  height: 34px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  border-radius: 7px;
  color: #687286;
  background: #f3f5fa;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.answer-body {
  flex: 1;
  margin-top: 22px;
  color: #586276;
  line-height: 1.9;
}

.answer-body h3 {
  margin-bottom: 12px;
  color: #252936;
  font-size: 16px;
  font-weight: 900;
}

.answer-markdown {
  color: #586276;
  font-size: 15px;
  line-height: 1.9;
  word-break: break-word;
}

.answer-markdown :deep(h1),
.answer-markdown :deep(h2),
.answer-markdown :deep(h3),
.answer-markdown :deep(h4),
.answer-markdown :deep(h5),
.answer-markdown :deep(h6) {
  margin: 1.2em 0 0.6em;
  color: #252936;
  font-weight: 900;
  line-height: 1.4;
}

.answer-markdown :deep(h1) {
  font-size: 24px;
}

.answer-markdown :deep(h2) {
  font-size: 21px;
}

.answer-markdown :deep(h3) {
  font-size: 18px;
}

.answer-markdown :deep(p) {
  margin: 0.85em 0;
}

.answer-markdown :deep(ul),
.answer-markdown :deep(ol) {
  margin: 0.9em 0;
  padding-left: 1.5em;
}

.answer-markdown :deep(li) {
  margin: 0.45em 0;
}

.answer-markdown :deep(strong) {
  color: #252936;
  font-weight: 900;
}

.answer-markdown :deep(blockquote) {
  margin: 1em 0;
  padding: 12px 16px;
  border-left: 4px solid #ffb39d;
  border-radius: 0 8px 8px 0;
  background: #fff3ee;
  color: #6f6172;
}

.answer-markdown :deep(a) {
  color: #ff5a2a;
  text-decoration: none;
}

.answer-markdown :deep(a:hover) {
  text-decoration: underline;
}

.answer-markdown :deep(code) {
  padding: 0.15em 0.45em;
  border-radius: 6px;
  background: #f4f6fb;
  color: #d9480f;
  font-size: 0.92em;
  font-family: Consolas, 'Courier New', monospace;
}

.answer-markdown :deep(pre) {
  margin: 1.2em 0;
  padding: 18px 20px;
  overflow: auto;
  border-radius: 12px;
  background: #171b2a;
  color: #e7ecff;
  font-size: 13px;
  line-height: 1.75;
}

.answer-markdown :deep(pre code) {
  padding: 0;
  background: transparent;
  color: inherit;
  font-size: inherit;
}

.answer-markdown :deep(table) {
  width: 100%;
  margin: 1.1em 0;
  border-collapse: collapse;
  border-spacing: 0;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e9edf5 inset;
}

.answer-markdown :deep(th),
.answer-markdown :deep(td) {
  padding: 12px 14px;
  border: 1px solid #e9edf5;
  text-align: left;
  vertical-align: top;
}

.answer-markdown :deep(th) {
  background: #f7f9fc;
  color: #252936;
  font-weight: 800;
}

.answer-markdown :deep(hr) {
  margin: 1.4em 0;
  border: none;
  border-top: 1px solid #e7ebf3;
}

.answer-markdown :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 1em 0;
  border-radius: 10px;
}

.answer-mask {
  flex: 1;
  margin-top: 22px;
  padding: 24px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #687286;
  background: #f6f7f9;
}

.question-footer {
  padding: 0 24px;
  display: grid;
  grid-template-columns: 120px 1fr 120px;
  align-items: center;
  border-top: 1px solid #e8edf5;
  background: #ffffff;
}

.question-footer button {
  height: 34px;
  border: none;
  border-radius: 7px;
  color: #737d91;
  background: #f2f4f8;
  font-weight: 900;
  cursor: pointer;
}

.question-footer button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.question-footer span {
  justify-self: center;
  color: #9aa3b6;
  font-size: 13px;
  font-weight: 800;
}

.question-footer .next-btn {
  color: #ffffff;
  background: #ff5a2a;
}

@media (max-width: 1020px) {
  .app-sidebar {
    position: static;
    width: 100%;
    min-height: auto;
  }

  .role-page {
    margin-left: 0;
    padding: 28px 20px 46px;
  }

  .side-nav {
    grid-template-columns: repeat(5, max-content);
    overflow-x: auto;
  }

  .user-card {
    display: none;
  }

  .question-workspace {
    height: 100%;
    min-height: 0;
    grid-template-columns: 1fr;
    grid-template-rows: minmax(180px, 34vh) minmax(0, 1fr);
  }

  .question-sidebar {
    min-height: 0;
    max-height: none;
    border-right: none;
    border-bottom: 1px solid #e8edf5;
  }
}

@media (max-width: 1360px) {
  .role-grid {
    grid-template-columns: repeat(3, minmax(220px, 1fr));
  }
}

@media (max-width: 1120px) {
  .role-grid {
    grid-template-columns: repeat(2, minmax(240px, 1fr));
  }
}

@media (max-width: 720px) {
  .role-grid {
    grid-template-columns: 1fr;
  }

  .question-content {
    padding: 38px 20px 40px;
  }

  .answer-markdown {
    font-size: 14px;
  }

  .answer-markdown :deep(table) {
    display: block;
    overflow-x: auto;
    white-space: nowrap;
  }

  .question-topbar {
    gap: 12px;
    align-items: flex-start;
    justify-content: center;
    flex-direction: column;
    height: auto;
    padding: 12px 20px;
  }

  .question-main {
    grid-template-rows: auto minmax(0, 1fr) 64px;
  }
}
</style>
