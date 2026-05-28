<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h2>管理员登录</h2>
        <p>仅管理员账号可进入后台管理系统</p>
      </div>
      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="图形验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              prefix-icon="Picture"
              maxlength="6"
            />
            <button class="captcha-image" type="button" @click="loadCaptcha" :disabled="captchaLoading" title="点击刷新验证码">
              <img v-if="captchaImage" :src="captchaImage" alt="图形验证码" />
              <span v-else>{{ captchaLoading ? '加载中' : '刷新' }}</span>
            </button>
          </div>
        </el-form-item>
        <div class="login-form-actions">
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
          <router-link class="back-link" to="/login">前往用户登录</router-link>
        </div>
        <el-form-item prop="agreement" class="agreement-item">
          <el-checkbox v-model="loginForm.agreement">
            我已阅读并同意 <router-link to="/privacy">隐私政策</router-link> 和 <router-link to="/privacy#terms">用户协议</router-link>
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { clearAuthSession, saveAuthSession } from '@/utils/auth'

const router = useRouter()
const loginFormRef = ref<any>(null)
const loading = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')

// 登录表单数据类型
interface LoginForm {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
  remember: boolean
  agreement: boolean
}

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
  remember: false,
  agreement: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3-20 之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6-30 之间', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' }
  ],
  agreement: [
    {
      validator: (_rule: any, value: boolean, callback: (error?: Error) => void) => {
        value ? callback() : callback(new Error('请先阅读并勾选隐私政策和用户协议'))
      },
      trigger: 'change'
    }
  ]
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  try {
    const response = await axios.get('/api/auth/captcha')
    const data = (response as any)?.data
    captchaImage.value = data?.captchaImage || ''
    loginForm.captchaKey = data?.captchaKey || ''
    loginForm.captchaCode = ''
  } catch (error: any) {
    ElMessage.error(error?.message || '图形验证码加载失败')
  } finally {
    captchaLoading.value = false
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const response = await axios.post('/api/auth/login/password', {
      username: loginForm.username,
      password: loginForm.password,
      captchaKey: loginForm.captchaKey,
      captchaCode: loginForm.captchaCode
    })
    const data = (response as any)?.data ?? response
    const { token, user } = data

    if (user?.role !== 'ROLE_ADMIN') {
      clearAuthSession()
      ElMessage.error('当前账号不是管理员账号')
      return
    }

    saveAuthSession(token, user)

    ElMessage.success('登录成功')
    router.replace('/admin')
  } catch (error: any) {
    console.error('登录表单验证失败:', error)
    ElMessage.error(error?.message || '登录失败，请检查用户名和密码')
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top right, rgba(253, 202, 23, 0.34), transparent 24%),
    linear-gradient(135deg, #1e50a2 0%, #153873 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background-color: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  box-shadow: 0 24px 50px rgba(6, 22, 47, 0.22);
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #173a74;
}

.login-header p {
  margin: 0;
  color: #6f84a7;
  font-size: 14px;
}

.login-form {
  width: 100%;
}

.login-form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-link {
  color: #1e50a2;
  font-weight: 700;
  text-decoration: none;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 800;
  border: none;
  background: linear-gradient(135deg, #1e50a2, #2a67c7);
}

.captcha-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 128px;
  gap: 10px;
}

.captcha-image {
  height: 40px;
  padding: 0;
  display: grid;
  place-items: center;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #606266;
  cursor: pointer;
}

.captcha-image img {
  width: 128px;
  height: 40px;
  display: block;
  object-fit: cover;
}

.agreement-item :deep(.el-checkbox) {
  align-items: flex-start;
  height: auto;
  white-space: normal;
}

.agreement-item :deep(.el-checkbox__label) {
  color: #6f84a7;
  line-height: 1.6;
}
</style>
