<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-brand">
        <div class="footer-logo">
          <div class="logo-mark">数</div>
          <div>
            <h3>数智面聘</h3>
            <p>围绕目标岗位，把选岗、刷题和复盘串成一条清晰的准备路径。</p>
          </div>
        </div>
        <div class="footer-tags">
          <span>模拟面试</span>
          <span>真实题库</span>
          <span>岗位专项</span>
          <span>练习复盘</span>
        </div>
      </div>

      <div class="footer-grid">
        <section>
          <h4>常用入口</h4>
          <router-link :to="userRole === 'admin' ? '/admin' : '/user'">首页</router-link>
          <router-link v-if="userRole !== 'admin'" to="/user/interview/ai/create">模拟面试</router-link>
          <router-link v-if="userRole !== 'admin'" to="/user/practice">岗位刷题</router-link>
          <router-link v-if="userRole !== 'admin'" to="/user/history">练习历史</router-link>
          <router-link v-if="userRole !== 'admin'" to="/user/favorites">我的收藏</router-link>
        </section>

        <section>
          <h4>平台支持</h4>
          <a href="mailto:support@intelliview.ai">题目反馈</a>
          <a href="mailto:contact@intelliview.ai">联系我们</a>
          <router-link to="/privacy#terms">用户协议</router-link>
          <router-link to="/privacy">隐私说明</router-link>
        </section>

        <section>
          <h4>训练节奏</h4>
          <p>从选定岗位开始，先刷高频题，再回到历史记录查漏补缺。</p>
          <div class="footer-metrics">
            <div>
              <strong>4</strong>
              <span>核心岗位</span>
            </div>
            <div>
              <strong>24h</strong>
              <span>持续训练</span>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div class="footer-bottom">
      <span>© 2026 数智面聘. 保留所有权利。</span>
      <span>浅色 / 深色双主题，面向真实岗位准备场景</span>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const userRole = computed(() => localStorage.getItem('userRole') || 'user')
</script>

<style scoped>
.footer {
  margin-top: 72px;
  background:
    radial-gradient(circle at top left, var(--color-primary-soft), transparent 34%),
    linear-gradient(180deg, color-mix(in srgb, var(--color-bg-panel) 92%, transparent), var(--color-bg-panel));
  border-top: 1px solid var(--color-border);
}

.footer-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 46px 24px 30px;
  display: grid;
  grid-template-columns: 1.2fr 1.8fr;
  gap: 36px;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.footer-logo {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.logo-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  box-shadow: var(--shadow-soft);
}

.footer-logo h3 {
  font-size: 22px;
  color: var(--color-text);
}

.footer-logo p {
  margin-top: 8px;
  max-width: 360px;
  line-height: 1.7;
  color: var(--color-text-secondary);
}

.footer-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.footer-tags span {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: var(--color-bg-muted);
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 700;
}

.footer-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.footer-grid section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-grid h4 {
  margin-bottom: 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}

.footer-grid a,
.footer-grid span,
.footer-grid p {
  color: var(--color-text-secondary);
  text-decoration: none;
  line-height: 1.7;
}

.footer-grid a:hover {
  color: var(--color-primary);
}

.footer-metrics {
  display: flex;
  gap: 14px;
  margin-top: 8px;
}

.footer-metrics div {
  min-width: 96px;
  padding: 14px;
  border-radius: 16px;
  background: var(--color-bg-panel);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.footer-metrics strong {
  display: block;
  font-size: 20px;
  color: var(--color-primary);
}

.footer-metrics span {
  font-size: 12px;
  color: var(--color-text-muted);
}

.footer-bottom {
  max-width: 1280px;
  margin: 0 auto;
  padding: 18px 24px 28px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: var(--color-text-muted);
  font-size: 13px;
}

@media (max-width: 960px) {
  .footer-container {
    grid-template-columns: 1fr;
  }

  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }

  .footer-bottom {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .footer-container {
    padding: 38px 16px 24px;
  }

  .footer-grid {
    grid-template-columns: 1fr;
  }

  .footer-bottom {
    padding: 18px 16px 24px;
  }
}
</style>
