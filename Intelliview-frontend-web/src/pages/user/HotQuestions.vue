<template>
  <div class="hot-questions-container">
    <!-- 头部组件 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <el-page-header @back="goBack" content="热门面试题目榜" />
        <h1 class="page-title">热门面试题目榜</h1>
        <p class="page-subtitle">展示最热门的100道题目</p>
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
            <el-button type="primary" size="small" @click="fetchHotQuestions">重试</el-button>
          </template>
        </el-empty>
      </div>
      
      <!-- 热门题目列表 -->
      <div v-else class="hot-questions-list">
        <el-table :data="hotQuestions" style="width: 100%">
          <el-table-column label="排名" width="80">
            <template #default="scope">
              <span class="ranking-number">{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="题目" min-width="400">
            <template #default="scope">
              <div class="question-content" @click="navigateToQuestion(scope.row.id)">
                <span class="question-title">{{ scope.row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="浏览量" width="120">
            <template #default="scope">
              <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" size="small" @click="navigateToQuestion(scope.row.id)">
                查看
              </el-button>
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
import axios from 'axios'
import Header from '../../components/Header/index.vue'
import Footer from '../../components/Footer/index.vue'

const router = useRouter()
const loading = ref(false)
const error = ref('')

interface HotQuestion {
  id: number
  title: string
  viewCount: number
}

const hotQuestions = ref<HotQuestion[]>([])

const goBack = () => {
  router.push('/user')
}

const navigateToQuestion = (id: number) => {
  router.push(`/question/${id}`)
}

// 从API获取热门题目
const fetchHotQuestions = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('开始获取热门题目...')
    const token = localStorage.getItem('token')
    // 使用GET /api/questions/hot接口，获取100条热门题目
    const response = await axios.get('/api/questions/hot', {
      params: {
        page: 0,
        size: 100
      },
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    
    console.log('获取热门题目成功:', response.data)
    if (response.data.success) {
      hotQuestions.value = response.data.data.records || []
      console.log('热门题目数据:', hotQuestions.value)
    } else {
      throw new Error(response.data.message || '获取热门题目失败')
    }
  } catch (err: any) {
    console.error('获取热门题目失败:', err)
    // 提供默认热门题目数据
    hotQuestions.value = [
      { id: 1, title: 'Spring Boot的自动配置原理是什么？', viewCount: 2722 },
      { id: 2, title: 'JVM的内存结构是怎样的？', viewCount: 2221 },
      { id: 3, title: 'Java中的多线程实现方式有哪些？', viewCount: 2100 },
      { id: 4, title: '什么是算法的时间复杂度和空间复杂度？', viewCount: 1980 },
      { id: 5, title: '计算机网络OSI七层模型和TCP/IP四层模型的区别是什么？', viewCount: 1850 },
      { id: 6, title: 'Java中抽象类和接口的区别是什么？', viewCount: 1720 },
      { id: 7, title: '什么是数据结构中的栈和队列？它们的区别是什么？', viewCount: 1650 },
      { id: 8, title: 'Java方法重载和方法重写之间的区别是什么？', viewCount: 1580 },
      { id: 9, title: '操作系统中的死锁是什么？如何避免？', viewCount: 1520 },
      { id: 10, title: '什么是RESTful API？如何设计一个好的RESTful API？', viewCount: 1480 },
      { id: 11, title: 'MySQL索引的最左前缀原则是什么？', viewCount: 1450 },
      { id: 12, title: 'Spring Cloud的核心组件有哪些？', viewCount: 1420 },
      { id: 13, title: 'Redis的数据类型有哪些？', viewCount: 1380 },
      { id: 14, title: '什么是微服务架构？它的优缺点是什么？', viewCount: 1350 },
      { id: 15, title: 'Java中的垃圾回收机制是怎样的？', viewCount: 1320 },
      { id: 16, title: 'MySQL的事务隔离级别有哪些？', viewCount: 1280 },
      { id: 17, title: 'Spring的IoC和AOP原理是什么？', viewCount: 1250 },
      { id: 18, title: '什么是分布式系统？分布式系统的CAP理论是什么？', viewCount: 1220 },
      { id: 19, title: 'Redis的持久化方式有哪些？', viewCount: 1180 },
      { id: 20, title: 'Java中的集合框架有哪些？它们的区别是什么？', viewCount: 1150 },
      { id: 21, title: '什么是设计模式？常见的设计模式有哪些？', viewCount: 1120 },
      { id: 22, title: '计算机网络中的TCP三次握手和四次挥手过程是什么？', viewCount: 1080 },
      { id: 23, title: 'MySQL的优化方法有哪些？', viewCount: 1050 },
      { id: 24, title: 'Spring Boot的核心注解有哪些？', viewCount: 1020 },
      { id: 25, title: '什么是线程安全？如何实现线程安全？', viewCount: 980 },
      { id: 26, title: 'Redis的缓存穿透、缓存击穿、缓存雪崩是什么？如何解决？', viewCount: 950 },
      { id: 27, title: 'Java中的反射机制是什么？它的优缺点是什么？', viewCount: 920 },
      { id: 28, title: '什么是分布式事务？如何实现分布式事务？', viewCount: 880 },
      { id: 29, title: 'MySQL的存储引擎有哪些？它们的区别是什么？', viewCount: 850 },
      { id: 30, title: 'Spring Cloud与Dubbo的区别是什么？', viewCount: 820 },
      { id: 31, title: 'Java中的异常处理机制是什么？', viewCount: 780 },
      { id: 32, title: '什么是分布式锁？如何实现分布式锁？', viewCount: 750 },
      { id: 33, title: 'Redis的集群模式有哪些？', viewCount: 720 },
      { id: 34, title: 'Java中的泛型是什么？它的优缺点是什么？', viewCount: 680 },
      { id: 35, title: '什么是Docker？它的优缺点是什么？', viewCount: 650 },
      { id: 36, title: 'MySQL的主从复制原理是什么？', viewCount: 620 },
      { id: 37, title: 'Spring Boot的启动流程是什么？', viewCount: 590 },
      { id: 38, title: '什么是Kubernetes？它的核心组件有哪些？', viewCount: 560 },
      { id: 39, title: 'Java中的Stream API是什么？如何使用？', viewCount: 520 },
      { id: 40, title: '什么是Git？常见的Git命令有哪些？', viewCount: 480 },
      { id: 41, title: 'MySQL的索引类型有哪些？', viewCount: 450 },
      { id: 42, title: 'Spring Cloud Config的作用是什么？', viewCount: 420 },
      { id: 43, title: 'Redis的过期策略有哪些？', viewCount: 380 },
      { id: 44, title: 'Java中的注解是什么？如何自定义注解？', viewCount: 350 },
      { id: 45, title: '什么是CI/CD？常见的CI/CD工具有哪些？', viewCount: 320 },
      { id: 46, title: 'MySQL的查询优化方法有哪些？', viewCount: 280 },
      { id: 47, title: 'Spring Cloud Gateway的作用是什么？', viewCount: 250 },
      { id: 48, title: 'Redis的内存淘汰策略有哪些？', viewCount: 220 },
      { id: 49, title: 'Java中的Lambda表达式是什么？如何使用？', viewCount: 180 },
      { id: 50, title: '什么是REST？RESTful API的设计原则是什么？', viewCount: 150 }
    ]
    error.value = '获取热门题目失败，显示默认数据'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotQuestions()
})
</script>

<style scoped>
.hot-questions-container {
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

.hot-questions-list {
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

.question-content {
  cursor: pointer;
  transition: all 0.3s ease;
}

.question-content:hover {
  color: #ff9800;
}

.question-title {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.view-count {
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
  
  .hot-questions-list {
    padding: 16px;
  }
  
  :deep(.el-table td) {
    padding: 12px !important;
  }
}
</style>