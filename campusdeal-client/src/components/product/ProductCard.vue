<template>
  <div class="card overflow-hidden cursor-pointer group" @click="$router.push(`/products/${product.id}`)">
    <div class="relative aspect-square overflow-hidden bg-gray-100">
      <img
        :src="product.coverImage || product.image || '/placeholder.jpg'"
        :alt="product.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div v-if="discount > 0" class="absolute top-3 left-3 bg-accent-500 text-white text-xs font-bold px-2 py-1 rounded-full">
        {{ discount }}折
      </div>
    </div>
    <div class="p-4">
      <h3 class="text-sm font-medium text-gray-900 line-clamp-2 mb-2 min-h-[2.5rem]">
        {{ product.title }}
      </h3>
      <div class="flex items-baseline space-x-2 mb-2">
        <span class="text-lg font-bold text-accent-600">&yen;{{ product.price }}</span>
        <span v-if="product.originalPrice" class="text-xs text-gray-400 line-through">&yen;{{ product.originalPrice }}</span>
      </div>
      <div class="flex items-center justify-between text-xs text-gray-500">
        <span>{{ product.campus || '未知校区' }}</span>
        <span>{{ product.viewCount || 0 }} 浏览</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const discount = computed(() => {
  if (!props.product.originalPrice || props.product.originalPrice <= 0) return 0
  const d = Math.round((props.product.price / props.product.originalPrice) * 10)
  return d < 10 ? d : 0
})
</script>
