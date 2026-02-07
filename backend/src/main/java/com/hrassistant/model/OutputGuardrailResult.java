package com.hrassistant.model;

import java.util.List;

/**
 * Outcome of output validation by the guardrail service.
 *
 * @param safe whether the response passed all safety checks
 * @param issues list of detected issues (empty if safe)
 * @param sanitizedContent cleaned content if partially salvageable, null if fully blocked
 */
public record OutputGuardrailResult(boolean safe, List<String> issues, String sanitizedContent) {

  public OutputGuardrailResult {
    if (issues == null) {
      issues = List.of();
    }
    if (safe) {
      issues = List.of();
      sanitizedContent = null;
    }
  }
}
