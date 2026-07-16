package org.xyjh.xyjhstartweb.duduplan.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "org.xyjh.xyjhstartweb.duduplan")
public class DuduPlanExceptionHandler {
    @ExceptionHandler(DuduPlanApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(DuduPlanApiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(Map.of("error", exception.getError()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
    }
}
