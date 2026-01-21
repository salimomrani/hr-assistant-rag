package com.hrassistant.exception;

import com.hrassistant.model.ErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HrAssistantException.class)
    public ResponseEntity<ErrorInfo> handleHrAssistantException(HrAssistantException ex) {
        log.error("HrAssistantException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());

        ErrorInfo errorInfo = ErrorInfo.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(ex.getErrorCode().name())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorInfo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorInfo errorInfo = ErrorInfo.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
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
