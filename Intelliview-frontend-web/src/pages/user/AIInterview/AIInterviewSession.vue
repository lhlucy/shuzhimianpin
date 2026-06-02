<template>
  <div class="session-page" :class="themeClass">
    <aside class="interviewer-panel">
      <nav class="left-tabs" aria-label="面试侧栏导航">
        <button type="button" :class="{ active: activeLeftPanel === 'interviewer' }" @click="activeLeftPanel = 'interviewer'">
          <el-icon><Service /></el-icon>
          面试信息
        </button>
        <button type="button" :class="{ active: activeLeftPanel === 'resume' }" @click="activeLeftPanel = 'resume'">
          <el-icon><Document /></el-icon>
          简历展示
        </button>
      </nav>

      <section v-show="activeLeftPanel === 'interviewer'" class="left-panel-section">
        <div class="avatar-stage">
          <AvatarDigitalHumanPlayer
            :session="avatarSession"
            :loading="avatarLoading"
            :error="avatarError"
            @error="handleAvatarPlayerError"
          />
        </div>
        <h1>AI 面试官</h1>
        <p>数智面聘 · {{ currentTechLabel }}</p>
        <span class="avatar-status" :class="{ online: avatarSession?.connected && !avatarError }">
          {{ avatarStatusText }}
        </span>

        <div class="interviewer-action-row">
          <button type="button" class="end-btn" :disabled="ending || !session || finished" @click="confirmEndInterview">
            <el-icon><Close /></el-icon>
            {{ finished ? '面试已结束' : '结束面试' }}
          </button>

          <button type="button" class="back-side-btn" @click="goBack">
            <el-icon><Back /></el-icon>
            {{ reportMode ? '返回历史记录' : '返回上一页' }}
          </button>
        </div>
      </section>

      <section v-show="activeLeftPanel === 'resume'" class="left-panel-section resume-panel">
        <div class="resume-head">
          <span class="resume-icon"><el-icon><Document /></el-icon></span>
          <div>
            <h1>简历展示</h1>
            <p>{{ sessionResume?.originalFileName || session?.resumeFileName || '本场面试未选择简历' }}</p>
          </div>
        </div>

        <article v-if="resumeLoading" class="resume-state">
          <span class="loader"></span>
          <strong>正在载入简历...</strong>
        </article>

        <article v-else-if="sessionResume" class="resume-readonly">
          <div class="resume-meta">
            <span>岗位：{{ sessionResume.intentionJob || '-' }}</span>
            <span>类型：{{ sessionResume.recruitmentType || '-' }}</span>
            <span>城市：{{ sessionResume.intentionCity || '-' }}</span>
            <span>薪资：{{ sessionResume.expectedSalary || '-' }}</span>
          </div>
          <div class="resume-summary">
            <strong>简历摘要</strong>
            <p>{{ sessionResume.summary || '暂无摘要' }}</p>
          </div>
          <div class="resume-section-list">
            <section v-for="section in parsedResumeSections" :key="section.key" class="resume-section-card">
              <strong>{{ section.title }}</strong>
              <div v-if="section.key === 'project' && section.projects?.length" class="resume-project-list">
                <article v-for="project in section.projects" :key="`${project.name}-${project.time}-${project.description}`" class="resume-project-card">
                  <div class="resume-project-head">
                    <h2>{{ project.name || '未命名项目' }}</h2>
                    <span v-if="project.time">{{ project.time }}</span>
                  </div>
                  <div class="resume-project-meta">
                    <span v-if="project.role">角色：{{ project.role }}</span>
                    <span v-if="project.techStack.length">技术栈：{{ project.techStack.join(' / ') }}</span>
                  </div>
                  <p v-if="project.description" class="resume-project-desc">{{ project.description }}</p>
                  <div v-if="project.responsibilities.length" class="resume-project-block">
                    <b>负责内容</b>
                    <ul>
                      <li v-for="item in project.responsibilities" :key="`resp-${item}`">{{ item }}</li>
                    </ul>
                  </div>
                  <div v-if="project.achievements.length" class="resume-project-block">
                    <b>成果亮点</b>
                    <ul>
                      <li v-for="item in project.achievements" :key="`ach-${item}`">{{ item }}</li>
                    </ul>
                  </div>
                  <div v-if="project.items.length" class="resume-project-block">
                    <b>补充信息</b>
                    <ul>
                      <li v-for="item in project.items" :key="`extra-${item}`">{{ item }}</li>
                    </ul>
                  </div>
                </article>
              </div>
              <template v-else>
                <ul v-if="section.items.length > 1">
                  <li v-for="item in section.items" :key="item">{{ item }}</li>
                </ul>
                <p v-else>{{ section.items[0] || '暂无' }}</p>
              </template>
            </section>
          </div>
        </article>

        <article v-else class="resume-state">
          <el-icon><Document /></el-icon>
          <strong>{{ session?.resumeFileName ? '未找到对应简历' : '暂无简历' }}</strong>
          <p>{{ session?.resumeFileName ? '该简历可能已被删除，当前仅保留文件名。' : '创建面试时未绑定简历。' }}</p>
        </article>

        <button type="button" class="back-side-btn" @click="goBack">
          <el-icon><Back /></el-icon>
          {{ reportMode ? '返回历史记录' : '返回上一页' }}
        </button>
      </section>
    </aside>

    <main class="dialog-panel">
      <header class="dialog-head">
        <div class="head-title">
          <h2>{{ activeMainTab === 'report' ? '面试报告' : '面试对话' }}</h2>
        </div>
        <div class="head-actions">
          <template v-if="activeLeftPanel === 'interviewer'">
            <span class="head-chip">{{ session?.targetPosition || '加载中' }}</span>
            <span class="head-chip">{{ currentTechLabel }}</span>
            <span class="head-chip time">{{ elapsedLabel }}</span>
            <span class="head-chip">第 {{ currentOrder }} / {{ effectiveQuestionCount }} 题</span>
          </template>
          <button type="button" class="theme-toggle" @click="toggleTheme">
            <el-icon><component :is="isDark ? Sunny : Moon" /></el-icon>
            {{ isDark ? '浅色' : '深色' }}
          </button>
          <span :class="{ done: finished }">{{ activeMainTab === 'report' ? '报告查看中' : (finished ? '已结束' : '面试进行中') }}</span>
        </div>
      </header>

      <nav class="content-tabs" aria-label="面试主内容切换">
        <button type="button" :class="{ active: activeMainTab === 'dialogue' }" @click="switchMainTab('dialogue')">对话页面</button>
        <button
          type="button"
          :class="{ active: activeMainTab === 'report' }"
          :disabled="!canViewReport"
          @click="switchMainTab('report')"
        >
          报告页面
        </button>
      </nav>

      <template v-if="activeMainTab === 'dialogue'">
        <section class="messages" ref="messagesRef">
          <article v-if="loadingSession" class="loading-card">
            <span class="loader"></span>
            <strong>正在载入 AI 面试...</strong>
          </article>

          <article v-for="message in messages" :key="message.id" class="message" :class="message.role">
            <span v-if="message.role === 'ai'" class="bot-icon">
              <img src="/images/shuzhimianpin_logo.png" alt="AI 面试官" />
            </span>
            <div :class="message.role === 'ai' ? 'bubble' : 'user-bubble'">
              <strong v-if="message.role === 'ai'">{{ message.title }}</strong>
              <p>{{ message.content }}</p>
              <div v-if="message.role === 'user' && message.expressionAnalysis?.enabled" class="message-expression">
                {{ formatExpressionLine(message.expressionAnalysis) }}
              </div>
            </div>
            <span v-if="message.role === 'user'" class="user-avatar">我</span>
          </article>

          <article v-if="waitingAI" class="message ai">
            <span class="bot-icon">
              <img src="/images/shuzhimianpin_logo.png" alt="AI 面试官" />
            </span>
            <div class="typing"><i></i><i></i><i></i><span>AI 面试官正在判断回答...</span></div>
          </article>

          <article v-if="finished && !waitingAI" class="finish-card">
            <strong>面试已结束</strong>
            <p>报告已经生成，可切换到上方“报告页面”查看完整复盘。</p>
          </article>
        </section>

        <footer class="input-shell">
          <div v-if="recording || transcribing" class="voice-live-card" :class="{ recording, transcribing }">
            <div class="voice-live-head">
              <div class="voice-wave" :class="{ active: recording }">
                <i></i>
                <i></i>
                <i></i>
                <i></i>
              </div>
              <div class="voice-live-copy">
                <strong>{{ recording ? '正在语音输入' : '正在整理语音内容' }}</strong>
                <span>{{ recognitionSupported ? '识别内容会实时显示，可在这里直接编辑' : '当前浏览器不支持实时识别，停止后会自动转写' }}</span>
              </div>
            </div>
            <div class="voice-draft-row">
              <el-input
                v-model="voiceDraft"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 5 }"
                placeholder="语音识别内容会显示在这里，你可以直接编辑..."
                @input="markVoiceDraftEdited"
              />
              <button type="button" class="voice-finish-btn" :disabled="inputDisabled || !voiceDraft.trim()" @click="completeVoiceAnswer">
                完成
              </button>
            </div>
          </div>

          <div class="input-bar">
            <el-input
              v-model="draft"
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              :disabled="inputDisabled"
              placeholder="输入你的回答，或点击麦克风语音输入..."
              @keydown.enter.exact.prevent="submitAnswer"
            />
            <button type="button" class="call-btn" :disabled="loadingSession" @click="openCallView">
              <el-icon><VideoCamera /></el-icon>
            </button>
            <button type="button" class="mic-btn" :class="{ active: recording }" :disabled="inputDisabled || transcribing" @click="toggleRecord">
              <el-icon><Microphone /></el-icon>
            </button>
            <button type="button" class="send-btn" :disabled="inputDisabled || !draft.trim()" @click="submitAnswer()">
              <el-icon><Promotion /></el-icon>
            </button>
          </div>
        </footer>
      </template>

      <section v-else class="report-page">
        <div v-if="summaryLoading || ending" class="report-loading-stage">
          <div class="report-loader-orbit">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <strong>{{ ending ? 'AI 正在生成更完整的面试报告...' : '正在载入面试报告...' }}</strong>
          <p>我们正在整理综合评分、优劣势、关键词覆盖和逐题复盘，请稍候片刻。</p>
        </div>

        <template v-else-if="summary">
          <section class="report-hero">
            <div class="report-score-panel">
              <span>综合评分</span>
              <strong>{{ Math.round(summary.overallScore || 0) }}</strong>
              <p>{{ summary.summary }}</p>
            </div>
            <div class="report-overview-grid">
              <article>
                <span>答题完成度</span>
                <strong>{{ Math.round(summary.completionRate || 0) }}%</strong>
              </article>
              <article>
                <span>回答平均分</span>
                <strong>{{ Math.round(summary.averageAnswerScore || 0) }}</strong>
              </article>
              <article>
                <span>表达稳定度</span>
                <strong>{{ Math.round(summary.expressionScore || 0) }}</strong>
              </article>
              <article>
                <span>关键词覆盖</span>
                <strong>{{ Math.round(summary.keywordCoverageScore || 0) }}%</strong>
              </article>
            </div>
          </section>

          <section class="report-meta-row">
            <div><span>岗位</span><strong>{{ session?.targetPosition || '-' }}</strong></div>
            <div><span>技术栈</span><strong>{{ (summary.techStacks || session?.techStacks || []).join(' / ') || '-' }}</strong></div>
            <div><span>答题数</span><strong>{{ summary.answeredQuestions }}/{{ summary.totalQuestions }}</strong></div>
            <div><span>用时</span><strong>{{ formatDuration(summary.durationSeconds || 0) }}</strong></div>
          </section>

          <section class="growth-join-card">
            <div>
              <span>成长曲线</span>
              <h3>{{ growthCurveJoined ? '本场面试已加入成长曲线' : '把本场面试纳入长期成长分析' }}</h3>
              <p>加入后会与其他面试报告一起生成五维趋势线，并在成长中心沉淀可追溯证据的短板建议。</p>
            </div>
            <button type="button" :disabled="growthCurveJoining || growthCurveJoined" @click="openGrowthJoinDialog">
              {{ growthCurveJoining ? '分析中...' : (growthCurveJoined ? '已加入' : '加入成长曲线') }}
            </button>
          </section>

          <section class="dimension-panel">
            <article class="radar-card">
              <div class="section-head">
                <h3>能力雷达</h3>
                <p>综合能力表现用于辅助解释本次面试评分。</p>
              </div>
              <div ref="radarChartRef" class="radar-chart" aria-label="能力维度雷达图"></div>
            </article>
            <article class="scoring-model-card">
              <div class="section-head">
                <h3>岗位评分体系</h3>
                <p>{{ session?.targetPosition || '当前岗位' }}专属能力维度与权重，权重越高代表面试更关注该能力。</p>
              </div>
              <div ref="scoringModelChartRef" class="radar-chart scoring-model-chart" aria-label="岗位评分体系权重雷达图"></div>
              <div class="scoring-model-legend">
                <span v-for="item in competencyItems" :key="item.code">{{ item.name }} {{ Math.round(item.weight) }}%</span>
              </div>
            </article>
          </section>

          <section class="report-section-grid">
            <article class="report-section-card">
              <h3>优势亮点</h3>
              <ul><li v-for="item in summary.strengths" :key="item">{{ item }}</li></ul>
            </article>
            <article class="report-section-card">
              <h3>当前短板</h3>
              <ul><li v-for="item in summary.weaknesses" :key="item">{{ item }}</li></ul>
            </article>
            <article class="report-section-card">
              <h3>改进建议</h3>
              <ul><li v-for="item in summary.suggestions" :key="item">{{ item }}</li></ul>
            </article>
          </section>

          <section class="expression-report-section">
            <div class="section-head">
              <h3>表达分析</h3>
              <p>基于语音作答的转写文本、语速、情绪标签和表达稳定性生成。</p>
            </div>
            <div v-if="expressionReport.enabled" class="expression-report-grid">
              <article>
                <span>平均语速</span>
                <strong>{{ expressionReport.averageSpeechRate }} 字/分钟</strong>
                <p>{{ expressionReport.speechRateLevel }}</p>
              </article>
              <article>
                <span>主要情绪</span>
                <strong>{{ expressionReport.emotionLabel }}</strong>
                <p>{{ expressionReport.voiceCount }} 道语音题</p>
              </article>
              <article>
                <span>平均清晰度</span>
                <strong>{{ expressionReport.averageClarity }}</strong>
                <p>满分 100</p>
              </article>
              <article>
                <span>平均自信度</span>
                <strong>{{ expressionReport.averageConfidence }}</strong>
                <p>满分 100</p>
              </article>
            </div>
            <div v-else class="expression-empty">
              <strong>本场主要采用文字作答</strong>
              <p>未采集语音表达数据，暂无语速、语气情绪和语音自信度分析。</p>
            </div>
          </section>

          <section class="question-review-section">
            <div class="section-head">
              <h3>逐题复盘</h3>
              <p>查看每道题的回答得分、简评、关键词覆盖和补强建议。</p>
            </div>

            <article v-for="review in summary.questionReviews || []" :key="`${review.questionId}-${review.questionOrder}`" class="review-card">
              <div class="review-top">
                <div>
                  <span class="review-order">第 {{ review.questionOrder || '-' }} 题 · {{ review.questionType || 'QUESTION' }}</span>
                  <h4>{{ review.questionContent || '题目内容暂无记录' }}</h4>
                </div>
                <div class="review-score">
                  <strong>{{ Math.round(review.score || 0) }}</strong>
                  <span>本题得分</span>
                </div>
              </div>

              <div class="review-meta">
                <span>作答时长：{{ formatDuration(review.duration || 0) }}</span>
                <span>表达信心：{{ review.confidenceLevel || 0 }}/10</span>
              </div>

              <div v-if="review.expressionAnalysis" class="review-expression" :class="{ disabled: !review.expressionAnalysis.enabled }">
                {{ formatExpressionLine(review.expressionAnalysis) }}
              </div>

              <div class="review-answer">
                <span>回答摘要</span>
                <p>{{ review.answerContent || '未作答' }}</p>
              </div>

              <p v-if="review.feedbackSummary" class="review-summary">{{ review.feedbackSummary }}</p>

              <div v-if="review.dimensionScores" class="review-dimension-row">
                <span
                  v-for="item in getReviewDimensionItems(review.dimensionScores)"
                  :key="`${review.questionId}-${item.key}`"
                  :class="{ weak: item.score < 65, strong: item.score >= 80 }"
                >
                  {{ item.label }} {{ Math.round(item.score) }}
                </span>
              </div>

              <div v-if="review.dimensionScores" class="review-job-dimension-row">
                <strong>岗位维度评分</strong>
                <span
                  v-for="item in getReviewCompetencyItems(review.dimensionScores)"
                  :key="`${review.questionId}-${item.code}`"
                  :class="{ weak: item.score < 65, strong: item.score >= 80 }"
                >
                  {{ item.name }} {{ Math.round(item.score) }} · 权重 {{ Math.round(item.weight) }}%
                </span>
              </div>

              <div class="review-columns">
                <section>
                  <h5>回答做得好</h5>
                  <ul><li v-for="item in review.strengths || []" :key="item">{{ item }}</li></ul>
                </section>
                <section>
                  <h5>还需要补充</h5>
                  <ul><li v-for="item in review.weaknesses || []" :key="item">{{ item }}</li></ul>
                </section>
                <section>
                  <h5>下一步建议</h5>
                  <ul><li v-for="item in review.suggestions || []" :key="item">{{ item }}</li></ul>
                </section>
              </div>

              <div class="keyword-row">
                <div>
                  <span>命中关键词</span>
                  <p>{{ (review.hitKeywords || []).join('、') || '暂无' }}</p>
                </div>
                <div>
                  <span>缺失关键词</span>
                  <p>{{ (review.missingKeywords || []).join('、') || '暂无' }}</p>
                </div>
              </div>
            </article>
          </section>
        </template>

        <article v-else class="report-empty">
          <strong>当前还没有可展示的报告</strong>
          <p>完成面试后，这里会显示完整的评估结果与逐题复盘。</p>
        </article>
      </section>

      <transition name="call-view">
        <section v-if="callMode" class="call-view">
          <header class="call-view-head">
            <div>
              <span>AI 通话面试</span>
              <h3>{{ session?.targetPosition || '模拟面试通话中' }}</h3>
            </div>
            <div class="call-view-actions">
              <button type="button" class="call-back-btn" @click="closeCallView">返回对话</button>
              <button type="button" class="call-end-btn" :disabled="ending || !session || finished" @click="confirmEndInterview">
                {{ finished ? '面试已结束' : '结束面试' }}
              </button>
            </div>
          </header>

          <section class="call-view-stage">
            <article class="call-card chat">
              <div class="call-card-head">
                <strong>对话记录</strong>
                <span>{{ voiceStatusText }}</span>
              </div>
              <div class="call-chat-panel" ref="callMessagesRef">
                <article v-for="message in messages" :key="`call-${message.id}`" class="call-chat-item" :class="message.role">
                  <span class="call-chat-role" :class="{ ai: message.role === 'ai' }">
                    <img v-if="message.role === 'ai'" src="/images/shuzhimianpin_logo.png" alt="AI 面试官" />
                    <template v-else>我</template>
                  </span>
                  <div class="call-chat-bubble">
                    <strong v-if="message.role === 'ai'">{{ message.title }}</strong>
                    <p>{{ message.content }}</p>
                    <div v-if="message.role === 'user' && message.expressionAnalysis?.enabled" class="message-expression">
                      {{ formatExpressionLine(message.expressionAnalysis) }}
                    </div>
                  </div>
                </article>

                <article v-if="waitingAI" class="call-chat-item ai waiting">
                  <span class="call-chat-role ai">
                    <img src="/images/shuzhimianpin_logo.png" alt="AI 面试官" />
                  </span>
                  <div class="typing"><i></i><i></i><i></i><span>AI 面试官正在判断回答...</span></div>
                </article>

                <div v-if="!messages.length && !loadingSession" class="call-chat-empty">
                  <strong>对话内容准备中</strong>
                  <p>进入面试后，这里会持续展示你和 AI 面试官的实时聊天记录。</p>
                </div>
              </div>
            </article>

            <article class="call-card me">
              <div class="call-card-head">
                <strong>我的画面</strong>
                <span>{{ cameraStatusText }}</span>
              </div>
              <div class="camera-panel" :class="{ active: cameraActive }">
                <video v-show="cameraActive" ref="cameraVideoRef" autoplay muted playsinline></video>
                <div v-if="!cameraActive" class="camera-fallback">
                  <el-icon><VideoCamera /></el-icon>
                  <strong>{{ cameraError || '正在准备摄像头画面' }}</strong>
                  <p>允许浏览器访问摄像头后，这里会显示你的实时人像。</p>
                </div>
              </div>
            </article>
          </section>

          <section class="call-question-card">
            <span>当前题目</span>
            <strong>{{ currentQuestion?.content || '题目同步中，请稍候...' }}</strong>
          </section>

          <footer class="call-footer">
            <div v-if="recording || transcribing" class="voice-live-card call-live-card" :class="{ recording, transcribing }">
              <div class="voice-live-head">
                <div class="voice-wave" :class="{ active: recording }">
                  <i></i>
                  <i></i>
                  <i></i>
                  <i></i>
                </div>
                <div class="voice-live-copy">
                  <strong>{{ recording ? '正在语音输入' : '正在整理语音内容' }}</strong>
                  <span>{{ recognitionSupported ? '识别内容会实时显示，可在这里直接编辑' : '当前浏览器不支持实时识别，停止后会自动转写' }}</span>
                </div>
              </div>
              <div class="voice-draft-row">
                <el-input
                  v-model="voiceDraft"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 5 }"
                  placeholder="语音识别内容会显示在这里，你可以直接编辑..."
                  @input="markVoiceDraftEdited"
                />
                <button type="button" class="voice-finish-btn" :disabled="inputDisabled || !voiceDraft.trim()" @click="completeVoiceAnswer">
                  完成
                </button>
              </div>
            </div>

            <div class="input-bar call-input-bar">
              <el-input
                v-model="draft"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 4 }"
                :disabled="inputDisabled"
                placeholder="输入你的回答，或点击麦克风语音输入..."
                @keydown.enter.exact.prevent="submitAnswer"
              />
              <button type="button" class="mic-btn" :class="{ active: recording }" :disabled="inputDisabled || transcribing" @click="toggleRecord">
                <el-icon><Microphone /></el-icon>
              </button>
              <button type="button" class="send-btn" :disabled="inputDisabled || !draft.trim()" @click="submitAnswer()">
                <el-icon><Promotion /></el-icon>
              </button>
            </div>
          </footer>
        </section>
      </transition>

      <el-dialog v-model="growthJoinVisible" title="加入成长曲线" width="520px" class="growth-dialog">
        <div class="growth-dialog-body">
          <strong>这会把本场面试作为一个成长样本</strong>
          <p>系统会异步读取本场报告的五个能力维度、逐题复盘和关键词缺失情况，用于生成成长中心的趋势线和证据型建议。你可以随时在成长中心取消勾选。</p>
          <label>
            <input v-model="growthDontRemind" type="checkbox" />
            不再提醒
          </label>
        </div>
        <template #footer>
          <el-button @click="growthJoinVisible = false">暂不加入</el-button>
          <el-button type="primary" :loading="growthCurveJoining" @click="confirmJoinGrowthCurve">加入并开始分析</el-button>
        </template>
      </el-dialog>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Back, Close, Document, Microphone, Moon, Promotion, Service, Sunny, VideoCamera } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { RadarChart } from 'echarts/charts'
