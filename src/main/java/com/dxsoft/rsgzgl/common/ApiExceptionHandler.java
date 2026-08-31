package com.dxsoft.rsgzgl.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "当前账号无权执行此操作。";
        }
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, message);
        detail.setProperties(Map.of("timestamp", Instant.now().toString()));
        return detail;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setProperties(Map.of("timestamp", Instant.now().toString()));
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setProperties(Map.of("timestamp", Instant.now().toString()));
        return detail;
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIllegalState(IllegalStateException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        detail.setProperties(Map.of("timestamp", Instant.now().toString()));
        return detail;
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    ProblemDetail handleDataAccess(org.springframework.dao.DataAccessException exception) {
        Throwable root = exception.getMostSpecificCause();
        String message = root == null || root.getMessage() == null || root.getMessage().isBlank()
                ? "数据库操作失败。"
                : root.getMessage();
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        detail.setProperties(Map.of("timestamp", Instant.now().toString()));
        return detail;
    }
}
