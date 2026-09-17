<template>
  <div class="users-container">
    <div class="page-header">
      <div>
        <h2>用户管理</h2>
        <p>共 {{ total }} 名注册用户</p>
      </div>
      <div class="header-actions">
        <el-input v-model="searchKeyword" placeholder="搜索用户名或邮箱" clearable style="width: 300px" @clear="handleSearch" @keyup.enter="handleSearch">
          <template #append><el-button :icon="Search" @click="handleSearch" /></template>
        </el-input>
      </div>
    </div>

    <el-table v-loading="loading" :data="userList" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }"><el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">{{ row.role }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="attemptCount" label="测评次数" width="100" />
      <el-table-column prop="lastLoginAt" label="最后登录" width="180">
        <template #default="{ row }">{{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '从未登录' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <template v-if="row.role !== 'ADMIN'">
            <el-button link type="primary" size="small" @click="toggleUserStatus(row)">{{ row.enabled ? '禁用' : '启用' }}</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
          <span v-else class="muted">受保护账号</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handlePageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi, type UserListItem } from '@/api/admin'

const loading = ref(false)
const searchKeyword = ref('')
const userList = ref<UserListItem[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatDate = (dateStr: string) => new Date(dateStr).toLocaleString('zh-CN')

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await adminApi.getUsers({
      keyword: searchKeyword.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: 'createdAt',
      direction: 'DESC'
    })
    if (response.code === 0) {
      userList.value = response.data.content
      total.value = response.data.totalElements
    } else {
      ElMessage.error(response.message || '加载用户列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; loadUsers() }
const handlePageChange = (page: number) => { currentPage.value = page; loadUsers() }
const handleSizeChange = (size: number) => { pageSize.value = size; currentPage.value = 1; loadUsers() }

const toggleUserStatus = async (user: UserListItem) => {
  try {
    await ElMessageBox.confirm(`确定${user.enabled ? '禁用' : '启用'}用户 ${user.username} 吗？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const response = await adminApi.updateUserStatus(user.id, !user.enabled)
    if (response.code === 0) { ElMessage.success('操作成功'); loadUsers() }
    else ElMessage.error(response.message || '操作失败')
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (user: UserListItem) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${user.username} 吗？此操作将删除该用户的所有测评记录且无法恢复！`, '警告', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error'
    })
    const response = await adminApi.deleteUser(user.id)
    if (response.code === 0) { ElMessage.success('删除成功'); loadUsers() }
    else ElMessage.error(response.message || '删除失败')
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.users-container { padding: 24px; }
.page-header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 24px; font-weight: 600; }
.page-header p { margin: 6px 0 0; color: #909399; }
.pagination-container { margin-top: 24px; display: flex; justify-content: flex-end; }
.muted { color: #909399; font-size: 13px; }
</style>
