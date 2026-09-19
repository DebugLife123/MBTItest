<template>
  <div class="type-detail" :style="{ '--group-color': `var(--mbti-${detail?.group ?? 'analyst'})` }">
    <template v-if="detail">
      <!-- ============================================================
           顶部：16 型快速切换
           ============================================================ -->
      <nav class="type-switcher">
        <router-link
          v-for="t in allTypes"
          :key="t.code"
          :to="`/types/${t.code}`"
          class="type-switcher__item"
          :class="{ 'is-active': t.code === detail.code }"
        >
          {{ t.code }}
        </router-link>
      </nav>

      <!-- 面包屑 -->
      <div class="crumb-row">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/types' }">人格类型</el-breadcrumb-item>
          <el-breadcrumb-item>{{ detail.code }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- ============================================================
           Hero 区：类型名 + 气质 + 测评引导（无插画，克制排版）
           ============================================================ -->
      <section class="hero">
        <div class="hero__inner">
          <div class="hero__watermark">{{ detail.code }}</div>
          <h1 class="hero__title">{{ detail.name }} {{ detail.code }}</h1>
          <p class="hero__temperament">{{ detail.temperament }}</p>
          <el-button color="#f28c68" round class="hero__cta" @click="router.push('/assessment')">
            前往测试
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </section>

      <!-- ============================================================
           基本画像
           ============================================================ -->
      <section class="block">
        <p class="block__en">Personality Portrait</p>
        <h2 class="block__title">基本画像</h2>
        <p class="block__text">{{ detail.portrait }}</p>
      </section>

      <!-- ============================================================
           四个字母代表什么
           ============================================================ -->
      <section class="block">
        <p class="block__en">What Do The Four Letters Represent</p>
        <h2 class="block__title">这四个字母代表什么</h2>
        <div class="letters">
          <div v-for="d in dimensions" :key="d.letter" class="letter-item">
            <span class="letter-item__letter">{{ d.letter }}</span>
            <span class="letter-item__label">{{ d.label }}</span>
            <span class="letter-item__desc">{{ d.desc }}</span>
          </div>
        </div>
      </section>

      <!-- ============================================================
           性格特点
           ============================================================ -->
      <section class="block">
        <p class="block__en">Your Character</p>
        <h2 class="block__title">性格特点</h2>
        <ul class="trait-list">
          <li v-for="(t, i) in detail.traits" :key="i">{{ t }}</li>
        </ul>
      </section>

      <!-- ============================================================
           适合的职业方向
           ============================================================ -->
      <section class="block">
        <p class="block__en">Career Directions</p>
        <h2 class="block__title">适合的职业方向</h2>
        <div class="career-tags">
          <el-tag
            v-for="c in detail.careers"
            :key="c"
            class="career-tag"
            effect="plain"
            round
          >
            {{ c }}
          </el-tag>
        </div>
      </section>

      <!-- 底部导航 -->
      <div class="foot-nav">
        <el-button text bg @click="router.push('/types')">
          <el-icon class="el-icon--left"><ArrowLeft /></el-icon>
          返回人格类型总览
        </el-button>
      </div>
    </template>

    <!-- 无效类型 -->
    <el-result
      v-else
      icon="warning"
      title="未找到该人格类型"
      sub-title="请确认类型代码是否正确，例如 INTJ、ENFP"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/types')">返回人格类型总览</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { MBTI_DETAILS, getMbtiDetail, getDimensions } from '@/utils/mbtiDetail'

const route = useRoute()
const router = useRouter()

// 当前类型详情（随路由参数变化）
const detail = computed(() => getMbtiDetail(String(route.params.code ?? '')))

// 四个维度释义
const dimensions = computed(() => (detail.value ? getDimensions(detail.value.code) : []))

// 顶部切换用的全部类型
const allTypes = MBTI_DETAILS
</script>

<style scoped lang="scss">
.type-detail {
  min-height: 100vh;
  padding: 24px 24px 64px;
  background: #f5f7fa;
  animation: fade-in-up 0.3s var(--app-ease) both;
}

/* ------------------------------------------------------------
 * 顶部 16 型切换
 * ------------------------------------------------------------ */
.type-switcher {
  max-width: 920px;
  margin: 0 auto 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  &__item {
    padding: 7px 14px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: var(--el-text-color-regular);
    text-decoration: none;
    transition:
      background-color var(--app-duration) var(--app-ease),
      color var(--app-duration) var(--app-ease);

    &:hover {
      background: rgba(59, 99, 216, 0.06);
      color: var(--el-color-primary);
    }

    &.is-active {
      background: var(--el-color-primary);
      color: #ffffff;
    }
  }
}

.crumb-row {
  max-width: 920px;
  margin: 0 auto 16px;
}

/* ------------------------------------------------------------
 * Hero 区
 * ------------------------------------------------------------ */
.hero {
  max-width: 920px;
  margin: 0 auto 24px;
  border-radius: 14px;
  overflow: hidden;
  background:
    linear-gradient(120deg, color-mix(in srgb, var(--group-color) 92%, #ffffff 0%) 0%, color-mix(in srgb, var(--group-color) 72%, #1f2937 8%) 100%);
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.07);

  &__inner {
    position: relative;
    padding: 44px 40px;
    color: #ffffff;
  }

  /* 大号半透明代码水印，代替插画 */
  &__watermark {
    position: absolute;
    right: 28px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 120px;
    font-weight: 800;
    letter-spacing: 0.06em;
    color: rgba(255, 255, 255, 0.12);
    user-select: none;
    pointer-events: none;
  }

  &__title {
    position: relative;
    margin: 0 0 10px;
    font-size: 30px;
    font-weight: 700;
    letter-spacing: 0.02em;
  }

  &__temperament {
    position: relative;
    margin: 0 0 22px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
  }

  &__cta {
    position: relative;
  }
}

/* ------------------------------------------------------------
 * 内容区块
 * ------------------------------------------------------------ */
.block {
  max-width: 920px;
  margin: 0 auto 20px;
  padding: 26px 28px;
  background: #ffffff;
  border: 1px solid var(--el-border-color-extra-light);
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.07);

  &__en {
    margin: 0 0 4px;
    font-size: 11px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: var(--el-text-color-placeholder);
  }

  &__title {
    margin: 0 0 16px;
    font-size: 19px;
    font-weight: 700;
    color: #1f2937;
  }

  &__text {
    margin: 0;
    font-size: 14px;
    line-height: 1.9;
    color: var(--el-text-color-regular);
  }
}

/* 四个字母 */
.letters {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.letter-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fbfcfe;
  border: 1px solid var(--el-border-color-extra-light);

  &__letter {
    font-size: 18px;
    font-weight: 700;
    color: var(--group-color);
  }

  &__label {
    flex-shrink: 0;
    font-size: 13px;
    font-weight: 600;
    color: #1f2937;
  }

  &__desc {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }
}

/* 性格特点 */
.trait-list {
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    position: relative;
    padding: 8px 0 8px 20px;
    font-size: 14px;
    line-height: 1.8;
    color: var(--el-text-color-regular);
    border-bottom: 1px dashed var(--el-border-color-extra-light);

    &:last-child { border-bottom: none; }

    &::before {
      content: '';
      position: absolute;
      left: 4px;
      top: 18px;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: var(--group-color);
    }
  }
}

/* 职业方向标签 */
.career-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.career-tag {
  font-size: 13px;
}

/* 底部导航 */
.foot-nav {
  max-width: 920px;
  margin: 8px auto 0;
}

/* 入场动效 */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式 */
@media (max-width: 768px) {
  .type-detail { padding: 16px 14px 48px; }
  .hero__inner { padding: 30px 22px; }
  .hero__title { font-size: 24px; }
  .hero__watermark { font-size: 72px; right: 16px; }
  .letters { grid-template-columns: 1fr; }
  .block { padding: 20px 18px; }
}
</style>
