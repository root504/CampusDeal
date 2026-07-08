<template>
  <div>
    <button @click="$router.push('/admin/basic-config')" class="back-btn">
      ← 返回
    </button>

    <el-card class="mb-6" shadow="hover">
      <template #header>
        <h3 class="section-heading">订单规则</h3>
      </template>
      <div v-if="orderRules.length > 0">
        <div class="rule-card-grid">
          <div v-for="rule in paginatedOrderRules" :key="rule.id" class="rule-card rule-card-order">
            <div class="rule-card-body">
              <div class="rule-card-badge">
                <span class="rule-badge bg-blue-100 text-blue-700">
                  {{ rule.autoCancelMinutes > 0 ? '取消订单超时' : '自定义' }}
                </span>
              </div>
              <p class="rule-card-desc">{{ rule.description }}</p>
              <p v-if="rule.autoCancelMinutes > 0" class="rule-card-extra">
                超过 {{ rule.autoCancelMinutes }} 分钟未支付则取消订单
              </p>
              <div class="rule-card-status">
                <span :class="rule.applied ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'" class="rule-badge">
                  {{ rule.applied ? '已应用' : '未应用' }}
                </span>
              </div>
            </div>
            <div class="rule-card-footer">
              <button @click="openCreateEditDialog('order', rule)" class="btn-link text-blue-600">编辑</button>
              <button v-if="!rule.applied" @click="applyOrderRule(rule.id)" class="btn-link text-green-600">应用</button>
              <button v-else @click="unapplyOrderRule(rule.id)" class="btn-link text-orange-600">取消</button>
              <button @click="deleteOrderRule(rule.id)" class="btn-link text-red-500">删除</button>
            </div>
          </div>
        </div>
        <div class="pagination-bar">
          <el-pagination v-model:current-page="orderRulePage" :page-size="4" :total="orderRules.length" layout="prev, pager, next" small background />
        </div>
        <div class="insert-bar">
          <el-button type="primary" plain size="small" @click="openCreateEditDialog('order', null)">+ 插入</el-button>
        </div>
      </div>
      <div v-else class="empty-state">
        <p class="mb-3">暂无规则</p>
        <el-button type="primary" plain size="small" @click="openCreateEditDialog('order', null)">+ 插入</el-button>
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
        <el-select v-model="editDialog.data._orderCategory" class="w-full" @change="onOrderCategoryChange">
          <el-option label="取消订单（超过X分钟未支付则取消订单）" value="cancel_timeout" />
          <el-option label="自定义输入" value="custom" />
        </el-select>
        <div v-if="editDialog.data._orderCategory === 'cancel_timeout'" class="dialog-block">
          <label class="form-label">超过多少分钟未支付取消订单</label>
          <el-input-number v-model="editDialog.data.autoCancelMinutes" :min="1" class="w-full" />
        </div>
        <div v-if="editDialog.data._orderCategory === 'custom'" class="dialog-block">
          <label class="form-label">自定义规则内容</label>
          <el-input v-model="editDialog.data._customText" type="textarea" :rows="3" placeholder="请输入自定义规则内容" />
        </div>
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
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

// ===== 订单规则 =====
const orderRules = ref([])
const orderRulePage = ref(1)
const paginatedOrderRules = computed(() => {
  const start = (orderRulePage.value - 1) * 4
  return orderRules.value.slice(start, start + 4)
})

const editDialog = ref({
  visible: false, title: '', type: 'order', data: {}, isCreate: false
})

onMounted(async () => {
  await loadOrderRules()
})

function openCreateEditDialog(type, existingData) {
  if (existingData) {
    const isCancelTimeout = (existingData.autoCancelMinutes && existingData.autoCancelMinutes > 0)
    editDialog.value = {
      visible: true, title: '编辑订单规则', type: 'order',
      data: {
        ...existingData,
        _orderCategory: isCancelTimeout ? 'cancel_timeout' : 'custom',
        _customText: isCancelTimeout ? '' : (existingData.description || '')
      }, isCreate: false
    }
  } else {
    editDialog.value = {
      visible: true, title: '创建订单规则', type: 'order',
      data: { description: '', autoCancelMinutes: 30, _orderCategory: 'cancel_timeout', _customText: '' }, isCreate: true
    }
  }
}

function onOrderCategoryChange(val) {
  if (val === 'cancel_timeout') {
    editDialog.value.data.autoCancelMinutes = 30
  } else {
    editDialog.value.data.autoCancelMinutes = 0
    editDialog.value.data._customText = ''
  }
}

async function loadOrderRules() {
  try {
    const res = await adminApi.getOrderRules()
    if (res.data?.code === 200) { orderRules.value = res.data.data || [] }
  } catch { /* ignore */ }
}

async function applyOrderRule(id) {
  try { await adminApi.applyOrderRule(id); ElMessage.success('规则已应用'); await loadOrderRules() }
  catch { ElMessage.error('应用失败') }
}

async function unapplyOrderRule(id) {
  try { await adminApi.unapplyOrderRule(id); ElMessage.success('已取消应用'); await loadOrderRules() }
  catch { ElMessage.error('操作失败') }
}

async function deleteOrderRule(id) {
  try { await adminApi.deleteOrderRule(id); ElMessage.success('规则已删除'); await loadOrderRules() }
  catch { ElMessage.error('删除失败') }
}

async function saveEdit() {
  const { data, isCreate } = editDialog.value
  try {
    if (!data.description || !data.description.trim()) { ElMessage.warning('请输入规则描述'); return }
    let payload
    if (data._orderCategory === 'cancel_timeout') {
      if (!data.autoCancelMinutes || data.autoCancelMinutes < 1) { ElMessage.warning('请输入有效分钟数'); return }
      payload = { description: data.description.trim(), category: 'cancel_order', autoCancelMinutes: data.autoCancelMinutes }
    } else {
      const customText = (data._customText || data.description || '').trim()
      if (!customText) { ElMessage.warning('请输入自定义规则内容'); return }
      payload = { description: customText, category: 'cancel_order', autoCancelMinutes: 0 }
    }
    if (isCreate) { await adminApi.createOrderRule(payload); ElMessage.success('规则已创建') }
    else { await adminApi.updateOrderRule(data.id, payload); ElMessage.success('保存成功') }
    await loadOrderRules()
    editDialog.value.visible = false
  } catch { ElMessage.error(isCreate ? '创建失败' : '保存失败') }
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
.rule-card-order { border-top: 4px solid #3b82f6; }
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
.rule-card-desc {
  font-size: 14px;
  color: #374151;
  line-height: 1.5;
  word-break: break-word;
}
.rule-card-extra {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
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
.bg-blue-100 { background: #dbeafe; }
.bg-green-100 { background: #dcfce7; }
.bg-gray-100 { background: #f3f4f6; }
.text-blue-700 { color: #1d4ed8; }
.text-green-700 { color: #15803d; }
.text-gray-500 { color: #6b7280; }
.text-blue-600 { color: #2563eb; }
.text-green-600 { color: #16a34a; }
.text-orange-600 { color: #ea580c; }
.text-red-500 { color: #ef4444; }
.mb-3 { margin-bottom: 12px; }
.mb-6 { margin-bottom: 24px; }
.dialog-form { display: flex; flex-direction: column; gap: 4px; }
.form-label {
  display: block;
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 4px;
  margin-top: 12px;
}
.form-label:first-child { margin-top: 0; }
.dialog-block { margin-top: 4px; }
.w-full { width: 100%; }
:deep(.el-select) { width: 100%; }
</style>
