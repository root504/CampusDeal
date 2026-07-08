<template>
  <div>
    <button @click="$router.push('/admin/basic-config')" class="back-btn">
      ← 返回
    </button>

    <!-- 信誉分规则 -->
    <el-card class="mb-6" shadow="hover">
      <template #header>
        <h3 class="section-heading">信誉分规则</h3>
      </template>
      <div v-if="creditRules.length > 0">
        <div class="rule-card-grid">
          <div v-for="rule in paginatedCreditRules" :key="rule.id" class="rule-card" :class="rule.category === 'bonus' ? 'rule-card-bonus' : 'rule-card-penalty'">
            <div class="rule-card-body">
              <div class="rule-card-badge">
                <span :class="rule.category === 'bonus' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-500'" class="rule-badge">
                  {{ rule.category === 'bonus' ? '加分' : '减分' }}
                </span>
                <span class="rule-score" :class="rule.category === 'bonus' ? 'text-green-600' : 'text-red-500'">
                  {{ rule.category === 'bonus' ? '+' : '-' }}{{ rule.score }}
                </span>
              </div>
              <p class="rule-card-desc">{{ rule.description }}</p>
              <div class="rule-card-status">
                <span :class="rule.applied ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'" class="rule-badge">
                  {{ rule.applied ? '已应用' : '未应用' }}
                </span>
              </div>
            </div>
            <div class="rule-card-footer">
              <button @click="openCreateEditDialog('credit', rule)" class="btn-link text-blue-600">编辑</button>
              <button v-if="!rule.applied" @click="applyCreditRule(rule.id)" class="btn-link text-green-600">应用</button>
              <button v-else @click="unapplyCreditRule(rule.id)" class="btn-link text-orange-600">取消</button>
              <button @click="deleteCreditRule(rule.id)" class="btn-link text-red-500">删除</button>
            </div>
          </div>
        </div>
        <div class="pagination-bar">
          <el-pagination v-model:current-page="creditRulePage" :page-size="4" :total="creditRules.length" layout="prev, pager, next" small background />
        </div>
        <div class="insert-bar">
          <el-button type="primary" plain size="small" @click="openCreateEditDialog('credit', null)">+ 插入</el-button>
        </div>
      </div>
      <div v-else class="empty-state">
        <p class="mb-3">暂无规则</p>
        <el-button type="primary" plain size="small" @click="openCreateEditDialog('credit', null)">+ 插入</el-button>
      </div>
    </el-card>

    <!-- 信誉分用户列表 -->
    <el-card class="mb-6" shadow="hover">
      <template #header>
        <h3 class="section-heading">信誉分用户</h3>
      </template>
      <table class="credit-table">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th>用户名</th>
            <th class="col-score">信誉分</th>
            <th class="col-time">更新时间</th>
            <th class="col-op">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in userList" :key="user.userId" class="table-row">
            <td class="text-gray-500">{{ user.userId }}</td>
            <td class="font-medium text-gray-900">{{ user.username }}</td>
            <td>
              <span class="font-bold" :class="user.currentScore >= 60 ? 'text-green-600' : 'text-red-500'">{{ user.currentScore }}</span>
            </td>
            <td class="text-gray-500">{{ formatDateTime(user.updatedAt) }}</td>
            <td>
              <button @click="goCreditRecords(user)" class="btn-link text-blue-600">明细</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="userList.length === 0" class="empty-state">暂无数据</div>
      <div v-if="creditTotal > 0" class="pagination-bar">
        <span class="total-info">共 {{ creditTotal }} 条</span>
        <div class="pagination-btns">
          <button @click="creditPrevPage" :disabled="creditPage <= 1" class="page-btn" :class="{ disabled: creditPage <= 1 }">上一页</button>
          <span class="page-info">{{ creditPage }} / {{ creditTotalPages }}</span>
          <button @click="creditNextPage" :disabled="creditPage >= creditTotalPages" class="page-btn" :class="{ disabled: creditPage >= creditTotalPages }">下一页</button>
        </div>
      </div>
    </el-card>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="editDialog.visible"
      :title="editDialog.title"
      width="480px"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <div class="dialog-form">
        <label class="form-label">规则描述</label>
        <el-input v-model="editDialog.data.description" placeholder="请输入规则描述" />
        <label class="form-label">分类</label>
        <el-select v-model="editDialog.data.category" class="w-full">
          <el-option label="加分" value="bonus" />
          <el-option label="减分" value="penalty" />
        </el-select>
        <label class="form-label">分值</label>
        <el-input-number v-model="editDialog.data.score" :min="1" class="w-full" />
      </div>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()

// ===== 信誉分用户 =====
const userList = ref([])
const creditPage = ref(1)
const creditSize = ref(3)
const creditTotal = ref(0)
const creditTotalPages = computed(() => Math.ceil(creditTotal.value / creditSize.value) || 1)

// ===== 信誉分规则 =====
const creditRules = ref([])
const creditRulePage = ref(1)
const paginatedCreditRules = computed(() => {
  const start = (creditRulePage.value - 1) * 4
  return creditRules.value.slice(start, start + 4)
})

// ===== 创建/编辑弹窗 =====
const editDialog = ref({
  visible: false, title: '', type: 'credit', data: {}, isCreate: false
})

onMounted(async () => {
  await Promise.all([loadCreditList(), loadCreditRules()])
})

// ==================== 弹窗打开 ====================
function openCreateEditDialog(type, existingData) {
  if (existingData) {
    editDialog.value = {
      visible: true, title: '编辑信誉分规则', type: 'credit',
      data: { ...existingData }, isCreate: false
    }
  } else {
    editDialog.value = {
      visible: true, title: '创建信誉分规则', type: 'credit',
      data: { description: '', category: 'bonus', score: 5 }, isCreate: true
    }
  }
}

