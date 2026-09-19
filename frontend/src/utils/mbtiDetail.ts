import type { MbtiGroup } from './mbti'

// ============================================================
// MBTI 16 型人格详情内容库：总览页分组文案 + 详情页结构化内容
// ============================================================

export interface MbtiGroupMeta {
  key: MbtiGroup
  title: string
  en: string
  desc: string
}

export const MBTI_GROUP_META: readonly MbtiGroupMeta[] = [
  {
    key: 'analyst',
    title: '理性主义',
    en: 'Analysts',
    desc: '直觉与思考的组合，偏好逻辑分析与长远规划，习惯用理性拆解复杂问题。'
  },
  {
    key: 'diplomat',
    title: '理想主义',
    en: 'Diplomats',
    desc: '直觉与情感的组合，重视意义、价值观与他人的成长，擅长共情与协调。'
  },
  {
    key: 'sentinel',
    title: '传统主义',
    en: 'Sentinels',
    desc: '实感与判断的组合，务实可靠、重视秩序与责任，是团队中最稳定的支撑。'
  },
  {
    key: 'explorer',
    title: '实用主义',
    en: 'Explorers',
    desc: '实感与知觉的组合，灵活务实、随机应变，擅长在行动中解决问题。'
  }
] as const

export interface MbtiDetail {
  code: string
  name: string
  group: MbtiGroup
  /** 气质标签（一句话点睛） */
  temperament: string
  /** 基本画像 */
  portrait: string
  /** 性格特点 */
  traits: string[]
  /** 适合的职业方向 */
  careers: string[]
}

