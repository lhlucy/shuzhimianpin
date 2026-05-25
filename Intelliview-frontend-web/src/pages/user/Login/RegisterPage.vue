<template>
  <div class="auth-page">
    <section class="brand-side">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>

      <div class="brand-copy">
        <p>New Training Space</p>
        <h1>创建你的专属面试训练账号</h1>
        <span>注册后即可保存岗位刷题进度、模拟面试记录、收藏题目和个人训练数据。</span>
      </div>

      <div class="data-card">
        <div>
          <strong>50,000+</strong>
          <span>练习题目</span>
        </div>
        <div>
          <strong>12,000+</strong>
          <span>模拟训练</span>
        </div>
        <div>
          <strong>98%</strong>
          <span>满意度</span>
        </div>
      </div>
    </section>

    <main class="form-side">
      <section class="auth-card">
        <div class="auth-head">
          <span>免费注册</span>
          <h2>开始备战</h2>
          <p>填写基础信息，进入数智面聘训练空间</p>
        </div>

        <el-form ref="formRef" :model="registerForm" :rules="rules" label-width="0" class="auth-form">
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">
              注册并进入训练
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <p>已有账号？ <router-link to="/login">立即登录</router-link></p>
          <small>注册即表示同意用户协议和 <router-link to="/privacy">隐私政策</router-link></small>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message, User } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref<any>(null)
const loading = ref(false)

interface RegisterForm {
  username: string
  email: string
  password: string
  confirmPassword: string
}

const registerForm = reactive<RegisterForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3-20 个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    if (registerForm.username === 'admin') {
      ElMessage.error('管理员账号无法通过注册页面创建')
      return
    }

    await formRef.value.validate()
    loading.value = true

    setTimeout(() => {
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('username', registerForm.username)
      localStorage.setItem('userRole', 'user')

      ElMessage.success('注册成功，自动登录中...')
      router.push('/user')
      loading.value = false
    }, 1000)
  } catch (_error) {
    ElMessage.error('请检查注册信息后重试')
  }
}
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
.auth-footer a {
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
  margin-bottom: 34px;
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

.data-card {
  max-width: 430px;
  margin-bottom: auto;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  border-radius: 12px;
  background: #ff5a2a;
  color: #ffffff;
}

.data-card div {
  min-width: 0;
  display: grid;
  justify-items: center;
  gap: 5px;
  border-left: 1px solid rgba(255, 255, 255, 0.26);
}

.data-card div:first-child {
  border-left: none;
}

.data-card strong {
  font-size: 20px;
  line-height: 1;
}

.data-card span {
  color: rgba(255, 255, 255, 0.72);
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

  .data-card {
    grid-template-columns: 1fr;
  }

  .data-card div {
    border-left: none;
  }
}
</style>
