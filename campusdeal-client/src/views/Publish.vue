<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">{{ isEdit ? '编辑商品' : '发布商品' }}</h1>

      <form @submit.prevent="handleSubmit" class="space-y-6">
        <!-- Title -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">商品标题 *</label>
          <input v-model="form.title" type="text" class="input-field" placeholder="请输入商品标题" required maxlength="200" />
        </div>

        <!-- Description -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">商品描述</label>
          <textarea v-model="form.description" rows="4" class="input-field" placeholder="描述商品成色、使用情况等..."></textarea>
        </div>

        <!-- Image Upload -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">商品图片 *</label>
          <div class="flex flex-wrap gap-3 mb-3">
            <div
              v-for="(img, idx) in form.images"
              :key="idx"
              class="relative w-24 h-24 rounded-lg overflow-hidden border border-gray-200"
            >
              <img :src="img" class="w-full h-full object-cover" />
              <button
                type="button"
                @click="removeImage(idx)"
                class="absolute top-0.5 right-0.5 w-5 h-5 bg-red-500 text-white rounded-full text-xs flex items-center justify-center"
              >&times;</button>
            </div>
            <label
              class="w-24 h-24 border-2 border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center cursor-pointer hover:border-primary-500 transition-colors"
            >
              <span class="text-2xl text-gray-400">+</span>
              <span class="text-xs text-gray-400 mt-1">上传图片</span>
              <input type="file" accept="image/*" multiple class="hidden" @change="handleImageUpload" />
            </label>
          </div>
          <p class="text-xs text-gray-400">支持 JPG/PNG 格式，最多 9 张</p>
        </div>

        <!-- Price -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">售价 *</label>
            <input v-model.number="form.price" type="number" step="0.01" class="input-field" placeholder="0.00" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">原价</label>
            <input v-model.number="form.originalPrice" type="number" step="0.01" class="input-field" placeholder="0.00" />
          </div>
        </div>

        <!-- Category (button group) -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">分类 *</label>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="cat in categoryList"
              :key="cat.id"
              type="button"
              @click="form.categoryId = cat.id"
              class="px-4 py-2 rounded-full text-sm transition-colors"
              :class="form.categoryId === cat.id ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>

        <!-- Condition (text input) -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">成色 *</label>
          <input v-model="form.conditionLevel" type="text" class="input-field" placeholder="如：全新、九五新、九成新等" required />
        </div>

        <!-- Campus (text input) -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">校区</label>
          <input v-model="form.campus" type="text" class="input-field" placeholder="如：南湖校区、东校区等" />
        </div>

        <!-- Submit button (create / edit) -->
        <button type="submit" class="btn-primary w-full" :disabled="submitting">
          {{ submitting ? '保存中...' : (isEdit ? '保存修改' : '发布商品') }}
        </button>
      </form>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { productApi } from '@/api/product'

const router = useRouter()
const route = useRoute()
const submitting = ref(false)
const uploading = ref(false)
const isEdit = ref(false)
const editId = ref(null)

// 8 predefined categories
const CATEGORY_NAMES = [
  '教材书籍', '数码电子', '服饰鞋包', '生活日用',
  '运动户外', '乐器文具', '美妆护肤', '其他好物'
]

const categoryList = ref([])

const form = reactive({
  title: '',
  description: '',
  price: 0,
  originalPrice: 0,
  categoryId: '',
  conditionLevel: '',
  campus: '',
  images: []
})

onMounted(async () => {
  try {
    const res = await productApi.getCategories()
    if (res.data?.code === 200) {
      const backendCats = res.data.data || []
      categoryList.value = CATEGORY_NAMES.map(name => {
        const found = backendCats.find(c => c.name === name)
        return { id: found ? found.id : null, name }
      }).filter(c => c.id !== null)
    }
  } catch {
    categoryList.value = CATEGORY_NAMES.map((name, i) => ({ id: i + 1, name }))
  }

  // Check edit mode
  const idParam = route.query.id
  if (idParam) {
    isEdit.value = true
    editId.value = Number(idParam)
    await loadProduct(editId.value)
  }
})

async function loadProduct(id) {
  try {
    const res = await productApi.getMyProduct(id)
    if (res.data?.code === 200) {
      const p = res.data.data
      form.title = p.title || ''
      form.description = p.description || ''
      form.price = p.price || 0
      form.originalPrice = p.originalPrice || 0
      form.categoryId = p.categoryId || ''
      form.conditionLevel = p.conditionLevel || ''
      form.campus = p.campus || ''
      if (p.images && p.images.length > 0) {
        form.images = p.images.map(img => img.url || img)
      } else if (p.coverImage) {
        form.images = [p.coverImage]
      }
    }
  } catch (e) {
    ElMessage.error('加载商品信息失败')
  }
}

async function handleImageUpload(e) {
  const files = Array.from(e.target.files)
  if (!files.length) return

  if (form.images.length + files.length > 9) {
    ElMessage.warning('最多上传 9 张图片')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    files.forEach(f => formData.append('files', f))
    const token = localStorage.getItem('token')
    const res = await axios.post('/api/v1/upload/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })
    if (res.data?.code === 200 && res.data.data) {
      form.images.push(...res.data.data)
    }
  } catch {
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

function removeImage(idx) {
  form.images.splice(idx, 1)
}

async function handleSubmit() {
  if (!form.images.length) {
    ElMessage.warning('请上传至少一张商品图片')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      const res = await productApi.updateProduct(editId.value, form)
      if (res.data?.code === 200) {
        ElMessage.success('修改成功，已重新提交审核')
        router.push('/my-products')
      }
    } else {
      const res = await productApi.createProduct(form)
      if (res.data?.code === 200) {
        ElMessage.success('发布成功，等待管理员审核')
        router.push('/products')
      }
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || (isEdit.value ? '修改失败' : '发布失败'))
  } finally {
    submitting.value = false
  }
}
</script>
