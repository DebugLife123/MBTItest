package com.debuglife.mbti.common.notification;

import com.debuglife.mbti.auth.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 站内通知服务：测评完成后异步发送结果邮件。
 *
 * <p>邮件通过独立线程异步发送，任何异常只记日志，绝不影响测评主流程；
 * 未配置 SMTP 时自动降级为纯日志，保证 demo 环境零依赖可跑。</p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String mailFrom;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 异步发送测评完成通知。用户未绑定邮箱或邮件未启用时安静跳过。
     */
    @Async
    public void sendAssessmentCompleted(User user, String typeCode, String typeName) {
        if (!mailEnabled) {
            log.info("[通知] 邮件未启用，跳过发送：user={} type={}", user.getUsername(), typeCode);
            return;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.info("[通知] 用户未绑定邮箱，跳过发送：user={}", user.getUsername());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("【MBTI 测评】你的测评结果已生成：" + typeCode);
            message.setText("你好 " + (user.getNickname() == null ? user.getUsername() : user.getNickname()) + "：\n\n"
                    + "你刚刚完成的 MBTI 职业性格测评结果已生成，人格类型为 "
                    + typeCode + (typeName == null ? "" : "（" + typeName + "）") + "。\n\n"
                    + "登录系统可查看完整测评报告、成长趋势与 AI 职业建议。\n\n"
                    + "—— MBTI 职业性格测评系统");
            mailSender.send(message);
            log.info("[通知] 测评完成邮件已发送：user={} to={} type={}", user.getUsername(), user.getEmail(), typeCode);
        } catch (Exception ex) {
            // 通知失败不回滚业务
            log.warn("[通知] 邮件发送失败：user={} err={}", user.getUsername(), ex.getMessage());
        }
    }
}
