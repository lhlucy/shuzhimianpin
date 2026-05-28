<template>
  <div class="auth-page">
    <section class="brand-side">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>

      <div class="brand-copy">
        <p>AI Interview Coach</p>
        <h1>回到你的面试训练空间</h1>
        <span>继续岗位刷题、模拟面试和个人成长复盘，让每一次练习都更接近真实面试。</span>
      </div>

      <div class="feature-list">
        <article>
          <el-icon><Collection /></el-icon>
          <div><strong>岗位化题库</strong><span>四大岗位专项刷题</span></div>
        </article>
        <article>
          <el-icon><Monitor /></el-icon>
          <div><strong>模拟面试</strong><span>技术栈定制追问</span></div>
        </article>
        <article>
          <el-icon><DataAnalysis /></el-icon>
          <div><strong>训练数据</strong><span>能力趋势持续追踪</span></div>
        </article>
      </div>
    </section>

    <main class="form-side">
      <section class="auth-card">
        <div class="auth-head">
          <span>欢迎回来</span>
          <h2>登录账号</h2>
          <p>输入账号密码后继续你的求职准备进度</p>
        </div>

        <div class="login-switch">
          <span>需要进入后台管理？</span>
          <router-link to="/admin/login">管理员登录</router-link>
        </div>

        <el-form ref="formRef" :model="loginForm" :rules="rules" label-width="0" class="auth-form">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input v-model="loginForm.captchaCode" placeholder="请输入图形验证码" :prefix-icon="Picture" maxlength="6" />
              <button class="captcha-image" type="button" @click="loadCaptcha" :disabled="captchaLoading" title="点击刷新验证码">
                <img v-if="captchaImage" :src="captchaImage" alt="图形验证码" />
                <span v-else>{{ captchaLoading ? '加载中' : '刷新' }}</span>
              </button>
            </div>
          </el-form-item>
          <div class="form-row">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#">忘记密码？</a>
          </div>
          <el-form-item prop="agreement" class="agreement-item">
            <el-checkbox v-model="loginForm.agreement">
              我已阅读并同意 <router-link to="/privacy">隐私政策</router-link> 和 <router-link to="/privacy#terms">用户协议</router-link>
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">
              登录并开始备战
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <p>还没有账号？ <router-link to="/register">免费注册</router-link></p>
          <small>登录即表示同意 <router-link to="/privacy#terms">用户协议</router-link> 和 <router-link to="/privacy">隐私政策</router-link></small>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Collection, DataAnalysis, Lock, Monitor, Picture, User } from '@element-plus/icons-vue'
import service from '@/utils/axios'
import { saveAuthSession } from '@/utils/auth'

const router = useRouter()
const formRef = ref<any>(null)
const loading = ref(false)
const captchaLoading = ref(false)
const rememberMe = ref(true)
const captchaImage = ref('')

interface LoginForm {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
  agreement: boolean
}

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
  agreement: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3-20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
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
    const response: any = await service.get('/api/auth/captcha')
    const data = response?.data
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
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const response: any = await service.post('/api/auth/login/password', {
      username: loginForm.username,
      password: loginForm.password,
      captchaKey: loginForm.captchaKey,
      captchaCode: loginForm.captchaCode
    })

    if (response.success) {
      const { token, user } = response.data
      if (user?.role === 'ROLE_ADMIN') {
        ElMessage.error('管理员账号请从管理员登录入口进入')
        return
      }

      saveAuthSession(token, user)

      ElMessage.success(response.message || '登录成功')
      router.push('/user')
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error: any) {
    loadCaptcha()
    ElMessage.error(error?.message || error?.response?.data?.message || '登录失败，请检查用户名、密码或验证码')
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 0.9fr) minmax(420px, 1.1fr);
  background: #f6f7f9;
  color: #242733;
}

.brand-side {
  min-height: 100vh;
  padding: 34px 44px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid #edf0f5;
}

.brand,
.auth-footer a,
.form-row a {
  text-decoration: none;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  width: fit-content;
}

.brand span {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #ffffff;
  background: #ff5a2a;
  font-size: 13px;
  font-weight: 900;
}

