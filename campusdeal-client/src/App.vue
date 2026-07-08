<template>
  <div id="app" class="min-h-screen flex flex-col">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.checkAuth()
})
</script>
