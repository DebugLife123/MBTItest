<template>
  <div class="home-container">
    <el-container class="layout-container">
      <!-- ============================================================
           左侧固定侧边栏（220px）：产品标题 + 纵向导航
           ============================================================ -->
      <el-aside :width="isCollapsed ? '64px' : '220px'" class="app-aside">
        <!-- 顶部产品标题 -->
        <div class="aside-brand" @click="router.push('/home')">
          <span class="brand-mark">M</span>
          <transition name="fade">
            <span v-if="!isCollapsed" class="brand-name">MBTI 职业性格测评系统</span>
          </transition>
        </div>

        <!-- 侧边纵向导航（选中项用主色高亮，hover 微过渡） -->
        <el-menu
          class="aside-menu"
          :collapse="isCollapsed"
          :default-active="activeMenu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="history">
            <el-icon><Clock /></el-icon>
            <template #title>测评历史</template>
          </el-menu-item>

          <el-menu-item index="ai">
            <el-icon><ChatLineRound /></el-icon>
            <template #title>AI 智能咨询</template>
          </el-menu-item>

          <el-menu-item index="growth">
            <el-icon><TrendCharts /></el-icon>
            <template #title>成长中心</template>
          </el-menu-item>

          <el-menu-item index="career">
            <el-icon><Suitcase /></el-icon>
            <template #title>职业建议</template>
          </el-menu-item>

          <el-menu-item index="compatibility">
            <el-icon><Connection /></el-icon>
            <template #title>性格匹配</template>
          </el-menu-item>

          <el-menu-item index="export">
            <el-icon><Download /></el-icon>
            <template #title>导出历史</template>
          </el-menu-item>

          <el-menu-item v-if="userStore.user?.role === 'ADMIN'" index="admin">
            <el-icon><Setting /></el-icon>
            <template #title>管理后台</template>
          </el-menu-item>

          <!-- 退出登录：与其他功能项分组 -->
          <el-menu-item index="logout" class="menu-logout">
            <el-icon><SwitchButton /></el-icon>
            <template #title>退出登录</template>
          </el-menu-item>
        </el-menu>

        <!-- 底部折叠开关（移动端/小屏使用） -->
        <div class="aside-collapse" @click="isCollapsed = !isCollapsed">
          <el-icon>
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <span v-if="!isCollapsed">收起导航</span>
        </div>
      </el-aside>

      <!-- ============================================================
           右侧：极简状态栏 + 主内容区
           ============================================================ -->
      <el-container class="main-container">
        <!-- 极简状态栏：仅显示当前登录用户，无下拉 -->
        <el-header class="app-header" height="56px">
          <div class="header-title">{{ pageTitle }}</div>
          <div class="header-user">
            <span class="user-avatar">{{ userInitial }}</span>
            <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
          </div>
        </el-header>

        <el-main class="app-main">
          <div class="main-inner">
            <!-- ============ 第一行：欢迎卡片 + 测评统计 ============ -->
            <el-row :gutter="20">
              <!-- 欢迎卡片 -->
              <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
                <el-card class="welcome-card anim-in" shadow="never">
                  <template #header>
                    <div class="card-header">欢迎来到 MBTI 职业性格测评</div>
                  </template>

                  <p class="welcome-desc">
                    MBTI 从四个维度评估你的性格倾向，帮助你认识自己、探索职业方向。
                  </p>

                  <!-- 四个维度：低饱和辅助色标签块 -->
                  <div class="dim-grid">
                    <div class="dim-block dim-block--indigo">
                      <div class="dim-block__pair">E · I</div>
                      <div class="dim-block__name">能量来源</div>
                      <div class="dim-block__desc">外向 / 内向</div>
                    </div>
                    <div class="dim-block dim-block--mint">
                      <div class="dim-block__pair">S · N</div>
                      <div class="dim-block__name">信息收集</div>
                      <div class="dim-block__desc">感觉 / 直觉</div>
                    </div>
                    <div class="dim-block dim-block--gray">
                      <div class="dim-block__pair">T · F</div>
                      <div class="dim-block__name">决策方式</div>
                      <div class="dim-block__desc">思考 / 情感</div>
                    </div>
                    <div class="dim-block dim-block--coral">
                      <div class="dim-block__pair">J · P</div>
                      <div class="dim-block__name">生活方式</div>
                      <div class="dim-block__desc">判断 / 知觉</div>
                    </div>
                  </div>

                  <!-- 操作按钮（保留原有业务逻辑） -->
                  <div class="action-buttons">
                    <el-button type="primary" size="large" @click="startTest">开始测评</el-button>
                    <el-button size="large" @click="viewHistory">查看历史记录</el-button>
                  </div>
                </el-card>
              </el-col>

              <!-- 测评统计卡片 -->
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-card class="stats-card anim-in anim-in--delay" shadow="never">
                  <template #header>
                    <div class="card-header">测评统计</div>
                  </template>

                  <div class="stats-top">
                    <div class="stats-left">
                      <el-statistic title="已完成测评" :value="stats.completedTests" />
                      <div v-if="stats.latestType" class="latest-result">
                        <div class="result-label">最近测评结果</div>
                        <MbtiTag :code="stats.latestType" size="large" show-name />
                        <div class="result-date">{{ stats.latestDate }}</div>
                      </div>
                    </div>

                    <!-- ECharts 迷你雷达缩略图 -->
                    <div v-if="stats.latestType" ref="radarRef" class="radar-chart"></div>
                  </div>

                  <div v-if="!stats.latestType" class="no-result">
                    <el-empty description="暂无测评记录" :image-size="60" />
                  </div>
                </el-card>

                <!-- 快捷功能小卡片 -->
                <div class="quick-grid">
                  <div class="quick-card" @click="startTest">
                    <el-icon class="quick-card__icon quick-card__icon--indigo"><EditPen /></el-icon>
                    <span>开始测评</span>
                  </div>
                  <div class="quick-card" @click="handleMenuSelect('history')">
                    <el-icon class="quick-card__icon quick-card__icon--mint"><Clock /></el-icon>
                    <span>测评历史</span>
                  </div>
                  <div class="quick-card" @click="handleMenuSelect('ai')">
                    <el-icon class="quick-card__icon quick-card__icon--coral"><ChatLineRound /></el-icon>
                    <span>AI 咨询</span>
                  </div>
                  <div class="quick-card" @click="handleMenuSelect('growth')">
                    <el-icon class="quick-card__icon quick-card__icon--gray"><TrendCharts /></el-icon>
                    <span>成长中心</span>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- ============ 最近测评记录 ============ -->
            <el-card class="recent-card anim-in anim-in--delay2" shadow="never">
              <template #header>
                <div class="card-header card-header--row">
                  <span>最近测评记录</span>
                  <el-button link type="primary" @click="viewHistory">查看全部</el-button>
                </div>
              </template>

              <template v-if="recentRecords.length > 0">
                <div
                  v-for="record in recentRecords"
                  :key="record.id"
                  class="record-row"
                  @click="router.push(`/result/${record.id}`)"
                >
                  <MbtiTag :code="record.typeCode" show-name />
                  <span class="record-date">{{ formatDate(record.createdAt) }}</span>
                  <el-button link type="primary" class="record-link">查看详情</el-button>
                </div>
              </template>

              <el-empty v-else description="暂无测评记录，开始你的第一次测评吧" :image-size="80">
                <el-button type="primary" @click="startTest">开始测评</el-button>
              </el-empty>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { assessmentApi } from '@/api/assessment'