export const MBTI_DETAILS: readonly MbtiDetail[] = [
  {
    code: 'INTJ', name: '建筑师', group: 'analyst', temperament: '军师气质',
    portrait: '富有战略眼光与长远规划能力，独立自主，善于构建系统化的思路。你习惯在独处中充电，用直觉洞察本质，用理性做出决策，是分析型的问题解决专家。',
    traits: ['战略思维强，擅长制定计划并预测趋势', '独立自主，对能力和知识有极高追求', '理性冷静，决策基于逻辑而非情绪', '高标准严要求，对自己和他人都力求卓越'],
    careers: ['战略规划师', '软件架构师', '数据分析师', '科研工作者', '管理咨询顾问', '投资分析师']
  },
  {
    code: 'INTP', name: '逻辑学家', group: 'analyst', temperament: '求知者气质',
    portrait: '热爱理论与抽象思考，擅长逻辑分析与拆解复杂问题。你对世界充满好奇，享受在思维世界中探索，追求精确的答案与自洽的体系。',
    traits: ['逻辑严密，擅长发现系统漏洞', '好奇心旺盛，热衷于理论与知识', '思维灵活开放，不轻易接受成见', '享受独处与深度思考，社交相对随性'],
    careers: ['算法工程师', '研究员', '数据科学家', '系统分析师', '高校教师', '产品经理']
  },
  {
    code: 'ENTJ', name: '指挥官', group: 'analyst', temperament: '王者气质',
    portrait: '天生的领导者，果断自信，擅长组织资源并推动目标达成。你视野开阔、行动力强，习惯站在全局思考问题，带领团队不断突破。',
    traits: ['领导力突出，善于统筹与激励他人', '目标导向，执行力与决断力兼备', '直面挑战，压力下依然保持冷静', '坦诚直接，沟通高效不绕弯子'],
    careers: ['企业管理者', '创业者', '项目经理', '咨询顾问', '律师', '投资总监']
  },
  {
    code: 'ENTP', name: '辩论家', group: 'analyst', temperament: '智多星气质',
    portrait: '思维敏捷、好奇心强，喜欢挑战与创新，是点子和灵感的发动机。你乐于在辩论中打磨想法，总能从多个角度发现问题的新解法。',
    traits: ['创意丰富，善于头脑风暴', '反应敏捷，享受智力交锋', '适应力强，讨厌一成不变', '表达出色，擅长说服与谈判'],
    careers: ['创业者', '市场营销', '公关策划', '律师', '风险投资', '产品设计师']
  },
  {
    code: 'INFJ', name: '提倡者', group: 'diplomat', temperament: '精神导师气质',
    portrait: '富有洞察力与使命感，关注他人的成长，是安静而坚定的理想主义者。你能敏锐感知他人情绪，愿意为自己认同的价值长期投入。',
    traits: ['洞察力强，善于理解他人动机', '理想主义，有坚定的价值信念', '共情能力强，乐于倾听与陪伴', '做事有计划，追求意义与深度'],
    careers: ['心理咨询师', '人力资源', '教育工作者', '作家', '公益组织从业者', '职业规划师']
  },
  {
    code: 'INFP', name: '调停者', group: 'diplomat', temperament: '治愈者气质',
    portrait: '温柔真诚的理想主义者，重视内心价值与自我表达，追求与价值观一致的生活。你想象力丰富，对美好事物有天然的感知力。',
    traits: ['价值观驱动，忠于内心选择', '富有同理心，善解人意', '想象力丰富，热爱创作', '追求和谐，不喜欢冲突与对抗'],
    careers: ['作家 / 编辑', '心理咨询师', '设计师', '教师', '社工', '音乐人']
  },
  {
    code: 'ENFJ', name: '主人公', group: 'diplomat', temperament: '教育家气质',
    portrait: '热情富有感染力，善于激励与带动他人，是天生的导师与组织者。你真诚关心每个人的成长，擅长把一群人凝聚成有温度的团队。',
    traits: ['感染力强，善于鼓舞人心', '组织能力出色，重视团队和谐', '敏锐察觉他人需求，乐于成全', '责任感强，答应的事一定做到'],
    careers: ['培训师', '人力资源', '教师', '公共关系', '团队管理者', '主持人']
  },
  {
    code: 'ENFP', name: '竞选者', group: 'diplomat', temperament: '追梦人气质',
    portrait: '热情自由的探索者，充满好奇心与创造力，享受人与人之间的连接。你总能发现生活的新鲜之处，并用热情感染身边的每一个人。',
    traits: ['热情洋溢，富有感染力', '创意不断，善于联想与发散', '重视自由，讨厌被束缚', '擅长社交，朋友遍布各个领域'],
    careers: ['创意策划', '市场营销', '记者', '培训师', '创业者', '新媒体运营']
  },
  {
    code: 'ISTJ', name: '物流师', group: 'sentinel', temperament: '检查者气质',
    portrait: '严谨务实的执行者，重视秩序、责任与承诺，是组织中最可靠的基石。你做事有条理、讲证据，答应的事情一定会按时高质量完成。',
    traits: ['责任心极强，言出必行', '注重细节与事实，一丝不苟', '逻辑清晰，按规则与流程办事', '稳定可靠，是团队的主心骨'],
    careers: ['会计师', '审计师', '工程师', '公务员', '项目管理', '法务']
  },
  {
    code: 'ISFJ', name: '守卫者', group: 'sentinel', temperament: '照顾者气质',
    portrait: '温和尽责的守护者，细心体贴，默默支持身边的人与团队。你记得每个人的喜好与需求，用行动而非言语表达关心。',
    traits: ['体贴入微，善于照顾他人', '踏实勤勉，尽职尽责', '重视传统与稳定，值得信赖', '谦逊低调，不喜争抢功劳'],
    careers: ['护士 / 医生', '教师', '行政人事', '客户服务', '图书档案管理', '社工']
  },
  {
    code: 'ESTJ', name: '总经理', group: 'sentinel', temperament: '管理者气质',
    portrait: '务实高效的管理者，重视规则与结果，擅长组织与推动落地。你说话算数、赏罚分明，是团队秩序与效率的维护者。',
    traits: ['组织能力强，擅长调配资源', '果断高效，结果导向', '原则性强，维护规则与秩序', '直率坦诚，沟通明确不模糊'],
    careers: ['企业管理者', '项目经理', '公务员', '军官 / 警官', '财务主管', '运营总监']
  },
  {
    code: 'ESFJ', name: '执政官', group: 'sentinel', temperament: '供给者气质',
    portrait: '热心友善的合作者，重视和谐与归属感，是团队氛围的维护者。你乐于助人、组织力强，总能让集体活动井然有序又充满温度。',
    traits: ['热情好客，善于活跃气氛', '乐于奉献，重视他人感受', '组织能力好，擅长协调安排', '忠诚可靠，珍视承诺与关系'],
    careers: ['人力资源', '教师', '医护工作者', '活动策划', '客户关系', '行政主管']
  },
  {
    code: 'ISTP', name: '鉴赏家', group: 'explorer', temperament: '巧匠气质',
    portrait: '冷静灵巧的问题解决者，动手能力强，擅长拆解与优化实际系统。你喜欢研究事物运作的原理，遇到故障总能最快找到症结。',
    traits: ['动手能力强，擅长实操与调试', '冷静理性，危机中临危不乱', '独立灵活，讨厌繁文缛节', '观察敏锐，注重实际效果'],
    careers: ['工程师', '飞行员', '机械师', '数据分析师', '急救医生', '电子竞技选手']
  },
  {
    code: 'ISFP', name: '探险家', group: 'explorer', temperament: '艺术家气质',
    portrait: '温和敏感的创作者，审美出众，享受当下与自由表达。你用作品而非言语表达自己，对美与体验有独到的感受力。',
    traits: ['审美敏锐，富有艺术天赋', '温和友善，与世无争', '活在当下，享受真实体验', '重视自由与个性表达'],
    careers: ['设计师', '摄影师', '音乐人', '手工艺人', '营养师', '宠物医生']
  },
  {
    code: 'ESTP', name: '企业家', group: 'explorer', temperament: '实践者气质',
    portrait: '精力充沛的实干家，反应敏捷，擅长在变化中抓住机会。你是天生的行动派，喜欢直接上场解决问题，而不是纸上谈兵。',
    traits: ['行动力强，说干就干', '临场反应快，擅长谈判与博弈', '现实敏锐，能捕捉细微机会', '热情外向，善于带动气氛'],
    careers: ['销售总监', '创业者', '投资经纪人', '公关经理', '体育教练', '应急管理']
  },
  {
    code: 'ESFP', name: '表演者', group: 'explorer', temperament: '乐天派气质',
    portrait: '热情活力的氛围担当，热爱生活，擅长用快乐感染身边的人。你享受舞台与关注，总能让平凡的场合变得生动有趣。',
    traits: ['活力四射，天生自带感染力', '乐观开朗，享受当下', '审美与品味在线，热爱生活', '乐于助人，朋友缘极佳'],
    careers: ['主持人 / 演员', '活动策划', '销售', '旅游顾问', '培训师', '时尚博主']
  }
] as const

