package com.debuglife.mbti.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Unified success response envelope")
public record ApiResponse<T>(
        @Schema(example = "0") int code,
        @Schema(example = "OK") String message,
        T data,
        OffsetDateTime timestamp,
        @Schema(example = "/api/v1/health") String path,
        @Schema(example = "b4c6b004-0bc2-4d25-95e2-3fefc425dc17") String traceId
) {
    // Simple success with data only
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "OK", data, OffsetDateTime.now(), null, null);
    }

    // Success with data, path and traceId
    public static <T> ApiResponse<T> success(T data, String path, String traceId) {
        return new ApiResponse<>(0, "OK", data, OffsetDateTime.now(), path, traceId);
    }

    // Success with custom message, data, path and traceId
    public static <T> ApiResponse<T> success(String message, T data, String path, String traceId) {
        return new ApiResponse<>(0, message, data, OffsetDateTime.now(), path, traceId);
    }
}
