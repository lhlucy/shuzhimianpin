<template>
  <main class="privacy-page">
    <section class="privacy-hero">
      <router-link to="/user" class="brand">
        <span>AI</span>
        <strong>数智面聘</strong>
      </router-link>
      <div class="hero-copy">
        <p>Privacy Notice</p>
        <h1>隐私说明</h1>
        <span>我们只在岗位化面试训练、评估报告生成和成长分析所必需的范围内处理数据，并通过登录鉴权、权限隔离和环境变量配置降低泄露风险。</span>
      </div>
    </section>

    <section class="privacy-content">
      <article v-for="section in sections" :key="section.title" class="privacy-section">
        <div class="section-icon">
          <el-icon><component :is="section.icon" /></el-icon>
        </div>
        <div>
          <h2>{{ section.title }}</h2>
          <ul>
            <li v-for="item in section.items" :key="item">{{ item }}</li>
          </ul>
        </div>
      </article>

      <section class="rights-panel">
        <h2>你的数据权利</h2>
        <p>你可以在个人中心删除简历、清理面试记录、退出登录或停用账号。涉及账号删除、数据更正和安全问题时，可联系平台管理员处理。</p>
        <router-link to="/login">返回登录</router-link>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { Connection, Document, Lock, Service, UserFilled } from '@element-plus/icons-vue'

const sections = [
  {
    title: '我们收集的数据',
    icon: UserFilled,
    items: ['账号信息：用户名、邮箱、头像、个人资料', '简历信息：上传文件、解析摘要、意向岗位、城市和薪资', '训练信息：刷题记录、面试回答、语音转写文本和评估报告']
  },
  {
    title: 'AI 如何使用数据',
    icon: Connection,
    items: ['根据岗位、简历和技术栈生成本次模拟面试题', '根据回答内容生成动态追问、评分和改进建议', '汇总历史表现，用于展示个人成长趋势和短板追踪']
  },
  {
    title: '第三方服务',
    icon: Service,
    items: ['阿里云百炼用于大模型生成、回答评估和语音能力', '阿里云语音服务用于语音识别和语音合成', '讯飞数字人用于数字人面试官会话和播报']
  },
  {
    title: '安全措施',
    icon: Lock,
    items: ['登录接口以外的个人数据接口需要身份认证', '管理员接口要求管理员权限', '数据库、邮箱、JWT 和第三方密钥通过环境变量注入']
  },
  {
    title: '文件与简历',
    icon: Document,
    items: ['简历文件仅用于解析和面试训练，不建议上传无关敏感材料', '简历直连访问已收紧，避免未授权公开读取', '删除简历后，前端列表不再展示该文件和解析摘要']
  }
]
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  background: #f6f7f9;
  color: #242733;
}

.privacy-hero {
  min-height: 100vh;
  padding: 34px 38px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #edf0f5;
  background: #ffffff;
}

.brand {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
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

.hero-copy {
  margin-top: auto;
  margin-bottom: auto;
}

.hero-copy p {
  color: #ff5a2a;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin-top: 14px;
  color: #242733;
  font-size: 44px;
  font-weight: 900;
}

.hero-copy span {
  display: block;
  margin-top: 18px;
  color: #687286;
  line-height: 1.8;
}

.privacy-content {
  padding: 42px;
  display: grid;
  gap: 16px;
  align-content: start;
}

.privacy-section {
  padding: 22px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 16px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(27, 36, 56, 0.025);
}

.section-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: #ff5a2a;
  background: #fff1eb;
}

.privacy-section h2,
.rights-panel h2 {
  color: #242733;
  font-size: 18px;
  font-weight: 900;
}

.privacy-section ul {
  margin-top: 12px;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: #687286;
  line-height: 1.7;
}

.rights-panel {
  padding: 24px;
  border-radius: 12px;
  color: #ffffff;
  background: #242733;
}

.rights-panel p {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.76);
  line-height: 1.8;
}

.rights-panel a {
  margin-top: 18px;
  width: fit-content;
  min-height: 36px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  background: #ff5a2a;
  color: #ffffff;
  font-size: 13px;
  font-weight: 900;
  text-decoration: none;
}

@media (max-width: 860px) {
  .privacy-page {
    grid-template-columns: 1fr;
  }

  .privacy-hero {
    min-height: auto;
  }

  .hero-copy {
    margin: 48px 0 10px;
  }

  .privacy-content {
    padding: 22px;
  }
}

@media (max-width: 520px) {
  .privacy-section {
    grid-template-columns: 1fr;
  }
}
</style>
