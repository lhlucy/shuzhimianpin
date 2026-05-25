<template>
  <div class="questions-page">
    <section class="summary-strip">
      <div class="summary-copy">
        <p class="section-kicker">Question Management</p>
        <h2>题库列表</h2>
        <span>点击题库直接进入对应题目列表，当前页只保留题库层级的维护与检索。</span>
      </div>
      <div class="summary-side">
        <strong>{{ total }}</strong>
        <span>题库总数</span>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h3>题库列表</h3>
          <p>参考卡片式服务列表布局，保留后台管理的操作密度。</p>
        </div>

        <div class="toolbar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索题库名称"
            clearable
            @input="handleFilterQuestions"
          >
            <template #suffix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" plain @click="handleAddQuestion">
            <el-icon><Plus /></el-icon>
            新建题库
          </el-button>
        </div>
      </div>

      <div class="filter-tabs">
        <button
          v-for="item in filterTabs"
          :key="item.key"
          type="button"
          class="filter-tab"
          :class="{ active: activeTab === item.key }"
          @click="activeTab = item.key; handleFilterQuestions()"
        >
          {{ item.label }}
        </button>
      </div>

      <div class="cards-shell">
        <div class="bank-grid">
        <article
          v-for="row in questionBanksData"
          :key="row.id"
          class="bank-card"
          @click="navigateToBankDetail(row.id)"
        >
          <div class="bank-card-head">
            <div class="bank-icon">题</div>
            <el-tag :type="row.status !== false ? 'success' : 'info'" effect="light" size="small">
              {{ row.status !== false ? '已启用' : '已停用' }}
            </el-tag>
          </div>

          <div class="bank-card-body">
            <div class="bank-no">编号 #{{ row.id }}</div>
            <h4>{{ row.name || row.title }}</h4>
            <div class="card-metrics">
              <span>{{ row.questionCount }} 道题</span>
              <span>{{ row.viewCount || 0 }} 浏览</span>
            </div>
          </div>

          <div class="bank-card-footer">
            <span>{{ row.createdAt || '-' }}</span>
            <div class="row-actions" @click.stop>
              <el-button size="small" type="primary" plain @click="handleEditQuestion(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteQuestion(row.id)">删除</el-button>
            </div>
          </div>
        </article>
        </div>
      </div>

      <el-empty v-if="!questionBanksData.length" description="暂无题库数据" />

      <div class="questions-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </section>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="640px">
      <el-form :model="formData" label-width="88px">
        <el-form-item label="题库名称">
          <el-input v-model="formData.name" placeholder="请输入题库名称" />
        </el-form-item>
        <el-form-item label="题库描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入题库描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveQuestion">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminApi, { type QuestionBankRecord } from '@/api/admin'

const router = useRouter()

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新建题库')
const currentEditId = ref<number | null>(null)
const activeTab = ref<'all' | 'active' | 'rich' | 'empty'>('all')
const filterTabs = [
  { key: 'all', label: '全部' },
  { key: 'active', label: '已启用' },
  { key: 'rich', label: '题量较多' },
  { key: 'empty', label: '待补充' }
] as const

interface QuestionBankForm {
  id?: number
  name: string
  description: string
}

const originalQuestionBanksData = ref<QuestionBankRecord[]>([])
const questionBanksData = ref<QuestionBankRecord[]>([])
const formData = ref<QuestionBankForm>({
  name: '',
  description: ''
})

const handleFilterQuestions = () => {
  let filtered = [...originalQuestionBanksData.value]

  if (activeTab.value === 'active') {
    filtered = filtered.filter((q) => q.status !== false)
  } else if (activeTab.value === 'rich') {
    filtered = filtered.filter((q) => (q.questionCount || 0) >= 20)
  } else if (activeTab.value === 'empty') {
    filtered = filtered.filter((q) => (q.questionCount || 0) === 0)
  }

  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.trim().toLowerCase()
    filtered = filtered.filter((q) =>
      (q.name || q.title)?.toLowerCase().includes(keyword)
    )
  }

  questionBanksData.value = filtered
  total.value = filtered.length
}

const handleAddQuestion = () => {
  dialogTitle.value = '新建题库'
  currentEditId.value = null
  formData.value = {
    name: '',
    description: ''
  }
  dialogVisible.value = true
}

const handleEditQuestion = (bank: QuestionBankRecord) => {
  dialogTitle.value = '编辑题库'
  currentEditId.value = bank.id
  formData.value = {
    name: bank.name || bank.title || '',
    description: bank.description || ''
  }
  dialogVisible.value = true
}

