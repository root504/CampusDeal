<template>
  <footer class="bg-gray-800 text-gray-300 mt-auto">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <div class="flex items-center space-x-2 mb-4">
            <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span class="text-white font-bold text-sm">CD</span>
            </div>
            <span class="text-lg font-bold text-white">CampusDeal</span>
          </div>
          <p class="text-sm text-gray-400">校园二手交易平台，安全便捷的闲置物品流转渠道。</p>
        </div>
        <div>
          <h4 class="text-white font-medium mb-4">快速链接</h4>
          <ul class="space-y-2 text-sm">
            <li><router-link to="/products" class="hover:text-white transition-colors">所有商品</router-link></li>
            <li><router-link to="/publish" class="hover:text-white transition-colors">发布商品</router-link></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-medium mb-4">帮助中心</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="hover:text-white transition-colors">新手指南</a></li>
            <li><a href="#" class="hover:text-white transition-colors">交易规则</a></li>
            <li><a href="#" class="hover:text-white transition-colors">安全提示</a></li>
          </ul>
        </div>
        <div>
          <h4 class="text-white font-medium mb-4">关于我们</h4>
          <ul class="space-y-2 text-sm">
            <li><a href="#" class="hover:text-white transition-colors">平台介绍</a></li>
            <li><a href="#" class="hover:text-white transition-colors">联系我们</a></li>
            <li><a href="#" class="hover:text-white transition-colors">用户协议</a></li>
          </ul>
        </div>
      </div>
      <div class="border-t border-gray-700 mt-8 pt-8 text-center text-sm text-gray-400">
        <p>&copy; 2026 CampusDeal. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>