import { GridComponent, RadarComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import aiInterviewApi, {
  type AIInterviewAvatarSession,
  type AIInterviewExpressionAnalysis,
  type AIInterviewQuestion,
  type AIInterviewSession,
  type AIInterviewSummary
} from '@/api/aiInterview'
import { apiBaseUrl } from '@/utils/axios'
import resumeApi, { type UserResume } from '@/api/resumes'
import AvatarDigitalHumanPlayer from '@/components/AIInterview/AvatarDigitalHumanPlayer.vue'

echarts.use([RadarChart, GridComponent, RadarComponent, TooltipComponent, CanvasRenderer])

interface ChatMessage {
  id: string
  role: 'ai' | 'user'
  title?: string
  content: string
  expressionAnalysis?: AIInterviewExpressionAnalysis
}

interface DimensionItem {
  key: string
  label: string
  score: number
  description: string
}

interface CompetencyItem {
  code: string
  name: string
  weight: number
  description?: string
  score?: number
}

interface ResumeProject {
  name: string
  time: string
  role: string
  techStack: string[]
  description: string
  responsibilities: string[]
  achievements: string[]
  items: string[]
}

interface ResumeSection {
  key: string
  title: string
  patterns: string[]
  items: string[]
  projects?: ResumeProject[]
}

const DIMENSION_DEFINITIONS: Array<Omit<DimensionItem, 'score'>> = [
  { key: 'technicalDepth', label: '技术深度', description: '核心原理、关键机制、边界条件' },
  { key: 'projectRelevance', label: '项目匹配', description: '项目经历、技术栈和岗位场景结合度' },
  { key: 'problemSolving', label: '问题分析', description: '拆解问题、说明思路、对比方案' },
  { key: 'communicationClarity', label: '表达清晰', description: '结构化表达、逻辑连贯、重点明确' },
  { key: 'jobMatch', label: '岗位匹配', description: '回答贴合目标岗位要求的程度' }
]

const route = useRoute()
const router = useRouter()
const session = ref<AIInterviewSession | null>(null)
const currentQuestion = ref<AIInterviewQuestion | null>(null)
const summary = ref<AIInterviewSummary | null>(null)
const resumes = ref<UserResume[]>([])
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const loadingSession = ref(true)
const resumeLoading = ref(false)
const waitingAI = ref(false)
const ending = ref(false)
const summaryLoading = ref(false)
const growthJoinVisible = ref(false)
const growthCurveJoining = ref(false)
const growthCurveJoined = ref(false)
const growthDontRemind = ref(localStorage.getItem('growthCurveJoinNoRemind') === 'true')
const recording = ref(false)
const transcribing = ref(false)
const liveTranscript = ref('')
const interimTranscript = ref('')
const voiceDraft = ref('')
const voiceFinalTranscript = ref('')
const voicePartialTranscript = ref('')
const voiceDraftEdited = ref(false)
const avatarLoading = ref(false)
const avatarSpeaking = ref(false)
const avatarError = ref('')
const avatarSession = ref<AIInterviewAvatarSession | null>(null)
const isDark = ref(false)
const activeLeftPanel = ref<'interviewer' | 'resume'>('interviewer')
const activeMainTab = ref<'dialogue' | 'report'>('dialogue')
const cameraVideoRef = ref<HTMLVideoElement | null>(null)
const messagesRef = ref<HTMLElement | null>(null)
const callMessagesRef = ref<HTMLElement | null>(null)
const radarChartRef = ref<HTMLElement | null>(null)
const scoringModelChartRef = ref<HTMLElement | null>(null)
const pendingAvatarTexts = ref<string[]>([])
let startedAt = Date.now()
let elapsedTimer: number | undefined
const elapsedSeconds = ref(0)
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
let voiceSocket: WebSocket | null = null
let voiceStream: MediaStream | null = null
let audioContext: AudioContext | null = null
let audioSource: MediaStreamAudioSourceNode | null = null
let audioProcessor: ScriptProcessorNode | null = null
const cameraStream = ref<MediaStream | null>(null)
let voiceAnswerStartedAt = 0
let voiceTranscriptBase = ''
let voiceCompleting = false
let avatarClosingPromise: Promise<void> | null = null
let radarChart: echarts.ECharts | null = null
let scoringModelChart: echarts.ECharts | null = null
let pageExitHandled = false
const cameraError = ref('')
const voiceError = ref('')
const realtimeEmotion = ref('')

const REALTIME_ASR_SAMPLE_RATE = 16000

const interviewId = computed(() => Number(route.params.id))
const reportMode = computed(() => route.query.report === '1')
const finished = computed(() => session.value?.status === 'COMPLETED')
const inputDisabled = computed(() => loadingSession.value || waitingAI.value || finished.value || activeMainTab.value === 'report' || !currentQuestion.value)
const canViewReport = computed(() => finished.value)
const themeClass = computed(() => (isDark.value ? 'theme-dark' : 'theme-light'))
const callMode = computed(() => route.query.view === 'call')
const cameraActive = computed(() => Boolean(cameraStream.value))
const cameraStatusText = computed(() => {
  if (cameraActive.value) return '摄像头已接通'
  if (cameraError.value) return cameraError.value
  return '等待授权'
})
const voiceStatusText = computed(() => {
  if (recording.value) return '语音识别进行中'
  if (transcribing.value) return '正在整理语音内容'
  if (voiceError.value) return voiceError.value
  return recognitionSupported.value ? '进入视频后会自动识别语音' : '当前浏览器仅支持录音后转写'
})
const currentTechLabel = computed(() => session.value?.techStacks?.slice(0, 2).join(' / ') || session.value?.targetPosition || 'AI 面试')
const effectiveQuestionCount = computed(() => {
  const total = session.value?.questionCount || 0
  const answered = session.value?.answeredCount || 0
  const currentQuestionOrder = currentQuestion.value?.questionOrder || 0
  return Math.max(total, answered + (currentQuestion.value ? 1 : 0), currentQuestionOrder)
})
const currentOrder = computed(() => {
  if (!session.value) return 0
  if (finished.value) return effectiveQuestionCount.value
  return Math.min((session.value.answeredCount || 0) + (currentQuestion.value ? 1 : 0), effectiveQuestionCount.value)
})
const avatarStatusText = computed(() => {
  if (avatarLoading.value) return '数字人连接中'
  if (avatarError.value) return avatarError.value
  if (avatarSession.value?.connected) return '数字人在线'
  return avatarSession.value?.message || '数字人未启用'
})
const sessionResume = computed(() => {
  const fileName = session.value?.resumeFileName?.trim()
  if (!fileName) return null
  return resumes.value.find((resume) => resume.originalFileName === fileName || resume.fileName === fileName) || null
})
const parsedResumeSections = computed(() => parseResumeSections(sessionResume.value?.content || ''))
const elapsedLabel = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60)
  const seconds = elapsedSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})
