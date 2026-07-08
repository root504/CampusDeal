<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <AppHeader />
    <main class="flex-1 max-w-lg mx-auto px-4 py-8 w-full">
      <button
        @click="$router.back()"
        class="flex items-center text-sm text-gray-500 hover:text-gray-700 mb-6 transition-colors"
      >
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回
      </button>

      <div class="bg-white rounded-2xl shadow-sm p-8">
        <h1 class="text-xl font-bold text-gray-900 mb-6">订单评价</h1>

        <!-- 订单信息 -->
        <div v-if="order" class="mb-8 p-4 bg-gray-50 rounded-xl">
          <p class="text-sm text-gray-500 mb-1">订单编号</p>
          <p class="text-sm font-medium text-gray-800">{{ order.orderNo }}</p>
          <div v-if="order.items && order.items.length" class="mt-3 space-y-2">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="flex items-center space-x-3"
            >
              <img
                v-if="item.productImage"
                :src="item.productImage"
                class="w-12 h-12 object-cover rounded-lg"
              />
              <span class="text-sm text-gray-700 truncate">{{ item.productTitle }}</span>
            </div>
          </div>
        </div>

        <!-- 评分 -->
        <div class="mb-6">
          <p class="text-sm font-medium text-gray-700 mb-3">评分</p>
          <div class="flex items-center space-x-1">
            <button
              v-for="star in 5"
              :key="star"
              @click="rating = star"
              class="text-3xl transition-colors focus:outline-none"
              :class="star <= rating ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-200'"
            >
              ★
            </button>
          </div>
          <p v-if="rating === 0" class="text-xs text-gray-400 mt-1">请点击星星评分</p>
        </div>

        <!-- 评价内容 -->
        <div class="mb-6">
          <p class="text-sm font-medium text-gray-700 mb-3">评价内容</p>
          <textarea
            v-model="content"
            rows="4"
            maxlength="500"
            placeholder="分享一下您的使用感受吧（选填）"
            class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm resize-none focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          ></textarea>
          <p class="text-xs text-gray-400 text-right mt-1">{{ content.length }}/500</p>
        </div>

        <!-- 错误提示 -->
        <p v-if="errorMsg" class="text-sm text-red-500 mb-4">{{ errorMsg }}</p>

        <!-- 提交按钮 -->
        <button
          @click="handleSubmit"
          :disabled="submitting || rating === 0"
          class="w-full py-3 rounded-xl text-white font-medium transition-colors"
          :class="rating > 0 && !submitting ? 'bg-primary-600 hover:bg-primary-700' : 'bg-gray-300 cursor-not-allowed'"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { reviewApi } from '@/api/review'
import { orderApi } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const orderId = ref(Number(route.params.orderId))
const productId = ref(Number(route.query.productId) || 0)
const order = ref(null)
const rating = ref(0)
const content = ref('')
const submitting = ref(false)
const errorMsg = ref('')

async function loadOrder() {
  try {
    const res = await orderApi.getOrder(orderId.value)
    order.value = res.data?.data || null
  } catch {
    errorMsg.value = '加载订单信息失败'
  }
}

async function handleSubmit() {
  if (rating.value === 0) {
    errorMsg.value = '请选择评分'
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    await reviewApi.submitReview(orderId.value, {
      rating: rating.value,
      content: content.value,
      productId: productId.value
    })
    ElMessage.success('评价提交成功')
    router.back()
  } catch (err) {
    errorMsg.value = err?.response?.data?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>
