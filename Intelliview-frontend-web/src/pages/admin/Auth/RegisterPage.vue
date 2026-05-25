<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h2>注册</h2>
        <p>创建新账号</p>
      </div>
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        label-position="top"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Check"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="register-form-footer">
          <span>已有账号？</span>
          <el-link type="primary" href="#/admin/login">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const registerFormRef = ref<any>(null)
const loading = ref(false)

// 注册表单数据类型
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

const registerRules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    },
    {
      min: 3,
      max: 20,
      message: '用户名长度在 3-20 之间',
      trigger: 'blur'
    }
  ],
  email: [
    {
      required: true,
      message: '请输入邮箱',
      trigger: 'blur'
    },
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      min: 6,
      max: 30,
      message: '密码长度在 6-30 之间',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur'
    },
    {
      validator: validatePassword,
      message: '两次输入密码不一致',
      trigger: 'blur'
    }
  ]
}

function validatePassword(_rule: any, value: string, callback: (error?: Error) => void) {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

function handleRegister() {
  if (!registerFormRef.value) return
  
  registerFormRef.value.validate((valid: boolean) => {
    if (valid) {
      loading.value = true
      
      // 模拟注册请求
      setTimeout(() => {
        console.log('注册请求:', registerForm)
        loading.value = false
        
        // 注册成功
        ElMessage.success('注册成功')
        setTimeout(() => {
          window.location.href = '#/admin/login'
        }, 1500)
      }, 1000)
    } else {
      console.log('表单验证失败')
    }
  })
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 30px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.register-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-form {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.register-form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #909399;
}

.register-form-footer a {
  margin-left: 5px;
}
</style>