const recognitionSupported = computed(() => {
  if (typeof window === 'undefined') return false
  return Boolean(window.WebSocket && typeof navigator.mediaDevices?.getUserMedia === 'function' && ((window as any).AudioContext || (window as any).webkitAudioContext))
})
const GROWTH_CURVE_STORAGE_KEY = 'growth_curve_interview_ids'
const INTERVIEW_TIMER_PREFIX = 'ai_interview_started_at_'
const getTimerStorageKey = () => `${INTERVIEW_TIMER_PREFIX}${interviewId.value}`
const parseTime = (value?: string) => {
  if (!value) return 0
  const time = new Date(value).getTime()
  return Number.isFinite(time) ? time : 0
}
const syncInterviewTimer = (data: AIInterviewSession) => {
  const key = getTimerStorageKey()
  if (data.status === 'COMPLETED') {
    const baseTime = parseTime(data.startedAt || data.createdAt) || Number(localStorage.getItem(key)) || Date.now()
    const endTime = parseTime(data.endedAt) || Date.now()
    startedAt = baseTime
    elapsedSeconds.value = Math.max(0, Math.floor((endTime - baseTime) / 1000))
    localStorage.removeItem(key)
    return
  }

  const storedStart = Number(localStorage.getItem(key))
  const serverStart = parseTime(data.startedAt || data.createdAt)
  startedAt = Number.isFinite(storedStart) && storedStart > 0 ? storedStart : (serverStart || Date.now())
  localStorage.setItem(key, String(startedAt))
  elapsedSeconds.value = Math.max(0, Math.floor((Date.now() - startedAt) / 1000))
}
const dimensionItems = computed(() => getReviewDimensionItems(summary.value?.dimensionScores))
const competencyItems = computed<CompetencyItem[]>(() => {
  const model = summary.value?.competencyModel || []
  if (model.length) {
    return model.map((item) => ({
      code: item.code,
      name: item.name,
      weight: normalizePercent(item.weight),
      description: item.description,
      score: normalizeScore(item.score ?? summary.value?.overallScore ?? 0)
    }))
  }
  return dimensionItems.value.map((item) => ({
    code: item.key,
    name: item.label,
    weight: normalizePercent(AIInterviewDimensionWeights[item.key] ?? 20),
    description: item.description,
    score: item.score
  }))
})

const formatDuration = (seconds?: number) => {
  const totalSeconds = Number(seconds || 0)
  if (totalSeconds <= 0) return '0 秒'
  if (totalSeconds < 60) return `${totalSeconds} 秒`
  const minutes = Math.floor(totalSeconds / 60)
  const remainSeconds = totalSeconds % 60
  return remainSeconds ? `${minutes} 分 ${remainSeconds} 秒` : `${minutes} 分钟`
}

const readGrowthCurveIds = () => {
  try {
    const value = JSON.parse(localStorage.getItem(GROWTH_CURVE_STORAGE_KEY) || '[]')
    return Array.isArray(value) ? value.map(Number).filter(Boolean) : []
  } catch {
    return []
  }
}

const refreshGrowthCurveJoined = () => {
  growthCurveJoined.value = readGrowthCurveIds().includes(interviewId.value)
}

const openGrowthJoinDialog = () => {
  refreshGrowthCurveJoined()
  if (growthCurveJoined.value) return
  if (growthDontRemind.value || localStorage.getItem('growthCurveJoinNoRemind') === 'true') {
    confirmJoinGrowthCurve()
    return
  }
  growthJoinVisible.value = true
}

const confirmJoinGrowthCurve = async () => {
  if (!session.value?.interviewId) return
  growthCurveJoining.value = true
  try {
    const ids = readGrowthCurveIds()
    if (!ids.includes(session.value.interviewId)) {
      ids.unshift(session.value.interviewId)
      localStorage.setItem(GROWTH_CURVE_STORAGE_KEY, JSON.stringify(ids.slice(0, 80)))
    }
    if (growthDontRemind.value) {
      localStorage.setItem('growthCurveJoinNoRemind', 'true')
    }
    growthCurveJoined.value = true
    growthJoinVisible.value = false
    ElMessage.success('已加入成长曲线，系统正在后台分析')
    Promise.allSettled([
      aiInterviewApi.getSummary(session.value.interviewId),
      aiInterviewApi.getGrowthAnalysis()
    ]).catch(() => undefined)
  } finally {
    growthCurveJoining.value = false
  }
}

const normalizeDimensionScore = (scores: Record<string, number> | undefined, key: string) => {
  const fallback = summary.value?.overallScore || 0
  const score = Number(scores?.[key] ?? fallback)
  return normalizeScore(score)
}

const normalizeScore = (score: number) => {
  return Number.isFinite(score) ? Math.max(0, Math.min(100, score)) : 0
}

const normalizePercent = (value: number) => {
  const percent = Number(value)
  return Number.isFinite(percent) ? Math.max(0, Math.min(100, percent)) : 0
}

const AIInterviewDimensionWeights: Record<string, number> = {
  technicalDepth: 30,
  projectRelevance: 25,
  problemSolving: 20,
  communicationClarity: 15,
  jobMatch: 10
}

const getReviewDimensionItems = (scores?: Record<string, number>): DimensionItem[] => {
  return DIMENSION_DEFINITIONS.map((item) => ({
    ...item,
    score: normalizeDimensionScore(scores, item.key)
  }))
}

const COMPETENCY_SOURCE_MAP: Record<string, Partial<Record<string, number>>> = {
  basicKnowledge: { technicalDepth: 0.72, problemSolving: 0.28 },
  projectExperience: { projectRelevance: 0.78, jobMatch: 0.22 },
  systemDesign: { technicalDepth: 0.35, problemSolving: 0.4, jobMatch: 0.25 },
  codingAbility: { problemSolving: 0.55, technicalDepth: 0.3, communicationClarity: 0.15 },
  frontendBasics: { technicalDepth: 0.7, problemSolving: 0.3 },
  frameworkAbility: { technicalDepth: 0.55, projectRelevance: 0.45 },
  engineering: { projectRelevance: 0.55, problemSolving: 0.45 },
  interaction: { communicationClarity: 0.45, projectRelevance: 0.35, jobMatch: 0.2 },
  performance: { technicalDepth: 0.45, problemSolving: 0.55 },
  mathBasics: { technicalDepth: 0.65, problemSolving: 0.35 },
  algorithmCoding: { problemSolving: 0.62, technicalDepth: 0.38 },
  modelUnderstanding: { technicalDepth: 0.58, communicationClarity: 0.22, jobMatch: 0.2 },
  engineeringLanding: { projectRelevance: 0.58, jobMatch: 0.24, problemSolving: 0.18 },
  modelBasics: { technicalDepth: 0.62, communicationClarity: 0.18, problemSolving: 0.2 },
  ragAgent: { technicalDepth: 0.42, problemSolving: 0.4, projectRelevance: 0.18 },
  promptEngineering: { problemSolving: 0.45, communicationClarity: 0.35, jobMatch: 0.2 },
  businessLanding: { projectRelevance: 0.5, jobMatch: 0.35, communicationClarity: 0.15 }
}

const addSourceWeight = (source: Partial<Record<string, number>>, key: string, weight: number) => {
  source[key] = Number(source[key] || 0) + weight
}

const getCompetencySourceMap = (item: CompetencyItem): Partial<Record<string, number>> | undefined => {
  if (COMPETENCY_SOURCE_MAP[item.code]) {
    return COMPETENCY_SOURCE_MAP[item.code]
  }
  if (AIInterviewDimensionWeights[item.code] !== undefined) {
    return { [item.code]: 1 }
  }
  const text = `${item.code} ${item.name} ${item.description || ''}`.toLowerCase()
  const source: Partial<Record<string, number>> = {}
  if (/basic|基础|原理|知识/.test(text)) {
    addSourceWeight(source, 'technicalDepth', 0.7)
    addSourceWeight(source, 'problemSolving', 0.3)
  }
  if (/project|项目|业务|落地|复盘/.test(text)) {
    addSourceWeight(source, 'projectRelevance', 0.6)
    addSourceWeight(source, 'jobMatch', 0.25)
    addSourceWeight(source, 'problemSolving', 0.15)
  }
  if (/design|设计|排查|优化|治理|定位|问题|方案/.test(text)) {
    addSourceWeight(source, 'problemSolving', 0.52)
    addSourceWeight(source, 'technicalDepth', 0.3)
    addSourceWeight(source, 'jobMatch', 0.18)
  }
  if (/表达|沟通|协同|产品/.test(text)) {
    addSourceWeight(source, 'communicationClarity', 0.58)
    addSourceWeight(source, 'projectRelevance', 0.24)
    addSourceWeight(source, 'jobMatch', 0.18)
  }
  if (/岗位|场景|匹配/.test(text)) {
    addSourceWeight(source, 'jobMatch', 0.55)
    addSourceWeight(source, 'projectRelevance', 0.3)
    addSourceWeight(source, 'communicationClarity', 0.15)
  }
  return Object.keys(source).length ? source : undefined
}

const getReviewCompetencyItems = (scores?: Record<string, number>) => {
  return competencyItems.value.map((item) => {
    const sourceMap = getCompetencySourceMap(item)
    const weightedScore = sourceMap
      ? Object.entries(sourceMap).reduce((sum, [key, weight]) => sum + normalizeDimensionScore(scores, key) * Number(weight || 0), 0)
      : Number(item.score || 0)
    return {
      ...item,
      score: normalizeScore(weightedScore || item.score || summary.value?.overallScore || 0)
    }
  })
}

const TIME_RANGE_PATTERN = /((?:19|20)\d{2}(?:[./年-]\s?\d{1,2})?(?:\s*(?:-|至|到|~|—|–)\s*(?:(?:19|20)\d{2}(?:[./年-]\s?\d{1,2})?|至今|现在|present|Present))?)/
const PROJECT_NAME_PATTERN = /^(?:项目(?:名称|名)?|项目\d+|项目[一二三四五六七八九十]+)\s*[:：、-]?\s*/i
const PROJECT_START_PATTERN = /^(?:项目\d+|项目[一二三四五六七八九十]+|项目(?:名称|名)?|Project\s*\d*)\s*[:：、-]?/i
const TECH_KEYWORD_PATTERN = /\b(Vue|React|Angular|Node|Java|Spring|SpringBoot|MyBatis|Python|Django|Flask|FastAPI|Go|Gin|Mysql|MySQL|Redis|MongoDB|Elasticsearch|Docker|Kubernetes|K8s|Linux|Nginx|RabbitMQ|Kafka|TypeScript|JavaScript|HTML|CSS|Uniapp|Uni-app|微信小程序|RAG|LangChain|LLM|Prompt|Cursor|Claude|Codex)\b/i

const splitTechStack = (value: string) => value
  .replace(/[；;，,、|｜]/g, '/')
  .split('/')
  .map((item) => item.trim())
  .filter(Boolean)

const cleanResumeLine = (line: string) => line
  .replace(/^[-*•·●◆■\d.、\s]+/, '')
  .replace(/\s+/g, ' ')
  .trim()

