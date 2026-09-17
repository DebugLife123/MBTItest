<template>
  <div class="admin-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>管理后台</h2>
          <el-button @click="$router.push('/home')">返回首页</el-button>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover">
              <el-statistic title="总用户数" :value="stats.totalUsers" />
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover">
              <el-statistic title="总测评数" :value="stats.totalTests" />
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover">
              <el-statistic title="今日测评" :value="stats.todayTests" />
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card shadow="hover">
              <el-statistic title="活跃用户" :value="stats.activeUsers" />
            </el-card>
          </el-col>
        </el-row>
        
        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <span>系统概览</span>
          </template>
          <p>管理后台功能开发中...</p>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const stats = ref({
  totalUsers: 0,
  totalTests: 0,
  todayTests: 0,
  activeUsers: 0
})

onMounted(() => {
  // TODO: 加载统计数据
  stats.value = {
    totalUsers: 100,
    totalTests: 500,
    todayTests: 20,
    activeUsers: 50
  }
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.el-header {
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #303133;
}

.el-main {
  padding: 20px;
}
</style>
