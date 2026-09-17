package com.debuglife.mbti.user.service;

import com.debuglife.mbti.assessment.entity.MbtiPersonality;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.repository.MbtiPersonalityRepository;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.exception.BusinessException;
import com.debuglife.mbti.user.dto.CareerAdviceResponse;
import com.debuglife.mbti.user.dto.CompatibilityResponse;
import com.debuglife.mbti.user.dto.GrowthPoint;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserFeatureService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 四对维度的字母组合，用于逐对解析兼容性 */
    private static final String[][] PAIRS = {
            {"E", "I"}, {"S", "N"}, {"T", "F"}, {"J", "P"}
    };

    private final UserRepository userRepository;
    private final TestResultRepository resultRepository;
    private final MbtiPersonalityRepository personalityRepository;

    public UserFeatureService(UserRepository userRepository,
                              TestResultRepository resultRepository,
                              MbtiPersonalityRepository personalityRepository) {
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.personalityRepository = personalityRepository;
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private TestResult requireLatestResult(Long userId) {
        return resultRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException("尚未完成任何测评，请先完成一次测评"));
    }

    // ---------------- 成长轨迹 ----------------

    @Transactional(readOnly = true)
    public List<GrowthPoint> getGrowthTrack(String username, int limit) {
        User user = requireUser(username);
        List<TestResult> results = resultRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, Math.min(Math.max(limit, 1), 50)))
                .getContent();

        // 按时间正序输出，便于前端按时间轴绘制
        List<TestResult> ordered = new ArrayList<>(results);
        ordered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));

        List<GrowthPoint> points = new ArrayList<>();
        TestResult previous = null;
        for (TestResult result : ordered) {
            GrowthPoint point = new GrowthPoint();
            point.setResultId(result.getId());
            point.setTypeCode(result.getTypeCode());
            point.setTypeName(result.getPersonality() == null ? null : result.getPersonality().getTypeName());
            point.setEScore(result.getEScore());
            point.setIScore(result.getIScore());
            point.setSScore(result.getSScore());
            point.setNScore(result.getNScore());
            point.setTScore(result.getTScore());
            point.setFScore(result.getFScore());
            point.setJScore(result.getJScore());
            point.setPScore(result.getPScore());
            point.setCreatedAt(result.getCreatedAt());
            point.setChangedDimensions(previous == null
                    ? List.of() : diffDimensions(previous.getTypeCode(), result.getTypeCode()));
            points.add(point);
            previous = result;
        }
        return points;
    }

    private List<String> diffDimensions(String previousType, String currentType) {
        List<String> changed = new ArrayList<>();
        if (previousType == null || currentType == null
                || previousType.length() < 4 || currentType.length() < 4) {
            return changed;
        }
        for (int i = 0; i < PAIRS.length; i++) {
            if (previousType.charAt(i) != currentType.charAt(i)) {
                changed.add(PAIRS[i][0] + "/" + PAIRS[i][1]);
            }
        }
        return changed;
    }

    // ---------------- 性格匹配度 ----------------

    @Transactional(readOnly = true)
    public CompatibilityResponse getCompatibility(String username, String otherUsername) {
        User me = requireUser(username);
        User other = requireUser(otherUsername);

        TestResult mine = requireLatestResult(me.getId());
        TestResult theirs = requireLatestResult(other.getId());

        String myType = mine.getTypeCode();
        String otherType = theirs.getTypeCode();
        if (myType == null || otherType == null) {
            throw new BusinessException("测评结果缺少性格类型");
        }

        int score = 0;
        List<String> shared = new ArrayList<>();
        List<String> complementary = new ArrayList<>();
        List<String> watchOuts = new ArrayList<>();

        for (int i = 0; i < PAIRS.length; i++) {
            boolean same = myType.charAt(i) == otherType.charAt(i);
            String label = PAIRS[i][0] + "/" + PAIRS[i][1];
            if (same) {
                score += 25;
                shared.add(label + " 倾向一致");
            } else {
                // 互补维度按 15 分计入，差异过大时给出沟通提示
                score += 15;
                complementary.add(label + " 互补");
                watchOuts.add(label + " 上偏好相反，注意沟通方式差异");
            }
        }

        CompatibilityResponse response = new CompatibilityResponse();
        response.setMyType(myType);
        response.setOtherType(otherType);
        response.setOtherUsername(other.getUsername());
        response.setScore(score);
        response.setLevel(levelOf(score));
        response.setSummary(buildSummary(myType, otherType, score));
        response.setSharedTraits(shared);
        response.setComplementaryTraits(complementary);
        response.setWatchOuts(watchOuts);
        return response;
    }

    private String levelOf(int score) {
        if (score >= 85) return "高度契合";
        if (score >= 65) return "较为契合";
        if (score >= 50) return "可以磨合";
        return "差异较大";
    }

    private String buildSummary(String myType, String otherType, int score) {
        return String.format("%s 与 %s 的匹配度为 %d 分。", myType, otherType, score)
                + (score >= 65
                ? "双方在多数维度上有共同语言，协作时容易同频。"
                : "双方在多处维度偏好相反，建议明确分工与沟通规则以发挥互补优势。");
    }

    // ---------------- 职业建议 ----------------

    @Transactional(readOnly = true)
    public CareerAdviceResponse getCareerAdvice(String username) {
        User user = requireUser(username);
        TestResult result = requireLatestResult(user.getId());

        MbtiPersonality personality = personalityRepository.findByTypeCode(result.getTypeCode())
                .orElseThrow(() -> new BusinessException("性格类型解析不存在: " + result.getTypeCode()));

        CareerAdviceResponse response = new CareerAdviceResponse();
        response.setTypeCode(personality.getTypeCode());
        response.setTypeName(personality.getTypeName());
        response.setSummary(personality.getDescription());
        response.setCareerSuggestions(personality.getCareerSuggestions());
        response.setRecommendedRoles(splitRoles(personality.getCareerSuggestions()));
        response.setSkillSuggestions(skillSuggestionsFor(personality.getTypeCode()));
        return response;
    }

    private List<String> splitRoles(String careerSuggestions) {
        if (careerSuggestions == null || careerSuggestions.isBlank()) {
            return List.of();
        }
        String normalized = careerSuggestions.replace('：', ':').replace('、', ',')
                .replace('，', ',').replace(';', ',');
        int colon = normalized.indexOf(':');
        String tail = colon >= 0 ? normalized.substring(colon + 1) : normalized;
        Set<String> roles = new LinkedHashSet<>();
        for (String part : tail.split(",")) {
            String role = part.replace("。", "").trim();
            if (!role.isEmpty() && role.length() <= 30) {
                roles.add(role);
            }
        }
        return new ArrayList<>(roles);
    }

    private List<String> skillSuggestionsFor(String typeCode) {
        return switch (typeCode) {
            case "INTJ" -> Arrays.asList("深化系统设计与架构能力", "练习把战略拆解为可执行里程碑", "主动做跨团队沟通与表达");
            case "INTP" -> Arrays.asList("把兴趣收敛为可交付项目", "补强工程规范与测试习惯", "练习在有限信息下做决策");
            case "ENTJ" -> Arrays.asList("提升倾听与教练式反馈能力", "建立风险与合规意识", "在决策中纳更多人的视角");
            case "ENTP" -> Arrays.asList("训练项目收尾与复盘习惯", "建立个人知识管理体系", "把创意转化为可验证的最小闭环");
            case "INFJ" -> Arrays.asList("设定边界，避免情绪过载", "把洞察沉淀为可复用方法论", "主动展示成果与影响力");
            case "INFP" -> Arrays.asList("建立稳定的交付节奏", "用作品集表达价值观", "练习结构化表达与谈判");
            case "ENFJ" -> Arrays.asList("平衡他人需求与自身精力", "学习数据驱动的决策方法", "培养接班人，避免过度承担");
            case "ENFP" -> Arrays.asList("聚焦少量高优目标", "建立进度跟踪机制", "打磨深度专业能力");
            case "ISTJ" -> Arrays.asList("练习在变化中快速试错", "提升对外沟通与影响力", "关注流程背后的人性因素");
            case "ISFJ" -> Arrays.asList("练习有技巧地拒绝", "主动争取个人发展机会", "用数据量化自己的贡献");
            case "ESTJ" -> Arrays.asList("增加决策弹性与授权", "提升共情式沟通", "关注创新与长期趋势");
            case "ESFJ" -> Arrays.asList("建立独立判断标准", "练习处理冲突与负面反馈", "提升结构化分析能力");
            case "ISTP" -> Arrays.asList("补充长期规划能力", "练习表达思考过程", "沉淀技术文档与经验");
            case "ISFP" -> Arrays.asList("建立职业成长节奏", "练习主动沟通需求", "把审美优势产品化");
            case "ESTP" -> Arrays.asList("强化计划与风险控制", "培养长期专业沉淀", "练习深度思考与复盘");
            case "ESFP" -> Arrays.asList("建立目标管理习惯", "提升专业深度", "训练结构化表达与规划");
            default -> Arrays.asList("明确职业目标", "持续积累可展示的成果", "补强沟通与技术能力");
        };
    }

    // ---------------- 导出 ----------------

    @Transactional(readOnly = true)
    public String exportHistoryCsv(String username) {
        User user = requireUser(username);
        List<TestResult> results = resultRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 200))
                .getContent();

        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("记录ID,测试时间,性格类型,E,I,S,N,T,F,J,P\n");
        for (TestResult r : results) {
            csv.append(r.getId()).append(',')
               .append(r.getCreatedAt() == null ? "" : DATE_FORMAT.format(r.getCreatedAt())).append(',')
               .append(r.getTypeCode()).append(',')
               .append(r.getEScore()).append(',').append(r.getIScore()).append(',')
               .append(r.getSScore()).append(',').append(r.getNScore()).append(',')
               .append(r.getTScore()).append(',').append(r.getFScore()).append(',')
               .append(r.getJScore()).append(',').append(r.getPScore()).append('\n');
        }
        return csv.toString();
    }
}
