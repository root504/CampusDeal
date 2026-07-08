<template>
  <div>
    <div class="flex items-center justify-end mb-6">
      <div class="flex space-x-3">
        <button @click="showForm = true" class="btn-primary text-sm">发布公告</button>
      </div>
    </div>

    <!-- Form Modal -->
    <div v-if="showForm" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showForm = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4">
        <h3 class="text-lg font-bold mb-4">{{ editingId ? '编辑公告' : '发布公告' }}</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">标题 *</label>
            <input v-model="form.title" type="text" class="input-field" placeholder="公告标题" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">内容 *</label>
            <textarea v-model="form.content" rows="4" class="input-field" placeholder="公告内容..."></textarea>
          </div>
          <div class="flex justify-end space-x-3">
            <button @click="showForm = false" class="btn-secondary">取消</button>
            <button @click="handleSubmit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Announcement List -->
    <div class="space-y-3">
      <div v-for="a in announcements" :key="a.id" class="bg-white rounded-xl shadow-sm p-4">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <h3 class="font-medium text-gray-900">{{ a.title }}</h3>
            <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ a.content }}</p>
            <p class="text-xs text-gray-400 mt-2">
              {{ formatDate(a.createdAt) }}
              <span class="ml-2 badge" :class="a.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'">
                {{ a.status === 1 ? '已发布' : '已下架' }}
              </span>
            </p>
          </div>
          <div class="flex space-x-2 ml-4">
            <button @click="editAnnouncement(a)" class="text-blue-600 hover:text-blue-800 text-sm">编辑</button>
            <button @click="deleteAnnouncement(a.id)" class="text-red-600 hover:text-red-800 text-sm">删除</button>
          </div>
        </div>
      </div>
      <div v-if="announcements.length === 0" class="text-center py-8 text-gray-400">暂无公告</div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadAnnouncements"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'

const announcements = ref([])
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const showForm = ref(false)
const submitting = ref(false)
const editingId = ref(null)

const form = reactive({ title: '', content: '' })

onMounted(() => loadAnnouncements())

async function loadAnnouncements() {
  try {
    const res = await adminApi.getAnnouncements({ page: currentPage.value, size: pageSize })
    const raw = res.data?.data
    if (Array.isArray(raw)) {
      announcements.value = raw
      total.value = raw.length
    } else if (raw && typeof raw === 'object') {
      announcements.value = raw.records || raw.list || raw.content || []
      total.value = raw.total ?? 0
    } else {
      announcements.value = []
    }
  } catch {
    // ignore
  }
}

function editAnnouncement(a) {
  editingId.value = a.id
  form.title = a.title
  form.content = a.content
  showForm.value = true
}

async function handleSubmit() {
  if (!form.title || !form.content) {
    alert('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await adminApi.updateAnnouncement(editingId.value, { ...form })
    } else {
      await adminApi.createAnnouncement({ ...form })
    }
    showForm.value = false
    editingId.value = null
    form.title = ''
    form.content = ''
    loadAnnouncements()
  } catch {
    // ignore
  } finally {
    submitting.value = false
  }
}

async function deleteAnnouncement(id) {
  if (!confirm('确认删除该公告？')) return
  await adminApi.deleteAnnouncement(id)
  loadAnnouncements()
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}
</script>
