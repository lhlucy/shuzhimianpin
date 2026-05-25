<template>
  <div class="user-rankings-container">
    <!-- 头部组件 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <el-page-header @back="goBack" content="用户备战活跃榜" />
        <h1 class="page-title">用户备战活跃榜</h1>
        <p class="page-subtitle">根据用户的练习活跃度和准备投入展示当前排名</p>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="10" animated />
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <el-empty>
          <template #description>
            <p>{{ error }}</p>
            <el-button type="primary" size="small" @click="fetchUserRankings">重试</el-button>
          </template>
        </el-empty>
      </div>
      
      <!-- 用户排名列表 -->
      <div v-else class="user-rankings-list">
        <el-table :data="userRankings" style="width: 100%">
          <el-table-column label="排名" width="80">
            <template #default="scope">
              <span class="ranking-number">{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="用户" min-width="200">
            <template #default="scope">
              <div class="user-info">
                <img :src="scope.row.avatar" :alt="scope.row.name" class="user-avatar" />
                <span class="user-name">{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="练习数" width="120">
            <template #default="scope">
              <span class="question-count">{{ scope.row.questionCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已完成" width="120">
            <template #default="scope">
              <span class="completed-count">{{ scope.row.completedCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="学习时长" width="120">
            <template #default="scope">
              <span class="duration">{{ scope.row.totalDuration || 0 }}分钟</span>
            </template>
          </el-table-column>
          <el-table-column label="积分" width="100">
            <template #default="scope">
              <span class="score">{{ scope.row.score }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 底部组件 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import service from '@/utils/axios'
import Header from '../../components/Header/index.vue'
import Footer from '../../components/Footer/index.vue'

const router = useRouter()
const loading = ref(false)
const error = ref('')

interface UserRanking {
  id: number
  name: string
  avatar: string
  score: number
  questionCount?: number
  completedCount?: number
  totalDuration?: number
}

const userRankings = ref<UserRanking[]>([
  { id: 1, name: '张三', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 1200, questionCount: 50, completedCount: 45, totalDuration: 1800 },
  { id: 2, name: '李四', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 980, questionCount: 42, completedCount: 38, totalDuration: 1500 },
  { id: 3, name: '王五', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 850, questionCount: 38, completedCount: 34, totalDuration: 1200 },
  { id: 4, name: '赵六', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 780, questionCount: 35, completedCount: 30, totalDuration: 1000 },
  { id: 5, name: '钱七', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 720, questionCount: 32, completedCount: 28, totalDuration: 900 },
  { id: 6, name: '孙八', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 680, questionCount: 30, completedCount: 26, totalDuration: 800 },
  { id: 7, name: '周九', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 650, questionCount: 28, completedCount: 24, totalDuration: 750 },
  { id: 8, name: '吴十', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 620, questionCount: 26, completedCount: 22, totalDuration: 700 },
  { id: 9, name: '郑一', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 590, questionCount: 24, completedCount: 20, totalDuration: 650 },
  { id: 10, name: '王二', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 560, questionCount: 22, completedCount: 18, totalDuration: 600 }
])

const goBack = () => {
  router.push('/user')
}

// 从API获取用户排名
const fetchUserRankings = async () => {
  loading.value = true
  error.value = ''
  
  try {
    // 使用GET /api/user/rankings接口，获取用户排名
    const response: any = await service.get('/api/user/rankings', {
      params: {
        type: 'points',
        page: 0,
        size: 100
      }
    })
    
    if (response.success) {
      userRankings.value = response.data || []
    } else {
      throw new Error(response.message || '获取用户排名失败')
    }
  } catch (err: any) {
    console.error('获取用户排名失败:', err)
    // 提供默认用户排名数据
    userRankings.value = [
      { id: 1, name: '张三', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 1200, questionCount: 50, completedCount: 45, totalDuration: 1800 },
      { id: 2, name: '李四', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 980, questionCount: 42, completedCount: 38, totalDuration: 1500 },
      { id: 3, name: '王五', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 850, questionCount: 38, completedCount: 34, totalDuration: 1200 },
      { id: 4, name: '赵六', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 780, questionCount: 35, completedCount: 30, totalDuration: 1000 },
      { id: 5, name: '钱七', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 720, questionCount: 32, completedCount: 28, totalDuration: 900 },
      { id: 6, name: '孙八', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 680, questionCount: 30, completedCount: 26, totalDuration: 800 },
      { id: 7, name: '周九', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 650, questionCount: 28, completedCount: 24, totalDuration: 750 },
      { id: 8, name: '吴十', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 620, questionCount: 26, completedCount: 22, totalDuration: 700 },
      { id: 9, name: '郑一', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 590, questionCount: 24, completedCount: 20, totalDuration: 650 },
      { id: 10, name: '王二', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 560, questionCount: 22, completedCount: 18, totalDuration: 600 },
      { id: 11, name: '陈三', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 520, questionCount: 20, completedCount: 16, totalDuration: 550 },
      { id: 12, name: '林四', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 480, questionCount: 18, completedCount: 14, totalDuration: 500 },
      { id: 13, name: '黄五', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 450, questionCount: 16, completedCount: 12, totalDuration: 450 },
      { id: 14, name: '刘六', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 420, questionCount: 14, completedCount: 10, totalDuration: 400 },
      { id: 15, name: '杨七', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 380, questionCount: 12, completedCount: 8, totalDuration: 350 },
      { id: 16, name: '马八', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 350, questionCount: 10, completedCount: 6, totalDuration: 300 },
      { id: 17, name: '朱九', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 320, questionCount: 8, completedCount: 4, totalDuration: 250 },
      { id: 18, name: '秦十', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 280, questionCount: 6, completedCount: 3, totalDuration: 200 },
      { id: 19, name: '尤一', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 250, questionCount: 5, completedCount: 2, totalDuration: 150 },
      { id: 20, name: '许二', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', score: 220, questionCount: 4, completedCount: 1, totalDuration: 100 }
    ]
    error.value = '获取用户排名失败，显示默认数据'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserRankings()
})
</script>

<style scoped>
.user-rankings-container {
  min-height: 100vh;
  background: #f5f5f5;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto 60px;
  padding: 40px 24px 0;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 16px 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.loading-state,
.error-state {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 40px;
  margin-bottom: 24px;
}

.user-rankings-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.ranking-number {
  font-size: 16px;
  font-weight: 600;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.question-count,
.completed-count,
.duration,
.score {
  font-size: 14px;
  color: #666;
}

/* 排名样式 */
:deep(.el-table__row:nth-child(1) .ranking-number),
:deep(.el-table__row:nth-child(2) .ranking-number),
:deep(.el-table__row:nth-child(3) .ranking-number) {
  color: #ff5722;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f5f5f5 !important;
  color: #333 !important;
  font-weight: 600 !important;
}

:deep(.el-table td) {
  padding: 16px !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 24px 16px 0;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .user-rankings-list {
    padding: 16px;
  }
  
  :deep(.el-table td) {
    padding: 12px !important;
  }
  
  .user-info {
    gap: 8px;
  }
  
  .user-avatar {
    width: 24px;
    height: 24px;
  }
}
</style>
