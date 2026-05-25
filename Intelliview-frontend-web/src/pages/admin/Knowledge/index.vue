<template>
  <div class="knowledge-page">
    <Header />

    <div class="knowledge-shell">
      <section class="hero-card">
        <div>
          <div class="eyebrow">Cloud Knowledge</div>
          <h2>云知识库配置</h2>
          <p>本地知识库、Embedding 和后台导入链路已下线。当前项目统一接入阿里云百炼知识库与智能体应用。</p>
        </div>
      </section>

      <section class="panel-grid">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-title">
              <span>当前接入方式</span>
              <small>运行时只走云端知识库</small>
            </div>
          </template>

          <ol class="steps">
            <li>题库表上传到百炼 `数据查询` 知识库。</li>
            <li>文档库和优秀回答范例上传到百炼 `文档搜索` 知识库。</li>
            <li>创建单智能体应用并关联两个知识库。</li>
            <li>后端通过 `DASHSCOPE_API_KEY` 和 `DASHSCOPE_APP_ID` 调用百炼应用。</li>
          </ol>
        </el-card>

        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-title">
              <span>上传文件</span>
              <small>直接去百炼控制台上传</small>
            </div>
          </template>

          <ul class="file-list">
            <li>[题库表.xlsx](/d:/Project_new/第十七届服创/lingshu/data/bailian-import/题库表.xlsx)</li>
            <li>[文档库.xlsx](/d:/Project_new/第十七届服创/lingshu/data/bailian-import/文档库.xlsx)</li>
            <li>[优秀回答范例.xlsx](/d:/Project_new/第十七届服创/lingshu/data/bailian-import/优秀回答范例.xlsx)</li>
          </ul>
        </el-card>
      </section>

      <section class="panel-grid">
        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-title">
              <span>推荐参数</span>
              <small>按当前项目场景调优</small>
            </div>
          </template>

          <div class="hint-block">
            <h4>题库表知识库</h4>
            <p>`数据查询`、`text-embedding-v4`、`qwen3-rerank（hybrid）`、阈值 `0.35`、召回 `5`。</p>
          </div>

          <div class="hint-block">
            <h4>文档知识库</h4>
            <p>`文档搜索`、`智能切分`、`text-embedding-v4`、`qwen3-rerank（hybrid）`、阈值 `0.25`、召回 `8`。</p>
          </div>
        </el-card>

        <el-card class="panel-card" shadow="hover">
          <template #header>
            <div class="panel-title">
              <span>项目内测试接口</span>
              <small>用于验证百炼知识库是否工作</small>
            </div>
          </template>

          <pre class="code-block">POST /api/cloud-knowledge/chat
{
  "prompt": "JVM 和 JMM 有什么区别？",
  "sessionId": ""
}</pre>

          <p class="tip">如果返回 `text`、`sessionId`、`requestId`、`appId`，说明云端知识库链路已经打通。</p>
        </el-card>
      </section>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import Header from '@/components/Header/index.vue'
import Footer from '@/components/Footer/index.vue'
</script>

<style scoped>
.knowledge-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top right, rgba(54, 207, 201, 0.12), transparent 24%),
    linear-gradient(180deg, #f7fbff 0%, #f2f6fb 100%);
}

.knowledge-shell {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 56px;
}

.hero-card {
  padding: 28px 32px;
  border-radius: 24px;
  background: linear-gradient(135deg, #165dff 0%, #36cfc9 100%);
  color: #fff;
  box-shadow: 0 20px 50px rgba(22, 93, 255, 0.2);
}

.eyebrow {
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  opacity: 0.85;
}

.hero-card h2 {
  margin: 0 0 12px;
  font-size: 32px;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  margin-top: 24px;
}

.panel-card {
  border-radius: 20px;
}

.panel-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.panel-title small {
  color: #86909c;
}

.steps,
.file-list {
  margin: 0;
  padding-left: 18px;
  color: #1d2129;
  line-height: 1.8;
}

.hint-block + .hint-block {
  margin-top: 18px;
}

.hint-block h4 {
  margin: 0 0 8px;
}

.code-block {
  padding: 16px;
  border-radius: 14px;
  background: #0f172a;
  color: #e2e8f0;
  overflow: auto;
  white-space: pre-wrap;
}

.tip {
  margin-top: 12px;
  color: #4e5969;
}

@media (max-width: 900px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
