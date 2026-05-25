<template>
  <div class="question-detail-container">
    <!-- 头部组件 -->
    <Header />
    <!-- 顶部渐变背景 -->
    <div class="top-gradient"></div>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 左侧内容 -->
      <div class="left-content">
        <!-- 题目详情 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="10" animated />
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="error" class="error-state">
          <el-empty>
            <template #description>
              <p>{{ error }}</p>
              <el-button type="primary" size="small" @click="fetchQuestion">重试</el-button>
            </template>
          </el-empty>
        </div>
        
        <!-- 题目内容 -->
        <div v-else-if="question" class="question-content">
          <!-- 顶部导航 -->
          <div class="top-nav">
            <el-page-header @back="goBack" content="题目详情" />
          </div>
          
          <!-- 题目标题 -->
          <div class="question-header">
            <div class="title-section">
              <h1 class="question-title">{{ question.id }}. {{ question.title }}</h1>
              <div class="question-tags">
                <el-tag type="warning" class="difficulty-tag">
                  {{ question.difficultyLabel || (question.difficulty === 'EASY' ? '简单' : question.difficulty === 'MEDIUM' ? '中等' : '困难') }}
                </el-tag>
                <template v-for="tag in question.tags" :key="tag?.id">
                  <el-tag v-if="tag" class="category-tag">
                    {{ tag.name }}
                  </el-tag>
                </template>
              </div>
            </div>
            
            <!-- 题目元信息 -->
            <div class="question-meta">
              <el-button 
                size="small" 
                class="meta-btn favorite-btn" 
                :class="{ 'favorited': question.isFavorited }"
                @click="toggleFavorite"
              >
                <el-icon v-if="question.isFavorited"><StarFilled /></el-icon>
                <el-icon v-else><Star /></el-icon> {{ question.isFavorited ? '已收藏' : '收藏' }}
              </el-button>
              <el-button size="small" class="meta-btn" @click="shareQuestion">
                <el-icon><Share /></el-icon> 分享
              </el-button>
              <el-button size="small" class="meta-btn" @click="toggleLike">
                <i class="fas fa-thumbs-up"></i> {{ question.likeCount }}
              </el-button>
              <el-button size="small" class="meta-btn">
                <el-icon><View /></el-icon> {{ question.viewCount }}
              </el-button>
            </div>
          </div>
          
          <!-- 题目描述 -->
          <h2 class="section-title">题目描述</h2>
          <div class="question-text" v-if="question.questionText">
            <div class="markdown-preview" v-html="formattedQuestionText"></div>
          </div>
          
          <!-- 功能按钮 -->
          <div class="action-buttons">
            <div class="all-buttons">
              <el-button type="primary" plain class="action-btn active">推荐答案</el-button>
              <el-button class="action-btn">视频讲解</el-button>
              <el-button class="action-btn">测试一下</el-button>
              <el-button class="action-btn">面试问答</el-button>
              <el-button class="action-btn">开始面试</el-button>
              <div class="right-buttons">
                <el-button size="small" class="action-btn">
                  <el-icon><Microphone /></el-icon> 语音朗读
                </el-button>
                <el-button size="small" class="action-btn" @click="showAnswer = !showAnswer">
                  <el-icon><View /></el-icon> {{ showAnswer ? '隐藏答案' : '展示答案' }}
                </el-button>
              </div>
            </div>
          </div>
          
          <!-- 答案区域 -->
          <div class="answer-section">
            <h2 class="section-title">题目答案</h2>
            <!-- 答案隐藏状态 -->
            <div v-if="!showAnswer" class="answer-hidden">
              <div class="hidden-icon">
                <el-icon class="hide-icon"><Hide /></el-icon>
              </div>
              <div class="hidden-text">答案已隐藏</div>
              <el-button type="primary" class="show-answer-btn" @click="showAnswer = true">
                显示答案
              </el-button>
            </div>
            
            <!-- 答案显示状态 -->
            <div v-else class="answer-content">
              <!-- Markdown渲染区域 -->
              <div class="markdown-preview" v-html="formattedContent"></div>
            </div>
          </div>
          
          <!-- 相关题目 -->
          <div class="related-questions" v-if="relatedQuestions.length > 0">
            <h3 class="section-title">相关题目</h3>
            <div class="related-list">
              <div v-for="item in relatedQuestions" :key="item.id" class="related-item" @click="goToQuestion(item.id)">
                <span class="related-id">{{ item.id }}.</span>
                <span class="related-title">{{ item.title }}</span>
                <el-tag size="small" class="related-difficulty">
                  {{ item.difficulty === 'EASY' ? '简单' : item.difficulty === 'MEDIUM' ? '中等' : '困难' }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <!-- 分享奖励 -->
          <div class="share-reward">
            <div class="reward-text">分享最高赚 ¥64.5</div>
            <div class="share-buttons">
              <el-button size="small" class="share-btn" @click="copyLink">
                <el-icon><Link /></el-icon>
              </el-button>
              <el-button size="small" class="share-btn" @click="shareToWechat">
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
              <el-button size="small" class="share-btn" @click="shareToMobile">
                <el-icon><Cellphone /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧内容 -->
      <div class="right-content">
        <div class="sidebar">
          <!-- 题目统计 -->
          <div class="stats-card">
            <h4 class="card-title">题目统计</h4>
            <div class="stats-list">
              <div class="stat-item">
                <span class="stat-label">浏览量</span>
                <span class="stat-value">{{ question?.viewCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">点赞数</span>
                <span class="stat-value">{{ question?.likeCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">回答数</span>
                <span class="stat-value">{{ question?.answerCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">收藏数</span>
                <span class="stat-value">{{ question?.markCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">分享数</span>
                <span class="stat-value">{{ question?.shareCount || 0 }}</span>
              </div>
            </div>
          </div>
          
          <!-- 题目分类 -->
          <div class="category-card" v-if="question">
            <h4 class="card-title">题目分类</h4>
            <div class="category-info">
              <span class="category-name">{{ question.category?.name || '面试题' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部组件 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, Share, StarFilled, View, Microphone, Hide, Link, ChatDotRound, Cellphone } from '@element-plus/icons-vue'
import Header from '../../components/Header/index.vue'
import Footer from '../../components/Footer/index.vue'
import service from '@/utils/axios'

const route = useRoute()
const router = useRouter()

interface Question {
  id: number
  title: string
  questionText: string
  answerText: string
  difficulty: string
  difficultyLabel: string
  tags: Array<{
    id: number
    name: string
  }>
  category: {
    id: number
    name: string
  }
  viewCount: number
  likeCount: number
  answerCount: number
  markCount: number
  shareCount: number
  createdAt: string
  isLiked: boolean
  isFavorited: boolean
  description: string
  slug: string
  browseCount: number
  lastBrowseTime: string
  keyPoints: any[]
  categoryId: number
  submitCount: number
  acceptCount: number
  acceptRate: number
  isVisible: boolean
  sortOrder: number
  createdBy: number
  updatedAt: string
}

interface RelatedQuestion {
  id: number
  title: string
  difficulty: string
  tags: Array<{
    id: number
    name: string
  }>
}

const question = ref<Question | null>(null)
const relatedQuestions = ref<RelatedQuestion[]>([])
const loading = ref(false)
const error = ref('')
const showAnswer = ref(false)

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false
})

const renderMarkdown = (content?: string, hiddenTitles: string[] = []) => {
  if (!content) return ''

  const normalizedContent = hiddenTitles.reduce((result, title) => {
    const escapedTitle = title.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    return result
      .replace(new RegExp(`^#\\s*${escapedTitle}\\s*$`, 'gim'), '')
      .replace(new RegExp(`^##\\s*${escapedTitle}\\s*$`, 'gim'), '')
      .replace(new RegExp(`^###\\s*${escapedTitle}\\s*$`, 'gim'), '')
      .replace(new RegExp(`^${escapedTitle}\\s*$`, 'gim'), '')
  }, content).trim()

  return DOMPurify.sanitize(markdown.render(normalizedContent))
}

// 刷题统计数据
const studyStats = ref({
  startTime: 0,
  endTime: 0,
  duration: 0,
  viewAnswer: false,
  questionId: 0
})

const goBack = () => {
  router.push('/user')
}

const goToQuestion = (id: number) => {
  router.push(`/question/${id}`)
}

// 初始化刷题统计数据
const initStudyStats = (questionId: number) => {
  const startTime = Date.now()
  
  studyStats.value = {
    startTime: startTime,
    endTime: 0,
    duration: 0,
    viewAnswer: false,
    questionId: questionId
  }
}

// 计算题目所需的最小停留时长（毫秒）
const calculateRequiredDuration = (question: Question): number => {
  // 基础时长（毫秒）
  let baseDuration = 30000; // 默认30秒
  
  // 根据难度调整基础时长
  switch (question.difficulty) {
    case 'EASY':
      baseDuration = 30000; // 简单题30秒
      break;
    case 'MEDIUM':
      baseDuration = 60000; // 中等题60秒
      break;
    case 'HARD':
      baseDuration = 120000; // 困难题120秒
      break;
  }
  
  // 计算内容长度（题目+答案）
  const contentLength = (question.questionText?.length || 0) + (question.answerText?.length || 0);
  
  // 每100个字符增加10秒
  const extraDuration = Math.floor(contentLength / 100) * 10000;
  
  // 最大调整不超过基础时长的2倍
  const maxExtraDuration = baseDuration * 2;
  const actualExtraDuration = Math.min(extraDuration, maxExtraDuration);
  
  return baseDuration + actualExtraDuration;
}

// 记录刷题行为
const recordPractice = async () => {
  if (!studyStats.value.questionId || !question.value) return
  
  const endTime = Date.now()
  studyStats.value.endTime = endTime
  studyStats.value.duration = endTime - studyStats.value.startTime
  
  // 计算所需的最小停留时长
  const requiredDuration = calculateRequiredDuration(question.value);
  
  // 标记题目为已完成（根据刷题规则：查看答案或刷题时长达到要求）
  const completed = studyStats.value.viewAnswer || studyStats.value.duration >= requiredDuration;
  
  try {
    const token = localStorage.getItem('token')
    const response = await service.post('/api/practice-history/record', {
      questionId: studyStats.value.questionId,
      completed: completed,
      viewAnswer: studyStats.value.viewAnswer,
      duration: studyStats.value.duration
    }, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    }) as any
    
    if (response.success) return
  } catch (err: any) {
    console.error('刷题记录提交失败:', err)
  }
}

// 格式化题目正文内容，支持Markdown语法
const formattedQuestionText = computed(() => {
  return renderMarkdown(question.value?.questionText, ['题目描述'])
})

// 格式化答案内容，支持Markdown语法
const formattedContent = computed(() => {
  return renderMarkdown(question.value?.answerText, ['回答重点'])
})



// 从API获取题目详情
const fetchQuestion = async () => {
  const questionId = route.params.id as string
  
  loading.value = true
  error.value = ''
  
  try {
    // 添加超时处理
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('请求超时，请检查网络连接')), 15000)
    })
    
    const response = await Promise.race([
      service.get(`/api/questions/detail/${questionId}`),
      timeoutPromise
    ]) as any
    
    // 检查 response 是否是有效的对象
    if (typeof response === 'object' && response !== null && 'success' in response) {
      if (response.success) {
        question.value = response.data
        fetchRelatedQuestions(questionId)
        
        // 初始化刷题统计数据
        if (question.value) {
          initStudyStats(question.value.id)
          // 增加浏览量
          incrementViewCount(question.value.id)
        }
      } else {
        error.value = response.message || '获取题目详情失败'
        console.error('获取题目详情失败:', error.value)
        ElMessage.error(error.value)
      }
    } else {
      throw new Error('无效的响应数据格式')
    }
  } catch (err: any) {
    console.error('获取题目详情失败:', err)
    console.error('错误详情:', err.response)
    error.value = err.message || '网络错误，请检查后端服务是否正常'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

// 获取相关题目
const fetchRelatedQuestions = async (questionId: string) => {
  try {
    const response = await service.get(`/api/questions/detail/${questionId}/related`) as any
    
    if (response.success) {
      relatedQuestions.value = response.data
    }
  } catch (err: any) {
    console.error('获取相关题目失败:', err)
  }
}

// 点赞题目
const toggleLike = async () => {
  if (!question.value) return
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      return
    }
    
    const endpoint = question.value.isLiked 
      ? `/api/questions/detail/${question.value.id}/unlike` 
      : `/api/questions/detail/${question.value.id}/like`
    
    const response = await service.post(endpoint, {}) as any
    
    if (response.success) {
      question.value.isLiked = !question.value.isLiked
      question.value.likeCount += question.value.isLiked ? 1 : -1
      ElMessage.success(response.message)
    }
  } catch (err: any) {
    console.error('点赞操作失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 收藏题目
const toggleFavorite = async () => {
  if (!question.value) return
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      return
    }
    
    // 调用后端API实现收藏/取消收藏
    const endpoint = `/api/favorites/${question.value.id}`
    const method = question.value.isFavorited ? 'delete' : 'post'
    
    // 使用已配置好的service实例发送请求
    let response: any
    if (method === 'post') {
      response = await service.post(endpoint, {})
    } else {
      response = await service.delete(endpoint, { data: {} })
    }
    if (response.success) {
      question.value.isFavorited = !question.value.isFavorited
      question.value.markCount += question.value.isFavorited ? 1 : -1
      ElMessage.success(response.message || (question.value.isFavorited ? '收藏成功' : '取消收藏成功'))
    } else {
      // 处理后端返回的错误
      ElMessage.error(response.message || '操作失败，请稍后重试')
    }
  } catch (err: any) {
    console.error('收藏操作失败:', err)
    console.error('错误详情:', err.response)
    // 处理网络错误或其他错误
    if (err.response) {
      // 后端返回的错误
      ElMessage.error(err.response.data.message || '操作失败，请稍后重试')
    } else {
      // 网络错误或其他错误
      ElMessage.error('网络错误，请检查后端服务是否正常')
    }
  }
}

// 分享题目
const shareQuestion = async () => {
  if (!question.value) return
  
  try {
    const response = await service.post(`/api/questions/detail/${question.value.id}/share`, {}) as any
    
    if (response.success) {
      question.value.shareCount += 1
      ElMessage.success('分享成功')
      
      // 显示分享链接
      const shareUrl = `${window.location.origin}/question/${question.value.id}`
      await ElMessageBox.alert(
        `<p>分享链接：</p><p style="word-break: break-all;">${shareUrl}</p>`,
        '分享题目',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '复制链接',
          callback: async () => {
            try {
              await navigator.clipboard.writeText(shareUrl)
              ElMessage.success('链接已复制到剪贴板')
            } catch {
              ElMessage.error('复制失败，请手动复制')
            }
          }
        }
      )
    }
  } catch (err: any) {
    console.error('分享操作失败:', err)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 复制链接
const copyLink = async () => {
  if (!question.value) return
  
  try {
    const shareUrl = `${window.location.origin}/question/${question.value.id}`
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 分享到微信
const shareToWechat = () => {
  ElMessage.info('请使用微信扫描二维码分享')
}

// 增加浏览量
const incrementViewCount = async (questionId: number) => {
  try {
    const token = localStorage.getItem('token')
    const response = await service.post(`/api/questions/detail/${questionId}/view`, {}, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    }) as any
    
    if (response.success && question.value) {
      // 更新本地浏览量
      question.value.viewCount += 1
    }
  } catch (error: any) {
    console.error('增加浏览量失败:', error)
    // 静默失败，不影响用户体验
  }
}

// 分享到手机
const shareToMobile = () => {
  ElMessage.info('请使用手机浏览器打开链接')
}

// 监听答案显示状态变化
watch(() => showAnswer.value, (newValue) => {
  if (newValue) {
    studyStats.value.viewAnswer = true
  }
})

// 页面卸载时提交刷题记录
onUnmounted(() => {
  recordPractice()
})

onMounted(() => {
  fetchQuestion()
})
</script>

<style scoped>
.question-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
  overflow-x: hidden;
}

/* 顶部导航样式 */
.top-nav {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 回退按钮统一样式 */
:deep(.el-page-header) {
  padding: 0 !important;
}

:deep(.el-page-header__left) {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.el-page-header__back) {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: #666;
}

:deep(.el-page-header__back:hover) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  transform: translateY(-1px);
}

:deep(.el-page-header__title) {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

/* 顶部渐变背景 */
.top-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.08;
  z-index: 0;
  border-radius: 0 0 32px 32px;
}

.main-content {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto 60px;
  padding: 40px 24px 0;
  position: relative;
  z-index: 1;
}

.left-content {
  flex: 1;
  min-width: 0;
}

.right-content {
  width: 300px;
  flex-shrink: 0;
}

.loading-state,
.error-state {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 40px;
  margin-bottom: 24px;
}

.question-content {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 32px;
}

.question-header {
  margin-bottom: 32px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 24px;
}

.title-section {
  margin-bottom: 16px;
}

.question-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 16px 0;
  line-height: 1.5;
  letter-spacing: 0.5px;
}

.question-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.difficulty-tag {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 2px;
}

.category-tag {
  font-size: 11px;
  padding: 2px 6px;
  background-color: #f0f0f0;
  color: #666666;
  border-radius: 2px;
}

.question-meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.meta-btn {
  font-size: 12px;
  padding: 4px 8px;
  color: #666666;
}

.favorite-btn {
  transition: all 0.3s ease;
}

.favorite-btn.favorited {
  color: #F56C6C;
}

.favorite-btn.favorited i {
  color: #F56C6C;
}

.action-buttons {
  margin-bottom: 32px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e0e0e0;
}

.all-buttons {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.right-buttons {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.action-btn {
  font-size: 13px;
  padding: 6px 14px;
  color: #666666;
  border-radius: 4px;
}

.action-btn.active {
  color: #409EFF;
  border-bottom: 2px solid #409EFF;
  background-color: #f8f9fa;
}

.right-buttons .action-btn {
  font-size: 12px;
  padding: 4px 10px;
}

.answer-section {
  margin-bottom: 32px;
}

.answer-hidden {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 80px 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.hidden-icon {
  margin-bottom: 24px;
  opacity: 0.6;
}

.hide-icon {
  font-size: 100px;
  color: #c0c4cc;
}

.hidden-text {
  font-size: 20px;
  color: #909399;
  margin-bottom: 32px;
  font-weight: 500;
}

.show-answer-btn {
  font-size: 16px;
  padding: 10px 40px;
  font-weight: 500;
}

.answer-content {
  padding: 0;
}

/* Markdown渲染样式 */
.markdown-preview {
  line-height: 1.8;
  color: #333333;
  font-size: 14px;
}

:deep(.markdown-preview h1),
:deep(.markdown-preview h2),
:deep(.markdown-preview h3),
:deep(.markdown-preview h4),
:deep(.markdown-preview h5),
:deep(.markdown-preview h6) {
  color: #2c3e50;
  font-weight: 700;
  line-height: 1.45;
  margin: 24px 0 14px;
}

:deep(.markdown-preview h1) {
  font-size: 24px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

:deep(.markdown-preview h2) {
  font-size: 20px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.markdown-preview h3) {
  font-size: 18px;
}

:deep(.markdown-preview p) {
  margin: 0 0 16px;
  text-align: justify;
}

:deep(.markdown-preview strong) {
  font-weight: 700;
  color: #2c3e50;
}

:deep(.markdown-preview em) {
  font-style: italic;
  color: #666666;
}

:deep(.markdown-preview ul),
:deep(.markdown-preview ol) {
  margin: 0 0 16px;
  padding-left: 24px;
}

:deep(.markdown-preview li) {
  margin: 6px 0;
  line-height: 1.7;
}

:deep(.markdown-preview pre) {
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 14px 16px;
  margin: 16px 0;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #333333;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

:deep(.markdown-preview code) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.92em;
  background: rgba(64, 158, 255, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
}

:deep(.markdown-preview pre code) {
  background: transparent;
  padding: 0;
  border-radius: 0;
}

:deep(.markdown-preview img),
:deep(.content-image) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.markdown-preview a) {
  color: #1890ff;
  text-decoration: none;
  border-bottom: 1px solid #1890ff;
  padding-bottom: 1px;
  transition: all 0.3s;
}

:deep(.markdown-preview a:hover) {
  color: #40a9ff;
  border-bottom-color: #40a9ff;
}

:deep(.markdown-preview blockquote) {
  margin: 16px 0;
  padding: 12px 16px;
  color: #5f6b7a;
  background: #f7faff;
  border-left: 4px solid #91caff;
  border-radius: 0 8px 8px 0;
}

:deep(.markdown-preview hr) {
  border: 0;
  border-top: 1px solid #e5e7eb;
  margin: 24px 0;
}

:deep(.markdown-preview table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #ebeef5 inset;
}

:deep(.markdown-preview th),
:deep(.markdown-preview td) {
  padding: 10px 12px;
  text-align: left;
  border: 1px solid #ebeef5;
  vertical-align: top;
}

:deep(.markdown-preview th) {
  font-weight: 600;
  color: #2c3e50;
  background: #f8fafc;
}

.related-questions {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #e0e0e0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.related-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.related-id {
  font-size: 12px;
  color: #909399;
  width: 40px;
  flex-shrink: 0;
}

.related-title {
  flex: 1;
  font-size: 14px;
  color: #333333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-difficulty {
  font-size: 10px;
  padding: 1px 6px;
  flex-shrink: 0;
}

.share-reward {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f0f7ff;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  margin-top: 32px;
}

.reward-text {
  font-size: 14px;
  color: #666666;
  font-weight: 500;
}

.share-buttons {
  display: flex;
  gap: 12px;
}

.share-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  color: #666666;
  font-size: 16px;
}

.share-btn:hover {
  background-color: #f5f5f5;
  color: #409EFF;
  border-color: #409EFF;
}

/* 右侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-card,
.category-card {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.stats-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 13px;
  color: #666666;
}

.stat-value {
  font-size: 13px;
  font-weight: 500;
  color: #333333;
}

.category-info {
  display: flex;
  align-items: center;
}

.category-name {
  font-size: 13px;
  color: #333333;
  background-color: #f0f0f0;
  padding: 4px 8px;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }
  
  .right-content {
    width: 100%;
  }
  
  .sidebar {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .stats-card,
  .category-card {
    flex: 1;
    min-width: 250px;
  }
}

@media (max-width: 768px) {
  .top-nav {
    padding: 0 16px;
  }
  
  .main-content {
    padding: 0 16px 32px;
    gap: 16px;
  }
  
  .question-content {
    padding: 20px;
  }
  
  .question-title {
    font-size: 20px;
  }
  
  .all-buttons {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .right-buttons {
    margin-left: 0;
    align-self: stretch;
    justify-content: flex-end;
  }
  
  .action-btn {
    font-size: 12px;
    padding: 6px 12px;
  }
  
  .answer-section {
    min-height: 300px;
  }
  
  .answer-content {
    padding: 20px;
  }
  
  .answer-title {
    font-size: 18px;
  }
  
  .hidden-icon {
    font-size: 80px;
  }
  
  .hidden-text {
    font-size: 16px;
  }
  
  .show-answer-btn {
    font-size: 14px;
    padding: 8px 32px;
  }
  
  .sidebar {
    flex-direction: column;
  }
  
  .stats-card,
  .category-card {
    width: 100%;
  }
  
  .share-reward {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .share-buttons {
    align-self: stretch;
    justify-content: flex-end;
  }
}
</style>