const removeInlineField = (line: string, labels: string[]) => {
  const pattern = labels.join('|')
  return line.replace(new RegExp(`(?:^|[；;|｜\\s])(?:${pattern})\\s*[:：]\\s*`, 'i'), ' ').trim()
}

const extractInlineField = (line: string, labels: string[]) => {
  const labelPattern = labels.join('|')
  const stopLabels = [
    '项目名称', '项目名', '项目时间', '起止时间', '时间', '周期', '角色', '项目角色', '担任角色',
    '职位', '岗位', '技术栈', '技术架构', '开发环境', '使用技术', '相关技术', '项目描述',
    '项目简介', '项目背景', '背景', '负责内容', '项目职责', '职责', '工作内容', '项目成果', '成果', '亮点'
  ].filter((label) => !labels.includes(label)).join('|')
  const match = line.match(new RegExp(`(?:^|[；;|｜\\s])(?:${labelPattern})\\s*[:：]\\s*(.+)$`, 'i'))
  if (!match?.[1]) return ''
  return match[1].replace(new RegExp(`\\s*(?:${stopLabels})\\s*[:：].*$`, 'i'), '').trim()
}

const isProjectHeading = (line: string) => {
  if (PROJECT_START_PATTERN.test(line)) return true
  if (TIME_RANGE_PATTERN.test(line) && line.length <= 56 && !/[。；;]/.test(line)) return true
  return !/[。；;]/.test(line) && line.length <= 34 && /(系统|平台|项目|网站|小程序|APP|应用|管理|商城|服务|工具|引擎|系统设计)$/i.test(line)
}

const looksLikeProjectStart = (line: string, index: number) => {
  if (PROJECT_START_PATTERN.test(line)) return true
  if (index === 0) return true
  return isProjectHeading(line)
}

const pushUnique = (target: string[], value: string) => {
  const cleaned = value.replace(/^[:：]/, '').trim()
  if (cleaned && !target.includes(cleaned)) target.push(cleaned)
}

const parseProjectEntry = (lines: string[]): ResumeProject => {
  const project: ResumeProject = {
    name: '',
    time: '',
    role: '',
    techStack: [],
    description: '',
    responsibilities: [],
    achievements: [],
    items: []
  }

  lines.forEach((rawLine, index) => {
    const line = cleanResumeLine(rawLine)
    if (!line) return

    const timeMatch = line.match(TIME_RANGE_PATTERN)
    if (timeMatch && !project.time) {
      project.time = timeMatch[1].replace(/\s+/g, '')
    }

    const inlineName = extractInlineField(line, ['项目名称', '项目名'])
    if (inlineName && !project.name) {
      project.name = inlineName
        .replace(TIME_RANGE_PATTERN, '')
        .replace(/[|｜].*$/, '')
        .trim()
    }

    if (PROJECT_NAME_PATTERN.test(line) && !project.name) {
      project.name = line
        .replace(PROJECT_NAME_PATTERN, '')
        .replace(TIME_RANGE_PATTERN, '')
        .replace(/[|｜].*$/, '')
        .trim()
    }

    const inlineTime = extractInlineField(line, ['时间', '项目时间', '周期', '起止时间'])
    if (inlineTime) {
      project.time = inlineTime.replace(/\s+/g, '')
    }

    const inlineRole = extractInlineField(line, ['角色', '项目角色', '担任角色', '职位', '岗位'])
    if (inlineRole && !project.role) {
      project.role = inlineRole
    }

    const inlineTech = extractInlineField(line, ['技术栈', '技术架构', '开发环境', '使用技术', '相关技术'])
    if (inlineTech) {
      project.techStack.push(...splitTechStack(inlineTech))
    }

    const inlineDesc = extractInlineField(line, ['项目描述', '项目简介', '项目背景', '背景'])
    if (inlineDesc && !project.description) {
      project.description = inlineDesc
    }

    const hasMetaField = Boolean(inlineName || inlineTime || inlineRole || inlineTech || inlineDesc)
    const inlineResponsibility = extractInlineField(line, ['负责内容', '项目职责', '职责', '工作内容'])
    if (inlineResponsibility) {
      pushUnique(project.responsibilities, inlineResponsibility)
      return
    }

    const inlineAchievement = extractInlineField(line, ['项目成果', '成果', '亮点'])
    if (inlineAchievement) {
      pushUnique(project.achievements, inlineAchievement)
      return
    }

    if (!project.name && (index === 0 || isProjectHeading(line))) {
      project.name = line
        .replace(TIME_RANGE_PATTERN, '')
        .replace(PROJECT_NAME_PATTERN, '')
        .replace(/[|｜].*$/, '')
        .trim()
      return
    }
    if (hasMetaField) return

    const cleaned = removeInlineField(line, [
      '项目名称', '项目名', '项目时间', '起止时间', '时间', '周期', '角色', '项目角色', '担任角色',
      '职位', '岗位', '技术栈', '技术架构', '开发环境', '使用技术', '相关技术', '项目描述',
      '项目简介', '项目背景', '背景'
    ])
    if (!cleaned || cleaned === project.name || cleaned === project.time) return
    if (TECH_KEYWORD_PATTERN.test(cleaned) && cleaned.length <= 90 && project.techStack.length < 8) {
      project.techStack.push(...splitTechStack(cleaned))
      return
    }
    if (/成果|优化|提升|降低|完成|实现|上线|获奖|通过|排名|效率|性能|准确率|覆盖率/.test(cleaned)) {
      pushUnique(project.achievements, cleaned)
      return
    }
    if (/负责|参与|设计|开发|搭建|封装|对接|实现|维护|测试|部署|编写/.test(cleaned)) {
      pushUnique(project.responsibilities, cleaned)
      return
    }
    pushUnique(project.items, cleaned)
  })

  project.techStack = Array.from(new Set(project.techStack))
  if (!project.description && project.items.length && /^(本项目|该项目|项目|基于).{8,}$/.test(project.items[0])) {
    project.description = project.items.shift() || ''
  }
  if (!project.name) {
    project.name = project.description ? project.description.slice(0, 18) : '项目经历'
  }
  return project
}

const parseProjects = (items: string[]) => {
  const groups: string[][] = []
  let current: string[] = []

  items.forEach((item, index) => {
    const line = cleanResumeLine(item)
    if (looksLikeProjectStart(line, index) && current.length) {
      groups.push(current)
      current = [line]
      return
    }
    current.push(line)
  })
  if (current.length) groups.push(current)

  return groups
    .map(parseProjectEntry)
    .filter((project) => project.name || project.items.length || project.description || project.responsibilities.length || project.achievements.length)
}

const parseResumeSections = (content: string): ResumeSection[] => {
  const normalized = content
    .replace(/\r/g, '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
  const sectionMatchers = [
    { key: 'basic', title: '基本信息', patterns: ['基本信息', '个人信息'] },
    { key: 'education', title: '教育经历', patterns: ['教育经历', '教育背景', '学历'] },
    { key: 'project', title: '项目经历', patterns: ['项目经历', '项目经验', '项目实践', '项目介绍'] },
    { key: 'work', title: '实习/工作经历', patterns: ['工作经历', '实习经历', '实践经历'] },
    { key: 'skills', title: '专业技能', patterns: ['专业技能', '技能清单', '技能特长', '技术栈'] },
    { key: 'awards', title: '证书/荣誉', patterns: ['证书', '荣誉', '奖项', '获奖'] },
    { key: 'self', title: '自我评价', patterns: ['自我评价', '个人评价', '个人优势'] }
  ]
  const buckets = new Map(sectionMatchers.map((item) => [item.key, { ...item, items: [] as string[] } as ResumeSection]))
  let currentKey = 'basic'

  const detectKey = (line: string) => {
    const compact = line.replace(/[:：\s]/g, '')
    return sectionMatchers.find((section) => section.patterns.some((pattern) => compact === pattern || compact.startsWith(pattern)))?.key
  }

  for (const line of normalized) {
    const nextKey = detectKey(line)
    if (nextKey && line.length <= 18) {
      currentKey = nextKey
      continue
    }
    const key = detectKey(line) || currentKey
    const cleaned = cleanResumeLine(line)
    if (cleaned) {
      buckets.get(key)?.items.push(cleaned)
    }
  }

  return [...buckets.values()]
    .map((section) => section.key === 'project' ? { ...section, projects: parseProjects(section.items) } : section)
    .filter((section) => section.items.length || section.projects?.length)
}

const renderRadarChart = async () => {
  await nextTick()
  if (!radarChartRef.value || !summary.value) return
  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }
  const items = dimensionItems.value
  radarChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      appendToBody: true,
      confine: false,
      extraCssText: 'z-index: 9999; max-width: 360px; white-space: normal;',
      formatter: () => items.map((item) => `${item.label}: ${Math.round(item.score)}`).join('<br/>')
    },
    radar: {
      radius: '68%',
      center: ['50%', '52%'],
      indicator: items.map((item) => ({ name: item.label, max: 100 })),
      splitNumber: 4,
      axisName: {
        color: isDark.value ? '#dbe7ff' : '#435064',
        fontWeight: 800
      },
      splitLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.16)' : 'rgba(67, 80, 100, 0.14)' } },
      splitArea: { areaStyle: { color: ['rgba(255, 90, 42, 0.04)', 'rgba(65, 110, 230, 0.04)'] } },
      axisLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.18)' : 'rgba(67, 80, 100, 0.16)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: items.map((item) => Number(item.score.toFixed(1))),
        name: '能力维度',
        areaStyle: { color: 'rgba(255, 90, 42, 0.22)' },
        lineStyle: { width: 3, color: '#ff5a2a' },
        itemStyle: { color: '#ff5a2a' }
      }]
    }]
  })
  radarChart.resize()
  renderScoringModelChart()
}

const renderScoringModelChart = async () => {
  await nextTick()
  if (!scoringModelChartRef.value || !summary.value) return
  if (!scoringModelChart) {
    scoringModelChart = echarts.init(scoringModelChartRef.value)
  }
  const items = competencyItems.value
  const maxWeight = Math.max(30, Math.ceil(Math.max(...items.map((item) => item.weight), 0) / 10) * 10)
  scoringModelChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      appendToBody: true,
      confine: false,
      extraCssText: 'z-index: 9999; max-width: 420px; white-space: normal;',
      formatter: () => items.map((item) => {
        const description = item.description ? `<br/><span style="opacity:.72">${item.description}</span>` : ''
        return `${item.name}: ${Math.round(item.weight)}%${description}`
      }).join('<br/>')
    },
    radar: {
      radius: '65%',
      center: ['50%', '52%'],
      indicator: items.map((item) => ({ name: item.name, max: maxWeight })),
      splitNumber: 3,
      axisName: {
        color: isDark.value ? '#dbe7ff' : '#435064',
        fontWeight: 800
      },
      splitLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.16)' : 'rgba(67, 80, 100, 0.14)' } },
      splitArea: { areaStyle: { color: ['rgba(65, 110, 230, 0.05)', 'rgba(255, 90, 42, 0.04)'] } },
      axisLine: { lineStyle: { color: isDark.value ? 'rgba(177, 196, 226, 0.18)' : 'rgba(67, 80, 100, 0.16)' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: items.map((item) => Number(item.weight.toFixed(1))),
        name: '岗位权重',
        areaStyle: { color: 'rgba(65, 110, 230, 0.22)' },
        lineStyle: { width: 3, color: '#416ee6' },
        itemStyle: { color: '#416ee6' }
      }]
    }]
  })
  scoringModelChart.resize()
}

const resizeRadarChart = () => {
  radarChart?.resize()
  scoringModelChart?.resize()
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
  if (callMessagesRef.value) {
    callMessagesRef.value.scrollTop = callMessagesRef.value.scrollHeight
  }
}

const pushMessage = (role: 'ai' | 'user', content: string, title = role === 'ai' ? '面试官' : '') => {
  const message: ChatMessage = { id: `${Date.now()}-${messages.value.length}`, role, title, content }
  messages.value.push(message)
  if (role === 'ai') {
    enqueueAvatarSpeech(content)
  }
  scrollToBottom()
  return message
}

const formatExpressionLine = (analysis?: AIInterviewExpressionAnalysis) => {
  if (!analysis) return ''
  if (!analysis.enabled) {
    return analysis.reason || '文字作答｜未采集语音表达数据'
  }
  return `表达分析：语速 ${Math.round(analysis.speechRate || 0)} 字/分钟｜情绪 ${analysis.emotionLabel || '未识别'}｜清晰度 ${Math.round(analysis.clarityScore || 0)}｜自信度 ${Math.round(analysis.confidenceScore || 0)}`
}

const expressionReport = computed(() => {
  const analyses = (summary.value?.questionReviews || [])
    .map((review) => review.expressionAnalysis)
    .filter((item): item is AIInterviewExpressionAnalysis => Boolean(item?.enabled))
  if (!analyses.length) {
    return {
      enabled: false,
      voiceCount: 0,
      averageSpeechRate: 0,
      speechRateLevel: '暂无',
      emotionLabel: '暂无',
      averageClarity: 0,
      averageConfidence: 0
    }
  }
  const averageSpeechRate = Math.round(analyses.reduce((sum, item) => sum + Number(item.speechRate || 0), 0) / analyses.length)
  const averageClarity = Math.round(analyses.reduce((sum, item) => sum + Number(item.clarityScore || 0), 0) / analyses.length)
  const averageConfidence = Math.round(analyses.reduce((sum, item) => sum + Number(item.confidenceScore || 0), 0) / analyses.length)
  const emotionCounts = analyses.reduce<Record<string, number>>((acc, item) => {
    const label = item.emotionLabel || '未识别'
    acc[label] = (acc[label] || 0) + 1
    return acc
  }, {})
  const emotionLabel = Object.entries(emotionCounts).sort((a, b) => b[1] - a[1])[0]?.[0] || '未识别'
  return {
    enabled: true,
    voiceCount: analyses.length,
    averageSpeechRate,
    speechRateLevel: averageSpeechRate < 110 ? '整体偏慢' : averageSpeechRate <= 230 ? '整体适中' : averageSpeechRate <= 270 ? '整体稍快' : '整体过快',
    emotionLabel,
    averageClarity,
    averageConfidence
  }
})

