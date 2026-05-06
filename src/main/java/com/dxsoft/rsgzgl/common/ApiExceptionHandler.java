package com.dxsoft.rsgzgl.common;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

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
}
