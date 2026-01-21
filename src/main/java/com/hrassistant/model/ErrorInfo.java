package com.hrassistant.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author : salimomrani
 * @email : omrani_salim@outlook.fr
 * @created : 21/01/2026, mercredi
 **/
@Data
@Builder
public class ErrorInfo {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
