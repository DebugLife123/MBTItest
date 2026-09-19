<template>
  <div class="test-container">
    <el-container v-if="!loading && questions.length > 0">
      <el-header>
        <div class="test-header">
          <h2>MBTI 性格测评</h2>
          <div class="progress-info">
            <span>题目进度: {{ currentIndex + 1 }} / {{ questions.length }}</span>
            <el-progress :percentage="progress" :show-text="false" />
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-card class="question-card" shadow="hover">
          <div class="question-number">第 {{ currentIndex + 1 }} 题</div>
          <div class="question-content">{{ currentQuestion.content }}</div>
          
          <div class="options">
            <el-button
              class="option-button"
              :class="{ 'is-selected': answers[currentQuestion.id] === 'A' }"
              size="large"
              @click="selectAnswer('A')"
            >
              <div class="option-label">A</div>
              <div class="option-text">{{ currentQuestion.optionA }}</div>
            </el-button>
            
            <el-button
              class="option-button"
              :class="{ 'is-selected': answers[currentQuestion.id] === 'B' }"
              size="large"
              @click="selectAnswer('B')"
            >
              <div class="option-label">B</div>
              <div class="option-text">{{ currentQuestion.optionB }}</div>
            </el-button>
          </div>
        </el-card>
        
        <div class="navigation">
          <el-button
            :disabled="currentIndex === 0"
            @click="prevQuestion"
          >
            上一题
          </el-button>
          
          <el-button
            v-if="currentIndex < questions.length - 1"
            type="primary"
            :disabled="!answers[currentQuestion.id]"
            @click="nextQuestion"
          >
            下一题
          </el-button>
          
          <el-button
            v-else
            type="primary"
            :disabled="!isAllAnswered"
            :loading="submitting"
            @click="submitTest"
          >
            提交测评
          </el-button>
        </div>
      </el-main>
    </el-container>
    
    <div v-else-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载测评题目中...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { assessmentApi } from '@/api/assessment'
import type { MbtiQuestion, TestAttempt } from '@/types'

const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const attempt = ref<TestAttempt | null>(null)
const questions = ref<MbtiQuestion[]>([])
const currentIndex = ref(0)
const answers = ref<Record<number, 'A' | 'B'>>({})

const currentQuestion = computed(() => questions.value[currentIndex.value])
const progress = computed(() => ((currentIndex.value + 1) / questions.value.length) * 100)
const isAllAnswered = computed(() => {
  return questions.value.every(q => answers.value[q.id])
})

const loadTest = async () => {
  try {
    // 创建测评会话
    const attemptRes = await assessmentApi.startAttempt()
    if (attemptRes.code !== 0 || !attemptRes.data) {
      ElMessage.error(attemptRes.message || '创建测评失败')
      router.push('/home')
      return
    }
    
    attempt.value = attemptRes.data
    
    // 加载题目
    const questionsRes = await assessmentApi.getQuestions(attemptRes.data.id)
    if (questionsRes.code !== 0 || !questionsRes.data) {
      ElMessage.error(questionsRes.message || '加载题目失败')
      router.push('/home')
      return
    }
    
    questions.value = questionsRes.data.sort((a, b) => a.sortOrder - b.sortOrder)
    loading.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '加载测评失败')
    router.push('/home')
  }
}

const selectAnswer = (answer: 'A' | 'B') => {
  answers.value[currentQuestion.value.id] = answer
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const submitTest = async () => {
  if (!isAllAnswered.value) {
    ElMessage.warning('请完成所有题目后再提交')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确认提交测评？提交后将无法修改答案。',
      '提示',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '再检查一下',
        type: 'warning'
      }
    )
    
    submitting.value = true
    
    // 提交答案
    const answersArray = questions.value.map(q => ({
      questionId: q.id,
      answer: answers.value[q.id]
    }))
    
    const submitRes = await assessmentApi.submitAnswers(attempt.value!.id, {
      answers: answersArray
    })
    
    if (submitRes.code !== 0) {
      ElMessage.error(submitRes.message || '提交答案失败')
      submitting.value = false
      return
    }
    
    // 完成测评
    const completeRes = await assessmentApi.completeAttempt(attempt.value!.id)
    if (completeRes.code !== 0 || !completeRes.data) {
      ElMessage.error(completeRes.message || '完成测评失败')
      submitting.value = false
      return
    }
    
    ElMessage.success('测评完成！')
    router.push(`/result/${completeRes.data.id}`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交测评失败')
    }
    submitting.value = false
  }
}

onMounted(() => {
  loadTest()
})
</script>

<style scoped>
.test-container {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.el-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: none;
  height: auto;
}

.test-header {
  width: 100%;
}

.test-header h2 {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.progress-info span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  white-space: nowrap;
  font-variant: tabular-nums;
}

.progress-info .el-progress {
  flex: 1;
}

.el-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 40px 20px 56px;
}

.question-card {
  width: 100%;
  max-width: 820px;
}

.question-number {
  margin-bottom: 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.question-content {
  margin-bottom: 32px;
  font-size: 19px;
  font-weight: 600;
  line-height: 1.7;
  color: var(--el-text-color-primary);
}

.options {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.option-button {
  width: 100%;
  height: auto;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  text-align: left;
  border: 1.5px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  background: var(--el-bg-color);
}

.option-button:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.option-button.is-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: inset 0 0 0 1px var(--el-color-primary-light-5);
}

.option-label {
  min-width: 36px;
  font-size: 19px;
  font-weight: 600;
  text-align: center;
  color: var(--el-color-primary);
}

.option-text {
  flex: 1;
  font-size: 15px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

.navigation {
  display: flex;
  gap: 12px;
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
