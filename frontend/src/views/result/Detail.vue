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
                <div class="type-code">{{ result.typeCode }}</div>
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
              <el-button type="primary" @click="$router.push('/home')" style="width: 100%;">
                重新测评
              </el-button>
              <el-button @click="$router.push('/history')" style="width: 100%; margin-top: 12px;">
                查看历史
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

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const result = ref<TestResult | null>(null)
const chartRef = ref<HTMLElement>()

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

.personality-type {
  text-align: center;
  padding: 40px 0;
}

.type-code {
  font-size: 64px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 16px;
}

.type-name {
  font-size: 24px;
  color: #303133;
}

.description-section {
  margin: 24px 0;
}

.description-section h3 {
  color: #303133;
  margin-bottom: 12px;
  font-size: 18px;
}

.description-section p {
  color: #606266;
  line-height: 1.8;
  text-indent: 2em;
}

.dimensions-card, .actions-card {
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 16px;
}
</style>