const enqueueAvatarSpeech = (content: string) => {
  const text = content.trim()
  if (!text) return
  pendingAvatarTexts.value.push(text)
  flushAvatarQueue()
}

const flushAvatarQueue = async () => {
  if (avatarLoading.value || avatarSpeaking.value || !avatarSession.value?.connected || !pendingAvatarTexts.value.length) {
    return
  }
  avatarSpeaking.value = true
  try {
    while (pendingAvatarTexts.value.length && avatarSession.value?.connected) {
      const text = pendingAvatarTexts.value.shift()
      if (!text) continue
      const spoken = await aiInterviewApi.speakAvatar(interviewId.value, { text })
      if (!spoken) {
        avatarSession.value = null
        avatarError.value = '数字人播报失败，已切换为文字 / 语音面试'
        pendingAvatarTexts.value = []
        break
      }
    }
  } catch (error: any) {
    avatarError.value = error?.message || '数字人播报失败'
  } finally {
    avatarSpeaking.value = false
  }
}

const initAvatarSession = async () => {
  avatarLoading.value = true
  avatarError.value = ''
  try {
    const data = await aiInterviewApi.initAvatarSession(interviewId.value)
    avatarSession.value = data
    if (!data.enabled || !data.connected) {
      avatarError.value = data.message || '数字人暂未启用，已切换为文字 / 语音面试'
      return
    }
    await flushAvatarQueue()
  } catch (error: any) {
    avatarSession.value = null
    avatarError.value = error?.message || '数字人连接失败，已切换为文字 / 语音面试'
  } finally {
    avatarLoading.value = false
  }
}

const handleAvatarPlayerError = (message: string) => {
  avatarError.value = message
}

const closeAvatarSession = async () => {
  if (!avatarSession.value?.connected) return
  if (avatarClosingPromise) {
    await avatarClosingPromise
    return
  }
  avatarClosingPromise = aiInterviewApi.stopAvatarSession(interviewId.value)
    .catch(() => undefined)
    .then(() => {
      avatarSession.value = null
      pendingAvatarTexts.value = []
    })
    .finally(() => {
      avatarClosingPromise = null
    })
  await avatarClosingPromise
}

const handlePageExit = () => {
  if (pageExitHandled || !avatarSession.value?.connected) return
  pageExitHandled = true
  aiInterviewApi.stopAvatarSessionOnPageExit(interviewId.value)
  avatarSession.value = null
  pendingAvatarTexts.value = []
}

const goBack = async () => {
  if (!reportMode.value && !finished.value && session.value) {
    try {
      await ElMessageBox.confirm(
        '面试还未结束，是否需要退出当前页面？退出后将关闭本场数字人会话。',
        '退出确认',
        { confirmButtonText: '确认退出', cancelButtonText: '继续面试', type: 'warning' }
      )
    } catch {
      return
    }
  }
  await closeAvatarSession()
  if (reportMode.value) {
    router.push('/user/history')
    return
  }
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/user/interview/ai/create')
}

const switchMainTab = async (tab: 'dialogue' | 'report') => {
  if (tab === 'report') {
    if (!canViewReport.value) {
      ElMessage.info('请先完成面试，再查看完整报告')
      return
    }
    activeMainTab.value = 'report'
    if (!summary.value && finished.value) {
      await openSummary()
    }
    return
  }
  activeMainTab.value = 'dialogue'
}

const loadInterview = async () => {
  loadingSession.value = true
  try {
    let data = await aiInterviewApi.getInterview(interviewId.value)
    if (!reportMode.value && data.status !== 'IN_PROGRESS' && data.status !== 'COMPLETED') {
      data = await aiInterviewApi.startInterview(interviewId.value)
    }
    session.value = data
    currentQuestion.value = data.currentQuestion || null
    syncInterviewTimer(data)
    messages.value = []
    pendingAvatarTexts.value = []
    initAvatarSession()
    if (data.openingMessage) {
      pushMessage('ai', data.openingMessage, '开场')
    }
    if (currentQuestion.value) {
      pushMessage('ai', currentQuestion.value.content, `第 ${currentQuestion.value.questionOrder} 题`)
    }
    if (reportMode.value) {
      if (data.status !== 'COMPLETED') {
        ElMessage.warning('该场面试还没有生成最终报告，已为你切回继续面试页面')
        router.replace(`/user/interview/ai/session/${interviewId.value}`)
        return
      }
      activeMainTab.value = 'report'
      await openSummary()
      return
    }
    if (data.status === 'COMPLETED') {
      await openSummary()
      return
    }
  } catch (error: any) {
    ElMessage.error(error?.message || '面试载入失败')
  } finally {
    loadingSession.value = false
  }
}

const loadResumes = async () => {
  resumeLoading.value = true
  try {
    resumes.value = await resumeApi.listResumes()
  } catch (error: any) {
    ElMessage.error(error?.message || '简历载入失败')
  } finally {
    resumeLoading.value = false
  }
}

const submitAnswer = async (override?: { content: string; inputMode?: 'TEXT' | 'VOICE'; durationSeconds?: number; emotion?: string }) => {
  const content = (override?.content ?? draft.value).trim()
  if (!content || !currentQuestion.value || !session.value) return
  const questionId = currentQuestion.value.questionId
  if (!override) {
    draft.value = ''
  }
  const inputMode = override?.inputMode || 'TEXT'
  const durationSeconds = override?.durationSeconds ?? (voiceAnswerStartedAt > 0 ? Math.max(1, Math.round((Date.now() - voiceAnswerStartedAt) / 1000)) : elapsedSeconds.value)
  const userMessage = pushMessage('user', content)
  waitingAI.value = true
  try {
    const result = await aiInterviewApi.submitAnswer(session.value.interviewId, questionId, {
      content,
      inputMode,
      duration: durationSeconds,
      expressionMeta: {
        emotion: override?.emotion || realtimeEmotion.value,
        transcriptText: content,
        durationSeconds
      }
    })
    userMessage.expressionAnalysis = result.expressionAnalysis
    voiceAnswerStartedAt = 0
    if (result.interviewerReply) {
      pushMessage('ai', result.interviewerReply)
    }
    session.value.questionCount = Math.max(result.questionCount || session.value.questionCount || 0, session.value.questionCount || 0)
    session.value.answeredCount = result.answeredCount ?? (session.value.answeredCount + 1)
    if (result.interviewCompleted || result.nextAction === 'END') {
      currentQuestion.value = null
      const shouldFinish = await confirmCompletePlannedInterview()
      if (shouldFinish) {
        await finishInterviewAndOpenReport()
      } else {
        pushMessage('ai', '本场规划题目已经全部完成，你可以点击左侧“结束面试”生成报告。', '完成提醒')
      }
      return
    }
    currentQuestion.value = result.nextQuestion || await aiInterviewApi.getNextQuestion(session.value.interviewId)
  } catch (error: any) {
    ElMessage.error(error?.message || '提交回答失败，请稍后重试')
  } finally {
    waitingAI.value = false
  }
}

const confirmCompletePlannedInterview = async () => {
  try {
    await ElMessageBox.confirm(
      `本场规划的 ${session.value?.questionCount || 0} 道题已经完成，是否现在结束面试并生成报告？`,
      '完成本轮面试',
      { confirmButtonText: '结束面试', cancelButtonText: '暂不结束', type: 'success' }
    )
    return true
  } catch {
    return false
  }
}

const finishInterviewAndOpenReport = async () => {
  if (!session.value) return
  ending.value = true
  activeMainTab.value = 'report'
  summaryLoading.value = true
  try {
    summary.value = await aiInterviewApi.endInterview(session.value.interviewId)
    session.value.status = 'COMPLETED'
    currentQuestion.value = null
    localStorage.removeItem(getTimerStorageKey())
    await closeAvatarSession()
  } finally {
    ending.value = false
    summaryLoading.value = false
  }
}

const confirmEndInterview = async () => {
  if (!session.value || finished.value) return
  const remaining = Math.max(0, (session.value.questionCount || 0) - (session.value.answeredCount || 0))
  try {
    await ElMessageBox.confirm(
      remaining > 0 ? `还有 ${remaining} 道题没有回答完，是否结束并生成当前进度的面试报告？` : '是否结束面试并生成报告？',
      '结束面试确认',
      { confirmButtonText: '结束面试', cancelButtonText: '继续作答', type: remaining > 0 ? 'warning' : 'success' }
    )
    await finishInterviewAndOpenReport()
  } catch {
    activeMainTab.value = 'dialogue'
  }
}

const openSummary = async () => {
  if (!session.value) return
  summaryLoading.value = true
  try {
    summary.value = await aiInterviewApi.getSummary(session.value.interviewId)
  } catch {
    summary.value = await aiInterviewApi.endInterview(session.value.interviewId)
  } finally {
    summaryLoading.value = false
  }
}

const toggleTheme = () => {
  isDark.value = !isDark.value
}

const syncCameraVideo = async () => {
  await nextTick()
  if (!cameraVideoRef.value || !cameraStream.value) return
  cameraVideoRef.value.srcObject = cameraStream.value
  try {
    await cameraVideoRef.value.play()
  } catch {
    // Ignore autoplay failures while video element is mounting.
  }
}

const stopCameraPreview = () => {
  cameraStream.value?.getTracks().forEach((track) => track.stop())
  cameraStream.value = null
  if (cameraVideoRef.value) {
    cameraVideoRef.value.srcObject = null
  }
}

const startCameraPreview = async () => {
  if (cameraStream.value) {
    await syncCameraVideo()
    return
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    cameraError.value = '当前浏览器不支持摄像头调用'
    return
  }
  try {
    cameraError.value = ''
    cameraStream.value = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'user',
        width: { ideal: 960 },
        height: { ideal: 540 }
      },
      audio: false
    })
    await syncCameraVideo()
  } catch (error: any) {
    cameraError.value = error?.message || '摄像头调用失败'
  }
}

const openCallView = async () => {
  voiceError.value = ''
  const nextQuery = { ...route.query, view: 'call' }
  await router.replace({ path: route.path, query: nextQuery }).catch(() => undefined)
  await Promise.allSettled([
    startCameraPreview(),
    !finished.value && !loadingSession.value ? startVoiceCapture() : Promise.resolve()
  ])
}

const closeCallView = () => {
  const nextQuery = { ...route.query }
  delete nextQuery.view
  router.replace({ path: route.path, query: nextQuery }).catch(() => undefined)
}

const buildRealtimeAsrLanguage = () => {
  const language = (session.value?.interviewLanguage || '').toLowerCase()
  return language.startsWith('en') || language.includes('english') || language.includes('英文') ? 'en' : 'zh'
}

const buildRealtimeAsrUrl = () => {
  const token = localStorage.getItem('token') || ''
  const base = new URL(apiBaseUrl)
  base.protocol = base.protocol === 'https:' ? 'wss:' : 'ws:'
  base.pathname = '/ws/ai/asr/realtime'
  base.search = ''
  base.searchParams.set('token', token)
  base.searchParams.set('language', buildRealtimeAsrLanguage())
  return base.toString()
}

const markVoiceDraftEdited = () => {
  voiceDraftEdited.value = true
}

const updateVoiceDraftFromRecognition = (text: string) => {
  if (!voiceDraftEdited.value) {
    voiceDraft.value = text.trim()
  }
}

const connectRealtimeAsr = () => new Promise<void>((resolve, reject) => {
  if (voiceSocket && voiceSocket.readyState === WebSocket.OPEN) {
    resolve()
    return
  }
  const socket = new WebSocket(buildRealtimeAsrUrl())
  voiceSocket = socket
  const timeout = window.setTimeout(() => {
    reject(new Error('实时语音识别连接超时'))
    socket.close()
  }, 8000)

  socket.onopen = () => {
    window.clearTimeout(timeout)
    resolve()
  }
  socket.onerror = () => {
    window.clearTimeout(timeout)
    reject(new Error('实时语音识别连接失败'))
  }
  socket.onmessage = (event) => {
    try {
      const payload = JSON.parse(String(event.data))
      if (payload.type === 'partial') {
        voicePartialTranscript.value = String(payload.displayText || `${payload.text || ''}${payload.stash || ''}`).trim()
        interimTranscript.value = voicePartialTranscript.value
        updateVoiceDraftFromRecognition([voiceTranscriptBase, voiceFinalTranscript.value, voicePartialTranscript.value].filter(Boolean).join(''))
        realtimeEmotion.value = payload.emotion || realtimeEmotion.value
      } else if (payload.type === 'final') {
        const transcript = String(payload.transcript || '').trim()
        if (transcript) {
          voiceFinalTranscript.value = [voiceFinalTranscript.value, transcript].filter(Boolean).join('')
          voicePartialTranscript.value = ''
          liveTranscript.value = [voiceTranscriptBase, voiceFinalTranscript.value].filter(Boolean).join('')
          updateVoiceDraftFromRecognition(liveTranscript.value)
        }
        realtimeEmotion.value = payload.emotion || realtimeEmotion.value
        interimTranscript.value = ''
      } else if (payload.type === 'error') {
        voiceError.value = payload.message || '实时语音识别失败'
      }
    } catch {
      // Ignore non-JSON heartbeat or protocol noise.
    }
  }
  socket.onclose = () => {
    if (voiceSocket === socket) {
      voiceSocket = null
    }
  }
})

const stopRealtimeAsr = () => {
  if (!voiceSocket) return
  if (voiceSocket.readyState === WebSocket.OPEN) {
    voiceSocket.send(JSON.stringify({ type: 'finish' }))
    window.setTimeout(() => voiceSocket?.close(), 600)
  } else {
    voiceSocket.close()
  }
}

