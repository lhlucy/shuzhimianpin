<template>
  <div class="favorites-page">
    <aside class="app-sidebar">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>

      <nav class="side-nav" aria-label="收藏导航">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" :class="{ active: item.path === '/user/favorites' }">
          <el-icon><component :is="item.icon" /></el-icon>
          {{ item.label }}
        </router-link>
      </nav>
    </aside>

    <main class="favorites-main">
      <header class="page-head">
        <div>
          <h1>我的收藏</h1>
          <p>共收藏 {{ favoriteItems.length }} 道题目</p>
        </div>
        <el-input v-model="keyword" class="search-input" placeholder="搜索收藏题目...">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </header>

      <div class="filter-tabs">
        <button v-for="tab in tabs" :key="tab" type="button" :class="{ active: activeTab === tab }" @click="activeTab = tab">
          {{ tab }}
        </button>
      </div>

      <section class="favorite-list">
        <article v-for="item in filteredFavorites" :key="item.id" class="favorite-card">
          <div class="question-main">
            <div class="tag-row">
              <span :class="item.roleClass">{{ item.roleLabel }}</span>
              <span>{{ item.difficulty }}</span>
            </div>
            <h2>{{ item.title }}</h2>
            <p>收藏于 {{ item.date }}</p>
          </div>

          <div class="actions">
            <button type="button" :class="item.roleClass" @click="openQuestion(item.roleCode, item.id)">去练习</button>
            <el-icon><StarFilled /></el-icon>
          </div>
        </article>
      </section>

      <div class="notice">
        <el-icon><Warning /></el-icon>
        在刷题页面遇到好题可点击星标收藏，收藏后会同步展示在这里
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Collection, House, Monitor, Search, Star, StarFilled, User, Warning } from '@element-plus/icons-vue'
import service from '@/utils/axios'

const router = useRouter()
const keyword = ref('')
const activeTab = ref('全部')
const favoriteItems = ref<any[]>([])
const tabs = computed(() => ['全部', ...Array.from(new Set(favoriteItems.value.map((item) => item.roleLabel).filter(Boolean)))])
const navItems = [
  { label: '首页', path: '/user', icon: House },
  { label: '岗位刷题', path: '/user/practice', icon: Collection },
  { label: '模拟面试', path: '/user/interview/ai/create', icon: Monitor },
  { label: '历史记录', path: '/user/history', icon: Clock },
  { label: '我的收藏', path: '/user/favorites', icon: Star },
  { label: '个人中心', path: '/user/profile', icon: User }
]

const filteredFavorites = computed(() => {
  const text = keyword.value.trim()
  const category = activeTab.value === '全部' ? '' : activeTab.value
  return favoriteItems.value.filter((item: any, index: number, list: any[]) => {
    const unique = list.findIndex((target: any) => target.id === item.id) === index
    const matchText = !text || item.title.includes(text)
    const matchCategory = !category || item.roleLabel === category
    return unique && matchText && matchCategory
  })
})

const loadFavorites = async () => {
  try {
    const response = await service.get('/api/questions/favorites', {
      params: { page: 0, size: 50 }
    })
    const data = (response as any)?.data ?? response
    favoriteItems.value = (data?.records || []).map((item: any) => ({
      id: item.id,
      title: item.title,
      date: (item.favoriteTime || item.createdAt || '').toString().slice(0, 10),
      difficulty: item.difficultyLabel || item.difficulty || '中等',
      roleCode: item.primaryJobRoleCode || 'java_backend',
      roleLabel: item.categoryName || '综合题目',
      roleClass: item.primaryJobRoleCode === 'frontend' ? 'front' : item.primaryJobRoleCode === 'algorithm' ? 'algo' : 'java'
    }))
  } catch (error) {
    console.error('加载收藏题目失败:', error)
    favoriteItems.value = []
  }
}

const openQuestion = (roleCode: string, id: number) => {
  router.push(`/user/practice?role=${roleCode}&question=${id}`)
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorites-page {
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

.favorites-main {
  margin-left: 200px;
  padding: 24px 30px 70px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.page-head h1 {
  color: #242733;
  font-size: 24px;
  font-weight: 900;
}

.page-head p {
  margin-top: 5px;
  color: #99a3b6;
  font-size: 13px;
}

.search-input {
  width: min(260px, 100%);
}

:deep(.search-input .el-input__wrapper) {
  height: 34px;
  border-radius: 8px;
  background: #f9fafc;
  box-shadow: 0 0 0 1px #e7ebf2 inset;
}

.filter-tabs {
  height: 52px;
  margin: 18px -30px 22px;
  padding-left: 30px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-top: 1px solid #edf0f5;
  border-bottom: 1px solid #edf0f5;
  background: #ffffff;
}

.filter-tabs button {
  height: 32px;
  padding: 0 16px;
  border: 1px solid #e5e9f0;
  border-radius: 7px;
  color: #687286;
  background: #ffffff;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.filter-tabs button.active {
  color: #ffffff;
  border-color: #ff5a2a;
  background: #ff5a2a;
}

.favorite-list {
  display: grid;
  gap: 14px;
}

.favorite-card {
  min-height: 88px;
  padding: 16px 20px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px;
  align-items: center;
  gap: 18px;
  border: 1px solid #e8edf5;
  border-radius: 11px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(27, 36, 56, 0.025);
}

.tag-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-row span {
  height: 22px;
  padding: 0 9px;
  display: inline-flex;
  align-items: center;
  border-radius: 5px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
}

.tag-row span.front {
  color: #416ee6;
  background: #eef3ff;
}

.tag-row span.algo {
  color: #8b48e8;
  background: #f3edff;
}

.question-main h2 {
  margin-top: 10px;
  color: #242733;
  font-size: 16px;
  line-height: 1.5;
  font-weight: 900;
}

.question-main p {
  margin-top: 7px;
  color: #9aa3b6;
  font-size: 12px;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  color: #f4b34d;
}

.actions button {
  height: 30px;
  padding: 0 13px;
  border: none;
  border-radius: 6px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.actions button.front {
  color: #416ee6;
  background: #eef3ff;
}

.actions button.algo {
  color: #8b48e8;
  background: #f3edff;
}

.notice {
  height: 36px;
  margin-top: 16px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #ffd0c1;
  border-radius: 8px;
  color: #ff5a2a;
  background: #fff1eb;
  font-size: 13px;
}

@media (max-width: 900px) {
  .app-sidebar {
    position: static;
    width: 100%;
  }

  .favorites-main {
    margin-left: 0;
  }
}

@media (max-width: 680px) {
  .page-head,
  .favorite-card {
    grid-template-columns: 1fr;
    align-items: stretch;
    flex-direction: column;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
