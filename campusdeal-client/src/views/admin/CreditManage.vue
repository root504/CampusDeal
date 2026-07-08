<template>
  <div>
    <div class="config-grid">
      <div
        v-for="section in sections"
        :key="section.path"
        class="config-square"
        @click="navigateTo(section.path)"
      >
        <div class="config-square-icon" :style="{ backgroundColor: section.color }">
          <svg class="config-square-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="section.icon" />
          </svg>
        </div>
        <h3 class="config-square-title">{{ section.title }}</h3>
        <p class="config-square-desc">{{ section.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const sections = [
  {
    path: '/admin/credit-rules',
    title: '信誉分规则',
    desc: '配置加分/减分规则，自动更新用户信誉分',
    icon: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z',
    color: '#22c55e'
  },
  {
    path: '/admin/order-rules',
    title: '订单规则',
    desc: '配置订单超时取消及订单相关规则',
    icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
    color: '#f59e0b'
  },
  {
    path: '/admin/sensitive-words',
    title: '敏感词过滤',
    desc: '管理平台敏感词库，自动过滤违规内容',
    icon: 'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z',
    color: '#a855f7'
  }
]

function navigateTo(path) {
  router.push(path)
}
</script>

<style scoped>
.config-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .config-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.config-square {
  border: 2px solid #e5e7eb;
  border-radius: 20px;
  padding: 36px 28px 32px;
  cursor: pointer;
  transition: all 0.25s ease;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 180px;
}

.config-square:hover {
  border-color: #6366f1;
  box-shadow: 0 8px 30px rgba(99, 102, 241, 0.18);
  transform: translateY(-4px);
}

.config-square-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.config-square-svg {
  width: 32px;
  height: 32px;
  color: white;
}

.config-square-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8px;
  line-height: 1.3;
}

.config-square-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}
</style>
