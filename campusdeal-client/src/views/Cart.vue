<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">购物车</h1>

      <template v-if="cartStore.items.length > 0">
        <!-- Select All -->
        <div class="bg-white rounded-xl p-4 mb-4 flex items-center">
          <label class="flex items-center space-x-3 cursor-pointer">
            <input
              type="checkbox"
              :checked="cartStore.isAllSelected"
              @change="cartStore.toggleSelectAll()"
              class="w-5 h-5 text-primary-600 rounded"
            />
            <span class="text-sm text-gray-600">全选</span>
          </label>
        </div>

        <!-- Items -->
        <div class="space-y-3">
          <div
            v-for="item in cartStore.items"
            :key="item.id"
            class="card flex items-center space-x-4 !p-4"
          >
            <input
              type="checkbox"
              :checked="item.selected"
              @change="cartStore.toggleSelect(item.id)"
              class="w-5 h-5 text-primary-600 rounded"
            />
            <img
              :src="item.product?.coverImage"
              class="w-20 h-20 object-cover rounded-lg"
            />
            <div class="flex-1 min-w-0">
              <p class="font-medium text-gray-900 truncate">{{ item.product?.title }}</p>
              <p class="text-xs text-gray-500 mt-1">{{ item.product?.campus || '' }}</p>
            </div>
            <div class="flex items-center space-x-2">
              <button
                @click="cartStore.updateQuantity(item.id, item.quantity - 1)"
                class="w-7 h-7 rounded border flex items-center justify-center hover:bg-gray-50"
                :disabled="item.quantity <= 1"
              >-</button>
              <span class="w-8 text-center text-sm">{{ item.quantity }}</span>
              <button
                @click="cartStore.updateQuantity(item.id, item.quantity + 1)"
                class="w-7 h-7 rounded border flex items-center justify-center hover:bg-gray-50"
              >+</button>
            </div>
            <div class="text-right min-w-[80px]">
              <p class="text-accent-600 font-bold">&yen;{{ (item.product?.price || 0) * item.quantity }}</p>
              <p class="text-xs text-gray-400">单价 &yen;{{ item.product?.price || 0 }}</p>
            </div>
            <button @click="cartStore.removeItem(item.id)" class="text-gray-400 hover:text-red-500 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Bottom Bar -->
        <div class="sticky bottom-0 bg-white border-t mt-6 p-4 rounded-t-xl shadow-lg">
          <div class="flex items-center justify-between">
            <div class="text-sm text-gray-600">
              已选 <span class="font-bold text-accent-600">{{ cartStore.totalCount }}</span> 件
            </div>
            <div class="flex items-center space-x-4">
              <span class="text-sm text-gray-500">合计：</span>
              <span class="text-2xl font-bold text-accent-600">&yen;{{ cartStore.totalAmount.toFixed(2) }}</span>
              <button
                @click="handleCheckout"
                class="btn-primary"
                :disabled="cartStore.totalCount === 0"
              >
                去结算
              </button>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="text-center py-16 text-gray-400">
        <svg class="w-20 h-20 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 100 4 2 2 0 000-4z" />
        </svg>
        <p class="text-lg mb-4">购物车还是空的</p>
        <router-link to="/products" class="btn-primary inline-block">去逛逛</router-link>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCart()
})

async function handleCheckout() {
  try {
    const res = await orderApi.createOrder({
      cartItemIds: cartStore.selectedIds
    })
    if (res.data?.code === 200) {
      router.push('/orders')
    }
  } catch {
    // ignore
  }
}
</script>
