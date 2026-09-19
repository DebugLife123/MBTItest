<template>
  <div class="types-page">
    <!-- ============================================================
         页头：标题 + 简介 + 开始测评引导
         ============================================================ -->
    <section class="page-hero">
      <h1 class="page-hero__title">人格类型总览</h1>
      <p class="page-hero__slogan">16 种人格类型，四种气质倾向，了解每一种独特的思考与行事方式</p>
      <el-button type="primary" round @click="router.push('/assessment')">
        开始测评
        <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </section>

    <!-- ============================================================
         四大气质分组：理性 / 理想 / 传统 / 实用
         ============================================================ -->
    <section
      v-for="g in groups"
      :key="g.key"
      class="type-group"
      :style="{ '--group-color': `var(--mbti-${g.key})` }"
    >
      <header class="type-group__head">
        <h2 class="type-group__title">{{ g.title }}</h2>
        <span class="type-group__en">{{ g.en }}</span>
        <p class="type-group__desc">{{ g.desc }}</p>
      </header>

      <div class="type-cards">
        <div
          v-for="t in typesOf(g.key)"
          :key="t.code"
          class="type-card"
          @click="router.push(`/types/${t.code}`)"
        >
          <span class="type-card__code">{{ t.code }}</span>
          <span class="type-card__name">{{ t.name }}</span>
          <span class="type-card__temperament">{{ t.temperament }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { MBTI_GROUP_META, MBTI_DETAILS, type MbtiDetail } from '@/utils/mbtiDetail'
import type { MbtiGroup } from '@/utils/mbti'

const router = useRouter()

// 四大分组元信息
const groups = MBTI_GROUP_META

// 按分组筛选类型
const typesOf = (g: MbtiGroup): MbtiDetail[] => MBTI_DETAILS.filter(t => t.group === g)
</script>

<style scoped lang="scss">
.types-page {
  min-height: 100vh;
  padding: 40px 24px 72px;
  background: #f5f7fa;
}

/* ------------------------------------------------------------
 * 页头
 * ------------------------------------------------------------ */
.page-hero {
  max-width: 1080px;
  margin: 0 auto 40px;
  text-align: center;
  animation: fade-in-up 0.3s var(--app-ease) both;

  &__title {
    margin: 0 0 12px;
    font-size: 30px;
    font-weight: 700;
    color: #1f2937;
    letter-spacing: 0.02em;
  }

  &__slogan {
    margin: 0 0 20px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }
}

/* ------------------------------------------------------------
 * 分组区块
 * ------------------------------------------------------------ */
.type-group {
  max-width: 1080px;
  margin: 0 auto 28px;
  padding: 24px 28px;
  background: #ffffff;
  border: 1px solid var(--el-border-color-extra-light);
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(31, 41, 55, 0.07);
  animation: fade-in-up 0.3s var(--app-ease) both;

  &__head {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 4px 12px;
    margin-bottom: 18px;
    padding-bottom: 14px;
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }

  &__title {
    margin: 0;
    font-size: 19px;
    font-weight: 700;
    color: var(--group-color);
    letter-spacing: 0.06em;
  }

  &__en {
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-placeholder);
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  &__desc {
    flex-basis: 100%;
    margin: 6px 0 0;
    font-size: 13px;
    line-height: 1.7;
    color: var(--el-text-color-secondary);
  }
}

/* ------------------------------------------------------------
 * 类型卡片网格
 * ------------------------------------------------------------ */
.type-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 22px 14px 18px;
  border: 1px solid var(--el-border-color-extra-light);
  border-radius: 12px;
  background: #fbfcfe;
  cursor: pointer;
  transition:
    transform var(--app-duration) var(--app-ease),
    box-shadow var(--app-duration) var(--app-ease),
    border-color var(--app-duration) var(--app-ease);

  &:hover {
    transform: translateY(-2px);
    border-color: color-mix(in srgb, var(--group-color) 38%, transparent);
    box-shadow: 0 8px 20px rgba(31, 41, 55, 0.1);
  }

  &__code {
    font-size: 17px;
    font-weight: 700;
    color: var(--group-color);
    letter-spacing: 0.1em;
  }

  &__name {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }

  &__temperament {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

/* 入场动效 */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式 */
@media (max-width: 900px) {
  .type-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .types-page { padding: 28px 16px 56px; }
  .page-hero__title { font-size: 24px; }
  .type-group { padding: 18px 16px; }
}
</style>
