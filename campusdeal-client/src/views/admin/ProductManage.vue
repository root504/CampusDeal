<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <span class="inline-flex items-center justify-center px-2.5 py-0.5 rounded-full text-sm font-medium bg-primary-100 text-primary-700">
        {{ total }} 件商品
      </span>
      <div class="flex items-center space-x-3">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索商品..."
          class="input-field w-48"
          @keyup.enter="searchProducts"
        />
        <select v-model="status" class="input-field w-32" @change="searchProducts">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">已上架</option>
          <option value="2">已驳回</option>
          <option value="3">已下架</option>
          <option value="4">已删除</option>
          <option value="5">已收回</option>
        </select>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-gray-600">ID</th>
            <th class="px-4 py-3 text-left text-gray-600">商品</th>
            <th class="px-4 py-3 text-left text-gray-600">描述</th>
            <th class="px-4 py-3 text-left text-gray-600">价格</th>
            <th class="px-4 py-3 text-left text-gray-600">卖家</th>
            <th class="px-4 py-3 text-left text-gray-600">分类</th>
            <th class="px-4 py-3 text-left text-gray-600">状态</th>
            <th class="px-4 py-3 text-left text-gray-600">时间</th>
            <th class="px-4 py-3 text-left text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-gray-500 text-xs">{{ p.id }}</td>
            <td class="px-4 py-3">
              <div class="flex items-center space-x-3">
                <img :src="p.coverImage" class="w-10 h-10 object-cover rounded flex-shrink-0" v-if="p.coverImage" />
                <div class="w-8 h-10 bg-gray-200 rounded flex-shrink-0" v-else></div>
                <span class="truncate max-w-[180px] font-medium">{{ p.title }}</span>
              </div>
            </td>
            <td class="px-4 py-3">
              <span class="truncate max-w-[150px] text-gray-500 block" :title="p.description">{{ p.description || '—' }}</span>
            </td>
            <td class="px-4 py-3">&yen;{{ p.price }}</td>
            <td class="px-4 py-3">{{ p.seller?.username || '-' }}</td>
            <td class="px-4 py-3">{{ p.category?.name || '-' }}</td>
            <td class="px-4 py-3">
              <div>
                <span class="badge text-xs" :class="getStatusClass(p.status)">{{ getStatusLabel(p.status) }}</span>
                <div v-if="p.auditRemark" class="text-xs text-gray-400 mt-0.5 truncate max-w-[120px]" :title="p.auditRemark">
                  {{ p.auditRemark }}
                </div>
              </div>
            </td>
            <td class="px-4 py-3 text-gray-500">{{ formatDate(p.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex items-center space-x-2">
                <button @click="openEditDialog(p)" class="text-blue-600 hover:text-blue-800 text-xs font-medium">编辑</button>
                <button @click="handleDelete(p)" class="text-red-600 hover:text-red-800 text-xs font-medium">删除</button>
                <span class="text-gray-300">|</span>
                <button v-if="p.status === 0" @click="handleApprove(p.id)" class="text-green-600 hover:text-green-800 text-xs font-medium">通过</button>
                <button v-if="p.status === 0" @click="openRejectDialog(p)" class="text-red-600 hover:text-red-800 text-xs font-medium">驳回</button>
                <button v-if="p.status === 1" @click="offShelf(p.id)" class="text-yellow-600 hover:text-yellow-800 text-xs">下架</button>
                <button v-if="p.status === 2 || p.status === 3 || p.status === 5" @click="onShelf(p.id)" class="text-green-600 hover:text-green-800 text-xs">上架</button>
                <button v-if="p.appealStatus === 1" @click="openAppealDialog(p)" class="text-orange-600 hover:text-orange-800 text-xs font-medium">处理申诉</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="products.length === 0" class="text-center py-8 text-gray-400">暂无数据</div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadProducts"
      />
    </div>

    <!-- 驳回弹窗 -->
    <div v-if="rejectDialogVisible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="rejectDialogVisible = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-bold mb-4">驳回商品</h3>
        <p class="text-sm text-gray-600 mb-3">商品：{{ rejectTarget?.title }}</p>
        <textarea
          v-model="rejectReason"
          rows="4"
          class="input-field w-full"
          placeholder="请填写驳回理由..."
        ></textarea>
        <div class="flex justify-end space-x-3 mt-4">
          <button @click="rejectDialogVisible = false" class="btn-secondary">取消</button>
          <button @click="confirmReject" class="btn-primary bg-red-600 hover:bg-red-700">确认驳回</button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="editDialogVisible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="editDialogVisible = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4 max-h-[80vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-4">编辑商品</h3>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">商品标题</label>
            <input v-model="editForm.title" type="text" class="input-field w-full" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">描述</label>
            <textarea v-model="editForm.description" rows="3" class="input-field w-full"></textarea>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">价格</label>
              <input v-model.number="editForm.price" type="number" step="0.01" class="input-field w-full" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">分类</label>
              <input v-model="editForm.categoryName" type="text" class="input-field w-full" disabled />
            </div>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-5">
          <button @click="editDialogVisible = false" class="btn-secondary">取消</button>
          <button @click="confirmEdit" class="btn-primary">保存修改</button>
        </div>
      </div>
    </div>

    <!-- 申诉处理弹窗 -->
    <div v-if="appealDialogVisible" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="appealDialogVisible = false">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg mx-4 max-h-[85vh] overflow-y-auto">
        <h3 class="text-lg font-bold mb-4">处理申诉</h3>
        <div class="text-sm text-gray-600 space-y-2 mb-4">
          <p><span class="font-medium">商品：</span>{{ appealTarget?.title }}</p>
          <p><span class="font-medium">申诉理由：</span>{{ appealTarget?.appealReason || '无' }}</p>
          <p v-if="appealTarget?.appealTime"><span class="font-medium">申诉时间：</span>{{ formatDate(appealTarget.appealTime) }}</p>
        </div>
        <div class="text-sm space-y-2 mb-4 p-3 bg-gray-50 rounded">
          <p class="font-medium text-gray-700">商品信息：</p>
          <p>标题：{{ appealTarget?.title }}</p>
          <p>描述：{{ appealTarget?.description || '无' }}</p>
          <p>价格：¥{{ appealTarget?.price }} <span v-if="appealTarget?.originalPrice" class="text-gray-400 line-through">¥{{ appealTarget.originalPrice }}</span></p>
        </div>
        <div class="flex justify-end space-x-3">
          <button @click="appealDialogVisible = false" class="btn-secondary">取消</button>
          <button @click="handleRejectAppeal" class="btn-primary bg-red-600 hover:bg-red-700">驳回</button>
          <button @click="handleApproveAppeal" class="btn-primary bg-green-600 hover:bg-green-700">通过</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const keyword = ref('')
const status = ref('')
const products = ref([])
const currentPage = ref(1)
const pageSize = 8
const total = ref(0)

// 驳回弹窗状态
const rejectDialogVisible = ref(false)
const rejectTarget = ref(null)
const rejectReason = ref('')

// 编辑弹窗状态
const editDialogVisible = ref(false)
const editTarget = ref(null)
const editForm = ref({ title: '', description: '', price: 0, categoryName: '' })

// 申诉处理弹窗状态
const appealDialogVisible = ref(false)
const appealTarget = ref(null)

onMounted(() => loadProducts())

function searchProducts() {
  currentPage.value = 1
  loadProducts()
}

async function loadProducts() {
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (status.value !== '') params.status = status.value
    const res = await adminApi.getProducts(params)
    const data = res.data?.data
    products.value = data?.list || data?.records || data || []
    total.value = data?.total ?? data?.totalElements ?? data?.count ?? (Array.isArray(data?.list) ? data.list.length : (Array.isArray(data?.records) ? data.records.length : (Array.isArray(data) ? data.length : 0)))
  } catch {
    // ignore
  }
}

async function handleApprove(id) {
  const product = products.value.find(p => p.id === id)
  if (!product || !product.coverImage || !product.coverImage.trim()) {
    ElMessage.warning('该商品缺少图片，无法通过审核')
    return
  }
  try {
    await adminApi.approveProduct(id)
    ElMessage.success('审核通过')
    loadProducts()
  } catch (e) {
    ElMessage.error(e?.message || e?.response?.data?.message || '操作失败')
  }
}

function openRejectDialog(product) {
  rejectTarget.value = product
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回理由')
    return
  }
  try {
    await adminApi.rejectProduct(rejectTarget.value.id, rejectReason.value)
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function offShelf(id) {
  try {
    await adminApi.offShelfProduct(id)
    ElMessage.success('已下架')
    loadProducts()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function onShelf(id) {
  try {
    await adminApi.onShelfProduct(id)
    ElMessage.success('已上架')
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

// 编辑功能
function openEditDialog(product) {
  editTarget.value = product
  editForm.value = {
    title: product.title || '',
    description: product.description || '',
    price: product.price || 0,
    categoryName: product.category?.name || '-'
  }
  editDialogVisible.value = true
}

async function confirmEdit() {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('商品标题不能为空')
    return
  }
  try {
    await adminApi.updateProduct(editTarget.value.id, {
      title: editForm.value.title,
      description: editForm.value.description,
      price: editForm.value.price
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

// 删除功能（软删除，将 status 改为 4）
async function handleDelete(product) {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品「${product.title}」吗？商品将标记为已删除。`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await adminApi.softDeleteProduct(product.id)
    ElMessage.success('已删除（软删除）')
    loadProducts()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
}

function getStatusLabel(s) {
  const map = { 0: '待审核', 1: '已上架', 2: '已驳回', 3: '已下架', 4: '已删除', 5: '已收回' }
  return map[s] || '未知'
}

function getStatusClass(s) {
  const map = { 0: 'bg-yellow-100 text-yellow-800', 1: 'bg-green-100 text-green-800', 2: 'bg-red-100 text-red-700', 3: 'bg-gray-100 text-gray-600', 4: 'bg-gray-200 text-gray-400 line-through', 5: 'bg-red-100 text-red-700' }
  return map[s] || ''
}

function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

// ========== 申诉处理 ==========
function openAppealDialog(product) {
  appealTarget.value = product
  appealDialogVisible.value = true
}

async function handleApproveAppeal() {
  try {
    await adminApi.approveAppeal(appealTarget.value.id)
    ElMessage.success('申诉已通过，商品已重新上架')
    appealDialogVisible.value = false
    loadProducts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function handleRejectAppeal() {
  try {
    await ElMessageBox.prompt('请输入驳回理由', '驳回申诉', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请填写驳回理由...'
    }).then(async ({ value }) => {
      await adminApi.rejectAppeal(appealTarget.value.id, value || '')
      ElMessage.success('申诉已驳回')
      appealDialogVisible.value = false
      loadProducts()
    })
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }
}
</script>