import MbtiTag from '@/components/MbtiTag.vue'
import * as echarts from 'echarts'
import {
  Clock,
  ChatLineRound,
  TrendCharts,
  Suitcase,
  Connection,
  Download,
  Setting,
  SwitchButton,
  EditPen,
  Expand,
  Fold
} from '@element-plus/icons-vue'

// ------------------------------------------------------------
// 路由与状态
// ------------------------------------------------------------
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)

// 欢迎卡片标题（极简，不带日期）
const pageTitle = '测评总览'

// 用户名首字母（顶栏头像）
const userInitial = computed(() => {
  const name = userStore.user?.nickname || userStore.user?.username || '?'
  return name.charAt(0).toUpperCase()
})

// ------------------------------------------------------------
// 测评统计 & 最近记录（沿用原接口，不改业务逻辑）
// ------------------------------------------------------------
const stats = ref({
  completedTests: 0,
  latestType: '',
  latestDate: ''
})

interface RecentRecord {
  id: number
  typeCode: string
  createdAt: string
  [key: string]: any
}

const recentRecords = ref<RecentRecord[]>([])

// ECharts 雷达图实例与 DOM 引用
const radarRef = ref<HTMLElement>()
let radarChart: echarts.ECharts | null = null

// 最新测评的八维得分（用于迷你雷达图）
const latestScores = ref<number[]>([])

