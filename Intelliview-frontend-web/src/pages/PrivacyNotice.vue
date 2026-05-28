<template>
  <main class="policy-page">
    <section class="policy-hero">
      <nav class="hero-nav" aria-label="协议页导航">
        <router-link to="/user" class="brand">
          <span class="brand-mark">数</span>
          <strong>数智面聘</strong>
        </router-link>
        <div class="hero-actions">
          <router-link to="/login">登录</router-link>
          <router-link to="/user">返回首页</router-link>
        </div>
      </nav>

      <div class="hero-grid">
        <div class="hero-copy">
          <p class="eyebrow">数智面聘 · 协议中心</p>
          <h1>协议与个人信息保护中心</h1>
          <p class="hero-desc">
            围绕数智面聘的 AI 模拟面试、岗位刷题、简历解析和成长报告场景，集中展示隐私政策、用户协议、AI 服务说明、第三方共享清单与账号注销规则。
          </p>
          <div class="hero-meta">
            <span>生效日期：2026 年 5 月 26 日</span>
            <span>适用对象：注册用户、管理员、访客</span>
          </div>
        </div>

        <aside class="summary-panel" aria-label="隐私重点摘要">
          <div>
            <span>最小必要</span>
            <strong>仅围绕训练、评估、安全风控处理数据</strong>
          </div>
          <div>
            <span>敏感场景</span>
            <strong>简历、语音、视频、AI 评估结果单独说明</strong>
          </div>
          <div>
            <span>用户权利</span>
            <strong>支持访问、更正、删除、注销与撤回授权</strong>
          </div>
        </aside>
      </div>
    </section>

    <section class="policy-shell">
      <aside class="policy-sidebar">
        <p>协议类型</p>
        <button
          v-for="agreement in agreements"
          :key="agreement.key"
          type="button"
          :class="{ active: activeAgreement === agreement.key }"
          @click="setAgreement(agreement.key)"
        >
          <el-icon><component :is="agreement.icon" /></el-icon>
          <span>{{ agreement.name }}</span>
        </button>
      </aside>

      <article class="policy-document">
        <header class="document-header">
          <div>
            <p>{{ currentAgreement.label }}</p>
            <h2>{{ currentAgreement.name }}</h2>
          </div>
          <span>{{ currentAgreement.status }}</span>
        </header>

        <section class="quick-read">
          <h3>阅读摘要</h3>
          <div class="quick-grid">
            <div v-for="item in currentAgreement.highlights" :key="item.title">
              <span>{{ item.title }}</span>
              <strong>{{ item.text }}</strong>
            </div>
          </div>
        </section>

        <section v-if="activeAgreement === 'privacy'" class="data-map">
          <h3>本项目涉及的个人信息类型</h3>
          <div class="data-table" role="table" aria-label="个人信息处理说明">
            <div class="table-row table-head" role="row">
              <span role="columnheader">场景</span>
              <span role="columnheader">可能处理的信息</span>
              <span role="columnheader">使用目的</span>
            </div>
            <div v-for="row in dataRows" :key="row.scene" class="table-row" role="row">
              <span role="cell">{{ row.scene }}</span>
              <span role="cell">{{ row.data }}</span>
              <span role="cell">{{ row.purpose }}</span>
            </div>
          </div>
        </section>

        <section class="clause-list">
          <div v-for="section in currentAgreement.sections" :key="section.title" :id="section.id" class="clause">
            <div class="clause-heading">
              <el-icon><component :is="section.icon" /></el-icon>
              <h3>{{ section.title }}</h3>
            </div>
            <ul>
              <li v-for="item in section.items" :key="item">{{ item }}</li>
            </ul>
          </div>
        </section>
      </article>

      <aside class="right-rail">
        <section>
          <h3>目录</h3>
          <a v-for="section in currentAgreement.sections" :key="section.id" :href="`#${section.id}`">
            {{ section.title.replace(/^.+?、/, '') }}
          </a>
        </section>

        <section>
          <h3>推荐适配</h3>
          <p>除隐私政策外，项目建议同步保留用户协议、AI 服务说明、第三方 SDK 与共享清单、账号注销规则。若后续上线移动端，还应补充权限申请说明。</p>
        </section>
      </aside>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  ChatLineRound,
  Connection,
  Document,
  Files,
  Key,
  Link,
  Lock,
  Microphone,
  Service,
  SwitchButton,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'

