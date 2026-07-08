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
        <h1 class="text-2xl font-bold">修改注册信息</h1>
      </div>

      <!-- 手机号修改 -->
      <section class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">修改手机号</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">当前手机号</label>
            <input
              :value="userStore.userInfo?.phone || ''"
              type="text"
              class="input-field w-full bg-gray-50"
              disabled
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">新手机号</label>
            <input
              v-model="phoneForm.newPhone"
              type="text"
              class="input-field w-full"
              placeholder="请输入新手机号"
              maxlength="11"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">验证密码</label>
            <input
              v-model="phoneForm.password"
              type="password"
              class="input-field w-full"
              placeholder="请输入当前密码以验证身份"
            />
          </div>
        </div>
      </section>

      <!-- 密码修改 -->
      <section class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">修改密码</h2>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">旧密码</label>
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              class="input-field w-full"
              placeholder="请输入旧密码"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">新密码</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              class="input-field w-full"
              placeholder="请输入新密码（6-20位）"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">确认新密码</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              class="input-field w-full"
              placeholder="请再次输入新密码"
            />
          </div>
        </div>
      </section>

      <!-- 应用按钮 -->
      <button @click="applyChanges" :disabled="saving" class="btn-primary w-full">
        {{ saving ? '保存中...' : '应用' }}
      </button>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()

const phoneForm = reactive({
  newPhone: '',
  password: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const saving = ref(false)

async function applyChanges() {
  const hasPhoneChange = phoneForm.newPhone.trim() && phoneForm.password
  const hasPwdChange = passwordForm.oldPassword || passwordForm.newPassword || passwordForm.confirmPassword

  if (!hasPhoneChange && !hasPwdChange) {
    ElMessage.warning('请至少填写一项修改')
    return
  }

  // 密码验证
  if (hasPwdChange) {
    if (!passwordForm.oldPassword) {
      ElMessage.warning('请输入旧密码')
      return
    }
    if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
      ElMessage.warning('新密码至少6位')
      return
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
      return
    }
  }

  saving.value = true
  try {
    // 修改密码
    if (hasPwdChange) {
      await userApi.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      ElMessage.success('密码已修改')
    }

    // 修改手机号
    if (hasPhoneChange) {
      await userApi.changePhone({
        phone: phoneForm.newPhone,
        password: phoneForm.password
      })
      ElMessage.success('手机号已修改，请使用新手机号登录')
      // 清除登录态，让用户重新登录
      setTimeout(() => {
        userStore.clearAuth()
      }, 1500)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}
</script>
