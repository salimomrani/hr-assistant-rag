package com.hrassistant.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HrAssistantException.class)
    public ResponseEntity<Map<String, Object>> handleHrAssistantException(HrAssistantException ex) {
        log.error("HrAssistantException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", ex.getErrorCode().name(),
                "message", ex.getMessage()
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "INTERNAL_ERROR",
                "message", "An unexpected error occurred"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private HttpStatus mapErrorCodeToStatus(HrAssistantException.ErrorCode errorCode) {
        return switch (errorCode) {
            case DOCUMENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case DOCUMENT_PROCESSING_ERROR, EMBEDDING_ERROR, LLM_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