type AgreementKey = 'privacy' | 'terms' | 'ai' | 'sdk' | 'account'

const activeAgreement = ref<AgreementKey>('privacy')
const route = useRoute()

const dataRows = [
  {
    scene: '账号与登录',
    data: '用户名、邮箱、密码加密摘要、登录时间、登录 IP、登录方式、安全日志',
    purpose: '完成注册登录、身份识别、异常提醒和账号安全保护'
  },
  {
    scene: '岗位刷题',
    data: '目标岗位、题目、答题记录、收藏、练习历史、搜索记录',
    purpose: '提供刷题、复盘、错题回看、题目推荐和学习路径'
  },
  {
    scene: 'AI 面试',
    data: '简历摘要、面试回答、语音转写文本、评分报告、成长趋势',
    purpose: '生成面试题、追问、评估报告和改进建议'
  },
  {
    scene: '简历解析',
    data: '主动上传的简历文件、教育经历、项目经历、技能关键词、求职意向',
    purpose: '辅助岗位匹配、面试题生成和个性化评估'
  },
  {
    scene: '运维风控',
    data: '浏览器类型、访问时间、接口调用日志、异常请求记录',
    purpose: '故障排查、反作弊、限流、安全审计和备份恢复'
  }
]

const agreements = [
  {
    key: 'privacy',
    name: '隐私政策',
    label: 'Privacy Notice',
    status: '建议注册与登录前展示',
    icon: Lock,
    highlights: [
      { title: '收集边界', text: '只处理账号、训练、简历、面试和安全所需信息。' },
      { title: '敏感信息', text: '简历、语音、评估报告按较高风险信息进行提示。' },
      { title: '控制权', text: '用户可访问、更正、删除、撤回授权或注销账号。' }
    ],
    sections: [
      {
        id: 'privacy-collect',
        title: '一、我们收集哪些信息',
        icon: UserFilled,
        items: [
          '账号注册与登录信息：用户名、邮箱、密码加密摘要、头像、昵称、登录时间、登录 IP、登录方式和必要的安全日志。',
          '求职与训练信息：目标岗位、技术栈、练习题目、答题记录、收藏、模拟面试记录、语音转写文本、评分报告、成长趋势和学习计划。',
          '简历与附件信息：你主动上传的简历文件、解析出的教育经历、项目经历、工作经历、技能关键词、意向城市和期望薪资。',
          '设备与网络信息：浏览器类型、访问时间、异常请求、接口调用日志和用于安全风控的基础设备信息。',
          '客服与反馈信息：你通过平台提交的咨询、问题反馈、截图、联系方式和处理记录。'
        ]
      },
      {
        id: 'privacy-use',
        title: '二、我们如何使用这些信息',
        icon: Connection,
        items: [
          '完成账号注册、登录认证、邮箱验证、身份识别、账号安全保护和异常登录提醒。',
          '根据岗位、简历和技术栈生成模拟面试题、动态追问、刷题推荐、评分维度和改进建议。',
          '生成并展示面试报告、能力雷达、训练历史、收藏内容、成长趋势和个人学习计划。',
          '进行平台安全运维、故障排查、反作弊、接口限流、风险识别和必要的数据备份。',
          '在去标识化或匿名化后，用于统计分析、产品体验优化和服务质量评估。'
        ]
      },
      {
        id: 'privacy-third-party',
        title: '三、第三方服务与 SDK',
        icon: Service,
        items: [
          '阿里云百炼或大模型服务用于生成题目、追问、评估文本和学习建议，调用时仅传输完成该功能所必需的上下文。',
          '阿里云语音服务用于语音识别、语音合成和面试语音交互，可能处理你在面试中主动提交的音频或转写文本。',
          '讯飞数字人服务用于数字人面试官会话、播报和互动展示，可能处理会话指令、播报文本和必要的会话状态。',
          '邮件服务用于发送注册、登录、找回密码等验证码，不会用于发送与服务无关的营销内容。',
          '如后续新增第三方 SDK、统计分析、云存储或支付服务，将在上线前更新本政策或在具体功能处单独告知。'
        ]
      },
      {
        id: 'privacy-sensitive',
        title: '四、敏感信息与权限说明',
        icon: Microphone,
        items: [
          '简历、面试回答、语音内容、评估报告、邮箱、登录 IP 等可能属于敏感或较高风险个人信息，请避免上传身份证号、银行卡号、医疗健康、精确定位等与面试训练无关的信息。',
          '麦克风权限仅在你主动进入语音或数字人面试时使用，用于采集回答内容；不使用相关功能时不会主动采集音频。',
          '文件上传权限仅用于你主动上传简历或材料，文件将用于解析、训练和报告生成，不建议上传与求职无关的私人文件。',
          '平台不会索取通讯录、短信、相册批量读取、精确定位等与当前服务无关的权限。'
        ]
      },
      {
        id: 'privacy-security',
        title: '五、我们如何保护信息安全',
        icon: Key,
        items: [
          '登录接口以外的个人数据接口需要身份认证，管理员接口要求管理员权限，关键操作保留必要安全日志。',
          '密码采用加密摘要存储，不以明文保存；JWT、数据库、邮箱和第三方密钥通过环境变量或服务端配置管理。',
          '简历直连访问已收紧，避免未授权公开读取；涉及个人数据的接口按用户身份进行隔离。',
          '我们会采取访问控制、最小权限、异常监控、备份恢复和安全配置检查等措施降低泄露、篡改、丢失风险。'
        ]
      },
      {
        id: 'privacy-rights',
        title: '六、保存期限、删除与账号注销',
        icon: SwitchButton,
        items: [
          '我们仅在实现本政策所述目的所需期间保存个人信息；法律法规另有要求的，按规定保存。',
          '你删除简历后，前端列表不再展示该文件和解析摘要；备份系统中的残留副本将在合理周期内清理或匿名化。',
          '你注销账号后，我们将停止提供产品服务，并删除或匿名化与你账号相关的个人信息，法律法规要求保留的除外。',
          '为保障账号安全，注销、导出、删除等高风险请求可能需要进行身份验证。'
        ]
      },
      {
        id: 'privacy-minors',
        title: '七、未成年人保护',
        icon: Warning,
        items: [
          '本平台主要面向具备求职、实习、校招或职业训练需求的用户。',
          '未满十四周岁的儿童使用本服务前，应由监护人阅读并同意本政策；如我们发现未取得监护人同意处理儿童个人信息，将尽快删除或停止处理。',
          '监护人可联系我们访问、更正、删除儿童个人信息，或撤回授权同意。'
        ]
      },
      {
        id: 'privacy-update',
        title: '八、政策更新与联系方式',
        icon: Document,
        items: [
          '当产品功能、个人信息处理目的、处理方式、第三方服务或用户权利入口发生重大变化时，我们会更新本政策，并通过页面提示、弹窗或站内通知进行告知。',
          '本政策与用户协议、安全说明、第三方 SDK 清单、权限说明、账号注销规则共同构成平台协议文件体系。',
          '如你对个人信息处理有疑问、投诉、撤回同意或行使个人信息权利，可联系平台管理员或通过项目维护方提供的联系方式处理。'
        ]
      }
    ]
  },
  {
    key: 'terms',
    name: '用户协议',
    label: 'Terms of Service',
    status: '建议登录注册勾选同意',
    icon: Files,
    highlights: [
      { title: '账号规则', text: '明确注册、登录、保管账号和禁止转让。' },
      { title: '内容责任', text: '规范简历、答题、评论和题库内容使用边界。' },
      { title: '服务变更', text: '说明功能调整、中断、终止和争议处理规则。' }
    ],
    sections: [
      {
        id: 'terms-account',
        title: '一、账号注册与使用',
        icon: UserFilled,
        items: [
          '用户应提供真实、准确、完整的注册信息，并及时更新邮箱、昵称、求职意向等资料。',
          '账号仅限本人使用，用户应妥善保管登录凭证，不得转让、出租、出借或共享给他人。',
          '因用户主动泄露密码、验证码或在不安全环境登录造成的损失，由用户自行承担相应责任。'
        ]
      },
      {
        id: 'terms-service',
        title: '二、服务内容与使用限制',
        icon: Service,
        items: [
          '平台提供岗位刷题、AI 模拟面试、简历解析、报告分析、收藏记录和后台管理等功能。',
          '用户不得利用平台发布违法违规、侵权、作弊、恶意爬取、干扰系统或破坏服务稳定性的内容或行为。',
          '平台生成的题目、评分、建议和学习计划仅供训练参考，不构成招聘录用承诺或职业结果保证。'
        ]
      },
      {
        id: 'terms-content',
        title: '三、用户内容与知识产权',
        icon: Document,
        items: [
          '用户对主动上传的简历、回答、评论等内容承担合法性与真实性责任。',
          '平台题库、页面设计、模型提示词、评估维度、报告模板和系统代码受知识产权保护，未经许可不得复制、传播或商业使用。',
          '为提供服务，用户授权平台在必要范围内处理其上传内容，用于解析、面试生成、评分反馈和历史记录展示。'
        ]
      },
      {
        id: 'terms-risk',
        title: '四、免责声明与风险提示',
        icon: Warning,
        items: [
          'AI 生成内容可能存在不完整、不准确或不适合特定岗位的情况，用户应结合自身判断使用。',
          '平台会努力保障服务连续性，但因网络、第三方服务、系统维护、不可抗力等原因可能出现中断或延迟。',
          '用户不得将平台训练结果作为唯一决策依据，涉及就业、薪酬、签约等重大事项应自行核验。'
        ]
      },
      {
        id: 'terms-dispute',
        title: '五、协议变更与争议处理',
        icon: Link,
        items: [
          '平台可根据业务和法律法规变化更新本协议，并以页面提示、站内通知或弹窗等方式告知。',
          '用户继续使用服务即表示接受更新后的协议；不同意更新内容的，可停止使用并申请注销账号。',
          '因本协议产生的争议，双方应优先友好协商解决。'
        ]
      }
    ]
  },
  {
    key: 'ai',
    name: 'AI 服务说明',
    label: 'AI Service Rules',
    status: '建议面试创建页重点提示',
    icon: ChatLineRound,
    highlights: [
      { title: '生成边界', text: 'AI 反馈用于训练参考，不替代专业判断。' },
      { title: '输入安全', text: '避免提交身份证号、银行卡号等无关敏感信息。' },
      { title: '人工复核', text: '重要结论应由用户或管理员复核确认。' }
    ],
    sections: [
      {
        id: 'ai-scope',
        title: '一、AI 功能范围',
        icon: Service,
        items: [
          'AI 功能包括简历解析、题目生成、动态追问、语音转写、评分报告、能力画像和学习建议。',
          'AI 输出基于用户输入、题库资料和模型推理生成，可能受输入质量、模型能力和服务状态影响。',
          '平台可对 AI 生成结果进行安全过滤、格式修正和质量优化。'
        ]
      },
      {
        id: 'ai-input',
        title: '二、用户输入规范',
        icon: Lock,
        items: [
          '请仅上传与求职训练相关的简历、项目经历、技能描述和面试回答。',
          '请勿输入国家秘密、商业秘密、他人隐私、违法内容或与面试训练无关的高度敏感信息。',
          '若用户输入侵权或违法内容，平台可依法删除、限制功能或保留必要证据。'
        ]
      },
      {
        id: 'ai-output',
        title: '三、AI 输出使用提醒',
        icon: Warning,
        items: [
          'AI 评分、岗位匹配和成长建议仅作为练习参考，不代表真实招聘方意见。',
          '用户应自行判断输出内容是否准确、完整和适用于目标岗位。',
          '平台不承诺通过使用 AI 服务一定提升面试结果、获得 offer 或达成特定职业目标。'
        ]
      },
      {
        id: 'ai-third-party',
        title: '四、模型与语音服务调用',
        icon: Connection,
        items: [
          '为完成 AI 面试功能，平台可能调用大模型、语音识别、语音合成和数字人服务。',
          '平台会尽量控制传输内容范围，仅提交完成当前功能所需的文本、音频或会话上下文。',
          '第三方服务变更时，应同步更新第三方共享清单或在功能入口补充告知。'
        ]
      }
    ]
  },
  {
    key: 'sdk',
    name: '第三方清单',
    label: 'Third-party List',
    status: '建议与隐私政策配套维护',
    icon: Link,
    highlights: [
      { title: '透明披露', text: '列明第三方名称、用途、数据类型和退出方式。' },
      { title: '最小共享', text: '仅共享完成验证码、AI、语音等功能所需信息。' },
      { title: '动态更新', text: '新增 SDK、云服务或统计能力时同步更新。' }
    ],
    sections: [
      {
        id: 'sdk-ai',
        title: '一、大模型与 AI 生成服务',
        icon: ChatLineRound,
        items: [
          '用途：生成面试题、追问、评估文本、学习计划和岗位能力建议。',
          '可能共享：岗位名称、技能标签、简历摘要、面试回答、必要上下文和去标识化会话信息。',
          '控制方式：停止使用 AI 面试、删除简历或删除历史记录后，相关展示数据将按规则删除或匿名化。'
        ]
      },
      {
        id: 'sdk-speech',
        title: '二、语音与数字人服务',
        icon: Microphone,
        items: [
          '用途：语音识别、语音合成、数字人播报和模拟面试互动。',
          '可能共享：用户主动提交的音频、转写文本、播报文本、会话标识和必要设备网络信息。',
          '控制方式：用户可不启用麦克风或选择文字面试模式。'
        ]
      },
      {
        id: 'sdk-email',
        title: '三、邮件与基础云服务',
        icon: Service,
        items: [
          '用途：发送验证码、找回密码、账号安全通知、存储简历文件和运行后端服务。',
          '可能共享：邮箱地址、验证码发送状态、文件对象标识、访问日志和必要安全信息。',
          '控制方式：用户可更换邮箱、删除文件或申请注销账号。'
        ]
      },
      {
        id: 'sdk-change',
        title: '四、清单维护规则',
        icon: Document,
        items: [
          '新增第三方服务前，应确认处理目的、数据类型、安全能力和隐私条款。',
          '第三方处理目的、共享数据类型或用户权益方式发生重大变化时，应更新页面并提示用户。',
          '若上线移动端 App，应增加 SDK 包名、运营主体、隐私政策链接、权限调用频率等字段。'
        ]
      }
    ]
  },
  {
    key: 'account',
    name: '账号注销规则',
    label: 'Account Closure',
    status: '建议个人中心提供入口',
    icon: SwitchButton,
    highlights: [
      { title: '前置校验', text: '注销前进行身份验证，避免账号被恶意关闭。' },
      { title: '影响说明', text: '注销后历史记录、简历、收藏和报告不可恢复。' },
      { title: '例外保留', text: '法律法规要求保留的安全日志可按规定保存。' }
    ],
    sections: [
      {
        id: 'account-condition',
        title: '一、注销条件',
        icon: UserFilled,
        items: [
          '账号处于正常状态，不存在未处理的安全风险、争议、违规调查或法律要求保留情形。',
          '用户已了解注销后无法继续访问个人资料、简历、训练历史、收藏、报告和管理员数据。',
          '平台可要求用户通过密码、邮箱验证码或其他合理方式确认本人操作。'
        ]
      },
      {
        id: 'account-data',
        title: '二、注销后的数据处理',
        icon: Lock,
        items: [
          '平台将停止为该账号提供服务，并删除或匿名化与账号相关的个人信息。',
          '已进入备份、审计或安全日志的数据，会在合理周期内清理；法律法规要求留存的除外。',
          '用户注销前可自行导出重要训练记录、报告或简历内容。'
        ]
      },
      {
        id: 'account-recover',
        title: '三、撤回与恢复',
        icon: SwitchButton,
        items: [
          '注销申请提交后，如平台设置冷静期，可在冷静期内撤回申请。',
          '注销完成后账号不可恢复，原邮箱可否再次注册取决于平台账号策略。',
          '若用户误操作或怀疑账号被盗，应尽快联系平台管理员处理。'
        ]
      },
      {
        id: 'account-admin',
        title: '四、管理员账号特别说明',
        icon: Key,
        items: [
          '管理员账号注销前，应先完成权限交接、题库与面试管理数据归属确认。',
          '管理员操作日志可能因安全审计、纠纷处理或系统合规要求被保留。',
          '管理员账号注销不影响平台依法保存的公共题库、统计报表和去标识化运营数据。'
        ]
      }
    ]
  }
] as const

