export type MbtiGroup = 'analyst' | 'diplomat' | 'sentinel' | 'explorer'

export interface MbtiInfo {
  code: string
  name: string
  group: MbtiGroup
}

export const MBTI_TYPES: readonly MbtiInfo[] = [
  { code: 'INTJ', name: '建筑师', group: 'analyst' },
  { code: 'INTP', name: '逻辑学家', group: 'analyst' },
  { code: 'ENTJ', name: '指挥官', group: 'analyst' },
  { code: 'ENTP', name: '辩论家', group: 'analyst' },
  { code: 'INFJ', name: '提倡者', group: 'diplomat' },
  { code: 'INFP', name: '调停者', group: 'diplomat' },
  { code: 'ENFJ', name: '主人公', group: 'diplomat' },
  { code: 'ENFP', name: '竞选者', group: 'diplomat' },
  { code: 'ISTJ', name: '物流师', group: 'sentinel' },
  { code: 'ISFJ', name: '守卫者', group: 'sentinel' },
  { code: 'ESTJ', name: '总经理', group: 'sentinel' },
  { code: 'ESFJ', name: '执政官', group: 'sentinel' },
  { code: 'ISTP', name: '鉴赏家', group: 'explorer' },
  { code: 'ISFP', name: '探险家', group: 'explorer' },
  { code: 'ESTP', name: '企业家', group: 'explorer' },
  { code: 'ESFP', name: '表演者', group: 'explorer' }
] as const

const TYPE_MAP = new Map<string, MbtiInfo>(MBTI_TYPES.map(t => [t.code, t]))

export function getMbtiInfo(code: string): MbtiInfo | null {
  return TYPE_MAP.get(code.toUpperCase()) ?? null
}

export function getMbtiName(code: string): string {
  return getMbtiInfo(code)?.name ?? code
}

export function mbtiGroupOf(code: string): MbtiGroup {
  const t = code.toUpperCase()
  if (t.includes('NT')) return 'analyst'
  if (t.includes('NF')) return 'diplomat'
  if (t[1] === 'S' && t[3] === 'J') return 'sentinel'
  return 'explorer'
}
