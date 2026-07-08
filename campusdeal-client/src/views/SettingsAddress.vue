<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <!-- 返回栏 -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-3">
          <button @click="$router.back()" class="p-1 -ml-1 rounded-lg hover:bg-gray-100">
            <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <h1 class="text-2xl font-bold">收货地址</h1>
        </div>
        <button @click="openNewAddressForm" class="text-primary-600 text-sm font-medium hover:text-primary-700">
          + 新增地址
        </button>
      </div>

      <div v-if="addresses.length === 0" class="text-center py-8 text-gray-400">
        <p>暂无收货地址</p>
      </div>

      <div v-else class="space-y-3 mb-6">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="border border-gray-200 rounded-lg p-4 bg-white relative"
          :class="{ 'border-primary-300 bg-primary-50/30': addr.isDefault === 1 }"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-1">
                <span class="font-medium text-gray-900">{{ addr.receiverName }}</span>
                <span class="text-sm text-gray-500">{{ addr.phone }}</span>
                <span v-if="addr.isDefault === 1" class="badge bg-primary-100 text-primary-700 text-xs">默认</span>
              </div>
              <p class="text-sm text-gray-600">
                {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
              </p>
            </div>
          </div>
          <div class="flex items-center space-x-3 mt-3 text-sm">
            <button @click="editAddress(addr)" class="text-primary-600 hover:text-primary-700">编辑</button>
            <button
              v-if="addr.isDefault !== 1"
              @click="setDefaultAddress(addr.id)"
              class="text-gray-500 hover:text-gray-700"
            >
              设为默认
            </button>
            <button @click="deleteAddress(addr.id)" class="text-red-500 hover:text-red-600">删除</button>
          </div>
        </div>
      </div>

      <!-- 应用按钮 -->
      <button @click="$router.back()" class="btn-primary w-full">完成</button>
    </main>
    <AppFooter />

    <!-- 地址编辑弹窗 -->
    <div
      v-if="showAddressForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click.self="showAddressForm = false"
    >
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md mx-4 p-6 animate-slide-up">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">
          {{ editingAddress ? '编辑地址' : '新增地址' }}
        </h3>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">收货人</label>
            <input v-model="addressForm.receiverName" type="text" class="input-field w-full" placeholder="请输入收货人姓名" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
            <input v-model="addressForm.phone" type="text" class="input-field w-full" placeholder="请输入手机号" />
          </div>
          <div class="grid grid-cols-3 gap-2">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">省份</label>
              <input v-model="addressForm.province" type="text" class="input-field w-full" placeholder="省份" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">城市</label>
              <input v-model="addressForm.city" type="text" class="input-field w-full" placeholder="城市" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">区/县</label>
              <input v-model="addressForm.district" type="text" class="input-field w-full" placeholder="区/县" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">详细地址</label>
            <input v-model="addressForm.detail" type="text" class="input-field w-full" placeholder="街道、门牌号等" />
          </div>
          <label class="flex items-center space-x-2 cursor-pointer">
            <input v-model="addressForm.isDefault" type="checkbox" class="w-4 h-4 text-primary-600 rounded" />
            <span class="text-sm text-gray-600">设为默认地址</span>
          </label>
        </div>
        <div class="flex space-x-3 mt-6">
          <button @click="showAddressForm = false" class="btn-secondary flex-1">取消</button>
          <button @click="saveAddress" :disabled="savingAddress" class="btn-primary flex-1">
            {{ savingAddress ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { addressApi } from '@/api/address'

const addresses = ref([])
const showAddressForm = ref(false)
const editingAddress = ref(null)
const savingAddress = ref(false)
const addressForm = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

onMounted(() => loadAddresses())

async function loadAddresses() {
  try {
    const res = await addressApi.getList()
    if (res.data.code === 200) {
      addresses.value = res.data.data || []
    }
  } catch { /* ignore */ }
}

function resetAddressForm() {
  addressForm.receiverName = ''
  addressForm.phone = ''
  addressForm.province = ''
  addressForm.city = ''
  addressForm.district = ''
  addressForm.detail = ''
  addressForm.isDefault = false
}

function openNewAddressForm() {
  editingAddress.value = null
  resetAddressForm()
  showAddressForm.value = true
}

function editAddress(addr) {
  editingAddress.value = addr
  addressForm.receiverName = addr.receiverName
  addressForm.phone = addr.phone
  addressForm.province = addr.province
  addressForm.city = addr.city
  addressForm.district = addr.district
  addressForm.detail = addr.detail
  addressForm.isDefault = addr.isDefault === 1
  showAddressForm.value = true
}

async function saveAddress() {
  if (!addressForm.receiverName || !addressForm.phone || !addressForm.province ||
      !addressForm.city || !addressForm.district || !addressForm.detail) {
    ElMessage.warning('请填写完整地址信息')
    return
  }
  savingAddress.value = true
  try {
    const data = {
      receiverName: addressForm.receiverName,
      phone: addressForm.phone,
      province: addressForm.province,
      city: addressForm.city,
      district: addressForm.district,
      detail: addressForm.detail,
      isDefault: addressForm.isDefault ? 1 : 0
    }
    const isEdit = !!editingAddress.value
    if (isEdit) {
      await addressApi.update(editingAddress.value.id, data)
    } else {
      await addressApi.create(data)
    }
    showAddressForm.value = false
    editingAddress.value = null
    resetAddressForm()
    await loadAddresses()
    ElMessage.success(isEdit ? '地址已更新' : '地址已添加')
  } catch {
    ElMessage.error('操作失败')
  } finally {
    savingAddress.value = false
  }
}

async function setDefaultAddress(id) {
  try {
    await addressApi.setDefault(id)
    await loadAddresses()
    ElMessage.success('已设为默认地址')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function deleteAddress(id) {
  try {
    await addressApi.delete(id)
    await loadAddresses()
    ElMessage.success('地址已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>