const currentAgreement = computed(() => {
  return agreements.find((agreement) => agreement.key === activeAgreement.value) || agreements[0]
})

const setAgreement = (key: AgreementKey) => {
  activeAgreement.value = key
  window.history.replaceState(null, '', `#${key}`)
}

const syncAgreementFromHash = (hash: string) => {
  const key = hash.replace('#', '') as AgreementKey
  if (agreements.some((agreement) => agreement.key === key)) {
    activeAgreement.value = key
  }
}

onMounted(() => {
  syncAgreementFromHash(window.location.hash)
})

watch(() => route.hash, syncAgreementFromHash)
</script>

<style scoped>
.policy-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 14% 8%, var(--color-primary-soft), transparent 28%),
    radial-gradient(circle at 86% 12%, var(--color-accent-soft), transparent 24%),
    linear-gradient(135deg, rgba(47, 111, 237, 0.08), transparent 32%),
    linear-gradient(180deg, var(--color-bg-panel) 0, var(--color-bg) 420px);
  color: var(--color-text);
}

.policy-hero {
  padding: 28px 32px 48px;
  border-bottom: 1px solid var(--color-border);
}

.hero-nav {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
}

.brand,
.hero-actions a {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
}

.brand {
  gap: 10px;
}

.brand-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 900;
  box-shadow: var(--shadow-soft);
}

