<template>
  <el-tag
    :class="['mbti-tag', `mbti-tag--${code.toLowerCase()}`]"
    :size="size"
    :effect="effect"
  >
    <span class="mbti-tag__code">{{ code }}</span>
    <span v-if="showName && name" class="mbti-tag__name">{{ name }}</span>
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getMbtiName } from '@/utils/mbti'

const props = defineProps<{
  code: string
  showName?: boolean
  size?: '' | 'large' | 'default' | 'small'
  effect?: '' | 'light' | 'plain' | 'dark'
}>()

const name = computed(() => getMbtiName(props.code))
</script>