const loadStats = async () => {
  try {
    const res = await assessmentApi.getMyResults(0, 3)
    if (res.code === 0 && res.data) {
      stats.value.completedTests = res.data.totalElements
      recentRecords.value = res.data.content.slice(0, 3)
      if (res.data.content.length > 0) {
        const latest = res.data.content[0]
        stats.value.latestType = latest.typeCode
        stats.value.latestDate = new Date(latest.createdAt).toLocaleDateString('zh-CN')
        latestScores.value = [
          latest.eScore, latest.iScore,
          latest.sScore, latest.nScore,
          latest.tScore, latest.fScore,
          latest.jScore, latest.pScore
        ]
      }
    }
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

// ------------------------------------------------------------
// ECharts 迷你雷达图（纯展示，不改业务）
// ------------------------------------------------------------
const initRadar = () => {
  if (!radarRef.value || !stats.value.latestType) return
  radarChart = echarts.init(radarRef.value)
  radarChart.setOption({
    radar: {
      indicator: [
        { name: 'E', max: 100 }, { name: 'I', max: 100 },
        { name: 'S', max: 100 }, { name: 'N', max: 100 },
        { name: 'T', max: 100 }, { name: 'F', max: 100 },
        { name: 'J', max: 100 }, { name: 'P', max: 100 }
      ],
      radius: '64%',
      center: ['50%', '52%'],
      axisName: { color: '#6b7280', fontSize: 10 },
      splitLine: { lineStyle: { color: '#e4e9f0' } },
      splitArea: { show: false },
      axisLine: { lineStyle: { color: '#eef2f6' } }
    },
    series: [{
      type: 'radar',
      symbol: 'none',
      lineStyle: { color: '#3b63d8', width: 1.5 },
      areaStyle: { color: 'rgba(59, 99, 216, 0.08)' },
      data: [{ value: latestScores.value }]
    }]
  })
}

const resizeRadar = () => radarChart?.resize()

// ------------------------------------------------------------
// 菜单与业务操作（路由跳转全部保留）
// ------------------------------------------------------------
const activeMenu = ref('')

const startTest = () => { router.push('/assessment') }
const viewHistory = () => { router.push('/history') }

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('zh-CN')

const handleMenuSelect = async (index: string) => {
  switch (index) {
    case 'history': router.push('/history'); break
    case 'ai': router.push('/ai-chat'); break
    case 'growth': router.push('/user-center?tab=growth'); break
    case 'career': router.push('/user-center?tab=career'); break
    case 'compatibility': router.push('/user-center?tab=matching'); break
    case 'export': router.push('/user-center?tab=export'); break
    case 'admin': router.push('/admin'); break
    case 'logout':
      await userStore.logout()
      router.push('/login')
      break
  }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  initRadar()
  window.addEventListener('resize', resizeRadar)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeRadar)
  radarChart?.dispose()
})
</script>

<style scoped>
/* ============================================================
 * 页面骨架
 * ============================================================ */
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.layout-container {
  min-height: 100vh;
}

/* ============================================================
 * 左侧侧边栏：固定 220px，浅冷灰底
 * ============================================================ */
.app-aside {
  display: flex;
  flex-direction: column;
  background: #f2f4f7;
  border-right: 1px solid var(--el-border-color-extra-light);
  transition: width var(--app-duration) var(--app-ease);
}

/* 顶部产品标题 */
.aside-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 8px;
  background: var(--el-color-primary);
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
}

.brand-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 0.01em;
  white-space: nowrap;
  overflow: hidden;
}

/* 导航菜单 */
.aside-menu {
  flex: 1;
  border-right: none;
  padding: 8px;
  background: transparent;
}

.aside-menu :deep(.el-menu-item) {
  height: 42px;
  margin-bottom: 2px;
  border-radius: 8px;
  color: var(--el-text-color-regular);
  transition:
    background-color var(--app-duration) var(--app-ease),
    color var(--app-duration) var(--app-ease),
    transform var(--app-duration) var(--app-ease);
}

.aside-menu :deep(.el-menu-item:hover) {
  background: rgba(59, 99, 216, 0.06);
  color: var(--el-color-primary);
}

.aside-menu :deep(.el-menu-item.is-active) {
  background: rgba(59, 99, 216, 0.1);
  color: var(--el-color-primary);
  font-weight: 600;
}

/* 退出登录：与其他项分组 */
.aside-menu :deep(.menu-logout) {
  margin-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  border-radius: 0 0 8px 8px;
  padding-top: 6px;
}

.aside-menu :deep(.menu-logout:hover) {
  background: rgba(196, 86, 86, 0.06);
  color: var(--el-color-danger);
}

/* 底部折叠开关 */
.aside-collapse {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  border-top: 1px solid var(--el-border-color-lighter);
  transition: color var(--app-duration) var(--app-ease);
  flex-shrink: 0;
}

.aside-collapse:hover {
  color: var(--el-color-primary);
}

/* ============================================================
 * 右侧：极简状态栏 + 主内容
 * ============================================================ */
