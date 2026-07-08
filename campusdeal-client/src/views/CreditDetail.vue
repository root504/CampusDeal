<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full">
      <h1 class="text-2xl font-bold mb-6">信誉分</h1>

      <div class="card text-center py-12">
        <div class="text-6xl font-bold text-green-600 mb-4">{{ creditScore }}</div>
        <p class="text-gray-500 mb-6">当前信誉分</p>
        <div class="text-left max-w-md mx-auto space-y-3">
          <h3 class="font-semibold text-gray-700">信誉分说明</h3>
          <ul class="text-sm text-gray-500 space-y-2 list-disc list-inside">
            <li v-for="(rule, idx) in creditRules" :key="idx">{{ rule }}</li>
          </ul>
        </div>
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { creditApi } from '@/api/credit'

const creditScore = ref('--')
const creditRules = ref([
  '初始信誉分：100 分',
  '成功交易一笔 +1 分',
  '被投诉且核实 -5 分',
  '违规发布商品 -10 分',
  '信誉分低于 60 分将限制发布商品'
])

onMounted(async () => {
  try {
    const res = await creditApi.getMyCredit()
    if (res.data?.code === 200) {
      const data = res.data.data
      creditScore.value = data.score ?? data.creditScore ?? 100
      if (data.rules && Array.isArray(data.rules)) {
        creditRules.value = data.rules
      }
    }
  } catch {
    // fallback: 使用默认值
  }
})
</script>
