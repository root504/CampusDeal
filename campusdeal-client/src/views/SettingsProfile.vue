<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- 返回栏 -->
      <div class="flex items-center space-x-3 mb-6">
        <button @click="$router.back()" class="p-1 -ml-1 rounded-lg hover:bg-gray-100">
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 class="text-2xl font-bold">修改个人信息</h1>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 space-y-5">
        <!-- 头像 -->
        <div class="flex items-center space-x-4">
          <div class="relative group cursor-pointer" @click="triggerAvatarUpload">
            <img
              :src="avatarPreview || userStore.userInfo?.avatarUrl || defaultAvatar"
              class="w-16 h-16 rounded-full object-cover border-2 border-gray-200"
              alt="头像"
            />
            <div class="absolute inset-0 rounded-full bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleAvatarChange"
          />
          <div>
            <p class="text-sm font-medium text-gray-900">头像</p>
            <p class="text-xs text-gray-400">点击更换头像</p>
          </div>
        </div>

        <!-- 昵称 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">昵称</label>
          <input
            v-model="form.username"
            type="text"
            class="input-field w-full"
            placeholder="请输入昵称"
            maxlength="20"
          />
        </div>

        <!-- 学校 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">学校</label>
          <div class="relative">
            <input
              v-model="schoolSearch"
              type="text"
              class="input-field w-full"
              placeholder="搜索或输入学校名称"
              @focus="showDropdown = true"
              @input="filterSchools"
            />
            <div
              v-if="showDropdown && filteredSchools.length > 0"
              class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg max-h-48 overflow-y-auto"
            >
              <div
                v-for="school in filteredSchools"
                :key="school"
                @click="selectSchool(school)"
                class="px-4 py-2 hover:bg-primary-50 cursor-pointer text-sm text-gray-700"
              >
                {{ school }}
              </div>
            </div>
          </div>
        </div>

        <!-- 校区 + 年级 -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">校区</label>
            <input
              v-model="form.campus"
              type="text"
              class="input-field w-full"
              placeholder="如：南湖校区"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">年级</label>
            <select v-model="form.grade" class="input-field w-full">
              <option value="">请选择</option>
              <option v-for="g in grades" :key="g" :value="g">{{ g }}</option>
            </select>
          </div>
        </div>

        <!-- 应用按钮 -->
        <button @click="saveProfile" :disabled="saving" class="btn-primary w-full mt-2">
          {{ saving ? '保存中...' : '应用' }}
        </button>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import request from '@/api'

const userStore = useUserStore()
const defaultAvatar = 'data:image/svg+xml;base64,' + btoa('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="#e5e7eb" width="100" height="100"/><text x="50" y="62" text-anchor="middle" font-size="36" fill="#9ca3af">?</text></svg>')

const CHINESE_UNIVERSITIES = [
  '北京大学', '清华大学', '复旦大学', '上海交通大学', '浙江大学', '南京大学',
  '中国科学技术大学', '哈尔滨工业大学', '西安交通大学', '武汉大学', '华中科技大学',
  '中山大学', '四川大学', '同济大学', '北京航空航天大学', '东南大学',
  '北京理工大学', '中国人民大学', '南开大学', '天津大学', '山东大学',
  '厦门大学', '吉林大学', '华南理工大学', '中南大学', '大连理工大学',
  '西北工业大学', '华东师范大学', '中国农业大学', '湖南大学', '电子科技大学',
  '重庆大学', '北京师范大学', '东北大学', '兰州大学', '中国海洋大学',
  '西北农林科技大学', '中央民族大学', '国防科技大学', '北京邮电大学',
  '上海财经大学', '中央财经大学', '对外经济贸易大学', '中国政法大学',
  '北京外国语大学', '上海外国语大学', '中国传媒大学', '北京交通大学',
  '南京航空航天大学', '南京理工大学', '武汉理工大学', '合肥工业大学',
  '长沙民政职业技术学院'
]

const grades = ['2022级', '2023级', '2024级', '2025级', '2026级', '2027级', '2028级']

const form = reactive({
  username: '',
  school: '',
  campus: '',
  grade: ''
})

const saving = ref(false)
const avatarInput = ref(null)
const avatarPreview = ref('')
const avatarFile = ref(null)

const schoolSearch = ref('')
const showDropdown = ref(false)
const filteredSchools = ref(CHINESE_UNIVERSITIES)

function filterSchools() {
  const keyword = schoolSearch.value.toLowerCase()
  filteredSchools.value = CHINESE_UNIVERSITIES.filter(s =>
    s.toLowerCase().includes(keyword)
  )
}

function selectSchool(school) {
  schoolSearch.value = school
  form.school = school
  showDropdown.value = false
}

watch(showDropdown, (val) => {
  if (val) {
    setTimeout(() => {
      document.addEventListener('click', handleClickOutside)
    }, 0)
  } else {
    document.removeEventListener('click', handleClickOutside)
  }
})

function handleClickOutside(e) {
  if (!e.target.closest('.relative')) {
    showDropdown.value = false
  }
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  avatarFile.value = file
  const reader = new FileReader()
  reader.onload = () => {
    avatarPreview.value = reader.result
  }
  reader.readAsDataURL(file)
}

onMounted(() => {
  if (userStore.userInfo) {
    form.username = userStore.userInfo.username || ''
    form.school = userStore.userInfo.school || ''
    form.campus = userStore.userInfo.campus || ''
    form.grade = userStore.userInfo.grade || ''
    schoolSearch.value = userStore.userInfo.school || ''
  }
})

async function saveProfile() {
  if (!form.username.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  saving.value = true
  try {
    // 如果有新头像，先上传
    let avatarUrl = null
    if (avatarFile.value) {
      const formData = new FormData()
      formData.append('file', avatarFile.value)
      const uploadRes = await request.post('/upload/image', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      if (uploadRes.data.code === 200) {
        avatarUrl = uploadRes.data.data
      }
    }

    const res = await userApi.updateProfile({
      username: form.username,
      school: form.school,
      campus: form.campus,
      grade: form.grade,
      ...(avatarUrl ? { avatarUrl } : {})
    })
    if (res.data.code === 200) {
      userStore.updateUserInfo(res.data.data)
      ElMessage.success('个人信息已更新')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>
