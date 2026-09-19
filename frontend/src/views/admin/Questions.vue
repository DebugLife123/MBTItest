<template>
  <div class="questions-container">
    <div class="page-header">
      <div>
        <h2>题库管理</h2>
        <p>维护测评题目、选项及计分维度，共 {{ questions.length }} 道题</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增题目</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="questions" stripe>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="dimensionId" label="维度" width="80" />
        <el-table-column prop="content" label="题目" min-width="260" show-overflow-tooltip />
        <el-table-column label="A 选项" min-width="220" show-overflow-tooltip>
          <template #default="{ row }"><span class="option-tag">A</span>{{ row.optionA }}</template>
        </el-table-column>
        <el-table-column label="B 选项" min-width="220" show-overflow-tooltip>
          <template #default="{ row }"><span class="option-tag">B</span>{{ row.optionB }}</template>
        </el-table-column>
        <el-table-column prop="answerType" label="A计分" width="80">
          <template #default="{ row }"><el-tag>{{ row.answerType }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑题目' : '新增题目'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属维度" prop="dimensionId">
          <el-select v-model="form.dimensionId" placeholder="请选择维度" style="width: 100%">
            <el-option v-for="item in dimensions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="选项 A" prop="optionA"><el-input v-model="form.optionA" maxlength="200" /></el-form-item>
        <el-form-item label="选项 B" prop="optionB"><el-input v-model="form.optionB" maxlength="200" /></el-form-item>
        <el-form-item label="A 计分维度" prop="answerType">
          <el-select v-model="form.answerType" placeholder="请选择" style="width: 100%">
            <el-option v-for="letter in answerTypes" :key="letter" :label="letter" :value="letter" />
          </el-select>
          <div class="form-tip">B 选项自动按对立维度计分</div>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder"><el-input-number v-model="form.sortOrder" :min="1" :max="9999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminApi, type QuestionPayload } from '@/api/admin'
import type { MbtiQuestion } from '@/types'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const questions = ref<MbtiQuestion[]>([])
const formRef = ref<FormInstance>()
const form = reactive<QuestionPayload>({ dimensionId: 1, content: '', optionA: '', optionB: '', answerType: 'E', sortOrder: 1 })
const dimensions = [
  { value: 1, label: '1 · E/I 能量来源' }, { value: 2, label: '2 · E/I 社交倾向' },
  { value: 3, label: '3 · S/N 信息获取' }, { value: 4, label: '4 · S/N 想象创造' },
  { value: 5, label: '5 · T/F 决策方式' }, { value: 6, label: '6 · T/F 价值判断' },
  { value: 7, label: '7 · J/P 生活方式' }, { value: 8, label: '8 · J/P 计划执行' }
]
const answerTypes = ['E', 'I', 'S', 'N', 'T', 'F', 'J', 'P'] as const
const rules: FormRules<QuestionPayload> = {
  dimensionId: [{ required: true, message: '请选择维度', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  optionA: [{ required: true, message: '请输入选项 A', trigger: 'blur' }],
  optionB: [{ required: true, message: '请输入选项 B', trigger: 'blur' }],
  answerType: [{ required: true, message: '请选择计分维度', trigger: 'change' }]
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const response = await adminApi.getQuestions()
    if (response.code === 0) questions.value = response.data
    else ElMessage.error(response.message || '加载题目失败')
  } catch (error: any) {
    ElMessage.error(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { dimensionId: 1, content: '', optionA: '', optionB: '', answerType: 'E', sortOrder: questions.value.length + 1 })
  formRef.value?.clearValidate()
}

const openCreate = () => { editingId.value = null; resetForm(); dialogVisible.value = true }
const openEdit = (row: MbtiQuestion) => {
  editingId.value = row.id
  Object.assign(form, { dimensionId: row.dimensionId, content: row.content, optionA: row.optionA, optionB: row.optionB, answerType: row.answerType, sortOrder: row.sortOrder })
  dialogVisible.value = true
}

const saveQuestion = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    const response = editingId.value
      ? await adminApi.updateQuestion(editingId.value, { ...form })
      : await adminApi.createQuestion({ ...form })
    if (response.code === 0) {
      ElMessage.success(editingId.value ? '题目已更新' : '题目已创建')
      dialogVisible.value = false
      loadQuestions()
    } else ElMessage.error(response.message || '保存失败')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const removeQuestion = async (row: MbtiQuestion) => {
  try {
    await ElMessageBox.confirm(`确定删除题目「${row.content}」吗？`, '警告', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    const response = await adminApi.deleteQuestion(row.id)
    if (response.code === 0) { ElMessage.success('删除成功'); loadQuestions() }
    else ElMessage.error(response.message || '删除失败')
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

onMounted(loadQuestions)
</script>

<style scoped>
.questions-container {
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

.option-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  margin-right: 8px;
  border-radius: 7px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 700;
  font-size: 12px;
}

.form-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
