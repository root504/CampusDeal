<template>
  <div>
    <button @click="$router.push('/admin/basic-config')" class="back-btn">
      ← 返回
    </button>

    <!-- 提示信息 -->
    <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-4 text-sm text-blue-700">
      每次操作后请点击「应用」按钮使更改生效
    </div>

    <!-- 操作按钮 -->
    <div class="flex items-center justify-between mb-6">
      <span class="text-sm text-gray-500">共 {{ total }} 条轮播图</span>
      <div class="flex space-x-3">
        <button @click="handleApply" class="btn-secondary text-sm" :disabled="applying">
          {{ applying ? '应用中...' : '应用' }}
        </button>
        <button @click="showAddForm = true" class="btn-primary text-sm">添加轮播图</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full">
        <thead class="bg-gray-50 border-b">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">图片预览</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">图片URL</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">跳转链接</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">排序</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y">
          <tr v-for="b in banners" :key="b.id">
            <td class="px-4 py-3 text-sm text-gray-900">{{ b.id }}</td>
            <td class="px-4 py-3">
              <img :src="b.imageUrl" class="w-24 h-12 object-cover rounded" />
            </td>
            <td class="px-4 py-3 text-sm text-gray-500 max-w-xs truncate" :title="b.imageUrl">{{ b.imageUrl }}</td>
            <td class="px-4 py-3 text-sm text-gray-500 max-w-xs truncate" :title="b.linkUrl">{{ b.linkUrl || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-900">{{ b.sortOrder }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-1 text-xs rounded-full" :class="b.status === 1 ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'">
                {{ b.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <div class="flex space-x-2">
                <button @click="editBanner(b)" class="text-blue-600 hover:text-blue-800 text-xs">更新图片</button>
                <button @click="moveUp(b)" class="text-gray-600 hover:text-gray-800 text-xs">上调</button>
                <button @click="moveDown(b)" class="text-gray-600 hover:text-gray-800 text-xs">下调</button>
                <button @click="deleteBanner(b.id)" class="text-red-600 hover:text-red-800 text-xs">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="banners.length === 0" class="text-center py-12 text-gray-400">暂无轮播图</div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadBanners"
      />
    </div>

    <!-- 添加/编辑弹窗 -->
    <div v-if="showAddForm || showEditForm" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="closeForm">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4">
        <h3 class="text-lg font-bold mb-4">{{ editingId ? '更新轮播图' : '添加轮播图' }}</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">图片URL *</label>
            <input v-model="form.imageUrl" type="text" class="input-field" placeholder="https://example.com/banner.jpg" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">跳转链接</label>
            <input v-model="form.linkUrl" type="text" class="input-field" placeholder="/products" />
          </div>
          <div class="flex justify-end space-x-3">
            <button @click="closeForm" class="btn-secondary">取消</button>
            <button @click="handleSubmit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : (editingId ? '保存' : '添加') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

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
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const banners = ref([])
const currentPage = ref(1)
const pageSize = 4
const total = ref(0)
const showAddForm = ref(false)
const showEditForm = ref(false)
const submitting = ref(false)
const applying = ref(false)
const editingId = ref(null)

const form = reactive({ imageUrl: '', linkUrl: '' })

onMounted(() => loadBanners())

async function loadBanners() {
  try {
    const res = await adminApi.getBanners({ page: currentPage.value, size: pageSize })
    const raw = res.data?.data
    if (raw && typeof raw === 'object') {
      banners.value = raw.list || raw.records || raw.content || []
      total.value = raw.total ?? 0
    } else {
      banners.value = []
    }
  } catch {
    // ignore
  }
}

function closeForm() {
  showAddForm.value = false
  showEditForm.value = false
  editingId.value = null
  form.imageUrl = ''
  form.linkUrl = ''
}

function editBanner(b) {
  editingId.value = b.id
  form.imageUrl = b.imageUrl
  form.linkUrl = b.linkUrl || ''
  showEditForm.value = true
}

async function handleSubmit() {
  if (!form.imageUrl) {
    ElMessage.warning('请输入图片URL')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await adminApi.updateBanner(editingId.value, { imageUrl: form.imageUrl, linkUrl: form.linkUrl || null })
    } else {
      await adminApi.createBanner({ imageUrl: form.imageUrl, linkUrl: form.linkUrl || null })
    }
    closeForm()
    ElMessage.info('请点击「应用」按钮使更改生效')
    loadBanners()
  } catch {
    // ignore
  } finally {
    submitting.value = false
  }
}

async function moveUp(b) {
  await adminApi.adjustBannerOrder(b.id, 'up')
  ElMessage.info('请点击「应用」按钮使更改生效')
  loadBanners()
}

async function moveDown(b) {
  await adminApi.adjustBannerOrder(b.id, 'down')
  ElMessage.info('请点击「应用」按钮使更改生效')
  loadBanners()
}

async function deleteBanner(id) {
  if (!confirm('确认删除该轮播图？')) return
  await adminApi.deleteBanner(id)
  ElMessage.info('请点击「应用」按钮使更改生效')
  loadBanners()
}

async function handleApply() {
  applying.value = true
  try {
    await adminApi.applyBanners()
    ElMessage.success('轮播图排序已应用')
    loadBanners()
  } catch {
    // ignore
  } finally {
    applying.value = false
  }
}
</script>
