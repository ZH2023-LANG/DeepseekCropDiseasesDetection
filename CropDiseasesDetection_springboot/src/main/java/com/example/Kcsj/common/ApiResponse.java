package com.example.Kcsj.common;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private String traceId;
    private String timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ErrorCode.SUCCESS.name());
        response.setMessage("ok");
        response.setData(data);
        response.setTraceId(TraceContext.getTraceId());
        response.setTimestamp(OffsetDateTime.now(ZoneOffset.ofHours(8)).toString());
        return response;
    }

    public static <T> ApiResponse<T> failure(ErrorCode code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code.name());
        response.setMessage(message);
        response.setTraceId(TraceContext.getTraceId());
        response.setTimestamp(OffsetDateTime.now(ZoneOffset.ofHours(8)).toString());
        return response;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

