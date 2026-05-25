<template>
  <div style="padding: 0; background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%); min-height: 100vh; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;">
    <!-- 顶部渐变背景 -->
    <div style="position: absolute; top: 0; left: 0; right: 0; height: 320px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); opacity: 0.1; z-index: 0;"></div>
    
    <!-- 主内容区 -->
    <div style="max-width: 1400px; margin: 0 auto 40px; padding: 0 20px; position: relative; z-index: 1;">
      <!-- 标题 -->
      <div style="display: flex; align-items: center; margin-bottom: 20; margin-top: 10;">
        <div style="width: 4; height: 24; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 2; margin-right: 12;"></div>
        <h2 style="margin: 0; color: #1f2937; font-weight: 700; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">
          面试题库
        </h2>
      </div>

      <!-- 搜索栏 -->
      <div style="background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(10px); border-radius: 16; box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15); border: 1px solid rgba(255, 255, 255, 0.2); padding: 20px 24px; margin-bottom: 32;">
        <el-space wrap style="justify-content: flex-start; row-gap: 12; column-gap: 8;">
          <el-button
            v-for="tag in tagButtons"
            :key="tag"
            :type="activeTag === tag ? 'primary' : 'default'"
            round
            @click="activeTag = tag"
            style="border-radius: 20; padding: 0 18px; height: 36; font-weight: 600; font-size: 14px; border: none; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);"
          >
            {{ tag }}
          </el-button>
        </el-space>
      </div>

      <!-- 题库卡片 -->
      <div style="max-width: 1400px; margin: 0 auto 60px; padding: 0 20px; position: relative; z-index: 1;">
        <!-- 加载状态 -->
        <div v-if="loading" style="text-align: center; padding: 40px 0;">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span style="margin-left: 8px;">加载中...</span>
        </div>
        
        <!-- 无数据状态 -->
        <div v-else-if="banks.length === 0" style="text-align: center; padding: 40px 0;">
          <el-icon><DocumentRemove /></el-icon>
          <p style="margin-top: 8px; color: #999;">暂无题库数据</p>
        </div>
        
        <!-- 题库列表 -->
        <el-row v-else :gutter="[20, 20]" justify="start">
          <el-col v-for="bank in filteredBanks" :key="bank.id" :xs="24" :sm="12" :md="8" :lg="6">
            <div 
              class="bank-card"
              @mouseenter="handleCardHover"
              @mouseleave="handleCardLeave"
              @click="navigateToBank(bank.id)"
            >
              <div class="bank-icon">{{ bank.icon }}</div>
              <h3 class="bank-title">{{ bank.title }}</h3>
              <p class="bank-description">{{ bank.description }}</p>
              <div class="bank-meta">
                <el-tag size="small" type="info">{{ bank.questionCount }} 题</el-tag>
                <span class="bank-count">{{ bank.popular ? '热门' : '' }}</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading, DocumentRemove } from '@element-plus/icons-vue'

const router = useRouter()
const activeTag = ref('热门')
const banks = ref<any[]>([])
const loading = ref(false)

// interface QuestionBank {
//   id: string
//   title: string
//   description: string
//   questionCount: number
//   icon: string
//   popular: boolean
// }

const tagButtons = ['热门', '后端', '前端', 'Python', '公司题库', '计算机基础', 'Java', '校招热题', '真实面经', 'Go', '数据库', '计算机网络', '操作系统', '算法', '项目', '人工智能', '测试', 'C++', '大数据', '移动开发', '运维', 'C# .NET', '游戏开发', '安全', '区块链', '编程语言', 'HR面试', '考研', 'PHP', '全部']

// 从后端获取题库列表
const fetchBanks = async () => {
  loading.value = true
  try {
    const response = await fetch('http://localhost:8080/api/banks/list')
    if (!response.ok) {
      throw new Error('Failed to fetch banks')
    }
    const data = await response.json()
    if (data.success) {
      // 转换后端数据格式
      banks.value = data.data.records.map((bank: any) => ({
        id: bank.id.toString(),
        title: bank.title,
        description: bank.description,
        questionCount: bank.questionCount,
        icon: bank.icon || '📚',
        popular: bank.popular || false
      }))
    }
  } catch (error) {
    console.error('Error fetching banks:', error)
  } finally {
    loading.value = false
  }
}

const filteredBanks = computed(() => {
  if (activeTag.value === '全部') return banks.value
  if (activeTag.value === '热门') return banks.value.filter(b => b.popular)
  // 这里可以根据标签过滤，暂时返回全部
  return banks.value
})

// const getDifficultyType = (difficulty: string): string => {
//   const typeMap: Record<string, string> = {
//     'easy': 'success',
//     'medium': 'warning',
//     'hard': 'danger'
//   }
//   return typeMap[difficulty] || 'default'
// }

const navigateToBank = (id: string) => {
  router.push(`/QuestionBankDetail/${id}`)
}

const handleCardHover = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  target.style.transform = 'translateY(-8px)'
  target.style.boxShadow = '0 8px 30px rgba(0, 0, 0, 0.12)'
  target.style.borderColor = '#A5B4FC'
}

const handleCardLeave = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  target.style.transform = ''
  target.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.08)'
  target.style.borderColor = 'rgba(255, 255, 255, 0.2)'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBanks()
})
</script>

<style scoped>
.bank-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16;
  padding: 20;
  height: 100%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.bank-icon {
  font-size: 32px;
  font-weight: bold;
  color: #6366f1;
  margin-bottom: 12;
}

.bank-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8 0;
}

.bank-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0 0 12 0;
}

.bank-meta {
  display: flex;
  align-items: center;
  gap: 8;
}

.bank-count {
  font-size: 12px;
  color: #9ca3af;
}
</style>