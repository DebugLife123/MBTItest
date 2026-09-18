<template>
  <div class="chat-page">
    <header class="chat-header">
      <div class="brand" @click="$router.push('/home')">
        <div class="brand-mark">AI</div>
        <div><h1>MBTI 智能咨询</h1><p>把性格倾向变成求职与成长行动</p></div>
      </div>
      <div class="header-actions">
        <el-tag v-if="status" :type="status.mockMode ? 'warning' : 'success'" effect="plain">
          {{ status.mockMode ? '演示模式' : status.activeProvider }} · {{ status.model }}
        </el-tag>
        <el-button @click="$router.push('/home')">返回首页</el-button>
        <el-button type="primary" @click="newChat">新对话</el-button>
      </div>
    </header>

    <el-alert
      v-if="status"
      class="mode-alert"
      :title="status.disclaimer"
      :type="status.mockMode ? 'warning' : 'info'"
      :closable="false"
      show-icon
    />

    <main class="chat-shell">
      <aside class="session-panel">
        <div class="panel-title">
          <span>咨询记录</span>
          <el-button link type="primary" @click="loadSessions">刷新</el-button>
        </div>
        <div v-loading="sessionLoading" class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.id === activeSessionId }"
            @click="openSession(session.id)"
          >
            <div class="session-main">
              <strong>{{ session.title }}</strong>
              <span>{{ session.personalityType || '未绑定测评' }} · {{ formatDate(session.updatedAt) }}</span>
            </div>
            <el-button link type="danger" @click.stop="removeSession(session.id)">删除</el-button>
          </div>
          <el-empty v-if="!sessionLoading && !sessions.length" :image-size="70" description="还没有咨询记录" />
        </div>
      </aside>

      <section class="conversation">
        <div ref="messageListRef" class="message-list">
          <div v-if="!messages.length" class="empty-chat">
            <div class="empty-logo">MBTI × AI</div>
            <h2>从一次真实的问题开始</h2>
            <p>我可以结合你最近一次测评结果，分析职业方向、项目表达、团队协作与求职策略。</p>
            <div class="prompt-grid">
              <button v-for="prompt in starterPrompts" :key="prompt" @click="usePrompt(prompt)">
                {{ prompt }}
              </button>
            </div>
          </div>

          <div v-for="(message, index) in messages" :key="message.id ?? index" class="message-row" :class="message.role.toLowerCase()">
            <div class="avatar">{{ message.role === 'USER' ? '我' : 'AI' }}</div>
            <div class="bubble">
              <div v-if="message.role === 'ASSISTANT'" class="markdown-body" v-html="renderSafeText(message.content || '正在思考…')"></div>
              <p v-else>{{ message.content }}</p>
              <span v-if="message.streaming" class="cursor">▍</span>
            </div>
          </div>
        </div>

        <div class="composer">
          <el-input
            v-model="draft"
            type="textarea"
            :rows="3"
            resize="none"
            maxlength="4000"
            show-word-limit
            placeholder="例如：我是 INFJ，想转 AI 全栈开发，未来三个月该如何准备？"
            @keydown.ctrl.enter.prevent="send"
            @keydown.meta.enter.prevent="send"
          />
          <div class="composer-actions">
            <span>Ctrl / ⌘ + Enter 发送</span>
            <el-button v-if="generating" type="danger" plain @click="stopGenerating">停止生成</el-button>
            <el-button v-else type="primary" :disabled="!draft.trim()" @click="send">发送</el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi, type AiChatMessage, type AiChatSession, type AiStatus } from '@/api/ai'
import { streamChat } from '@/utils/sse'
import { renderSafeText } from '@/utils/rich-text'

const route = useRoute()
const router = useRouter()
const status = ref<AiStatus | null>(null)
const sessions = ref<AiChatSession[]>([])
const messages = ref<AiChatMessage[]>([])
const activeSessionId = ref<number | null>(null)
const draft = ref('')
const generating = ref(false)
const sessionLoading = ref(false)
const messageListRef = ref<HTMLElement>()
let controller: AbortController | null = null

const starterPrompts = [
  '结合我的 MBTI 类型，分析最适合的 AI 全栈岗位方向',
  '如何把性格优势写进项目经历和面试自我介绍？',
  '我和不同类型同事协作时，最容易出现什么沟通盲区？',
  '给我一份未来 90 天的求职行动清单'
]

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) messageListRef.value.scrollTop = messageListRef.value.scrollHeight
}

