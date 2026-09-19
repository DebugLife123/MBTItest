<template>
  <div class="login-page">
    <!-- 噪点层 -->
    <div class="noise-layer" aria-hidden="true"></div>

    <!-- ====================================================
         左侧品牌区（大屏 45%）：slogan + 极简 MBTI 意象
         ==================================================== -->
    <section class="brand-pane" aria-label="品牌介绍">
      <!-- 角落几何线条装饰 -->
      <div class="deco deco--tl" aria-hidden="true"></div>
      <div class="deco deco--br" aria-hidden="true"></div>

      <div class="brand-inner">
        <div class="brand-logo">
          <span class="brand-logo__mark" aria-hidden="true">M</span>
          <span class="brand-logo__name">MBTI 测评平台</span>
        </div>

        <h1 class="brand-slogan">
          发现你的人格优势，<br />
          探索职业发展方向
        </h1>
        <p class="brand-sub">
          基于荣格心理类型理论的专业测评体系，用科学的方式认识自己。
        </p>

        <ul class="brand-metrics">
          <li class="brand-metrics__item">
            <span class="brand-metrics__value">16</span>
            <span class="brand-metrics__label">人格类型</span>
          </li>
          <li class="brand-metrics__divider" aria-hidden="true"></li>
          <li class="brand-metrics__item">
            <span class="brand-metrics__value">93</span>
            <span class="brand-metrics__label">专业题目</span>
          </li>
          <li class="brand-metrics__divider" aria-hidden="true"></li>
          <li class="brand-metrics__item">
            <span class="brand-metrics__value">4</span>
            <span class="brand-metrics__label">维度解析</span>
          </li>
        </ul>

        <!-- 极简抽象 MBTI 意象：四象限点阵 -->
        <div class="brand-visual" aria-hidden="true">
          <svg width="132" height="132" viewBox="0 0 132 132" fill="none">
            <!-- 十字轴 -->
            <line x1="66" y1="8" x2="66" y2="124" stroke="#3b63d8" stroke-opacity="0.22" stroke-width="1.5" stroke-dasharray="3 5"/>
            <line x1="8" y1="66" x2="124" y2="66" stroke="#3b63d8" stroke-opacity="0.22" stroke-width="1.5" stroke-dasharray="3 5"/>
            <!-- 外框圆 -->
            <circle cx="66" cy="66" r="58" stroke="#3b63d8" stroke-opacity="0.14" stroke-width="1.5"/>
            <!-- 四象限色点 -->
            <circle cx="41" cy="41" r="7" fill="#3b63d8" fill-opacity="0.85"/>
            <circle cx="91" cy="41" r="7" fill="#64bcac" fill-opacity="0.85"/>
            <circle cx="41" cy="91" r="7" fill="#6b7280" fill-opacity="0.6"/>
            <circle cx="91" cy="91" r="7" fill="#f28c68" fill-opacity="0.85"/>
            <!-- 点缀小圆 -->
            <circle cx="66" cy="20" r="3" fill="#3b63d8" fill-opacity="0.3"/>
            <circle cx="66" cy="112" r="3" fill="#3b63d8" fill-opacity="0.3"/>
            <circle cx="20" cy="66" r="3" fill="#3b63d8" fill-opacity="0.3"/>
            <circle cx="112" cy="66" r="3" fill="#3b63d8" fill-opacity="0.3"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- ====================================================
         右侧表单区（大屏 55%）
         ==================================================== -->
    <section class="form-pane">
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
    </section>
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
/* ============================================================
 * 页面骨架：大屏左右两栏（45 / 55），小屏回退单卡片居中
 * ============================================================ */
.login-page {
  position: relative;
  display: flex;
  min-height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}

/* 极低透明度细噪点，覆盖全页 */
.noise-layer {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='140' height='140'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='2' stitchTiles='stitch'/%3E%3CfeColorMatrix type='saturate' values='0'/%3E%3C/filter%3E%3Crect width='140' height='140' filter='url(%23n)' opacity='0.03'/%3E%3C/svg%3E");
}

