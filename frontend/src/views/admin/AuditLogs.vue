<template>
  <div class="audit-page">
    <div class="page-header">
      <div>
        <h2>操作审计日志</h2>
        <p>用户登录、测评提交、管理端变更等关键操作留痕，支持按操作类型与操作人筛选</p>
      </div>
      <el-button @click="loadLogs">刷新</el-button>
    </div>

    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <el-form inline @submit.prevent>
        <el-form-item label="操作类型">
          <el-input v-model="filters.action" placeholder="如 AUTH_LOGIN / ADMIN_UPDATE_USER" clearable style="width: 260px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="filters.username" placeholder="用户名模糊匹配" clearable style="width: 180px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="logs" stripe>
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="120">
          <template #default="{ row }">{{ row.username ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="220">
          <template #default="{ row }">
            <el-tag size="small" :type="tagType(row.action)" effect="plain">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求" min-width="220">
          <template #default="{ row }">
            <span class="method">{{ row.httpMethod }}</span>
            <span class="path">{{ row.path }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="130">
          <template #default="{ row }">{{ row.ip ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.success ? 'success' : 'danger'" effect="light">
              {{ row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.costMs == null ? '-' : row.costMs + ' ms' }}</template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="失败原因" min-width="160">
          <template #default="{ row }">
            <span v-if="row.errorMessage" class="error-text">{{ row.errorMessage }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无审计日志" />
        </template>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadLogs"
          @size-change="onSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi, type AuditLogItem } from '@/api/admin'

const loading = ref(false)
const logs = ref<AuditLogItem[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const filters = reactive({ action: '', username: '' })

const loadLogs = async () => {
  loading.value = true
  try {
    const response = await adminApi.getAuditLogs({
      action: filters.action || undefined,
      username: filters.username || undefined,
      page: page.value - 1,
      size: size.value
    })
    if (response.code === 0) {
      logs.value = response.data.content
      total.value = response.data.totalElements
    } else {
      ElMessage.error(response.message || '加载审计日志失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const onSearch = () => { page.value = 1; loadLogs() }
const onReset = () => { filters.action = ''; filters.username = ''; onSearch() }

const formatTime = (value: string) => (value ? value.replace('T', ' ').slice(0, 19) : '-')

const tagType = (action: string) => {
  if (action.startsWith('AUTH_')) return 'primary'
  if (action.startsWith('ADMIN_')) return 'warning'
  if (action.startsWith('AI_')) return 'success'
  return 'info'
}

onMounted(loadLogs)
</script>

<style scoped lang="scss">
.audit-page {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 20px;

  h2 { margin: 0 0 6px; font-size: 20px; color: #1f2937; }
  p { margin: 0; font-size: 13px; color: var(--el-text-color-secondary); }
}

.filter-card { margin-bottom: 16px; }

.method {
  display: inline-block;
  min-width: 46px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.path { color: var(--el-text-color-regular); }

.error-text { color: var(--el-color-danger); font-size: 12px; }

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
