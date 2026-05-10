package com.example.Kcsj.common;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ApiResponse<Void> handleApiException(ApiException ex, HttpServletResponse response) {
        setStatusFromCode(response, ex.getErrorCode());
        return ApiResponse.failure(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        MissingServletRequestParameterException.class,
        HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidationException(Exception ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String message = "invalid parameter";
        if (ex instanceof MethodArgumentNotValidException) {
            message = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError).collect(Collectors.joining("; "));
        } else if (ex instanceof BindException) {
            message = ((BindException) ex).getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError).collect(Collectors.joining("; "));
        } else if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException e = (MissingServletRequestParameterException) ex;
            message = "missing parameter: " + e.getParameterName();
        } else if (ex.getMessage() != null) {
            message = ex.getMessage();
        }
        return ApiResponse.failure(ErrorCode.INVALID_PARAM, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return ApiResponse.failure(ErrorCode.FORBIDDEN, "forbidden");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ApiResponse<Void> handleExpiredJwt(ExpiredJwtException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return ApiResponse.failure(ErrorCode.TOKEN_EXPIRED, "token expired");
    }

    @ExceptionHandler(JwtException.class)
    public ApiResponse<Void> handleJwt(JwtException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return ApiResponse.failure(ErrorCode.TOKEN_INVALID, "token invalid");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception ex, HttpServletResponse response) {
        log.error("Unhandled exception traceId={}", TraceContext.getTraceId(), ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return ApiResponse.failure(ErrorCode.INTERNAL_ERROR, "internal error");
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private void setStatusFromCode(HttpServletResponse response, ErrorCode code) {
        if (code == ErrorCode.UNAUTHORIZED || code == ErrorCode.TOKEN_INVALID || code == ErrorCode.TOKEN_EXPIRED) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (code == ErrorCode.FORBIDDEN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (code == ErrorCode.INVALID_PARAM) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (code == ErrorCode.RESOURCE_NOT_FOUND || code == ErrorCode.USER_NOT_FOUND) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (code == ErrorCode.CONFLICT) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}