.brand strong {
  color: #252936;
  font-size: 17px;
  font-weight: 900;
}

.brand-copy {
  margin-top: auto;
  margin-bottom: 38px;
}

.brand-copy p {
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-copy h1 {
  max-width: 460px;
  margin-top: 14px;
  color: #242733;
  font-size: 42px;
  line-height: 1.2;
  font-weight: 900;
}

.brand-copy span {
  display: block;
  max-width: 430px;
  margin-top: 18px;
  color: #687286;
  line-height: 1.8;
}

.feature-list {
  display: grid;
  gap: 12px;
  margin-bottom: auto;
}

.feature-list article {
  max-width: 430px;
  padding: 16px;
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  border: 1px solid #e8edf5;
  border-radius: 10px;
  background: #fbfcfe;
}

.feature-list .el-icon {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  color: #ff5a2a;
  background: #fff1eb;
}

.feature-list div {
  display: grid;
  gap: 4px;
}

.feature-list strong {
  color: #242733;
  font-size: 14px;
}

.feature-list span {
  color: #8f98aa;
  font-size: 12px;
}

.form-side {
  display: grid;
  place-items: center;
  padding: 34px;
}

.auth-card {
  width: min(430px, 100%);
  padding: 32px;
  border: 1px solid #e8edf5;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 18px 42px rgba(27, 36, 56, 0.06);
}

.auth-head span {
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 900;
}

.auth-head h2 {
  margin-top: 9px;
  color: #242733;
  font-size: 28px;
  font-weight: 900;
}

.auth-head p {
  margin-top: 9px;
  color: #7b8496;
  font-size: 14px;
}

.auth-form {
  margin-top: 28px;
}

.login-switch {
  margin-top: 18px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #ffe0d2;
  border-radius: 10px;
  background: #fff7f3;
}

.login-switch span {
  color: #7b8496;
  font-size: 13px;
}

.login-switch a {
  color: #ff5a2a;
  font-size: 13px;
  font-weight: 900;
  text-decoration: none;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 8px;
  background: #fbfcfe;
  box-shadow: 0 0 0 1px #e5e9f0 inset;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow: 0 0 0 1px #ff5a2a inset, 0 0 0 4px rgba(255, 90, 42, 0.1);
}

.auth-form :deep(.el-input__prefix-inner) {
  color: #9aa3b6;
}

.form-row {
  margin: -2px 0 18px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  font-size: 13px;
}

.form-row a {
  color: #ff5a2a;
  font-weight: 800;
}

.captcha-row {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 128px;
  gap: 10px;
}

.captcha-image {
  height: 44px;
  padding: 0;
  display: grid;
  place-items: center;
  overflow: hidden;
  border: 1px solid #e5e9f0;
  border-radius: 8px;
  background: #fbfcfe;
  color: #687286;
  cursor: pointer;
}

.captcha-image img {
  width: 128px;
  height: 44px;
  display: block;
  object-fit: cover;
}

.agreement-item {
  margin-top: -6px;
}

.agreement-item :deep(.el-checkbox) {
  align-items: flex-start;
  height: auto;
  white-space: normal;
}

.agreement-item :deep(.el-checkbox__label) {
  color: #687286;
  line-height: 1.6;
}

:deep(.submit-btn.el-button--primary) {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: 8px;
  background: #ff5a2a;
  font-weight: 900;
}

.auth-footer {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #edf0f5;
  text-align: center;
}

.auth-footer p {
  color: #687286;
  font-size: 14px;
}

.auth-footer a {
  color: #ff5a2a;
  font-weight: 900;
}

.auth-footer small {
  display: block;
  margin-top: 12px;
  color: #a0aabd;
  line-height: 1.6;
}

@media (max-width: 880px) {
  .auth-page {
    grid-template-columns: 1fr;
  }

  .brand-side {
    min-height: auto;
    padding: 24px;
  }

  .brand-copy {
    margin: 38px 0 22px;
  }

  .brand-copy h1 {
    font-size: 32px;
  }
}

@media (max-width: 520px) {
  .form-side {
    padding: 18px;
  }

  .auth-card {
    padding: 24px 18px;
  }
}
</style>