/* ============================================================
 * 左：品牌区
 * ============================================================ */
.brand-pane {
  position: relative;
  flex: 0 0 45%;
  display: flex;
  align-items: center;
  padding: 64px 56px;
  overflow: hidden;
  /* 低饱和靛蓝底：主色 4% + 顶部径向晕染 10% */
  background:
    radial-gradient(760px 420px at 50% -8%, rgba(59, 99, 216, 0.10), rgba(59, 99, 216, 0) 70%),
    rgba(59, 99, 216, 0.04);
  border-right: 1px solid var(--el-border-color-extra-light);
}

/* 角落抽象几何线条 */
.deco {
  position: absolute;
  width: 220px;
  height: 220px;
  pointer-events: none;
  opacity: 0.08;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='220' height='220' fill='none' stroke='%233b63d8' stroke-width='1.5'%3E%3Ccircle cx='55' cy='55' r='42'/%3E%3Ccircle cx='140' cy='110' r='68'/%3E%3Cpath d='M0 200 L220 0'/%3E%3Cpath d='M28 220 L220 38'/%3E%3Crect x='158' y='158' width='42' height='42' rx='9'/%3E%3Ccircle cx='36' cy='156' r='4' fill='%233b63d8' stroke='none'/%3E%3Ccircle cx='100' cy='28' r='4' fill='%233b63d8' stroke='none'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-size: contain;
}

.deco--tl {
  top: -48px;
  left: -48px;
}

.deco--br {
  right: -48px;
  bottom: -48px;
  transform: rotate(180deg);
}

.brand-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
}

/* 品牌 logo */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
}

.brand-logo__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 9px;
  background: var(--el-color-primary);
  color: #ffffff;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0;
}

.brand-logo__name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  letter-spacing: 0.04em;
}

/* slogan */
.brand-slogan {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: -0.01em;
  color: #1f2937;
}

.brand-sub {
  margin: 16px 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-secondary);
}

/* 指标条 */
.brand-metrics {
  display: flex;
  align-items: center;
  gap: 22px;
  margin: 36px 0 0;
  padding: 0;
  list-style: none;
}

.brand-metrics__item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.brand-metrics__value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--el-color-primary);
  font-variant-numeric: tabular-nums;
}

.brand-metrics__label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.04em;
}

.brand-metrics__divider {
  width: 1px;
  height: 30px;
  background: var(--el-border-color);
}

/* 极简 MBTI 意象 */
.brand-visual {
  margin-top: 44px;
}

/* ============================================================
 * 右：表单区
 * ============================================================ */
.form-pane {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

/* ------------------------------------------------------------
 * 登录卡片
 * ------------------------------------------------------------ */
.login-card {
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

.login-card :deep(.el-card__header) {
  padding: 8px 4px 20px;
  border-bottom: none;
}

.login-card :deep(.el-card__body) {
  padding-top: 4px;
}

/* ------------------------------------------------------------
 * 文字层级
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
 * 表单：focus 仅边框变色，无光晕
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
 * 登录按钮
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
 * 注册链接
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

/* ============================================================
 * 响应式：小屏回退单卡片居中
 * ============================================================ */
@media (max-width: 1024px) {
  .brand-pane {
    padding: 56px 40px;
  }

  .brand-slogan {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .login-page {
    background:
      radial-gradient(1100px 560px at 50% -14%, rgba(59, 99, 216, 0.12), rgba(59, 99, 216, 0) 68%),
      #f5f7fa;
  }

  .brand-pane {
    display: none;
  }

  .form-pane {
    padding: 32px 20px;
  }
}

@media (max-width: 480px) {
  .form-pane {
    padding: 24px 16px;
  }

  .login-card {
    --el-card-padding: 22px;
  }

  .card-title {
    font-size: 19px;
  }
}
</style>
