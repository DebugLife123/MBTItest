<template>
  <div class="home-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>MBTI 职业性格测评系统</h1>
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                <el-icon><UserFilled /></el-icon>
                {{ userStore.user?.nickname || userStore.user?.username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="history">测评历史</el-dropdown-item>
                  <el-dropdown-item command="ai">AI 智能咨询</el-dropdown-item>
                  <el-dropdown-item command="growth">成长中心</el-dropdown-item>
                  <el-dropdown-item command="career">职业建议</el-dropdown-item>
                  <el-dropdown-item command="compatibility">性格匹配</el-dropdown-item>
                  <el-dropdown-item command="export">导出历史</el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="userStore.user?.role === 'ADMIN'">
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
            <el-card class="welcome-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>欢迎来到 MBTI 职业性格测评</span>
                </div>
              </template>
              <div class="welcome-content">
                <p>MBTI 性格测评是一种自我报告式的性格评估测试，旨在衡量人们在感知世界和做决定方面的心理倾向。</p>
                <p>该测评将从四个维度评估您的性格特征：</p>
                <ul>
                  <li><strong>外向 (E) vs 内向 (I)</strong> - 能量来源</li>
                  <li><strong>感觉 (S) vs 直觉 (N)</strong> - 信息收集方式</li>
                  <li><strong>思考 (T) vs 情感 (F)</strong> - 决策方式</li>
                  <li><strong>判断 (J) vs 知觉 (P)</strong> - 生活方式</li>
                </ul>
                <p>测评共有 36 道题目，请根据您的真实情况选择最符合的选项。</p>
                <div class="action-buttons">
                  <el-button type="primary" size="large" @click="startTest">
                    开始测评
                  </el-button>
                  <el-button size="large" @click="viewHistory">
                    查看历史记录
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-card class="stats-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>测评统计</span>
                </div>
              </template>
              <div class="stats-content">
                <el-statistic title="已完成测评" :value="stats.completedTests" />
                <el-divider />
                <div v-if="stats.latestType" class="latest-result">
                  <div class="result-label">最近测评结果</div>
                  <div class="result-type">
                    <MbtiTag :code="stats.latestType" size="large" show-name />
                  </div>
                  <div class="result-date">{{ stats.latestDate }}</div>
                </div>
                <div v-else class="no-result">
                  <el-empty description="暂无测评记录" :image-size="60" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { assessmentApi } from '@/api/assessment'
import MbtiTag from '@/components/MbtiTag.vue'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  completedTests: 0,
  latestType: '',
  latestDate: ''
})

const loadStats = async () => {
  try {
    const res = await assessmentApi.getMyResults(0, 1)
    if (res.code === 0 && res.data) {
      stats.value.completedTests = res.data.totalElements
      if (res.data.content.length > 0) {
        const latest = res.data.content[0]
        stats.value.latestType = latest.typeCode
        stats.value.latestDate = new Date(latest.createdAt).toLocaleDateString('zh-CN')
      }
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const startTest = () => {
  router.push('/assessment')
}

const viewHistory = () => {
  router.push('/history')
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'history':
      router.push('/history')
      break
    case 'ai':
      router.push('/ai-chat')
      break
    case 'growth':
      router.push('/user-center?tab=growth')
      break
    case 'career':
      router.push('/user-center?tab=career')
      break
    case 'compatibility':
      router.push('/user-center?tab=matching')
      break
    case 'export':
      router.push('/user-center?tab=export')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/login')
      break
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.el-header {
  display: flex;
  align-items: center;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: none;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-content h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.header-content h1::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 10px;
  border-radius: 3px;
  background: var(--el-color-primary);
  vertical-align: middle;
}

.user-info {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: var(--el-border-radius-small);
  color: var(--el-text-color-regular);
  cursor: pointer;
}

.el-dropdown-link:hover {
  background: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.el-main {
  padding: 24px;
}

.welcome-card,
.stats-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 15px;
  font-weight: 600;
}

.welcome-content {
  color: var(--el-text-color-regular);
  line-height: 1.85;
}

.welcome-content p {
  margin: 12px 0;
}

.welcome-content ul {
  margin: 16px 0;
  padding-left: 22px;
}

.welcome-content li {
  margin: 8px 0;
}

.welcome-content strong {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.stats-content {
  text-align: center;
}

.latest-result {
  margin-top: 16px;
}

.result-label {
  margin-bottom: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.result-type {
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.result-date {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.no-result {
  padding: 20px 0;
}
</style>
