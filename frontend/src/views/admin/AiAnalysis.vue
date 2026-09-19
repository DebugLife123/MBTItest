<template>
  <div class="analysis-page">
    <div class="page-header">
      <div>
        <h1>AI 团队画像分析</h1>
        <p>基于团队 MBTI 类型分布生成沟通、协作与管理建议</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.push('/admin')">返回管理后台</el-button>
        <el-button type="primary" :loading="generating" @click="generate">生成分析</el-button>
      </div>
    </div>

    <el-alert
      v-if="status"
      :title="status.disclaimer"
      :type="status.mockMode ? 'warning' : 'info'"
      :closable="false"
      show-icon
      class="mode-alert"
    />

    <el-row :gutter="20">
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="config-card">
          <template #header><div class="card-header"><span>团队类型配置</span><el-tag>{{ typeCodes.length }} 人</el-tag></div></template>
          <div class="quick-actions">
            <el-button size="small" @click="loadRealTypes">载入真实测评类型</el-button>
            <el-button size="small" @click="addAllTypes">均匀加入 16 型</el-button>
            <el-button size="small" type="danger" plain @click="clearTypes">清空</el-button>
          </div>
          <div class="type-grid">
            <el-check-tag
              v-for="type in allTypes"
              :key="type"
              :checked="!!selectedCounts[type]"
              class="type-chip"
              :class="`mbti-tag--${type.toLowerCase()}`"
              @click="addType(type)"
              @contextmenu.prevent="removeType(type)"
            >
              <strong>{{ type }}</strong>
              <span>{{ selectedCounts[type] || 0 }}</span>
            </el-check-tag>
          </div>
          <p class="hint">左键增加成员，右键减少。人员总数：{{ typeCodes.length }}</p>
          <el-divider />
          <el-form label-position="top">
            <el-form-item label="特别关注点（可选）">
              <el-input v-model="focus" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="例如：研发团队跨部门沟通、远程协作效率、管理者培养" />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="large" style="width:100%" :loading="generating" @click="generate">生成团队画像报告</el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="report-card">
          <template #header>
            <div class="card-header">
              <span>分析报告</span>
              <div v-if="current" class="report-meta">
                <el-tag size="small">{{ current.totalMembers }} 人</el-tag>
                <el-tag size="small" type="info">{{ current.provider }} / {{ current.model }}</el-tag>
              </div>
            </div>
          </template>
          <div v-if="generating" class="generating-panel">
            <el-icon class="is-loading" :size="34"><Loading /></el-icon>
            <p>正在分析团队类型分布与协作风险…</p>
          </div>
          <article v-else-if="current" class="report-content markdown-body" v-html="renderSafeText(current.report)"></article>
          <el-empty v-else description="配置团队类型后生成第一份画像报告" />
        </el-card>

        <el-card shadow="never" class="history-card">
          <template #header><div class="card-header"><span>历史分析</span><el-button link type="primary" @click="loadHistory">刷新</el-button></div></template>
          <el-table :data="history" v-loading="loadingHistory" stripe @row-click="selectHistory">
            <el-table-column label="时间" width="165"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
            <el-table-column label="人数" width="75" prop="totalMembers" />
            <el-table-column label="类型分布" min-width="220"><template #default="{ row }">{{ formatCounts(row.typeCounts) }}</template></el-table-column>
            <el-table-column label="关注点" prop="focus" min-width="150" show-overflow-tooltip />
            <el-table-column label="Provider" prop="provider" width="110" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { aiApi, type AiStatus, type TeamAnalysisRecord } from '@/api/ai'
import { renderSafeText } from '@/utils/rich-text'

const allTypes = ['INTJ','INTP','ENTJ','ENTP','INFJ','INFP','ENFJ','ENFP','ISTJ','ISFJ','ESTJ','ESFJ','ISTP','ISFP','ESTP','ESFP']
const status = ref<AiStatus | null>(null)
const typeCodes = ref<string[]>([])
const focus = ref('')
const generating = ref(false)
const current = ref<TeamAnalysisRecord | null>(null)
const history = ref<TeamAnalysisRecord[]>([])
const loadingHistory = ref(false)

const selectedCounts = computed<Record<string, number>>(() => {
  const counts: Record<string, number> = {}
  for (const type of typeCodes.value) counts[type] = (counts[type] || 0) + 1
  return counts
})

const loadStatus = async () => {
  try { const r = await aiApi.status(); if (r.code === 0) status.value = r.data } catch { /* status is non-blocking */ }
}

const loadHistory = async () => {
  loadingHistory.value = true
  try {
    const response = await aiApi.teamHistory()
    if (response.code === 0) {
      history.value = response.data
      if (!current.value && response.data.length) current.value = response.data[0]
    }
  } catch (error: any) {
    ElMessage.error(error.message || '加载团队分析历史失败')
  } finally {
    loadingHistory.value = false
  }
}

const loadRealTypes = async () => {
  try {
    const response = await aiApi.availableTypes()
    if (response.code !== 0) return
    typeCodes.value = response.data
    ElMessage.success(response.data.length ? `已载入 ${response.data.length} 条真实测评类型` : '暂无真实测评数据')
  } catch (error: any) {
    ElMessage.error(error.message || '载入类型失败')
  }
}

const addType = (type: string) => { typeCodes.value.push(type) }
const removeType = (type: string) => {
  const index = typeCodes.value.lastIndexOf(type)
  if (index >= 0) typeCodes.value.splice(index, 1)
}
const addAllTypes = () => { typeCodes.value = [...allTypes] }
const clearTypes = () => { typeCodes.value = [] }
const selectHistory = (record: TeamAnalysisRecord) => { current.value = record }

const generate = async () => {
  if (!typeCodes.value.length) { ElMessage.warning('请至少加入一名成员'); return }
  generating.value = true
  try {
    const response = await aiApi.analyzeTeam({ typeCodes: typeCodes.value, focus: focus.value })
    if (response.code === 0) {
      current.value = response.data
      await loadHistory()
      ElMessage.success('团队画像已生成')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '生成团队画像失败')
  } finally {
    generating.value = false
  }
}

const formatDate = (value: string) => value ? new Date(value).toLocaleString('zh-CN') : ''
const formatCounts = (counts: Record<string, number>) => Object.entries(counts || {}).map(([type, count]) => `${type}×${count}`).join('，')

onMounted(async () => { await loadStatus(); await loadHistory() })
</script>

<style scoped>
.analysis-page {
  min-height: 100vh;
  padding: 24px;
  background: var(--el-bg-color-page);
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.page-header p {
  margin: 7px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.mode-alert {
  margin-bottom: 18px;
}

.config-card,
.report-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.type-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 9px 4px;
  border-radius: 9px;
  cursor: pointer;
  font-weight: 600;
  line-height: 1.4;
}

.type-chip strong {
  font-size: 13px;
  letter-spacing: 0.04em;
}

.type-chip span {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.72;
  font-variant: tabular-nums;
}

.hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.7;
}

.report-meta {
  display: flex;
  gap: 6px;
}

.report-content {
  min-height: 390px;
  color: var(--el-text-color-regular);
  line-height: 1.9;
}

.generating-panel {
  display: flex;
  min-height: 390px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--el-text-color-secondary);
}

.markdown-body :deep(.md-heading) {
  margin: 16px 0 8px;
  color: var(--el-text-color-primary);
}

.history-card {
  margin-bottom: 0;
}

@media (max-width: 900px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .type-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
