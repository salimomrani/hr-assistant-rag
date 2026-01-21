package com.hrassistant.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorInfo {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
