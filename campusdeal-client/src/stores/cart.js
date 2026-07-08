import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const selectedItems = computed(() => items.value.filter(item => item.selected))
  const selectedIds = computed(() => selectedItems.value.map(item => item.id))

  const totalAmount = computed(() => {
    return selectedItems.value.reduce((sum, item) => {
      const price = item.product?.price || 0
      return sum + price * item.quantity
    }, 0)
  })

  const totalCount = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const isAllSelected = computed(() => {
    return items.value.length > 0 && items.value.every(item => item.selected)
  })

  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.getCart()
      if (res.data.code === 200) {
        items.value = res.data.data || []
      }
    } finally {
      loading.value = false
    }
  }

  async function addToCart(productId, quantity = 1) {
    const res = await cartApi.addToCart({ productId, quantity })
    if (res.data.code === 200) {
      await fetchCart()
    }
    return res.data
  }

  async function updateQuantity(id, quantity) {
    if (quantity < 1) quantity = 1
    const res = await cartApi.updateQuantity(id, { quantity })
    if (res.data.code === 200) {
      const item = items.value.find(i => i.id === id)
      if (item) item.quantity = quantity
    }
    return res.data
  }

  async function toggleSelect(id) {
    const item = items.value.find(i => i.id === id)
    if (item) {
      item.selected = !item.selected
      await cartApi.toggleSelect(id)
    }
  }

  async function toggleSelectAll() {
    const newState = !isAllSelected.value
    items.value.forEach(item => (item.selected = newState))
    await cartApi.selectAll(newState)
  }

  async function removeItem(id) {
    const res = await cartApi.removeItem(id)
    if (res.data.code === 200) {
      items.value = items.value.filter(i => i.id !== id)
    }
    return res.data
  }

  async function removeBatch(ids) {
    const res = await cartApi.removeBatch(ids)
    if (res.data.code === 200) {
      items.value = items.value.filter(i => !ids.includes(i.id))
    }
    return res.data
  }

  return {
    items,
    loading,
    selectedItems,
    selectedIds,
    totalAmount,
    totalCount,
    isAllSelected,
    fetchCart,
    addToCart,
    updateQuantity,
    toggleSelect,
    toggleSelectAll,
    removeItem,
    removeBatch
  }
})
