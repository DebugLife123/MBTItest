<template>
  <div class="center-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <div>
            <h2>成长中心</h2>
            <p>把测评结果转化为持续可执行的职业行动</p>
          </div>
          <div class="header-actions">
            <el-button @click="$router.push('/history')">测评历史</el-button>
            <el-button type="primary" plain @click="$router.push('/ai-chat')">AI 智能咨询</el-button>
            <el-button type="primary" @click="$router.push('/assessment')">重新测评</el-button>
          </div>
        </div>
      </el-header>

      <el-main v-loading="loading">
        <el-alert v-if="!loading && !growth.length" title="完成一次测评后，这里会生成你的成长轨迹和职业建议" type="info" show-icon :closable="false">
          <template #default><el-button type="primary" link @click="$router.push('/assessment')">立即开始测评</el-button></template>
        </el-alert>

        <template v-else>
          <el-row :gutter="20">
            <el-col :xs="24" :lg="16">
              <el-card id="growth-section" shadow="never" class="section-card">
                <template #header><div class="card-header"><span>成长轨迹</span><el-tag type="info">{{ growth.length }} 次测评</el-tag></div></template>
                <el-timeline v-if="growth.length">
                  <el-timeline-item v-for="point in [...growth].reverse()" :key="point.resultId" :timestamp="formatDate(point.createdAt)" placement="top" :type="point === growth[growth.length - 1] ? 'primary' : 'success'">
                    <div class="growth-item">
                      <div class="growth-main"><MbtiTag :code="point.typeCode" show-name /><span>{{ point.typeName || '性格类型' }}</span></div>
                      <div class="growth-scores">
                        <span>E {{ point.eScore }}</span><span>I {{ point.iScore }}</span><span>S {{ point.sScore }}</span><span>N {{ point.nScore }}</span>
                        <span>T {{ point.tScore }}</span><span>F {{ point.fScore }}</span><span>J {{ point.jScore }}</span><span>P {{ point.pScore }}</span>
                      </div>
                      <div v-if="point.changedDimensions.length" class="changed">本次变化：{{ point.changedDimensions.join('、') }}</div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </el-card>

              <el-card id="career-section" shadow="never" class="section-card">
                <template #header><div class="card-header"><span>职业建议</span><MbtiTag v-if="career" :code="career.typeCode" /></div></template>
                <template v-if="career">
                  <p class="summary">{{ career.summary }}</p>
                  <h4>推荐方向</h4>
                  <div class="tag-list"><el-tag v-for="role in career.recommendedRoles" :key="role" type="primary" effect="plain">{{ role }}</el-tag></div>
                  <h4>能力提升</h4>
                  <ul class="suggestion-list"><li v-for="item in career.skillSuggestions" :key="item">{{ item }}</li></ul>
                  <el-divider />
                  <p class="career-detail">{{ career.careerSuggestions }}</p>
                </template>
                <el-empty v-else description="暂无职业建议" />
              </el-card>
            </el-col>

            <el-col :xs="24" :lg="8">
              <el-card id="matching-section" shadow="never" class="section-card">
                <template #header><span>性格匹配</span></template>
                <el-form @submit.prevent="calculateCompatibility">
                  <el-form-item label="对方用户名"><el-input v-model="otherUsername" placeholder="请输入已注册用户名" @keyup.enter="calculateCompatibility" /></el-form-item>
                  <el-button type="primary" :loading="matching" style="width: 100%" @click="calculateCompatibility">计算匹配度</el-button>
                </el-form>
                <div v-if="compatibility" class="compatibility-result">
                  <el-progress type="dashboard" :percentage="compatibility.score" :color="scoreColor" />
                  <h3>{{ compatibility.level }}</h3><p>{{ compatibility.summary }}</p>
                  <div class="match-grid"><div><strong>你</strong><span>{{ compatibility.myType }}</span></div><div><strong>对方</strong><span>{{ compatibility.otherType }}</span></div></div>
                  <div v-if="compatibility.sharedTraits.length" class="match-list"><h4>共同特质</h4><el-tag v-for="item in compatibility.sharedTraits" :key="item" type="success" effect="plain">{{ item }}</el-tag></div>
                  <div v-if="compatibility.complementaryTraits.length" class="match-list"><h4>互补特质</h4><el-tag v-for="item in compatibility.complementaryTraits" :key="item" type="warning" effect="plain">{{ item }}</el-tag></div>
                  <div v-if="compatibility.watchOuts.length" class="match-list"><h4>沟通提醒</h4><ul><li v-for="item in compatibility.watchOuts" :key="item">{{ item }}</li></ul></div>
                </div>
              </el-card>

              <el-card id="export-section" shadow="never" class="section-card">
                <template #header><span>数据导出</span></template>
                <p class="export-tip">导出全部测评记录为 CSV，可用于求职复盘或作品集数据整理。</p>
                <el-button :loading="exporting" style="width: 100%" @click="exportHistory">导出历史记录 CSV</el-button>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, type CareerAdviceResponse, type CompatibilityResponse, type GrowthPoint } from '@/api/user'
