package com.hrassistant.exception;

import lombok.Getter;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Getter
public class HrAssistantException extends RuntimeException {

    private final ErrorCode errorCode;

    public HrAssistantException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public HrAssistantException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public HrAssistantException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public HrAssistantException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public enum ErrorCode {
        DOCUMENT_NOT_FOUND,
        DOCUMENT_PROCESSING_ERROR,
        EMBEDDING_ERROR,
        LLM_ERROR,
        INVALID_INPUT,
        INTERNAL_ERROR
    }
}
