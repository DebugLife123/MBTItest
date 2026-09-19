<template>
  <div class="dashboard-container">
    <div class="page-header">
      <div>
        <h2>管理后台</h2>
        <p>测评运营概览与内容维护入口</p>
      </div>
      <el-button @click="loadStatistics">刷新数据</el-button>
    </div>

    <el-row :gutter="24">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon :size="30"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="quick-card">
      <template #header><span class="card-title">快速导航</span></template>
      <div class="quick-links">
        <el-button type="primary" @click="$router.push('/admin/users')">用户管理</el-button>
        <el-button type="success" @click="$router.push('/admin/analytics')">数据分析</el-button>
        <el-button type="warning" @click="$router.push('/admin/questions')">题目管理</el-button>
        <el-button type="primary" plain @click="$router.push('/admin/ai-analysis')">AI 团队分析</el-button>
        <el-button type="info" @click="$router.push('/history')">测评记录</el-button>
        <el-button type="info" plain @click="$router.push('/admin/audit-logs')">审计日志</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, CircleCheck, Document, TrendCharts } from '@element-plus/icons-vue'
import { adminApi, type UserStatistics } from '@/api/admin'

const statistics = ref<Partial<UserStatistics>>({})
const statCards = computed(() => [
  { label: '总用户数', value: statistics.value.totalUsers ?? 0, color: '#3b63d8', icon: User },
  { label: '活跃用户', value: statistics.value.activeUsers ?? 0, color: '#3f8f72', icon: CircleCheck },
  { label: '总测评次数', value: statistics.value.totalAttempts ?? 0, color: '#c9843a', icon: Document },
  { label: '完成率', value: (statistics.value.completionRate ?? 0).toFixed(1) + '%', color: '#c45656', icon: TrendCharts }
])

const loadStatistics = async () => {
  try {
    const response = await adminApi.getStatistics()
    if (response.code === 0) statistics.value = response.data
    else ElMessage.error(response.message || '加载统计数据失败')
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  }
}

onMounted(loadStatistics)
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.page-header p {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  margin-bottom: 4px;
  font-size: 24px;
  font-weight: 700;
  font-variant: tabular-nums;
}

.stat-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.quick-card {
  margin-top: 4px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
