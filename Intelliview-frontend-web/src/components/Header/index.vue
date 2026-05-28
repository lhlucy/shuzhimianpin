<template>
  <header class="header">
    <router-link to="/user" class="brand" aria-label="数智面聘首页">
      <span class="brand-mark">智</span>
      <span class="brand-text">
        <strong>数智面聘</strong>
        <small>AI 模拟面试与岗位刷题</small>
      </span>
    </router-link>

    <nav class="nav" aria-label="主导航">
      <router-link v-for="item in navItems" :key="item.path" :to="item.path">
        {{ item.label }}
      </router-link>
    </nav>

    <div class="actions">
      <button class="theme-button" type="button" @click="toggleTheme">
        <el-icon><MoonNight v-if="themeMode === 'light'" /><Sunny v-else /></el-icon>
        <span>{{ themeMode === 'light' ? '深色' : '浅色' }}</span>
      </button>
      <router-link v-if="isAuthenticated" class="profile-link" to="/user/profile">
        <el-avatar :size="34" :src="authUser?.avatar" class="avatar">{{ avatarInitial }}</el-avatar>
        <span>{{ displayName }}</span>
      </router-link>
      <router-link v-else class="profile-link" to="/login">登录</router-link>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { MoonNight, Sunny } from '@element-plus/icons-vue'
import { authUser, getAvatarInitial, getDisplayName, isAuthenticated, loadCurrentUser, refreshAuthState } from '@/utils/auth'

const themeMode = ref<'light' | 'dark'>('light')
const displayName = computed(() => getDisplayName())
const avatarInitial = computed(() => getAvatarInitial())
const navItems = [
  { label: '首页', path: '/user' },
  { label: '岗位刷题', path: '/user/practice' },
  { label: '模拟面试', path: '/user/interview/ai/create' },
  { label: '历史', path: '/user/history' },
  { label: '收藏', path: '/user/favorites' },
  { label: '成长中心', path: '/user/growth' },
  { label: '个人中心', path: '/user/profile' }
]

const applyTheme = () => {
  document.documentElement.classList.toggle('dark-mode', themeMode.value === 'dark')
  localStorage.setItem('themeMode', themeMode.value)
}

const toggleTheme = () => {
  themeMode.value = themeMode.value === 'light' ? 'dark' : 'light'
  applyTheme()
}

onMounted(() => {
  themeMode.value = localStorage.getItem('themeMode') === 'dark' ? 'dark' : 'light'
  applyTheme()
  refreshAuthState()
  loadCurrentUser().catch(() => refreshAuthState())
})
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 50;
  min-height: 72px;
  padding: 12px clamp(18px, 4vw, 44px);
  display: flex;
  align-items: center;
  gap: 24px;
  border-bottom: 1px solid var(--color-border);
  background: color-mix(in srgb, var(--color-bg-elevated) 92%, transparent);
  backdrop-filter: blur(18px);
}

.brand,
.nav a,
.profile-link {
  text-decoration: none;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 220px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  font-weight: 900;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  box-shadow: var(--shadow-soft);
}

.brand-text {
  display: grid;
  gap: 2px;
}

.brand-text strong {
  color: var(--color-text);
  font-size: 18px;
}

.brand-text small {
  color: var(--color-text-muted);
  font-size: 12px;
}

.nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  overflow-x: auto;
}

.nav a {
  height: 42px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.nav a:hover,
.nav a.router-link-active {
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.theme-button,
.profile-link {
  height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-panel);
  color: var(--color-text);
  padding: 0 12px;
  font-weight: 700;
}

.theme-button {
  cursor: pointer;
}

.avatar {
  color: #fff;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  font-weight: 800;
}

@media (max-width: 920px) {
  .header {
    flex-wrap: wrap;
  }

  .brand {
    flex: 1;
    min-width: 0;
  }

  .nav {
    order: 3;
    width: 100%;
  }
}

@media (max-width: 560px) {
  .profile-link span,
  .theme-button span,
  .brand-text small {
    display: none;
  }
}
</style>