// ==================== 数据加载 ====================
async function loadCreditList() {
  try {
    const res = await adminApi.getCreditList({ page: creditPage.value, size: creditSize.value })
    const data = res.data?.data
    if (data) {
      userList.value = data.list || data.records || []
      creditTotal.value = data.total || 0
    }
  } catch { /* ignore */ }
}

async function loadCreditRules() {
  try {
    const res = await adminApi.getCreditRules()
    if (res.data?.code === 200) { creditRules.value = res.data.data || [] }
  } catch { /* ignore */ }
}

// ==================== 信誉分规则操作 ====================
async function applyCreditRule(id) {
  try {
    await adminApi.applyCreditRule(id)
    ElMessage.success('规则已应用')
    await Promise.all([loadCreditRules(), loadCreditList()])
  } catch { ElMessage.error('应用失败') }
}

async function unapplyCreditRule(id) {
  try {
    await adminApi.unapplyCreditRule(id)
    ElMessage.success('已取消应用')
    await loadCreditRules()
  } catch { ElMessage.error('操作失败') }
}

async function deleteCreditRule(id) {
  try {
    await adminApi.deleteCreditRule(id)
    ElMessage.success('规则已删除')
    await loadCreditRules()
  } catch { ElMessage.error('删除失败') }
}

// ==================== 信誉分用户操作 ====================
function creditPrevPage() { if (creditPage.value > 1) { creditPage.value--; loadCreditList() } }
function creditNextPage() { if (creditPage.value < creditTotalPages.value) { creditPage.value++; loadCreditList() } }

function goCreditRecords(user) {
  router.push({
    path: '/admin/credit-records',
    query: { userId: user.userId, username: user.username, currentScore: user.currentScore }
  })
}

// ==================== 保存 ====================
async function saveEdit() {
  const { data, isCreate } = editDialog.value
  try {
    if (!data.description || !data.description.trim()) { ElMessage.warning('请输入规则描述'); return }
    if (!data.score || data.score < 1) { ElMessage.warning('请输入有效分值'); return }
    if (isCreate) {
      await adminApi.createCreditRule({ description: data.description.trim(), category: data.category, score: data.score })
      ElMessage.success('规则已创建')
    } else {
      await adminApi.updateCreditRule(data.id, { description: data.description.trim(), category: data.category, score: data.score })
      ElMessage.success('保存成功')
    }
    await loadCreditRules()
    editDialog.value.visible = false
  } catch { ElMessage.error(isCreate ? '创建失败' : '保存失败') }
}

function formatDateTime(date) {
  if (!date) return ''
  const d = new Date(date)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  margin-bottom: 16px;
  transition: all 0.15s;
}
.back-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.section-heading {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}
.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
  font-size: 14px;
}
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}
.insert-bar {
  text-align: center;
  margin-top: 12px;
}
.btn-link {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 0;
  font-size: 13px;
  font-weight: 500;
}
.btn-link:hover { text-decoration: underline; }
.total-info {
  font-size: 13px;
  color: #6b7280;
  margin-right: 16px;
}
.pagination-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-btn {
  padding: 4px 12px;
  font-size: 13px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  transition: background 0.15s;
  color: #374151;
}
.page-btn:hover:not(.disabled) { background: #f3f4f6; }
.page-btn.disabled { opacity: 0.4; cursor: not-allowed; }
.page-info {
  font-size: 13px;
  color: #374151;
  min-width: 60px;
  text-align: center;
}
.rule-card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1200px) { .rule-card-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 900px)  { .rule-card-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px)  { .rule-card-grid { grid-template-columns: 1fr; } }
.rule-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.rule-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.rule-card-bonus { border-top: 4px solid #22c55e; }
.rule-card-penalty { border-top: 4px solid #ef4444; }
.rule-card-body {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rule-card-badge { display: flex; align-items: center; }
.rule-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}
.rule-score {
  font-size: 18px;
  font-weight: 700;
  margin-left: 8px;
}
.rule-card-desc {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  word-break: break-word;
}
.rule-card-status { margin-top: auto; padding-top: 8px; }
.rule-card-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14px;
  padding: 10px 16px;
  border-top: 1px solid #f3f4f6;
  background: #fafafa;
}
.credit-table {
  width: 100%;
  font-size: 14px;
  border-collapse: collapse;
}
.credit-table thead th {
  background: #f9fafb;
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #4b5563;
  font-size: 13px;
  border-bottom: 1px solid #e5e7eb;
}
.credit-table tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
}
.table-row:hover { background: #f9fafb; }
.col-id { width: 60px; }
.col-score { width: 100px; }
.col-time { width: 160px; }
.col-op { width: 60px; }
.text-gray-500 { color: #6b7280; }
.text-green-600 { color: #16a34a; }
.text-red-500 { color: #ef4444; }
.text-blue-600 { color: #2563eb; }
.text-green-600 { color: #16a34a; }
.text-orange-600 { color: #ea580c; }
.font-bold { font-weight: 700; }
.font-medium { font-weight: 500; }
.text-gray-900 { color: #111827; }
.mb-3 { margin-bottom: 12px; }
.mb-6 { margin-bottom: 24px; }
.bg-green-100 { background: #dcfce7; }
.bg-red-100 { background: #fee2e2; }
.bg-gray-100 { background: #f3f4f6; }
.text-green-700 { color: #15803d; }
.text-red-500 { color: #ef4444; }
.text-gray-500 { color: #6b7280; }
.dialog-form { display: flex; flex-direction: column; gap: 4px; }
.form-label {
  display: block;
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 4px;
  margin-top: 12px;
}
.form-label:first-child { margin-top: 0; }
.w-full { width: 100%; }
:deep(.el-select) { width: 100%; }
</style>
