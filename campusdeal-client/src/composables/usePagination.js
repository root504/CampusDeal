import { ref, computed } from 'vue'

export function usePagination(fetchFn, defaultSize = 10) {
  const page = ref(1)
  const size = ref(defaultSize)
  const total = ref(0)
  const list = ref([])
  const loading = ref(false)
  const finished = ref(false)

  const totalPages = computed(() => Math.ceil(total.value / size.value) || 0)
  const hasMore = computed(() => list.value.length < total.value)

  async function loadData(reset = false) {
    if (loading.value) return
    if (reset) {
      page.value = 1
      list.value = []
      finished.value = false
    }
    if (finished.value) return

    loading.value = true
    try {
      const res = await fetchFn({ page: page.value, size: size.value })
      const data = res.data?.data || res.data || {}
      const records = data.records || data.content || data.list || []
      total.value = data.total || data.totalElements || records.length

      if (reset || page.value === 1) {
        list.value = records
      } else {
        list.value = [...list.value, ...records]
      }

      if (list.value.length >= total.value) {
        finished.value = true
      }
    } finally {
      loading.value = false
    }
  }

  async function nextPage() {
    if (!hasMore.value || loading.value) return
    page.value++
    await loadData(false)
  }

  async function reset() {
    await loadData(true)
  }

  return {
    page,
    size,
    total,
    list,
    loading,
    finished,
    totalPages,
    hasMore,
    loadData,
    nextPage,
    reset
  }
}