const DETAIL_MAP = new Map<string, MbtiDetail>(MBTI_DETAILS.map(d => [d.code, d]))

export function getMbtiDetail(code: string): MbtiDetail | null {
  return DETAIL_MAP.get(code.toUpperCase()) ?? null
}

// ============================================================
// 四个字母（维度）释义：详情页「这四个字母代表什么」
// ============================================================

export interface DimensionInfo {
  letter: string
  label: string
  desc: string
}

const DIMENSION_LETTERS: Record<string, DimensionInfo> = {
  E: { letter: 'E', label: '外向', desc: '从外部世界和人际互动中获得能量' },
  I: { letter: 'I', label: '内向', desc: '在独处时精力充沛，向内获得能量' },
  S: { letter: 'S', label: '实感', desc: '关注事实和细节，相信看得见的信息' },
  N: { letter: 'N', label: '直觉', desc: '专注于想法和概念，而不是事实和细节' },
  T: { letter: 'T', label: '理性', desc: '基于逻辑和理性做出决定' },
  F: { letter: 'F', label: '感性', desc: '基于价值观和情感做出决定' },
  J: { letter: 'J', label: '判断', desc: '更喜欢计划和组织，而不是自发和灵活' },
  P: { letter: 'P', label: '感知', desc: '更喜欢灵活和自发，而不是计划和组织' }
}

export function getDimensions(code: string): DimensionInfo[] {
  return code
    .toUpperCase()
    .split('')
    .map(l => DIMENSION_LETTERS[l])
    .filter((d): d is DimensionInfo => Boolean(d))
}
