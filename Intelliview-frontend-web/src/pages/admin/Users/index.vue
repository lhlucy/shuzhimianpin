<template>
  <div class="admin-page">
    <section class="summary-strip">
      <div class="summary-copy">
        <p class="section-kicker">User Management</p>
        <h2>用户列表</h2>
        <span>统一查看账号、联系方式和最近活跃，并直接完成角色调整。</span>
      </div>
      <div class="summary-metrics">
        <article>
          <strong>{{ totalUsers }}</strong>
          <span>账号</span>
        </article>
        <article>
          <strong>{{ adminCount }}</strong>
          <span>管理员</span>
        </article>
        <article>
          <strong>{{ normalUserCount }}</strong>
          <span>普通用户</span>
        </article>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>用户列表</h3>
          <p>按角色拆分查看账号，管理员与普通用户不再混在同一组里。</p>
        </div>
        <div class="toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名或昵称"
            clearable
            @keyup.enter="fetchUsers"
            @clear="fetchUsers"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
        </div>
      </div>

      <div class="role-tabs">
        <button
          v-for="item in roleTabs"
          :key="item.key"
          type="button"
          class="role-tab"
          :class="{ active: activeTab === item.key }"
          @click="activeTab = item.key"
        >
          {{ item.label }}
          <span>{{ item.count }}</span>
        </button>
      </div>

      <div class="table-shell">
      <el-table
        v-loading="loading"
        :data="displayedUsers"
        class="admin-table"
        table-layout="fixed"
        :max-height="tableHeight"
      >
        <el-table-column label="用户" min-width="230">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="30" :src="row.avatar">
                {{ (row.nickname || row.username || '用').trim().slice(0, 1).toUpperCase() }}
              </el-avatar>
              <div class="user-meta">
                <strong>{{ row.nickname || row.username }}</strong>
                <span>@{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系方式" min-width="220">
          <template #default="{ row }">
            <div class="stacked-cell">
              <strong>{{ row.email || '-' }}</strong>
              <span>{{ row.phone || '未绑定手机号' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ROLE_ADMIN' ? 'warning' : 'info'" effect="light">
              {{ row.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃情况" min-width="220">
          <template #default="{ row }">
            <div class="stacked-cell">
              <strong>最近登录：{{ row.lastLoginTime || '-' }}</strong>
              <span>注册时间：{{ row.createTime || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="132" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.role === 'ROLE_ADMIN' ? 'info' : 'warning'"
              :loading="updatingUser === row.username"
              @click="toggleRole(row)"
            >
              {{ row.role === 'ROLE_ADMIN' ? '降为用户' : '设为管理员' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <el-empty v-if="!loading && !displayedUsers.length" description="当前分组暂无账号" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import adminApi, { type AdminUser } from '@/api/admin'

const users = ref<AdminUser[]>([])
const keyword = ref('')
const loading = ref(false)
const updatingUser = ref('')
const activeTab = ref<'all' | 'admin' | 'user'>('user')
const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 900)

const totalUsers = computed(() => users.value.length)
const adminCount = computed(() => users.value.filter((item) => item.role === 'ROLE_ADMIN').length)
const normalUserCount = computed(() => users.value.filter((item) => item.role !== 'ROLE_ADMIN').length)
const displayedUsers = computed(() => {
  if (activeTab.value === 'admin') {
    return users.value.filter((item) => item.role === 'ROLE_ADMIN')
  }

  if (activeTab.value === 'user') {
    return users.value.filter((item) => item.role !== 'ROLE_ADMIN')
  }

  return users.value
})
const roleTabs = computed<Array<{ key: 'user' | 'admin' | 'all'; label: string; count: number }>>(() => ([
  { key: 'user', label: '普通用户', count: normalUserCount.value },
  { key: 'admin', label: '管理员', count: adminCount.value },
  { key: 'all', label: '全部账号', count: totalUsers.value }
]))
const tableHeight = computed(() => Math.max(viewportHeight.value - 360, 320))

const syncViewportHeight = () => {
  viewportHeight.value = window.innerHeight
}

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await adminApi.getUsers(keyword.value.trim() || undefined)
  } catch (error: any) {
    ElMessage.error(error?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const toggleRole = async (user: AdminUser) => {
  const nextRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN'
  updatingUser.value = user.username
  try {
    await adminApi.updateUserRole(user.username, nextRole)
    user.role = nextRole
    ElMessage.success(nextRole === 'ROLE_ADMIN' ? '已设为管理员' : '已降为普通用户')
  } catch (error: any) {
    ElMessage.error(error?.message || '角色更新失败')
  } finally {
    updatingUser.value = ''
  }
}

onMounted(() => {
  window.addEventListener('resize', syncViewportHeight)
  fetchUsers()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewportHeight)
})
</script>

<style scoped>
.admin-page {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.summary-strip,
.panel {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(30, 80, 162, 0.1);
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
}

.summary-strip {
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
}

.section-kicker {
  margin-bottom: 6px;
  color: #1e50a2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.summary-copy h2 {
  color: #173a74;
  font-size: 19px;
}

.summary-copy span {
  display: block;
  margin-top: 6px;
  max-width: 560px;
  color: #69809f;
  line-height: 1.6;
  font-size: 12px;
}

.summary-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  min-width: 300px;
}

.summary-metrics article {
  padding: 10px 12px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f6f9ff 0%, #eef4ff 100%);
  text-align: center;
}

.summary-metrics strong {
  display: block;
  color: #173a74;
  font-size: 20px;
  font-weight: 900;
}

.summary-metrics span {
  margin-top: 4px;
  color: #6d84a5;
  font-size: 11px;
}

.panel {
  padding: 14px 16px;
  min-width: 0;
  overflow: hidden;
}

.panel-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.panel-header h3 {
  color: #173a74;
  font-size: 17px;
}

.panel-header p {
  margin-top: 6px;
  color: #7387a4;
  font-size: 12px;
}

.toolbar {
  display: flex;
  gap: 10px;
}

.toolbar .el-input {
  width: 220px;
}

.role-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.role-tab {
  min-width: 110px;
  height: 36px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  border: 1px solid #e4e9f2;
  border-radius: 999px;
  background: #f7f9fc;
  color: #6d7f9d;
  font-size: 13px;
  cursor: pointer;
}

.role-tab span {
  color: #9aa8be;
  font-size: 12px;
}

.role-tab.active {
  color: #2f6bff;
  background: #edf3ff;
  border-color: #cfe0ff;
}

.role-tab.active span {
  color: #2f6bff;
}

.table-shell {
  max-height: calc(100vh - 340px);
  overflow: auto;
  padding-right: 4px;
}

.admin-table {
  border: 1px solid rgba(30, 80, 162, 0.08);
  border-radius: 14px;
  overflow: hidden;
  width: 100%;
  min-width: 0;
}

.admin-table :deep(.el-table__header-wrapper th) {
  background: #f7faff;
  color: #173a74;
  font-size: 12px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.admin-table :deep(.el-table__row td) {
  font-size: 12px;
  padding-top: 10px;
  padding-bottom: 10px;
 }

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-meta,
.stacked-cell {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.user-meta strong,
.stacked-cell strong {
  color: #173a74;
  font-size: 12px;
  font-weight: 700;
}

.user-meta span,
.stacked-cell span {
  color: #7b8ca8;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 1080px) {
  .summary-strip,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-metrics {
    min-width: 0;
    width: 100%;
  }

  .table-shell {
    max-height: calc(100vh - 390px);
  }
}

@media (max-width: 720px) {
  .summary-strip,
  .panel {
    padding: 16px;
  }

  .summary-metrics {
    grid-template-columns: 1fr;
  }

  .toolbar {
    width: 100%;
    flex-direction: column;
  }

  .toolbar .el-input {
    width: 100%;
  }

  .table-shell {
    max-height: none;
    overflow: visible;
  }
}
</style>