const downsampleBuffer = (buffer: Float32Array, inputSampleRate: number, outputSampleRate: number) => {
  if (outputSampleRate === inputSampleRate) return buffer
  const ratio = inputSampleRate / outputSampleRate
  const newLength = Math.round(buffer.length / ratio)
  const result = new Float32Array(newLength)
  let offsetResult = 0
  let offsetBuffer = 0
  while (offsetResult < result.length) {
    const nextOffsetBuffer = Math.round((offsetResult + 1) * ratio)
    let accum = 0
    let count = 0
    for (let index = offsetBuffer; index < nextOffsetBuffer && index < buffer.length; index += 1) {
      accum += buffer[index]
      count += 1
    }
    result[offsetResult] = count > 0 ? accum / count : 0
    offsetResult += 1
    offsetBuffer = nextOffsetBuffer
  }
  return result
}

const encodePcm16 = (input: Float32Array) => {
  const output = new Int16Array(input.length)
  for (let index = 0; index < input.length; index += 1) {
    const sample = Math.max(-1, Math.min(1, input[index]))
    output[index] = sample < 0 ? sample * 0x8000 : sample * 0x7fff
  }
  return output.buffer
}

const startPcmStreaming = async (stream: MediaStream) => {
  const AudioContextCtor = (window as any).AudioContext || (window as any).webkitAudioContext
  const context = new AudioContextCtor()
  const source = context.createMediaStreamSource(stream)
  const processor = context.createScriptProcessor(4096, 1, 1)
  audioContext = context
  audioSource = source
  audioProcessor = processor
  processor.onaudioprocess = (event: AudioProcessingEvent) => {
    if (!voiceSocket || voiceSocket.readyState !== WebSocket.OPEN) return
    const input = event.inputBuffer.getChannelData(0)
    const downsampled = downsampleBuffer(input, context.sampleRate || 48000, REALTIME_ASR_SAMPLE_RATE)
    voiceSocket.send(encodePcm16(downsampled))
  }
  source.connect(processor)
  processor.connect(context.destination)
}

const stopPcmStreaming = () => {
  audioProcessor?.disconnect()
  audioSource?.disconnect()
  audioProcessor = null
  audioSource = null
  audioContext?.close().catch(() => undefined)
  audioContext = null
}

const stopVoiceCapture = () => {
  stopPcmStreaming()
  stopRealtimeAsr()
  if (mediaRecorder && recording.value) {
    mediaRecorder.stop()
  }
  recording.value = false
}

const startVoiceCapture = async (silent = false) => {
  if (recording.value || transcribing.value) return
  voiceError.value = ''
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    voiceStream = stream
    audioChunks = []
    voiceTranscriptBase = ''
    voiceFinalTranscript.value = ''
    voicePartialTranscript.value = ''
    voiceDraftEdited.value = false
    voiceDraft.value = ''
    liveTranscript.value = voiceTranscriptBase
    interimTranscript.value = ''
    realtimeEmotion.value = ''
    voiceAnswerStartedAt = Date.now()
    await connectRealtimeAsr()
    await startPcmStreaming(stream)
    mediaRecorder = new MediaRecorder(stream)
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) audioChunks.push(event.data)
    }
    mediaRecorder.onstop = async () => {
      stream.getTracks().forEach((track) => track.stop())
      if (voiceStream === stream) {
        voiceStream = null
      }
      if (voiceCompleting) {
        interimTranscript.value = ''
        transcribing.value = false
        return
      }
      const realtimeText = [liveTranscript.value, interimTranscript.value].filter(Boolean).join('').trim()
      if (realtimeText && realtimeText !== voiceTranscriptBase) {
        liveTranscript.value = realtimeText
        updateVoiceDraftFromRecognition(realtimeText)
        interimTranscript.value = ''
        transcribing.value = false
        return
      }
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const file = new File([blob], 'answer.webm', { type: 'audio/webm' })
      transcribing.value = true
      try {
        const result = await aiInterviewApi.transcribeAudio(file, session.value?.interviewLanguage)
        const transcript = (result.transcript || '').trim()
        if (transcript) {
          liveTranscript.value = transcript
          updateVoiceDraftFromRecognition(transcript)
        }
      } catch (error: any) {
        ElMessage.error(error?.message || '语音识别失败')
      } finally {
        interimTranscript.value = ''
        transcribing.value = false
      }
    }
    mediaRecorder.start(1000)
    recording.value = true
  } catch (error: any) {
    stopPcmStreaming()
    stopRealtimeAsr()
    voiceStream?.getTracks().forEach((track) => track.stop())
    voiceStream = null
    voiceError.value = error?.message || '麦克风权限未开启'
    if (!silent) {
      ElMessage.error('无法访问麦克风，请检查浏览器权限')
    }
  }
}

const toggleRecord = async () => {
  if (recording.value) {
    stopVoiceCapture()
    return
  }
  await startVoiceCapture()
}

const completeVoiceAnswer = async () => {
  const content = voiceDraft.value.trim()
  if (!content || inputDisabled.value || !currentQuestion.value || !session.value) return
  const durationSeconds = voiceAnswerStartedAt > 0 ? Math.max(1, Math.round((Date.now() - voiceAnswerStartedAt) / 1000)) : 0
  const wasRecording = recording.value
  voiceCompleting = true
  if (wasRecording) {
    stopVoiceCapture()
  }
  const emotion = realtimeEmotion.value
  voiceDraft.value = ''
  voiceFinalTranscript.value = ''
  voicePartialTranscript.value = ''
  voiceDraftEdited.value = false
  liveTranscript.value = ''
  interimTranscript.value = ''
  try {
    await submitAnswer({
      content,
      inputMode: 'VOICE',
      durationSeconds,
      emotion
    })
  } finally {
    if (wasRecording) {
      window.setTimeout(() => {
        voiceCompleting = false
      }, 1200)
    } else {
      voiceCompleting = false
    }
  }
}

watch(
  callMode,
  (enabled) => {
    if (enabled) {
      return
    }
    stopCameraPreview()
    if (recording.value) {
      stopVoiceCapture()
    }
  },
  { immediate: true }
)

watch(
  [activeMainTab, finished],
  ([tab, interviewFinished]) => {
    if ((tab === 'report' || interviewFinished) && callMode.value) {
      closeCallView()
    }
  }
)

watch(
  loadingSession,
  async (loading) => {
    if (!loading && callMode.value && !recording.value && !finished.value) {
      await startVoiceCapture()
    }
  }
)

watch(
  [summary, activeMainTab, isDark],
  () => {
    if (activeMainTab.value === 'report' && summary.value) {
      renderRadarChart()
    }
  },
  { deep: true }
)

onMounted(() => {
  activeMainTab.value = reportMode.value ? 'report' : 'dialogue'
  refreshGrowthCurveJoined()
  loadInterview()
  loadResumes()
  window.addEventListener('pagehide', handlePageExit)
  window.addEventListener('beforeunload', handlePageExit)
  window.addEventListener('resize', resizeRadarChart)
  elapsedTimer = window.setInterval(() => {
    elapsedSeconds.value = Math.floor((Date.now() - startedAt) / 1000)
  }, 1000)
})

onBeforeRouteLeave(async () => {
  refreshGrowthCurveJoined()
  if (finished.value && summary.value && !growthCurveJoined.value && localStorage.getItem('growthCurveJoinNoRemind') !== 'true') {
    try {
      await ElMessageBox.confirm(
        '这场面试还没有加入成长曲线。加入后可在成长中心生成五维趋势与短板证据分析，是否现在加入？',
        '加入成长曲线',
        { confirmButtonText: '加入', cancelButtonText: '暂不加入', type: 'info' }
      )
      await confirmJoinGrowthCurve()
    } catch {
      // 用户选择暂不加入时继续离开页面。
    }
  }
  stopCameraPreview()
  await closeAvatarSession()
})

onUnmounted(() => {
  window.removeEventListener('pagehide', handlePageExit)
  window.removeEventListener('beforeunload', handlePageExit)
  window.removeEventListener('resize', resizeRadarChart)
  if (elapsedTimer) window.clearInterval(elapsedTimer)
  stopCameraPreview()
  stopPcmStreaming()
  stopRealtimeAsr()
  radarChart?.dispose()
  radarChart = null
  scoringModelChart?.dispose()
  scoringModelChart = null
  if (mediaRecorder && recording.value) mediaRecorder.stop()
  void closeAvatarSession()
})
</script>

<style scoped>
.session-page {
  --page-bg: #fff7f3;
  --panel-bg: #ffffff;
  --side-bg: #ffffff;
  --card-bg: #fff2ec;
  --card-border: #ffd8c8;
  --primary-text: #242733;
  --muted-text: #7a8496;
  --soft-text: #96a0af;
  --accent: #ff5a2a;
  --accent-soft: #fff1eb;
  --bot-icon-bg: #fff1eb;
  --bot-bubble-bg: #ffffff;
  --user-bubble-bg: #ff5a2a;
  --input-bg: #ffffff;
  --input-border: #f1d9ce;
  --success: #24a865;
  --success-bg: #eafaf1;
  --report-card-bg: #fbfcfe;
  --report-card-border: #e7ebf3;
  --report-divider: rgba(36, 39, 51, 0.08);
  --report-shadow: 0 10px 24px rgba(27, 36, 56, 0.035);
  --report-muted-bg: #ffffff;
  --report-text: #4f5968;
  --report-hero-shadow: 0 24px 48px rgba(255, 90, 42, 0.22);
  height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 38.2%) minmax(0, 61.8%);
  overflow: hidden;
  background: var(--page-bg);
  color: var(--primary-text);
}

.theme-dark {
  --page-bg: #101b2f;
  --panel-bg: #111c30;
  --side-bg: #142441;
  --card-bg: rgba(255, 255, 255, 0.07);
  --card-border: rgba(151, 170, 204, 0.16);
  --primary-text: #eef4ff;
  --muted-text: #7f8fa9;
  --soft-text: #8797b1;
  --accent: #ff6a35;
  --accent-soft: rgba(255, 90, 42, 0.12);
  --bot-icon-bg: #203860;
  --bot-bubble-bg: #1c3765;
  --input-bg: #1c3765;
  --input-border: transparent;
  --success: #43d783;
  --success-bg: rgba(67, 215, 131, 0.12);
  --report-card-bg: rgba(255, 255, 255, 0.04);
  --report-card-border: rgba(148, 170, 204, 0.16);
  --report-divider: rgba(219, 229, 245, 0.12);
  --report-shadow: 0 14px 28px rgba(2, 6, 23, 0.24);
  --report-muted-bg: rgba(255, 255, 255, 0.03);
  --report-text: #c3d0e5;
  --report-hero-shadow: 0 24px 48px rgba(0, 0, 0, 0.22);
}

.interviewer-panel {
  min-height: 0;
  padding: 24px 30px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-right: 1px solid var(--card-border);
  background: var(--side-bg);
}

.left-tabs {
  width: 100%;
  padding: 4px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px;
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: var(--card-bg);
}

.left-tabs button {
  min-width: 0;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 8px;
  color: var(--muted-text);
  background: transparent;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.left-tabs button.active {
  color: #ffffff;
  background: var(--accent);
}

.left-panel-section {
  width: 100%;
  min-height: 0;
  margin-top: 28px;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
}

.avatar-stage {
  width: 100%;
  min-height: 0;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.interviewer-action-row {
  width: 100%;
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.avatar-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
}

.avatar-status.online {
  color: #ffffff;
  background: linear-gradient(135deg, #ff6a35, #ff8a63);
}

.interviewer-panel h1 {
  margin-top: 18px;
  font-size: 20px;
  font-weight: 900;
}

.interviewer-panel p {
  margin-top: 10px;
  color: var(--muted-text);
  font-size: 13px;
}

.end-btn {
  width: 100%;
  height: 38px;
  border: 1px solid rgba(255, 90, 42, 0.45);
  border-radius: 8px;
  color: #ff4f4f;
  background: var(--accent-soft);
  font-weight: 900;
  cursor: pointer;
}

.end-btn:disabled {
  border-color: rgba(148, 163, 184, 0.42);
  color: #8a94a6;
  background: rgba(148, 163, 184, 0.12);
  opacity: 1;
  cursor: not-allowed;
}

.resume-panel {
  margin-top: 22px;
  align-items: stretch;
  overflow: hidden;
}

.resume-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resume-icon {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 22px;
}

.resume-head h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
}

.resume-head p {
  max-width: 244px;
  margin: 7px 0 0;
  color: var(--muted-text);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-readonly {
  min-height: 0;
  margin-top: 18px;
  padding-right: 4px;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 14px;
  overflow: auto;
}

.resume-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.resume-meta span {
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid var(--card-border);
  border-radius: 8px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resume-summary,
.resume-state {
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: var(--card-bg);
}

.resume-summary {
  padding: 14px;
}

.resume-summary strong {
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.resume-summary p {
  margin: 8px 0 0;
  color: var(--muted-text);
  font-size: 13px;
  line-height: 1.8;
}

.resume-section-list {
  display: grid;
  gap: 10px;
}

.resume-section-card {
  padding: 13px 14px;
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: var(--card-bg);
}

.resume-section-card strong {
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.resume-section-card p,
.resume-section-card li {
  color: var(--muted-text);
  font-size: 12px;
  line-height: 1.8;
}

.resume-section-card p {
  margin: 8px 0 0;
}

.resume-section-card ul {
  margin: 8px 0 0;
  padding-left: 17px;
}

.resume-project-list {
  margin-top: 10px;
  display: grid;
  gap: 12px;
}

.resume-project-card {
  padding: 12px;
  border: 1px solid var(--card-border);
  border-radius: 10px;
  background: color-mix(in srgb, var(--card-bg) 74%, var(--panel-bg) 26%);
}

.resume-project-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.resume-project-head h2 {
  margin: 0;
  color: var(--primary-text);
  font-size: 14px;
  line-height: 1.45;
  font-weight: 900;
}

.resume-project-head span {
  flex: none;
  padding: 3px 7px;
  border-radius: 999px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 11px;
  font-weight: 900;
}

.resume-project-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.resume-project-meta span {
  padding: 4px 7px;
  border: 1px solid var(--card-border);
  border-radius: 7px;
  color: var(--muted-text);
  background: var(--panel-bg);
  font-size: 11px;
  line-height: 1.5;
}

.resume-project-desc {
  margin: 10px 0 0;
  color: var(--muted-text);
  font-size: 12px;
  line-height: 1.8;
}

.resume-project-block {
  margin-top: 10px;
}

.resume-project-block b {
  color: var(--primary-text);
  font-size: 12px;
  font-weight: 900;
}

.resume-project-card ul {
  margin-top: 6px;
}

.resume-state {
  min-height: 180px;
  margin-top: 18px;
  padding: 22px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: var(--muted-text);
  text-align: center;
}

.resume-state > .el-icon {
  color: var(--accent);
  font-size: 28px;
}

.resume-state strong {
  color: var(--primary-text);
  font-size: 14px;
}

.resume-state p {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
}

.dialog-panel {
  position: relative;
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: 52px auto minmax(0, 1fr) auto;
  background: var(--panel-bg);
}

.call-stage-panel {
  position: absolute;
  inset: 94px 0 0;
  z-index: 8;
  padding: 26px;
  display: grid;
  place-items: center;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.17), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.14), transparent 24%),
    linear-gradient(180deg, rgba(255, 248, 244, 0.94), rgba(255, 255, 255, 0.98));
}

.theme-dark .call-stage-panel {
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.16), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.08), transparent 24%),
    linear-gradient(180deg, rgba(12, 24, 42, 0.96), rgba(17, 28, 48, 0.98));
}

