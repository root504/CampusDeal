<template>
  <div>
    <div class="flex items-center justify-end mb-6">
      <div class="flex items-center space-x-3">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索用户..."
          class="input-field w-48"
          @keyup.enter="searchUsers"
        />
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-gray-600">用户</th>
            <th class="px-4 py-3 text-left text-gray-600">手机号</th>
            <th class="px-4 py-3 text-left text-gray-600">学校</th>
            <th class="px-4 py-3 text-left text-gray-600">角色</th>
            <th class="px-4 py-3 text-left text-gray-600">信用分</th>
            <th class="px-4 py-3 text-left text-gray-600">认证</th>
            <th class="px-4 py-3 text-left text-gray-600">状态</th>
            <th class="px-4 py-3 text-left text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3">
              <div class="flex items-center space-x-3">
                <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                  <span class="text-primary-600 text-sm font-bold">{{ user.username?.charAt(0) || 'U' }}</span>
                </div>
                <span>{{ user.username }}</span>
              </div>
            </td>
            <td class="px-4 py-3">{{ user.phone }}</td>
            <td class="px-4 py-3">{{ user.school || '-' }}</td>
            <td class="px-4 py-3">
              <span v-if="user.phone === '13900000000'" class="text-amber-600 font-medium">超级管理员</span>
              <span v-else :class="user.role === 1 ? 'text-blue-600' : 'text-gray-500'">
                {{ user.role === 1 ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td class="px-4 py-3">{{ user.creditScore }}</td>
            <td class="px-4 py-3">
              <span :class="user.isVerified ? 'text-green-600' : 'text-gray-400'">
                {{ user.isVerified ? '已认证' : '未认证' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span class="badge" :class="user.status === 1 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                {{ user.status === 1 ? '正常' : '已封禁' }}
              </span>
            </td>
            <td class="px-4 py-3">
              <div class="flex space-x-2">
                <template v-if="user.phone === '13900000000'">
                  <span class="text-xs text-gray-400">超级管理员</span>
                </template>
                <template v-else>
                  <button @click="openEdit(user)" class="text-blue-600 hover:text-blue-800 text-xs font-medium">编辑</button>
                  <button
                    @click="toggleStatus(user)"
                    class="text-xs"
                    :class="user.status === 1 ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'"
                  >
                    {{ user.status === 1 ? '封禁' : '解封' }}
                  </button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="users.length === 0" class="text-center py-8 text-gray-400">暂无用户</div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadUsers"
      />
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEdit" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-lg w-96 p-6">
        <h3 class="text-lg font-bold mb-4">编辑用户</h3>
        <div class="space-y-3">
          <div>
            <label class="block text-sm text-gray-600 mb-1">昵称</label>
            <input v-model="editForm.username" class="input-field w-full" />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">手机号</label>
            <input v-model="editForm.phone" class="input-field w-full" />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">学校</label>
            <input v-model="editForm.school" class="input-field w-full" />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">校区</label>
            <input v-model="editForm.campus" class="input-field w-full" />
          </div>
          <div class="flex items-center space-x-2 pt-2">
            <input type="checkbox" id="adminRole" v-model="editForm.role" :true-value="1" :false-value="0" />
            <label for="adminRole" class="text-sm text-gray-700">设为管理员</label>
            <span class="text-xs text-gray-400 ml-1">设为管理员后该用户可用手机号登录后台管理系统</span>
          </div>
        </div>
        <div class="flex justify-end space-x-3 mt-6">
          <button @click="showEdit = false" class="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg">取消</button>
          <button @click="saveEdit" class="px-4 py-2 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { adminApi } from '@/api/admin'

const keyword = ref('')
const users = ref([])
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const showEdit = ref(false)
const editForm = reactive({
  id: null,
  username: '',
  phone: '',
  school: '',
  campus: '',
  role: 0
})

onMounted(() => loadUsers())

async function loadUsers() {
  try {
    const params = { page: currentPage.value, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    const res = await adminApi.getUsers(params)
    users.value = res.data?.data?.list || []
    total.value = res.data?.data?.total ?? 0
  } catch {
    // ignore
  }
}

function searchUsers() {
  currentPage.value = 1
  loadUsers()
}

async function toggleStatus(user) {
  const newStatus = user.status === 1 ? 0 : 1
  await adminApi.updateUserStatus(user.id, newStatus)
  loadUsers()
}

function openEdit(user) {
  editForm.id = user.id
  editForm.username = user.username
  editForm.phone = user.phone
  editForm.school = user.school || ''
  editForm.campus = user.campus || ''
  editForm.role = user.role || 0
  showEdit.value = true
}

async function saveEdit() {
  try {
    await adminApi.editUser(editForm.id, {
      username: editForm.username,
      phone: editForm.phone,
      school: editForm.school,
      campus: editForm.campus
    })
    await adminApi.updateUserRole(editForm.id, editForm.role)
    alert('权限已更新')
    showEdit.value = false
    loadUsers()
  } catch (e) {
    alert(e?.response?.data?.message || '保存失败')
  }
}
</script>
