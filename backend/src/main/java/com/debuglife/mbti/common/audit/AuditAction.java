package com.debuglife.mbti.common.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上，声明该方法需要写入操作审计日志。
 *
 * <p>{@link #value()} 为操作类型代码，建议统一使用 {@code 模块_动作} 风格，
 * 如 {@code AUTH_LOGIN}、{@code ADMIN_UPDATE_USER}。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditAction {

    /** 操作类型代码 */
    String value();

    /** 操作描述，便于日志检索与展示 */
    String description() default "";
}