const handleSaveQuestion = async () => {
  if (!formData.value.name) {
    ElMessage.error('请输入题库名称')
    return
  }

  try {
    const payload = {
      title: formData.value.name,
      description: formData.value.description
    }

    if (currentEditId.value) {
      await adminApi.updateQuestionBank(currentEditId.value, payload)
      ElMessage.success('题库编辑成功')
    } else {
      await adminApi.createQuestionBank(payload)
      ElMessage.success('题库创建成功')
    }

    dialogVisible.value = false
    await fetchQuestionBanks()
  } catch (error: any) {
    ElMessage.error(error?.message || '保存题库失败，请稍后重试')
  }
}

const handleDeleteQuestion = (id: number) => {
  ElMessageBox.confirm('确定要删除这个题库吗？', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await adminApi.deleteQuestionBank(id)
    ElMessage.success('题库删除成功')
    await fetchQuestionBanks()
  }).catch(() => {
  })
}

const navigateToBankDetail = (bankId: number) => {
  router.push(`/admin/bank/${bankId}`)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchQuestionBanks()
}

const handleCurrentChange = (current: number) => {
  currentPage.value = current
  fetchQuestionBanks()
}

const fetchQuestionBanks = async () => {
  try {
    const data = await adminApi.getQuestionBanks(currentPage.value - 1, pageSize.value)
    total.value = data.total
    originalQuestionBanksData.value = data.records.map((bank: any) => ({
      id: bank.id,
      name: bank.name || bank.title,
      title: bank.title || bank.name,
      questionCount: bank.questionCount || bank.question_count || 0,
      createdAt: bank.createdAt || bank.created_at || '',
      status: true,
      description: bank.description || '',
      viewCount: bank.viewCount || bank.view_count || 0,
      updatedAt: bank.updatedAt || bank.updated_at || ''
    }))
    currentPage.value = data.current + 1
    handleFilterQuestions()
  } catch (error: any) {
    ElMessage.error(error?.message || '获取题库数据失败，请稍后重试')
    originalQuestionBanksData.value = []
    questionBanksData.value = []
    total.value = 0
  }
}

onMounted(fetchQuestionBanks)
</script>

<style scoped>
.questions-page {
  display: grid;
  gap: 12px;
}

.summary-strip,
.panel {
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(30, 80, 162, 0.1);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14px 28px rgba(18, 44, 93, 0.07);
}

.summary-strip {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
}

.section-kicker {
  margin-bottom: 6px;
  color: #1e50a2;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.summary-copy h2 {
  color: #173a74;
  font-size: 19px;
}

.summary-copy span {
  display: block;
  max-width: 560px;
  margin-top: 6px;
  color: #7287a5;
  line-height: 1.6;
  font-size: 12px;
}

.summary-side {
  min-width: 124px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(30, 80, 162, 0.06), rgba(253, 202, 23, 0.16));
  text-align: center;
}

.summary-side strong {
  display: block;
  color: #173a74;
  font-size: 24px;
  font-weight: 900;
}

.summary-side span {
  margin-top: 4px;
  font-size: 11px;
}

.panel-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.panel-header h3 {
  color: #173a74;
  font-size: 17px;
}

.panel-header p {
  margin-top: 6px;
  color: #7386a3;
  font-size: 12px;
}

.toolbar {
  display: flex;
  gap: 10px;
}

.toolbar .el-input {
  width: 260px;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-tab {
  min-width: 84px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid #e4e9f2;
  border-radius: 999px;
  background: #f7f9fc;
  color: #6d7f9d;
  font-size: 13px;
  cursor: pointer;
}

.filter-tab.active {
  color: #2f6bff;
  background: #edf3ff;
  border-color: #cfe0ff;
}

.cards-shell {
  max-height: calc(100vh - 360px);
  overflow: auto;
  padding-right: 4px;
}

.bank-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
}

.bank-card {
  padding: 16px;
  border: 1px solid #e5ebf5;
  border-radius: 12px;
  background: #fff;
  display: grid;
  gap: 16px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.bank-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(47, 107, 255, 0.08);
  border-color: #cfe0ff;
}

.bank-card-head,
.bank-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.bank-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #5f7cff, #6f58ff);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.bank-card-body {
  display: grid;
  gap: 10px;
}

.bank-no {
  color: #7b8ca8;
  font-size: 12px;
  font-weight: 600;
}

.bank-card-body h4 {
  color: #25324a;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
}

.card-metrics {
  display: flex;
  gap: 14px;
  color: #7b8ca8;
  font-size: 12px;
}

.bank-card-footer > span {
  color: #8b98af;
  font-size: 12px;
}

.row-actions {
  display: flex;
  gap: 6px;
}

.questions-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .summary-strip,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .bank-card-footer {
    align-items: flex-start;
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .summary-strip,
  .panel {
    padding: 16px;
  }

  .toolbar {
    width: 100%;
    flex-direction: column;
  }

  .toolbar .el-input {
    width: 100%;
  }

  .bank-grid {
    grid-template-columns: 1fr;
  }

  .cards-shell {
    max-height: none;
    overflow: visible;
  }
}
</style>