.brand strong {
  font-size: 18px;
  font-weight: 900;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.hero-actions a {
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 800;
  background: var(--color-bg-elevated);
}

.hero-grid {
  max-width: 1280px;
  margin: 62px auto 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 42px;
  align-items: end;
}

.hero-copy {
  max-width: 790px;
}

.eyebrow,
.document-header p,
.policy-sidebar p {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin-top: 16px;
  font-size: 42px;
  line-height: 1.16;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-desc {
  margin-top: 18px;
  color: var(--color-text-secondary);
  font-size: 16px;
  line-height: 1.9;
}

.hero-meta {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-meta span,
.quick-grid div,
.summary-panel,
.policy-sidebar,
.policy-document,
.right-rail section {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-bg-elevated);
}

.hero-meta span {
  min-height: 34px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 700;
}

.summary-panel {
  padding: 18px;
  display: grid;
  gap: 12px;
  box-shadow: var(--shadow-card);
}

.summary-panel div {
  padding: 15px;
  border-left: 3px solid var(--color-accent);
  background: var(--color-bg-muted);
  border-radius: 8px;
}

.summary-panel span,
.quick-grid span {
  display: block;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 800;
}

.summary-panel strong,
.quick-grid strong {
  display: block;
  margin-top: 6px;
  color: var(--color-text);
  font-size: 14px;
  line-height: 1.55;
}

.policy-shell {
  width: min(1280px, calc(100% - 48px));
  margin: 0 auto;
  padding: 34px 0 56px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 260px;
  gap: 18px;
  align-items: start;
}

.policy-sidebar,
.right-rail {
  position: sticky;
  top: 18px;
}

.policy-sidebar {
  padding: 14px;
  display: grid;
  gap: 8px;
}

.policy-sidebar p {
  padding: 4px 4px 8px;
}

.policy-sidebar button {
  min-height: 44px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.policy-sidebar button:hover,
.policy-sidebar button.active {
  border-color: var(--color-border-strong);
  background: linear-gradient(135deg, var(--color-primary-soft), var(--color-accent-soft));
  color: var(--color-primary);
}

.policy-sidebar button span {
  font-size: 14px;
  font-weight: 800;
}

.policy-document {
  padding: 24px;
  box-shadow: var(--shadow-card);
}

.document-header {
  padding-bottom: 22px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  border-bottom: 1px solid var(--color-border);
}

.document-header h2 {
  margin-top: 8px;
  font-size: 28px;
  line-height: 1.25;
  font-weight: 900;
}

.document-header > span {
  height: 34px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  border-radius: 8px;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: 13px;
  font-weight: 900;
}

.quick-read,
.data-map,
.clause-list {
  margin-top: 24px;
}

.quick-read h3,
.data-map h3,
.right-rail h3 {
  font-size: 16px;
  font-weight: 900;
}

.quick-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.quick-grid div {
  padding: 14px;
}

.data-table {
  margin-top: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
}

.table-row {
  display: grid;
  grid-template-columns: 0.8fr 1.5fr 1.4fr;
  border-top: 1px solid var(--color-border);
}

.table-row:first-child {
  border-top: 0;
}

.table-row span {
  padding: 13px 14px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.65;
}

.table-row span + span {
  border-left: 1px solid var(--color-border);
}

.table-head {
  background: var(--color-bg-muted);
}

.table-head span {
  color: var(--color-text);
  font-weight: 900;
}

.clause-list {
  display: grid;
  gap: 16px;
}

.clause {
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: color-mix(in srgb, var(--color-bg-panel) 78%, transparent);
}

.clause-heading {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clause-heading .el-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.clause-heading h3 {
  font-size: 18px;
  font-weight: 900;
}

.clause ul {
  margin-top: 12px;
  padding-left: 20px;
  display: grid;
  gap: 8px;
}

.clause li {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.8;
}

.right-rail {
  display: grid;
  gap: 14px;
}

.right-rail section {
  padding: 16px;
}

.right-rail a {
  margin-top: 10px;
  display: block;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
  text-decoration: none;
}

.right-rail a:hover {
  color: var(--color-primary);
}

.right-rail p {
  margin-top: 10px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.8;
}

@media (max-width: 1120px) {
  .hero-grid,
  .policy-shell {
    grid-template-columns: 1fr;
  }

  .summary-panel,
  .policy-sidebar,
  .right-rail {
    position: static;
  }

  .policy-sidebar {
    grid-template-columns: repeat(5, minmax(130px, 1fr));
    overflow-x: auto;
  }

  .policy-sidebar p {
    display: none;
  }

  .right-rail {
    display: none;
  }
}

@media (max-width: 760px) {
  .policy-hero {
    padding: 22px 18px 34px;
  }

  .hero-nav,
  .document-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .hero-grid {
    margin-top: 42px;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .policy-shell {
    width: calc(100% - 28px);
    padding: 22px 0 42px;
  }

  .policy-sidebar {
    grid-template-columns: repeat(5, minmax(124px, 1fr));
  }

  .policy-document {
    padding: 18px;
  }

  .quick-grid,
  .table-row {
    grid-template-columns: 1fr;
  }

  .table-row span + span {
    border-left: 0;
    border-top: 1px solid var(--color-border);
  }
}
</style>
