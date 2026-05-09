package com.example.Kcsj.common;

public final class TraceContext {
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new ThreadLocal<>();
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    private TraceContext() {
    }

    public static void setTraceId(String traceId) {
        TRACE_ID_HOLDER.set(traceId);
    }

    public static String getTraceId() {
        String traceId = TRACE_ID_HOLDER.get();
        return traceId == null ? "" : traceId;
    }

    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}

