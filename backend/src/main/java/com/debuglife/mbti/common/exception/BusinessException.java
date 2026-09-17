package com.debuglife.mbti.common.exception;

/**
 * 可预期业务异常，由全局异常处理器转换成统一的 RFC 7807 响应。
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        this("BUSINESS_ERROR", message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code == null || code.isBlank() ? "BUSINESS_ERROR" : code;
    }

    public BusinessException(String message, Throwable cause) {
        this("BUSINESS_ERROR", message, cause);
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code == null || code.isBlank() ? "BUSINESS_ERROR" : code;
    }

    public String getCode() {
        return code;
    }
}
