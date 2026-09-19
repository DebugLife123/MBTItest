<template>
  <div class="analytics-container">
    <div class="page-header">
      <div><h2>数据分析</h2><p>测评完成率趋势与性格类型分布</p></div>
      <div class="header-actions">
        <el-radio-group v-model="days" size="small" @change="loadAll">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="14">近 14 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
        </el-radio-group>
        <el-button @click="loadAll">刷新数据</el-button>
      </div>
    </div>

    <el-card shadow="never" class="chart-card">
      <template #header><div class="card-header"><span class="card-title">完成率趋势</span><el-tag type="info">已开始 vs 已完成</el-tag></div></template>
      <el-empty v-if="!trendData.length" description="该时间范围内暂无测评会话" />
      <div v-show="trendData.length" ref="trendRef" class="trend-chart"></div>
    </el-card>

    <el-row :gutter="24">
      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header><span class="card-title">性格类型分布</span></template>
          <el-empty v-if="!distributionData.length" description="暂无已完成测评数据" />
          <div v-show="distributionData.length" ref="chartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header><span class="card-title">分布详情</span></template>
          <el-table :data="distributionData" stripe max-height="430">
            <el-table-column prop="personalityType" label="类型" width="90" />
            <el-table-column prop="count" label="人数" width="80" />
            <el-table-column prop="percentage" label="占比" width="90"><template #default="{ row }">{{ Number(row.percentage).toFixed(2) }}%</template></el-table-column>
            <el-table-column label="进度"><template #default="{ row }"><el-progress :percentage="Number(row.percentage)" :color="getProgressColor(row.percentage)" /></template></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import { adminApi, type CompletionRatePoint, type PersonalityDistribution } from '@/api/admin'

const days = ref(14)
const trendRef = ref<HTMLElement>()
const chartRef = ref<HTMLElement>()
const trendData = ref<CompletionRatePoint[]>([])
const distributionData = ref<PersonalityDistribution[]>([])
let trendInstance: echarts.ECharts | null = null
let chartInstance: echarts.ECharts | null = null

const getProgressColor = (percentage: number) => percentage > 15 ? '#64bcac' : percentage > 8 ? '#3b63d8' : percentage > 4 ? '#c9843a' : '#c45656'

const loadAll = async () => {
  try {
    const [trendResponse, distributionResponse] = await Promise.all([
      adminApi.getCompletionRateTrend(days.value),
      adminApi.getPersonalityDistribution()
    ])
    if (trendResponse.code !== 0 || distributionResponse.code !== 0) {
      ElMessage.error(trendResponse.message || distributionResponse.message || '加载数据失败')
      return
    }
    trendData.value = trendResponse.data
    distributionData.value = distributionResponse.data
    await nextTick()
    renderTrend()
    renderDistribution()
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  }
}

const tooltipFormatter = (params: any) => {
  const rows = Array.isArray(params) ? params : [params]
  const index = rows[0]?.dataIndex ?? 0
  const point = trendData.value[index]
  return `${point.date}<br/>开始：${point.totalAttempts}<br/>完成：${point.completedAttempts}<br/>完成率：${point.completionRate}%`
}

const renderTrend = () => {
  if (!trendRef.value || trendData.value.length === 0) return
  if (!trendInstance) trendInstance = echarts.init(trendRef.value)
  const option: EChartsOption = {
    tooltip: { trigger: 'axis', formatter: tooltipFormatter },
    legend: { data: ['开始测评', '完成测评'], top: 0 },
    grid: { left: 40, right: 30, top: 50, bottom: 30 },
    xAxis: { type: 'category', data: trendData.value.map(item => item.date.slice(5)), boundaryGap: false },
    yAxis: [{ type: 'value', name: '次数' }, { type: 'value', name: '完成率', min: 0, max: 100, axisLabel: { formatter: '{value}%' } }],
    series: [
      { name: '开始测评', type: 'line', smooth: true, data: trendData.value.map(item => item.totalAttempts), lineStyle: { color: '#3b63d8' }, itemStyle: { color: '#3b63d8' } },
      { name: '完成测评', type: 'line', smooth: true, data: trendData.value.map(item => item.completedAttempts), lineStyle: { color: '#64bcac' }, itemStyle: { color: '#64bcac' } },
      { name: '完成率', type: 'line', yAxisIndex: 1, smooth: true, data: trendData.value.map(item => item.completionRate), lineStyle: { color: '#c9843a', type: 'dashed' }, itemStyle: { color: '#c9843a' } }
    ]
  }
  trendInstance.setOption(option, true)
  trendInstance.resize()
}

const renderDistribution = () => {
  if (!chartRef.value || distributionData.value.length === 0) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const option: EChartsOption = {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center', type: 'scroll' },
    series: [{ name: '性格类型', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 }, label: { show: true, formatter: '{b}: {d}%' }, emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } }, labelLine: { show: true }, data: distributionData.value.map(item => ({ name: item.personalityType, value: item.count })) }]
  }
  chartInstance.setOption(option, true)
  chartInstance.resize()
}

const handleResize = () => { trendInstance?.resize(); chartInstance?.resize() }
onMounted(() => { loadAll(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); trendInstance?.dispose(); chartInstance?.dispose() })
</script>

<style scoped>
.analytics-container {
  padding: 24px;
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
}

.chart,
.trend-chart {
  width: 100%;
  height: 430px;
}

@media (max-width: 900px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