const loadStatus = async () => {
  try {
    const response = await aiApi.status()
    if (response.code === 0) status.value = response.data
  } catch (error: any) {
    ElMessage.error(error.message || 'AI 状态加载失败')
  }
}

const loadSessions = async () => {
  sessionLoading.value = true
  try {
    const response = await aiApi.sessions()
    if (response.code === 0) sessions.value = response.data
  } catch (error: any) {
    ElMessage.error(error.message || '咨询记录加载失败')
  } finally {
    sessionLoading.value = false
  }
}

const openSession = async (id: number) => {
  if (generating.value) return
  try {
    const response = await aiApi.session(id)
    if (response.code !== 0) return
    activeSessionId.value = id
    messages.value = response.data.messages || []
    await scrollToBottom()
  } catch (error: any) {
    ElMessage.error(error.message || '会话加载失败')
  }
}

const newChat = () => {
  if (generating.value) stopGenerating()
  activeSessionId.value = null
  messages.value = []
  draft.value = ''
}

const usePrompt = (prompt: string) => {
  draft.value = prompt
}

const send = async () => {
  const content = draft.value.trim()
  if (!content || generating.value) return
  draft.value = ''
  messages.value.push({ role: 'USER', content })
  const assistantMessage: AiChatMessage = { role: 'ASSISTANT', content: '', streaming: true }
  messages.value.push(assistantMessage)
  generating.value = true
  controller = new AbortController()
  await scrollToBottom()

  try {
    await streamChat(
      { sessionId: activeSessionId.value, message: content },
      {
        onDelta: (delta) => {
          assistantMessage.content += delta
          scrollToBottom()
        },
        onDone: async (payload) => {
          assistantMessage.content = payload.reply || assistantMessage.content
          assistantMessage.id = undefined
          assistantMessage.streaming = false
          activeSessionId.value = payload.sessionId
          await loadSessions()
          await scrollToBottom()
        },
        onError: (error) => {
          assistantMessage.streaming = false
          if (!assistantMessage.content) assistantMessage.content = error?.message || 'AI 服务暂时不可用'
          ElMessage.error(error?.message || 'AI 服务暂时不可用')
        }
      },
      controller.signal
    )
  } catch (error: any) {
    assistantMessage.streaming = false
    if (error?.name === 'AbortError') {
      if (!assistantMessage.content) assistantMessage.content = '已停止生成。'
    } else {
      if (!assistantMessage.content) assistantMessage.content = error?.message || 'AI 服务暂时不可用'
      ElMessage.error(error?.message || 'AI 服务暂时不可用')
    }
  } finally {
    generating.value = false
    controller = null
    await scrollToBottom()
  }
}

const stopGenerating = () => {
  controller?.abort()
  generating.value = false
}