.call-stage-backdrop span {
  position: absolute;
  border-radius: 50%;
  filter: blur(18px);
  opacity: 0.8;
}

.call-stage-backdrop span:nth-child(1) {
  width: 220px;
  height: 220px;
  top: 42px;
  left: 42px;
  background: rgba(255, 90, 42, 0.14);
}

.call-stage-backdrop span:nth-child(2) {
  width: 160px;
  height: 160px;
  right: 92px;
  top: 86px;
  background: rgba(255, 170, 78, 0.18);
}

.call-stage-backdrop span:nth-child(3) {
  width: 180px;
  height: 180px;
  right: 48px;
  bottom: 46px;
  background: rgba(255, 124, 71, 0.12);
}

.call-stage-card {
  position: relative;
  z-index: 1;
  width: min(760px, 100%);
  padding: 36px;
  display: grid;
  justify-items: center;
  gap: 22px;
  border: 1px solid rgba(255, 164, 133, 0.28);
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 26px 72px rgba(255, 90, 42, 0.16);
  text-align: center;
  backdrop-filter: blur(16px);
}

.theme-dark .call-stage-card {
  border-color: rgba(255, 164, 133, 0.16);
  background: rgba(20, 36, 65, 0.74);
  box-shadow: 0 26px 72px rgba(0, 0, 0, 0.26);
}

.call-stage-avatar {
  position: relative;
  width: 168px;
  height: 168px;
  display: grid;
  place-items: center;
}

.call-avatar-core {
  position: relative;
  z-index: 2;
  width: 104px;
  height: 104px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff854d 60%, #ffb173 100%);
  box-shadow: 0 18px 38px rgba(255, 90, 42, 0.24);
  font-size: 40px;
}

.orbit {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 90, 42, 0.18);
  border-radius: 50%;
  animation: orbitPulse 2.5s ease-out infinite;
}

.orbit-b {
  inset: 18px;
  animation-delay: 0.25s;
}

.orbit-c {
  inset: 36px;
  animation-delay: 0.5s;
}

.call-stage-copy {
  display: grid;
  gap: 10px;
}

.call-stage-tag {
  width: fit-content;
  justify-self: center;
  padding: 6px 11px;
  border-radius: 999px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 11px;
  font-weight: 900;
}

.call-stage-copy h3 {
  font-size: 32px;
  line-height: 1.15;
  font-weight: 900;
}

.call-stage-copy p {
  max-width: 540px;
  color: var(--muted-text);
  line-height: 1.85;
}

.call-stage-meta {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.call-stage-meta article {
  padding: 16px;
  display: grid;
  gap: 8px;
  border-radius: 18px;
  border: 1px solid var(--card-border);
  background: color-mix(in srgb, var(--card-bg) 80%, var(--panel-bg) 20%);
  text-align: left;
}

.call-stage-meta span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 800;
}

.call-stage-meta strong {
  color: var(--primary-text);
  font-size: 15px;
  line-height: 1.6;
  font-weight: 900;
}

.call-stage-timeline {
  display: flex;
  align-items: center;
  gap: 10px;
}

.call-stage-timeline i {
  width: 54px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 90, 42, 0.14);
  transition: background 0.25s ease, transform 0.25s ease;
}

.call-stage-timeline i.done {
  background: linear-gradient(90deg, #ff5a2a, #ffb173);
  transform: scaleX(1.06);
}

.call-action-btn {
  min-width: 186px;
  height: 52px;
  padding: 0 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff7f48 60%, #ffb173 100%);
  box-shadow: 0 18px 34px rgba(255, 90, 42, 0.22);
  font-size: 14px;
  font-weight: 900;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, opacity 0.25s ease;
}

.call-action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 22px 40px rgba(255, 90, 42, 0.26);
}

.call-action-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.dialog-head {
  padding: 0 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--card-border);
}

.head-title {
  display: grid;
}

.dialog-head h2 {
  font-size: 15px;
  font-weight: 900;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.theme-toggle {
  height: 28px;
  padding: 0 11px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1px solid var(--card-border);
  border-radius: 999px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.back-side-btn {
  width: 100%;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid var(--card-border);
  border-radius: 12px;
  color: var(--muted-text);
  background: transparent;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.back-side-btn:hover {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-soft);
}

.dialog-head span {
  height: 22px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--success);
  background: var(--success-bg);
  font-size: 12px;
  font-weight: 900;
}

.dialog-head span.done {
  color: var(--muted-text);
  background: var(--card-bg);
}

.head-chip {
  max-width: 220px;
  height: 28px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.head-chip.time {
  color: var(--accent);
}

.content-tabs {
  padding: 8px 22px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--report-divider);
}

.content-tabs button {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  color: var(--muted-text);
  background: transparent;
  font-size: 12px;
  font-weight: 900;
  cursor: pointer;
}

.content-tabs button.active {
  color: #ffffff;
  background: var(--accent);
}

.content-tabs button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.messages {
  min-height: 0;
  overflow: auto;
  padding: 18px 18px 22px;
}

.message {
  max-width: 910px;
  margin: 0 auto 26px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message.user {
  justify-content: flex-end;
}

.bot-icon,
.user-avatar {
  width: 34px;
  height: 34px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
}

.bot-icon {
  overflow: hidden;
  background: transparent;
  box-shadow: 0 8px 18px rgba(255, 90, 42, 0.16);
}

.bot-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar {
  color: #ffffff;
  background: var(--accent);
  font-weight: 900;
}

.bubble,
.user-bubble,
.typing,
.finish-card,
.loading-card {
  border-radius: 10px;
  line-height: 1.8;
}

.bubble {
  max-width: 640px;
  padding: 15px 18px;
  color: var(--primary-text);
  background: var(--bot-bubble-bg);
  border: 1px solid var(--card-border);
}

.bubble strong {
  color: var(--accent);
  font-size: 12px;
}

.bubble p,
.user-bubble p {
  margin-top: 6px;
  font-size: 14px;
  white-space: pre-wrap;
}

.message-expression {
  margin-top: 10px;
  padding: 7px 9px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.14);
  font-size: 12px;
  line-height: 1.6;
  font-weight: 800;
}

.message-expression.disabled {
  color: rgba(255, 255, 255, 0.74);
  background: rgba(255, 255, 255, 0.1);
  font-weight: 700;
}

.user-bubble {
  max-width: 520px;
  padding: 12px 18px;
  color: #ffffff;
  background: var(--user-bubble-bg);
}

.typing {
  padding: 10px 16px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--soft-text);
  background: var(--bot-bubble-bg);
  border: 1px solid var(--card-border);
}

.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
  animation: pulse 1s infinite ease-in-out;
}

.typing i:nth-child(2) { animation-delay: 0.15s; }
.typing i:nth-child(3) { animation-delay: 0.3s; }

.finish-card,
.loading-card {
  max-width: 620px;
  margin: 0 auto;
  padding: 18px;
  color: var(--primary-text);
  background: var(--success-bg);
  border: 1px solid rgba(67, 215, 131, 0.25);
}

.loading-card {
  display: grid;
  justify-items: center;
  gap: 10px;
  background: var(--card-bg);
}

.finish-card strong {
  color: var(--success);
}

.input-shell {
  padding: 10px 18px;
  display: grid;
  gap: 10px;
  border-top: 1px solid var(--card-border);
}

.voice-live-card {
  padding: 12px 14px;
  border: 1px solid var(--card-border);
  border-radius: 14px;
  background: color-mix(in srgb, var(--accent-soft) 72%, var(--panel-bg) 28%);
}

.voice-live-card.recording {
  box-shadow: 0 10px 24px rgba(255, 90, 42, 0.12);
}

.voice-live-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.voice-live-copy {
  display: grid;
  gap: 2px;
}

.voice-live-copy strong {
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.voice-live-copy span {
  color: var(--muted-text);
  font-size: 12px;
}

.voice-wave {
  width: 44px;
  height: 28px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
}

.voice-wave i {
  width: 4px;
  height: 8px;
  border-radius: 999px;
  background: var(--accent);
  opacity: 0.5;
}

.voice-wave.active i {
  animation: voiceWave 1s ease-in-out infinite;
}

.voice-wave.active i:nth-child(2) {
  animation-delay: 0.12s;
}

.voice-wave.active i:nth-child(3) {
  animation-delay: 0.24s;
}

.voice-wave.active i:nth-child(4) {
  animation-delay: 0.36s;
}

.voice-draft-row {
  margin-top: 10px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 64px;
  align-items: stretch;
  gap: 10px;
}

:deep(.voice-draft-row .el-textarea__inner) {
  min-height: 72px !important;
  border-radius: 9px;
  color: var(--primary-text);
  background: var(--input-bg);
  border-color: var(--input-border);
  box-shadow: none;
  line-height: 1.65;
  resize: none;
}

.voice-finish-btn {
  min-height: 72px;
  border: none;
  border-radius: 9px;
  color: #ffffff;
  background: var(--accent);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.voice-finish-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.voice-finish-btn:disabled {
  opacity: 0.48;
  cursor: not-allowed;
}

.input-bar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 38px 38px 38px;
  align-items: end;
  gap: 8px;
}

:deep(.input-bar .el-textarea__inner) {
  min-height: 42px !important;
  border-radius: 9px;
  color: var(--primary-text);
  background: var(--input-bg);
  border-color: var(--input-border);
  box-shadow: none;
  line-height: 1.55;
  resize: none;
}

.call-btn,
.mic-btn,
.send-btn {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 9px;
  color: #ffffff;
  cursor: pointer;
}

.call-btn {
  color: var(--accent);
  background: var(--accent-soft);
  border: 1px solid var(--card-border);
}

.mic-btn {
  background: var(--accent);
}

.mic-btn.active {
  background: #ff3b3b;
}

.send-btn {
  background: var(--primary-text);
}

.theme-dark .send-btn {
  background: #294a7e;
}

.call-btn:disabled,
.mic-btn:disabled,
.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.call-view {
  position: absolute;
  inset: 0;
  z-index: 9;
  padding: 20px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  gap: 16px;
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.1), transparent 24%),
    linear-gradient(180deg, rgba(255, 248, 244, 0.98), rgba(255, 255, 255, 0.98));
}

.theme-dark .call-view {
  background:
    radial-gradient(circle at top left, rgba(255, 111, 60, 0.14), transparent 28%),
    radial-gradient(circle at bottom right, rgba(255, 189, 121, 0.08), transparent 24%),
    linear-gradient(180deg, rgba(14, 26, 45, 0.98), rgba(17, 28, 48, 0.98));
}

.call-view-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.call-view-head span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 900;
}

.call-view-head h3 {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 900;
}

.call-view-actions {
  display: flex;
  gap: 10px;
}

