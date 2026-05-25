<template>
  <div class="profile-container">
    <!-- 头部组件 -->
    <Header />
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="profile-wrapper">
        <h2>个人中心</h2>
        
        <div class="profile-card">
          <div class="profile-header">
            <el-avatar size="80" :icon="'UserFilled'" style="margin-bottom: 16px;" />
            <h3>{{ username }}</h3>
            <p class="role-tag">{{ userRole === 'admin' ? '管理员' : '普通用户' }}</p>
          </div>
          
          <div class="profile-body">
            <el-form label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" disabled />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input v-model="userInfo.phone" disabled />
              </el-form-item>
              <el-form-item label="注册时间">
                <el-input v-model="userInfo.registerTime" disabled />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input 
                  v-model="userInfo.bio" 
                  type="textarea" 
                  rows="4" 
                  disabled 
                />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="profile-footer">
            <el-button type="primary">编辑个人信息</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部组件 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Header from '../../../components/Header/index.vue'
import Footer from '../../../components/Footer/index.vue'

const username = ref(localStorage.getItem('username') || '')
const userRole = ref(localStorage.getItem('userRole') || 'user')

// 用户信息
const userInfo = ref({
  username: username.value,
  email: 'admin@example.com',
  phone: '13800138000',
  registerTime: '2026-01-01 00:00:00',
  bio: '这是管理员用户的个人简介。'
})

</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
}

.profile-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.profile-wrapper h2 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.profile-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.profile-header {
  text-align: center;
  margin-bottom: 30px;
}

.role-tag {
  display: inline-block;
  padding: 4px 12px;
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 12px;
  font-size: 14px;
  margin-top: 8px;
}

.profile-body {
  margin-bottom: 30px;
}

.profile-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
