<template>
  <div>
    <button @click="$router.push('/admin/basic-config')" class="back-btn">
      ← 返回
    </button>

    <el-card class="mb-6" shadow="hover">
      <template #header>
        <h3 class="section-heading">敏感词过滤</h3>
      </template>
      <div v-if="sensitiveWords.length > 0">
        <div class="rule-card-grid">
          <div v-for="word in paginatedSensitiveWords" :key="word.id" class="rule-card rule-card-sensitive">
            <div class="rule-card-body">
              <div class="rule-card-badge">
                <span class="rule-badge bg-purple-100 text-purple-700">敏感词</span>
              </div>
              <p class="rule-card-desc sensitive-word-text">{{ word.word }}</p>
              <div class="rule-card-status">
                <span :class="word.applied ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'" class="rule-badge">
                  {{ word.applied ? '已应用' : '未应用' }}
                </span>
              </div>
            </div>
            <div class="rule-card-footer">
              <button @click="openCreateEditDialog('sensitive', word)" class="btn-link text-blue-600">编辑</button>
              <button v-if="!word.applied" @click="applySensitiveWord(word.id)" class="btn-link text-green-600">应用</button>
              <button v-else @click="unapplySensitiveWord(word.id)" class="btn-link text-orange-600">取消</button>
              <button @click="deleteSensitiveWord(word.id)" class="btn-link text-red-500">删除</button>
            </div>
          </div>
        </div>
        <div class="pagination-bar">
          <el-pagination v-model:current-page="sensitiveWordPage" :page-size="4" :total="sensitiveWords.length" layout="prev, pager, next" small background />
        </div>
        <div class="insert-bar">
          <el-button type="primary" plain size="small" @click="openCreateEditDialog('sensitive', null)">+ 插入</el-button>
        </div>
      </div>
      <div v-else class="empty-state">
        <p class="mb-3">暂无敏感词</p>
        <el-button type="primary" plain size="small" @click="openCreateEditDialog('sensitive', null)">+ 插入</el-button>
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
        <label class="form-label">敏感词</label>
        <el-input v-model="editDialog.data.word" placeholder="请输入敏感词" />
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

// ===== 敏感词 =====
const sensitiveWords = ref([])
const sensitiveWordPage = ref(1)
const paginatedSensitiveWords = computed(() => {
  const start = (sensitiveWordPage.value - 1) * 4
  return sensitiveWords.value.slice(start, start + 4)
})

const editDialog = ref({
  visible: false, title: '', type: 'sensitive', data: {}, isCreate: false
})

onMounted(async () => {
  await loadSensitiveWords()
})

function openCreateEditDialog(type, existingData) {
  if (existingData) {
    editDialog.value = {
      visible: true, title: '编辑敏感词', type: 'sensitive',
      data: { ...existingData }, isCreate: false
    }
  } else {
    editDialog.value = {
      visible: true, title: '创建敏感词', type: 'sensitive',
      data: { word: '' }, isCreate: true
    }
  }
}

async function loadSensitiveWords() {
  try {
    const res = await adminApi.getSensitiveWords()
    if (res.data?.code === 200) { sensitiveWords.value = res.data.data || [] }
  } catch { /* ignore */ }
}

async function applySensitiveWord(id) {
  try { await adminApi.applySensitiveWord(id); ElMessage.success('敏感词已应用'); await loadSensitiveWords() }
  catch { ElMessage.error('应用失败') }
}

async function unapplySensitiveWord(id) {
  try { await adminApi.unapplySensitiveWord(id); ElMessage.success('已取消应用'); await loadSensitiveWords() }
  catch { ElMessage.error('操作失败') }
}

async function deleteSensitiveWord(id) {
  try { await adminApi.deleteSensitiveWord(id); ElMessage.success('敏感词已删除'); await loadSensitiveWords() }
  catch { ElMessage.error('删除失败') }
}

async function saveEdit() {
  const { data, isCreate } = editDialog.value
  try {
    if (!data.word || !data.word.trim()) { ElMessage.warning('请输入敏感词'); return }
    if (isCreate) { await adminApi.createSensitiveWord({ word: data.word.trim() }); ElMessage.success('敏感词已创建') }
    else { await adminApi.updateSensitiveWord(data.id, { word: data.word.trim() }); ElMessage.success('保存成功') }
    await loadSensitiveWords()
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
.rule-card-sensitive { border-top: 4px solid #a855f7; }
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
.sensitive-word-text {
  font-size: 18px !important;
  font-weight: 600;
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
.bg-purple-100 { background: #f3e8ff; }
.bg-green-100 { background: #dcfce7; }
.bg-gray-100 { background: #f3f4f6; }
.text-purple-700 { color: #7e22ce; }
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
.w-full { width: 100%; }
:deep(.el-select) { width: 100%; }
</style>