import MbtiTag from '@/components/MbtiTag.vue'

const route = useRoute()
const loading = ref(true)
const matching = ref(false)
const exporting = ref(false)
const otherUsername = ref('')
const growth = ref<GrowthPoint[]>([])
const career = ref<CareerAdviceResponse | null>(null)
const compatibility = ref<CompatibilityResponse | null>(null)

const formatDate = (value: string) => new Date(value).toLocaleString('zh-CN')
const scoreColor = (score: number) => score >= 80 ? '#67c23a' : score >= 60 ? '#409eff' : score >= 40 ? '#e6a23c' : '#f56c6c'

const loadData = async () => {
  loading.value = true
  try {
    const [growthResponse, careerResponse] = await Promise.all([userApi.getGrowthTrack(), userApi.getCareerAdvice()])
    if (growthResponse.code === 0) growth.value = growthResponse.data
    if (careerResponse.code === 0) career.value = careerResponse.data
  } catch (error: any) {
    if (error?.code !== 'NO_RESULT') ElMessage.error(error?.message || '加载成长数据失败')
  } finally {
    loading.value = false
  }
}

const calculateCompatibility = async () => {
  const username = otherUsername.value.trim()
  if (!username) { ElMessage.warning('请输入对方用户名'); return }
  matching.value = true
  try {
    const response = await userApi.getCompatibility(username)
    if (response.code === 0) compatibility.value = response.data
    else ElMessage.error(response.message || '匹配失败')
  } catch (error: any) {
    ElMessage.error(error?.message || '匹配失败，请确认对方已完成测评')
  } finally { matching.value = false }
}

const exportHistory = async () => {
  exporting.value = true
  try {
    const blob = await userApi.exportHistory()
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `mbti-history-${new Date().toISOString().slice(0, 10)}.csv`
    anchor.click()
    URL.revokeObjectURL(url)
    ElMessage.success('历史记录已导出')
  } catch (error: any) {
    ElMessage.error(error?.message || '导出失败')
  } finally { exporting.value = false }
}

const focusRequestedSection = async () => {
  const tab = typeof route.query.tab === 'string' ? route.query.tab : ''
  const target = ['growth', 'career', 'matching', 'export'].includes(tab) ? `#${tab}-section` : ''
  if (!target) return
  await nextTick()
  document.querySelector(target)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  await loadData()
  await focusRequestedSection()
})
</script>

<style scoped>
.center-container {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.el-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: none;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 16px;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-content p {
  margin: 5px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.el-main {
  padding: 24px;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.growth-item {
  line-height: 1.8;
}

.growth-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.growth-main span {
  color: var(--el-text-color-regular);
}

.growth-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-variant: tabular-nums;
}

.growth-scores span {
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
}

.changed {
  margin-top: 8px;
  color: #c45c3d;
  font-size: 13px;
}

.summary {
  color: var(--el-text-color-regular);
  line-height: 1.85;
}

.career-detail {
  color: var(--el-text-color-regular);
  line-height: 1.9;
  white-space: pre-line;
}

.tag-list,
.match-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 10px 0 18px;
}

.suggestion-list {
  padding-left: 20px;
  color: var(--el-text-color-regular);
  line-height: 2;
}

.compatibility-result {
  margin-top: 22px;
  text-align: center;
}

.compatibility-result h3 {
  margin: 12px 0 6px;
}

.compatibility-result p {
  color: var(--el-text-color-regular);
  line-height: 1.75;
  text-align: left;
}

.match-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin: 16px 0;
}

.match-grid div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  border-radius: var(--el-border-radius-small);
  background: var(--el-fill-color-lighter);
}

.match-grid strong {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 500;
}

.match-grid span {
  color: var(--el-color-primary);
  font-weight: 600;
}

.match-list {
  display: block;
  text-align: left;
}

.match-list h4 {
  margin: 14px 0 8px;
  font-size: 14px;
}

.match-list ul {
  margin: 0;
  padding-left: 20px;
  color: var(--el-text-color-regular);
  line-height: 1.85;
}

.export-tip {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.7;
}

@media (max-width: 600px) {
  .header-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .el-header {
    height: auto;
    padding: 16px;
  }
}
</style>