.call-back-btn,
.call-end-btn {
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.call-back-btn {
  border: 1px solid var(--card-border);
  color: var(--muted-text);
  background: var(--panel-bg);
}

.call-end-btn {
  border: none;
  color: #ffffff;
  background: #ff5a5a;
}

.call-end-btn:disabled {
  color: #8a94a6;
  background: rgba(148, 163, 184, 0.16);
  cursor: not-allowed;
}

.call-view-stage {
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.call-card,
.call-question-card {
  border: 1px solid var(--card-border);
  border-radius: 24px;
  background: color-mix(in srgb, var(--panel-bg) 82%, var(--card-bg) 18%);
  box-shadow: 0 16px 36px rgba(27, 36, 56, 0.08);
}

.call-card {
  min-height: 0;
  padding: 16px;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 14px;
}

.call-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.call-card-head strong {
  font-size: 16px;
  font-weight: 900;
}

.call-card-head span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 800;
}

.call-avatar-panel,
.camera-panel {
  min-height: 0;
  border-radius: 18px;
  overflow: hidden;
  background: #0f172a;
}

.call-avatar-panel {
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-panel {
  position: relative;
  display: grid;
  place-items: center;
}

.call-chat-panel {
  min-height: 0;
  padding: 8px;
  display: grid;
  align-content: start;
  gap: 12px;
  overflow: auto;
  border-radius: 18px;
  background: color-mix(in srgb, var(--panel-bg) 84%, var(--card-bg) 16%);
}

.call-chat-item {
  display: grid;
  justify-items: start;
  gap: 8px;
}

.call-chat-item.user {
  justify-items: end;
}

.call-chat-role {
  min-width: 28px;
  min-height: 28px;
  padding: 4px 9px;
  display: inline-grid;
  place-items: center;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--card-bg);
  font-size: 11px;
  font-weight: 900;
}

.call-chat-role.ai {
  width: 30px;
  height: 30px;
  padding: 0;
  overflow: hidden;
  background: transparent;
}

.call-chat-role.ai img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.call-chat-bubble {
  max-width: min(100%, 420px);
  padding: 12px 14px;
  border-radius: 16px;
  border: 1px solid var(--card-border);
  background: var(--bot-bubble-bg);
}

.call-chat-item.user .call-chat-bubble {
  color: #ffffff;
  background: var(--user-bubble-bg);
}

.call-chat-bubble strong {
  color: var(--accent);
  font-size: 12px;
}

.call-chat-item.user .call-chat-bubble strong {
  color: rgba(255, 255, 255, 0.86);
}

.call-chat-bubble p {
  margin-top: 6px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.call-chat-item.waiting {
  justify-items: start;
}

.call-chat-empty {
  min-height: 160px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: var(--muted-text);
  text-align: center;
}

.call-chat-empty strong {
  color: var(--primary-text);
  font-size: 15px;
}

.camera-panel video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.camera-fallback {
  padding: 24px;
  display: grid;
  justify-items: center;
  gap: 10px;
  color: #ffffff;
  text-align: center;
}

.camera-fallback .el-icon {
  font-size: 34px;
  color: #ffb173;
}

.camera-fallback strong {
  font-size: 16px;
  font-weight: 900;
}

.camera-fallback p {
  max-width: 280px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.7;
}

.call-question-card {
  padding: 18px 20px;
  display: grid;
  gap: 8px;
}

.call-question-card span {
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 900;
}

.call-question-card strong {
  color: var(--primary-text);
  font-size: 16px;
  line-height: 1.8;
}

.call-footer {
  display: grid;
  gap: 10px;
}

.call-live-card {
  margin-bottom: 2px;
}

.call-input-bar {
  padding: 0;
}

.loader {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 3px solid rgba(255, 90, 42, 0.22);
  border-top-color: var(--accent);
  animation: spin 0.8s linear infinite;
}

.report-page {
  min-height: 0;
  overflow: auto;
  padding: 22px;
  display: grid;
  align-content: start;
  gap: 18px;
}

.report-loading-stage {
  min-height: 100%;
  display: grid;
  place-items: center;
  align-content: center;
  justify-items: center;
  gap: 14px;
  text-align: center;
  color: var(--muted-text);
}

.report-loading-stage strong {
  color: var(--primary-text);
  font-size: 18px;
  font-weight: 900;
}

.report-loading-stage p {
  max-width: 420px;
  line-height: 1.8;
}

.report-loader-orbit {
  width: 98px;
  height: 98px;
  position: relative;
}

.report-loader-orbit span {
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.report-loader-orbit span:nth-child(2) {
  inset: 10px;
  border-top-color: #416ee6;
  animation-duration: 1.3s;
}

.report-loader-orbit span:nth-child(3) {
  inset: 22px;
  border-top-color: #26b96d;
  animation-duration: 1.6s;
}

.report-hero {
  display: grid;
  grid-template-columns: minmax(280px, 0.9fr) minmax(0, 1.1fr);
  gap: 16px;
}

.report-score-panel {
  padding: 24px;
  display: grid;
  gap: 8px;
  border-radius: 18px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5a2a 0%, #ff7a45 56%, #ff9c6b 100%);
  box-shadow: var(--report-hero-shadow);
}

.report-score-panel span {
  font-size: 12px;
  opacity: 0.82;
}

.report-score-panel strong {
  font-size: 58px;
  line-height: 1;
  font-weight: 900;
}

.report-score-panel p {
  max-width: 420px;
  line-height: 1.8;
  opacity: 0.96;
}

.report-overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.report-overview-grid article,
.report-meta-row div,
.report-section-card,
.review-card {
  border: 1px solid var(--report-card-border);
  background: var(--report-card-bg);
  box-shadow: var(--report-shadow);
}

.report-overview-grid article {
  min-height: 122px;
  padding: 20px;
  display: grid;
  align-content: space-between;
  border-radius: 16px;
}

.report-overview-grid span,
.report-meta-row span,
.section-head p,
.review-order,
.review-meta span,
.review-answer span,
.keyword-row span {
  color: var(--muted-text);
  font-size: 12px;
}

.report-overview-grid strong {
  color: var(--primary-text);
  font-size: 28px;
  font-weight: 900;
}

.report-meta-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.report-meta-row div {
  padding: 16px 18px;
  display: grid;
  gap: 8px;
  border-radius: 14px;
  position: relative;
}

.report-meta-row div::after,
.report-section-card::after,
.review-card::after {
  content: "";
  position: absolute;
  left: 18px;
  right: 18px;
  top: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, var(--report-divider) 18%, var(--report-divider) 82%, transparent 100%);
}

.report-meta-row strong {
  color: var(--primary-text);
  font-size: 14px;
  font-weight: 800;
}

.expression-report-section {
  padding: 20px;
  display: grid;
  gap: 16px;
  border: 1px solid var(--report-card-border);
  border-radius: 16px;
  background: var(--report-card-bg);
  box-shadow: var(--report-shadow);
}

.expression-report-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.expression-report-grid article {
  min-height: 104px;
  padding: 16px;
  display: grid;
  align-content: space-between;
  gap: 8px;
  border: 1px solid var(--report-card-border);
  border-radius: 12px;
  background: color-mix(in srgb, var(--report-muted-bg) 76%, var(--panel-bg) 24%);
}

.expression-report-grid span,
.expression-empty p {
  color: var(--muted-text);
  font-size: 12px;
}

.expression-report-grid strong {
  color: var(--primary-text);
  font-size: 22px;
  font-weight: 900;
}

.expression-report-grid p {
  color: var(--report-text);
  font-size: 13px;
}

.expression-empty {
  padding: 16px;
  display: grid;
  gap: 6px;
  border: 1px dashed var(--report-card-border);
  border-radius: 12px;
  background: color-mix(in srgb, var(--report-muted-bg) 72%, transparent 28%);
}

.expression-empty strong {
  color: var(--primary-text);
  font-size: 14px;
  font-weight: 900;
}

.expression-empty p {
  line-height: 1.7;
}

.growth-join-card {
  padding: 18px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  border: 1px solid var(--report-card-border);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 90, 42, 0.1), rgba(65, 110, 230, 0.08));
  box-shadow: var(--report-shadow);
}

.growth-join-card span {
  color: var(--accent);
  font-size: 12px;
  font-weight: 900;
}

.growth-join-card h3 {
  margin-top: 6px;
  color: var(--primary-text);
  font-size: 17px;
  font-weight: 900;
}

.growth-join-card p {
  margin-top: 6px;
  color: var(--muted-text);
  line-height: 1.7;
  font-size: 13px;
}

.growth-join-card button {
  flex: none;
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  background: var(--accent);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.growth-join-card button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.growth-dialog-body {
  display: grid;
  gap: 12px;
}

.growth-dialog-body strong {
  color: #252936;
  font-size: 16px;
  font-weight: 900;
}

.growth-dialog-body p {
  color: #647084;
  line-height: 1.8;
}

.growth-dialog-body label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #647084;
  font-size: 13px;
  font-weight: 800;
}

.dimension-panel {
  display: grid;
  grid-template-columns: minmax(300px, 0.94fr) minmax(0, 1.06fr);
  gap: 14px;
}

.radar-card,
.scoring-model-card {
  border: 1px solid var(--report-card-border);
  background: var(--report-card-bg);
  box-shadow: var(--report-shadow);
}

.radar-card {
  min-height: 360px;
  padding: 18px;
  display: grid;
  grid-template-rows: auto minmax(260px, 1fr);
  gap: 12px;
  border-radius: 16px;
}

.radar-chart {
  width: 100%;
  min-height: 282px;
}

.scoring-model-card {
  min-height: 360px;
  padding: 18px;
  display: grid;
  grid-template-rows: auto minmax(252px, 1fr) auto;
  gap: 14px;
  border-radius: 16px;
  overflow: hidden;
}

.scoring-model-chart {
  min-height: 252px;
}

.scoring-model-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.scoring-model-legend span {
  padding: 6px 8px;
  border: 1px solid var(--report-divider);
  border-radius: 8px;
  background: var(--report-muted-bg);
  color: var(--muted-text);
  font-size: 12px;
  font-weight: 800;
}

.report-section-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.report-section-card {
  padding: 18px;
  border-radius: 16px;
  position: relative;
}

.report-section-card h3,
.section-head h3,
.review-top h4,
.review-columns h5 {
  color: var(--primary-text);
  font-weight: 900;
}

.report-section-card h3,
.section-head h3 {
  font-size: 16px;
}

.report-section-card ul,
.review-columns ul {
  margin: 12px 0 0;
  padding-left: 18px;
  color: #5d6676;
  line-height: 1.8;
}

.question-review-section {
  display: grid;
  gap: 14px;
}

.section-head {
  display: grid;
  gap: 6px;
}

.review-card {
  padding: 20px;
  display: grid;
  gap: 16px;
  border-radius: 16px;
  position: relative;
}

.review-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.review-top h4 {
  margin-top: 6px;
  font-size: 17px;
  line-height: 1.6;
}

.review-score {
  min-width: 92px;
  padding: 12px;
  display: grid;
  justify-items: center;
  gap: 4px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--accent-soft) 78%, var(--panel-bg) 22%);
  border: 1px solid var(--report-card-border);
}

.review-score strong {
  color: var(--accent);
  font-size: 32px;
  line-height: 1;
  font-weight: 900;
}

.review-score span {
  color: var(--muted-text);
  font-size: 12px;
}

.review-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.review-expression {
  width: fit-content;
  max-width: 100%;
  padding: 8px 10px;
  border: 1px solid rgba(255, 90, 42, 0.18);
  border-radius: 9px;
  color: var(--accent);
  background: var(--accent-soft);
  font-size: 12px;
  line-height: 1.65;
  font-weight: 900;
}

.review-expression.disabled {
  color: var(--muted-text);
  background: var(--report-muted-bg);
  border-color: var(--report-card-border);
  font-weight: 700;
}

.review-answer {
  display: grid;
  gap: 8px;
}

.review-answer p,
.review-summary {
  color: var(--report-text);
  line-height: 1.8;
  white-space: pre-wrap;
}

.review-dimension-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-dimension-row span,
.review-job-dimension-row span {
  min-height: 28px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  color: var(--muted-text);
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
  font-size: 12px;
  font-weight: 900;
}

.review-dimension-row span.strong,
.review-job-dimension-row span.strong {
  color: #168052;
  background: rgba(36, 168, 101, 0.1);
}

.review-dimension-row span.weak,
.review-job-dimension-row span.weak {
  color: #d3462f;
  background: rgba(211, 70, 47, 0.1);
}

.review-job-dimension-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--report-card-border);
  border-radius: 14px;
  background: color-mix(in srgb, var(--report-muted-bg) 78%, var(--panel-bg) 22%);
}

.review-job-dimension-row > strong {
  margin-right: 4px;
  color: var(--primary-text);
  font-size: 13px;
  font-weight: 900;
}

.review-columns {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.review-columns section {
  min-width: 0;
  padding: 14px;
  border-radius: 14px;
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
}

.keyword-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.keyword-row div {
  padding: 14px;
  border-radius: 14px;
  background: var(--report-muted-bg);
  border: 1px solid var(--report-card-border);
}

.keyword-row p {
  margin-top: 8px;
  color: var(--report-text);
  line-height: 1.8;
}

.report-empty {
  min-height: 320px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  text-align: center;
  border: 1px solid var(--report-card-border);
  border-radius: 18px;
  color: var(--muted-text);
  background: var(--report-card-bg);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.45; }
  40% { transform: scale(1); opacity: 1; }
}

@keyframes voiceWave {
  0%, 100% {
    height: 8px;
    opacity: 0.45;
  }
  50% {
    height: 24px;
    opacity: 1;
  }
}

@keyframes orbitPulse {
  0% {
    transform: scale(0.9);
    opacity: 0.84;
  }
  100% {
    transform: scale(1.08);
    opacity: 0;
  }
}

.call-stage-enter-active,
.call-stage-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.call-stage-enter-from,
.call-stage-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

.call-view-enter-active,
.call-view-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.call-view-enter-from,
.call-view-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 900px) {
  .session-page {
    height: auto;
    min-height: 100vh;
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .interviewer-panel {
    padding: 36px 22px;
  }

  .dialog-panel {
    min-height: 680px;
  }

  .dialog-head {
    height: auto;
    padding: 14px 18px;
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .head-actions {
    width: 100%;
    justify-content: space-between;
  }

  .content-tabs,
  .report-page {
    padding-left: 18px;
    padding-right: 18px;
  }

  .call-view {
    padding: 14px;
    grid-template-rows: auto minmax(0, auto) auto auto;
  }

  .call-view-head,
  .call-view-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .call-view-stage {
    grid-template-columns: 1fr;
  }

  .report-hero,
  .report-meta-row,
  .growth-join-card,
  .expression-report-grid,
  .dimension-panel,
  .scoring-model-legend,
  .report-section-grid,
  .review-columns,
  .keyword-row {
    grid-template-columns: 1fr;
  }

  .review-top {
    flex-direction: column;
  }

  .voice-draft-row {
    grid-template-columns: 1fr;
  }

  .voice-finish-btn {
    min-height: 40px;
  }
}
</style>
