package com.example.Kcsj.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TraceIdLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String traceId = request.getHeader(TraceContext.HEADER_TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TraceContext.setTraceId(traceId);
        MDC.put("traceId", traceId);
        long startMs = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - startMs;
            response.setHeader(TraceContext.HEADER_TRACE_ID, traceId);
            log.info("request method={} path={} status={} costMs={} ip={} userId={}",
                request.getMethod(), request.getRequestURI(), response.getStatus(), cost, request.getRemoteAddr(), resolveUserId());
            TraceContext.clear();
            MDC.remove("traceId");
        }
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.example.Kcsj.security.UserPrincipal) {
            return String.valueOf(((com.example.Kcsj.security.UserPrincipal) principal).getUserId());
        }
        return "anonymous";
    }
}
