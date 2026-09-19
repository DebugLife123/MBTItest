<template>
  <div class="result-container">
    <el-container v-if="!loading && result">
      <el-header>
        <div class="header-content">
          <h2>测评结果</h2>
          <el-button @click="$router.push('/home')">返回首页</el-button>
        </div>
      </el-header>
      
      <el-main>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="16" :lg="16">
            <el-card class="result-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>您的性格类型</span>
                </div>
              </template>
              
              <div class="personality-type">
                <div class="type-code">
                  <MbtiTag :code="result.typeCode" size="large" show-name />
                </div>
                <div class="type-name">{{ result.personality.typeName }}</div>
              </div>
              
              <el-divider />
              
              <div class="description-section">
                <h3>性格描述</h3>
                <p>{{ result.personality.description }}</p>
              </div>
              
              <div class="description-section" v-if="result.personality.strengths">
                <h3>优势特点</h3>
                <p>{{ result.personality.strengths }}</p>
              </div>
              
              <div class="description-section" v-if="result.personality.weaknesses">
                <h3>需要注意</h3>
                <p>{{ result.personality.weaknesses }}</p>
              </div>
              
              <div class="description-section" v-if="result.personality.careerSuggestions">
                <h3>职业建议</h3>
                <p>{{ result.personality.careerSuggestions }}</p>
              </div>
            </el-card>
          </el-col>
          
          <el-col :xs="24" :sm="24" :md="8" :lg="8">
            <el-card class="dimensions-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>维度得分</span>
                </div>
              </template>
              
              <div ref="chartRef" style="width: 100%; height: 400px;"></div>
            </el-card>
            
            <el-card class="actions-card" shadow="hover" style="margin-top: 20px;">
              <el-button type="primary" @click="$router.push('/assessment')" style="width: 100%;">
                重新测评
              </el-button>
              <el-button @click="$router.push('/history')" style="width: 100%; margin-top: 12px;">
                查看历史
              </el-button>
              <el-button type="primary" @click="askAi" style="width: 100%; margin-top: 12px;">
                AI 深度解读
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
    
    <div v-else-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载测评结果中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { assessmentApi } from '@/api/assessment'
import type { TestResult } from '@/types'
import MbtiTag from '@/components/MbtiTag.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const result = ref<TestResult | null>(null)
const chartRef = ref<HTMLElement>()

const askAi = () => {
  router.push({
    path: '/ai-chat',
    query: {
      prompt: `请结合我的 ${result.value?.typeCode} 测评结果，分析职业优势、适合方向与下一步行动。`
    }
  })
}

const loadResult = async () => {
  const resultId = Number(route.params.id)
  if (!resultId) {
    ElMessage.error('无效的结果ID')
    router.push('/home')
    return
  }
  
  try {
    const res = await assessmentApi.getResult(resultId)
    if (res.code !== 0 || !res.data) {
      ElMessage.error(res.message || '加载结果失败')
      router.push('/home')
      return
    }
    
    result.value = res.data
    loading.value = false
    
    // 等待DOM更新后渲染图表
    setTimeout(() => {
      renderChart()
    }, 100)
  } catch (error: any) {
    ElMessage.error(error.message || '加载结果失败')
    router.push('/home')
  }
}

const renderChart = () => {
  if (!chartRef.value || !result.value) return
  
  const chart = echarts.init(chartRef.value)
  
  const option = {
    radar: {
      indicator: [
        { name: 'E/I', max: 10 },
        { name: 'S/N', max: 10 },
        { name: 'T/F', max: 10 },
        { name: 'J/P', max: 10 }
      ]
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [
              result.value.eScore,
              result.value.sScore,
              result.value.tScore,
              result.value.jScore
            ],
            name: '您的得分'
          }
        ]
      }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  loadResult()
})
</script>

<style scoped>
.result-container {
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

.header-content h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.el-main {
  padding: 24px;
}

.personality-type {
  padding: 36px 0 40px;
  text-align: center;
}

.type-code {
  margin-bottom: 18px;
}

.type-name {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.description-section {
  margin: 24px 0;
}

.description-section h3 {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
}

.description-section h3::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 14px;
  margin-right: 8px;
  border-radius: 2px;
  background: var(--el-color-primary);
  vertical-align: middle;
}

.description-section p {
  color: var(--el-text-color-regular);
  line-height: 1.85;
  text-indent: 2em;
}

.dimensions-card,
.actions-card {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.loading-container p {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
</style>
