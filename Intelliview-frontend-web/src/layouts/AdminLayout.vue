<template>
  <div class="admin-layout">
    <aside class="admin-sidebar" :class="{ collapsed }">
      <router-link to="/admin" class="brand">
        <span class="brand-mark"><img src="/images/shuzhimianpin_logo.png" alt="" /></span>
        <div v-if="!collapsed" class="brand-copy">
          <strong>数智面聘</strong>
          <small>Admin Console</small>
        </div>
      </router-link>

      <nav class="nav-list">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span v-if="!collapsed">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <button class="collapse-btn" type="button" @click="collapsed = !collapsed">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          <span v-if="!collapsed">收起导航</span>
        </button>
      </div>
    </aside>

    <div class="admin-main">
      <header class="admin-topbar">
        <div>
          <p class="page-eyebrow">管理员后台</p>
          <h1>{{ currentTitle }}</h1>
        </div>

        <div class="topbar-actions">
          <div class="admin-user">
            <el-avatar :size="38" :src="authUser?.avatar">{{ avatarInitial }}</el-avatar>
            <div class="admin-user-meta">
              <strong>{{ displayName }}</strong>
              <span>超级管理员</span>
            </div>
          </div>
          <el-button type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatDotRound,
  DataAnalysis,
  Expand,
  Fold,
  HomeFilled,
  Tickets,
  UserFilled
} from '@element-plus/icons-vue'
import { authUser, clearAuthSession, getAvatarInitial, getDisplayName } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const navItems = [
  { label: '总览分析', path: '/admin', icon: HomeFilled },
  { label: '用户管理', path: '/admin/users', icon: UserFilled },
  { label: '题目管理', path: '/admin/questions', icon: Tickets },
  { label: '面试管理', path: '/admin/interviews', icon: ChatDotRound },
  { label: '数据分析', path: '/admin/analytics', icon: DataAnalysis }
]

const pageTitleMap: Record<string, string> = {
  '/admin': '后台总览',
  '/admin/users': '用户管理',
  '/admin/questions': '题目管理',
  '/admin/interviews': '面试管理',
  '/admin/analytics': '数据分析',
  '/admin/bank': '题库详情'
}

const currentTitle = computed(() => {
  const match = Object.keys(pageTitleMap)
    .sort((a, b) => b.length - a.length)
    .find((key) => route.path.startsWith(key))
  return match ? pageTitleMap[match] : '后台管理'
})

const displayName = computed(() => getDisplayName())
const avatarInitial = computed(() => getAvatarInitial())

const handleLogout = () => {
  clearAuthSession()
  router.replace('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 236px minmax(0, 1fr);
  background:
    radial-gradient(circle at top right, rgba(253, 202, 23, 0.18), transparent 26%),
    linear-gradient(180deg, #f4f7fb 0%, #eef3f9 100%);
}

.admin-sidebar {
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, #f7f2e8 0%, #efe6d8 100%);
  color: #173a74;
  box-shadow: 18px 0 36px rgba(63, 76, 95, 0.08);
  border-right: 1px solid rgba(30, 80, 162, 0.08);
}

.admin-sidebar.collapsed {
  padding-inline: 12px;
}

.brand,
.nav-item {
  text-decoration: none;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 9px 10px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  color: #173a74;
  border: 1px solid rgba(30, 80, 162, 0.08);
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  overflow: hidden;
  background: transparent;
  box-shadow: 0 10px 24px rgba(255, 106, 0, 0.18);
}

.brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand-copy {
  display: grid;
  gap: 4px;
}

.brand-copy strong {
  font-size: 16px;
}

.brand-copy small {
  color: #8a846f;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-size: 11px;
}

.nav-list {
  display: grid;
  gap: 8px;
}

.nav-item {
  min-height: 42px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-radius: 12px;
  color: #6a7182;
  font-weight: 700;
  font-size: 13px;
}

.nav-item:hover,
.nav-item.router-link-exact-active {
  color: #173a74;
  background: #fffaf0;
  box-shadow: inset 3px 0 0 #fdca17;
}

.nav-item .el-icon {
  font-size: 16px;
}

.sidebar-footer {
  margin-top: auto;
}

.collapse-btn {
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid rgba(30, 80, 162, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.72);
  color: #173a74;
  cursor: pointer;
  font-size: 13px;
}

.admin-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.admin-topbar {
  padding: 16px 20px 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.page-eyebrow {
  margin-bottom: 8px;
  color: #1e50a2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.admin-topbar h1 {
  font-size: 22px;
  color: #173a74;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user {
  padding: 5px 10px 5px 5px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(30, 80, 162, 0.1);
}

.admin-user-meta {
  display: grid;
}

.admin-user-meta strong {
  color: #173a74;
  font-size: 13px;
}

.admin-user-meta span {
  color: #6680aa;
  font-size: 11px;
}

.admin-content {
  flex: 1;
  padding: 8px 20px 20px;
}

@media (max-width: 1080px) {
  .admin-layout {
    grid-template-columns: 92px minmax(0, 1fr);
  }

  .admin-sidebar {
    padding-inline: 12px;
  }

  .brand-copy,
  .nav-item span,
  .collapse-btn span {
    display: none;
  }

  .brand,
  .nav-item,
  .collapse-btn {
    justify-content: center;
    padding-inline: 0;
  }
}

@media (max-width: 820px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: sticky;
    top: 0;
    z-index: 30;
    flex-direction: row;
    align-items: center;
    overflow-x: auto;
    padding: 14px;
  }

  .nav-list {
    display: flex;
  }

  .sidebar-footer {
    display: none;
  }

  .admin-topbar {
    padding: 20px 18px 12px;
    flex-direction: column;
    align-items: flex-start;
  }

  .admin-content {
    padding: 12px 18px 20px;
  }

  .topbar-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