.main-container {
  min-height: 100vh;
}

/* 极简状态栏：只显示当前用户，无下拉 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: none;
  padding: 0 24px;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(59, 99, 216, 0.1);
  color: var(--el-color-primary);
  font-size: 13px;
  font-weight: 600;
}

.user-name {
  font-size: 13px;
  color: var(--el-text-color-regular);
}

/* ============================================================
 * 主内容区
 * ============================================================ */
.app-main {
  padding: 24px;
}

.main-inner {
  max-width: 1280px;
  margin: 0 auto;
}

/* ------------------------------------------------------------
 * 卡片通用：圆角 14px，软阴影，hover 上移 2px
 * ------------------------------------------------------------ */
.welcome-card,
.stats-card,
.recent-card {
  --el-card-border-radius: 14px;
  --el-card-padding: 24px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.07);
  margin-bottom: 20px;
  transition:
    transform var(--app-duration) var(--app-ease),
    box-shadow var(--app-duration) var(--app-ease);
}

.welcome-card:hover,
.stats-card:hover,
.recent-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(31, 41, 55, 0.1);
}

.card-header {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.card-header--row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ------------------------------------------------------------
 * 欢迎卡片
 * ------------------------------------------------------------ */
.welcome-desc {
  margin: 0 0 20px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-secondary);
}

/* 四个维度标签块 */
.dim-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.dim-block {
  padding: 14px 16px;
  border-radius: 10px;
  transition:
    transform var(--app-duration) var(--app-ease),
    box-shadow var(--app-duration) var(--app-ease);
}

.dim-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(31, 41, 55, 0.08);
}

.dim-block--indigo { background: #e8eefc; }
.dim-block--mint   { background: #e7f6f3; }
.dim-block--gray   { background: #eef1f5; }
.dim-block--coral  { background: #fdeee8; }

.dim-block__pair {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}

.dim-block--indigo .dim-block__pair { color: #2c4cb3; }
.dim-block--mint   .dim-block__pair { color: #2f7f74; }
.dim-block--gray   .dim-block__pair { color: #4b5563; }
.dim-block--coral  .dim-block__pair { color: #c45c3d; }

.dim-block__name {
  font-size: 12px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.dim-block__desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
}

/* ------------------------------------------------------------
 * 测评统计卡片
 * ------------------------------------------------------------ */
.stats-top {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.stats-left {
  flex: 1;
}

.latest-result {
  margin-top: 16px;
}

.result-label {
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.result-date {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

/* 迷你雷达图 */
.radar-chart {
  width: 140px;
  height: 140px;
  flex-shrink: 0;
}

.no-result {
  padding: 12px 0;
}

/* 快捷功能小卡片 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  font-size: 12px;
  color: var(--el-text-color-regular);
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.07);
  transition:
    transform var(--app-duration) var(--app-ease),
    box-shadow var(--app-duration) var(--app-ease);
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(31, 41, 55, 0.1);
}

.quick-card__icon {
  font-size: 20px;
}

.quick-card__icon--indigo { color: #3b63d8; }
.quick-card__icon--mint   { color: #64bcac; }
.quick-card__icon--coral  { color: #f28c68; }
.quick-card__icon--gray   { color: #6b7280; }

/* ------------------------------------------------------------
 * 最近测评记录
 * ------------------------------------------------------------ */
.record-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 4px;
  border-bottom: 1px solid var(--el-border-color-extra-light);
  cursor: pointer;
  transition: background-color var(--app-duration) var(--app-ease);
}

.record-row:last-child {
  border-bottom: none;
}

.record-row:hover {
  background: var(--el-fill-color-lighter);
}

.record-date {
  flex: 1;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.record-link {
  font-size: 13px;
}

/* ------------------------------------------------------------
 * 入场动效：淡入 + 上移，0.3s
 * ------------------------------------------------------------ */
.anim-in {
  animation: card-in 0.3s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.anim-in--delay {
  animation-delay: 0.06s;
}

.anim-in--delay2 {
  animation-delay: 0.12s;
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 折叠时隐藏文字 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--app-duration) var(--app-ease);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ============================================================
 * 响应式：小屏折叠侧边栏
 * ============================================================ */
@media (max-width: 1024px) {
  .app-aside {
    width: 64px !important;
  }

  .brand-name,
  .aside-collapse span {
    display: none;
  }

  .aside-collapse {
    justify-content: center;
    padding: 0;
  }
}

@media (max-width: 768px) {
  .app-main {
    padding: 16px;
  }

  .dim-grid {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-top {
    flex-direction: column;
  }

  .radar-chart {
    width: 100%;
    height: 160px;
  }
}
</style>
