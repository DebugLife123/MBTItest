<template>
  <div class="history-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>测评历史</h2>
          <el-button @click="$router.push('/home')">返回首页</el-button>
        </div>
      </el-header>
      
      <el-main>
        <el-card shadow="hover">
          <el-table :data="results" v-loading="loading" style="width: 100%">
            <el-table-column prop="typeCode" label="性格类型" width="120">
              <template #default="{ row }">
                <MbtiTag :code="row.typeCode" size="large" show-name />
              </template>
            </el-table-column>
            
            <el-table-column prop="personality.typeName" label="类型名称" />
            
            <el-table-column prop="createdAt" label="测评时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewDetail(row.id)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-pagination
            v-if="total > 0"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, total"
            @current-change="handlePageChange"
            style="margin-top: 20px; justify-content: center;"
          />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { assessmentApi } from '@/api/assessment'
import type { TestResult } from '@/types'
import MbtiTag from '@/components/MbtiTag.vue'

const router = useRouter()
const loading = ref(true)
const results = ref<TestResult[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadResults = async () => {
  loading.value = true
  try {
    const res = await assessmentApi.getMyResults(currentPage.value - 1, pageSize.value)
    if (res.code === 0 && res.data) {
      results.value = res.data.content
      total.value = res.data.totalElements
    } else {
      ElMessage.error(res.message || '加载历史记录失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const viewDetail = (id: number) => {
  router.push(`/result/${id}`)
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadResults()
}

onMounted(() => {
  loadResults()
})
</script>

<style scoped>
.history-container {
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
</style>
