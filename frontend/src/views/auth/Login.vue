<template>
  <div class="login-container">
    <div class="bg-decoration bg-decoration--tr" aria-hidden="true"></div>
    <div class="bg-decoration bg-decoration--bl" aria-hidden="true"></div>

    <el-card class="login-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2 class="card-title">MBTI 职业性格测评系统</h2>
          <p class="card-slogan">发现你的人格优势，探索职业发展方向</p>
          <p class="card-subtitle">登录账号</p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="login-action">
          <el-button
            class="login-button"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="footer-links">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await userStore.login(form.username, form.password)
      if (res.code === 0) {
        ElMessage.success('登录成功')
        router.push('/home')
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败，请检查网络连接')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
/* ------------------------------------------------------------
 * 背景层：#f5f7fa 底色 + 顶部低饱和靛蓝径向晕染 + 细噪点
 * 角落几何线条装饰 opacity 0.08，无粒子动画
 * ------------------------------------------------------------ */
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32px 20px;
  overflow: hidden;
  background:
    radial-gradient(1100px 560px at 50% -14%, rgba(59, 99, 216, 0.12), rgba(59, 99, 216, 0) 68%),
    #f5f7fa;
}

/* 极低透明度细微噪点 */
.login-container::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' stitchTiles='stitch'/%3E%3CfeColorMatrix type='saturate' values='0'/%3E%3C/filter%3E%3Crect width='140' height='140' filter='url(%23n)' opacity='0.035'/%3E%3C/svg%3E");
}

/* 角落抽象几何线条装饰 */
.bg-decoration {
  position: absolute;
  width: 240px;
  height: 240px;
  pointer-events: none;
  opacity: 0.08;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='240' height='240' fill='none' stroke='%233b63d8' stroke-width='1.5'%3E%3Ccircle cx='60' cy='60' r='44'/%3E%3Ccircle cx='150' cy='120' r='72'/%3E%3Cpath d='M0 220 L240 0'/%3E%3Cpath d='M30 240 L240 40'/%3E%3Crect x='170' y='170' width='46' height='46' rx='10'/%3E%3Ccircle cx='40' cy='170' r='4' fill='%233b63d8' stroke='none'/%3E%3Ccircle cx='110' cy='30' r='4' fill='%233b63d8' stroke='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: contain;
}

.bg-decoration--tr {
  top: -60px;
  right: -60px;
}

.bg-decoration--bl {
  bottom: -60px;
  left: -60px;
  transform: rotate(180deg);
}

/* ------------------------------------------------------------
 * 登录卡片：圆角 16px，分层软阴影，增大上下 padding
 * 入场动效：淡入 + 向上微偏移，0.3s
 * ------------------------------------------------------------ */
.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  --el-card-border-radius: 16px;
  --el-card-padding: 28px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow:
    0 16px 40px rgba(31, 41, 55, 0.08),
    0 4px 12px rgba(31, 41, 55, 0.04);
  animation: login-card-enter 0.3s cubic-bezier(0.4, 0, 0.2, 1) both;
}

@keyframes login-card-enter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 padding 加厚 */
.login-card :deep(.el-card__header) {
  padding: 8px 4px 20px;
  border-bottom: none;
}

.login-card :deep(.el-card__body) {
  padding-top: 4px;
}

/* ------------------------------------------------------------
 * 文字层级：大标题 > slogan 副标题 > 登录账号小标题
 * 正文使用 #1f2937 深炭灰，不用纯黑
 * ------------------------------------------------------------ */
.card-header {
  text-align: center;
}

.card-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: #1f2937;
}

.card-slogan {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}

.card-subtitle {
  margin: 20px 0 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

/* ------------------------------------------------------------
 * 表单：focus 边框变色，禁止发光光晕
 * ------------------------------------------------------------ */
.login-card :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  transition:
    box-shadow var(--app-duration) var(--app-ease),
    border-color var(--app-duration) var(--app-ease);
}

.login-card :deep(.el-input__wrapper.is-focus) {
  /* 仅边框变色，无发光光晕 */
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.login-card :deep(.el-input__wrapper:hover) {
  transform: none;
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset;
}

.login-card :deep(.el-input__wrapper.is-focus:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

/* ------------------------------------------------------------
 * 登录按钮：圆角 10px，hover 上移 2px + 柔和加深阴影
 * ------------------------------------------------------------ */
.login-action {
  margin-bottom: 8px;
}

.login-button {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.login-button:not(.is-disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(59, 99, 216, 0.22);
}

/* ------------------------------------------------------------
 * 注册链接：hover 下划线
 * ------------------------------------------------------------ */
.footer-links {
  margin-top: 4px;
  text-align: center;
  font-size: 13px;
}

.footer-links a {
  color: var(--el-color-primary);
  transition: color var(--app-duration) var(--app-ease);
}

.footer-links a:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

/* ------------------------------------------------------------
 * 移动端适配：卡片 max-width 420px，小屏左右留边距
 * ------------------------------------------------------------ */
@media (max-width: 480px) {
  .login-container {
    padding: 24px 16px;
  }

  .login-card {
    --el-card-padding: 22px;
  }

  .card-title {
    font-size: 19px;
  }

  .bg-decoration {
    width: 160px;
    height: 160px;
  }
}
</style>