const removeSession = async (id: number) => {
  try {
    await ElMessageBox.confirm('删除后会话及消息不可恢复，确认删除？', '删除咨询记录', { type: 'warning' })
    const response = await aiApi.deleteSession(id)
    if (response.code === 0) {
      if (activeSessionId.value === id) newChat()
      await loadSessions()
      ElMessage.success('已删除')
    }
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

const formatDate = (value: string) => value ? new Date(value).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

onMounted(async () => {
  await loadStatus()
  await loadSessions()
  const sessionId = Number(route.query.sessionId)
  if (sessionId) await openSession(sessionId)
  const prompt = typeof route.query.prompt === 'string' ? route.query.prompt : ''
  if (prompt) draft.value = prompt
  if (route.query.type) {
    messages.value = []
  }
})

onUnmounted(() => controller?.abort())
</script>

<style scoped>
.chat-page { min-height: 100vh; background: radial-gradient(circle at 12% 0%, #e8f1ff 0, #f6f8fc 34%, #f4f6fa 100%); padding: 20px; }
.chat-header { max-width: 1440px; margin: 0 auto 14px; display: flex; justify-content: space-between; align-items: center; gap: 20px; }
.brand { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.brand-mark { width: 46px; height: 46px; border-radius: 14px; background: linear-gradient(135deg, #246bfd, #7b61ff); color: #fff; display: grid; place-items: center; font-weight: 800; box-shadow: 0 8px 24px rgba(36,107,253,.24); }
.brand h1 { margin: 0; font-size: 21px; color: #17223b; }
.brand p { margin: 4px 0 0; color: #7b8499; font-size: 13px; }
.header-actions { display: flex; align-items: center; gap: 10px; }
.mode-alert { max-width: 1440px; margin: 0 auto 14px; }
.chat-shell { max-width: 1440px; height: calc(100vh - 142px); min-height: 620px; margin: 0 auto; display: grid; grid-template-columns: 290px 1fr; gap: 16px; }
.session-panel, .conversation { background: rgba(255,255,255,.94); border: 1px solid #e8ecf4; border-radius: 18px; box-shadow: 0 12px 36px rgba(26,43,79,.07); overflow: hidden; }
.session-panel { display: flex; flex-direction: column; }
.panel-title { display: flex; justify-content: space-between; align-items: center; padding: 18px 18px 12px; font-weight: 700; color: #25314d; }
.session-list { flex: 1; overflow: auto; padding: 0 10px 12px; }
.session-item { display: flex; align-items: center; gap: 6px; padding: 12px; border-radius: 12px; cursor: pointer; margin-bottom: 6px; transition: .2s; }
.session-item:hover, .session-item.active { background: #eef4ff; }
.session-main { min-width: 0; flex: 1; display: flex; flex-direction: column; gap: 5px; }
.session-main strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #303b55; font-size: 14px; }
.session-main span { color: #929bad; font-size: 12px; }
.conversation { display: flex; flex-direction: column; }
.message-list { flex: 1; overflow-y: auto; padding: 28px 34px; }
.empty-chat { max-width: 760px; margin: 8vh auto 0; text-align: center; }
.empty-logo { display: inline-block; padding: 8px 14px; border-radius: 999px; background: #eef3ff; color: #3566d6; font-weight: 700; letter-spacing: .04em; }
.empty-chat h2 { margin: 18px 0 10px; color: #1f2b46; font-size: 28px; }
.empty-chat p { color: #778197; line-height: 1.8; }
.prompt-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin-top: 26px; text-align: left; }
.prompt-grid button { padding: 15px 16px; border: 1px solid #dfe7f5; background: #fff; color: #3c4963; border-radius: 13px; cursor: pointer; line-height: 1.5; transition: .2s; }
.prompt-grid button:hover { border-color: #7ea6ff; background: #f7faff; transform: translateY(-1px); }
.message-row { display: flex; gap: 12px; margin-bottom: 24px; align-items: flex-start; }
.message-row.user { flex-direction: row-reverse; }
.avatar { flex: 0 0 36px; height: 36px; border-radius: 12px; display: grid; place-items: center; background: #eef3ff; color: #3768d5; font-weight: 700; }
.message-row.user .avatar { background: #2f6df6; color: white; }
.bubble { position: relative; max-width: min(780px, 78%); padding: 14px 17px; border-radius: 16px 16px 16px 5px; background: #f5f7fb; color: #303a51; line-height: 1.75; }
.message-row.user .bubble { border-radius: 16px 16px 5px 16px; background: #2f6df6; color: #fff; }
.bubble p { white-space: pre-wrap; }
.cursor { animation: blink .8s infinite; color: #2f6df6; }
@keyframes blink { 50% { opacity: 0; } }
.composer { border-top: 1px solid #edf0f6; padding: 16px 20px 18px; background: #fff; }
.composer-actions { display: flex; align-items: center; justify-content: flex-end; gap: 12px; margin-top: 10px; }
.composer-actions span { margin-right: auto; color: #99a1b2; font-size: 12px; }
.markdown-body :deep(.md-heading) { margin: 12px 0 7px; color: #1f2b46; }
.markdown-body :deep(.md-item) { padding-left: 4px; }
.markdown-body :deep(code) { padding: 2px 5px; border-radius: 5px; background: #e8edf7; font-family: Consolas, monospace; }
@media (max-width: 850px) { .chat-page { padding: 10px; } .chat-header { align-items: flex-start; flex-direction: column; } .chat-shell { grid-template-columns: 1fr; height: auto; min-height: 0; } .session-panel { max-height: 250px; } .conversation { min-height: 70vh; } .message-list { padding: 20px 14px; } .prompt-grid { grid-template-columns: 1fr; } .bubble { max-width: 88%; } }
</style>
