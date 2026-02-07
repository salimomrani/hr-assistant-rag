package com.hrassistant.model;

/**
 * Outcome of input classification by the guardrail service.
 *
 * @param hrRelated whether the question is HR-related
 * @param category HR category (null if off-topic)
 * @param confidence classification confidence level
 */
public record GuardrailResult(boolean hrRelated, HrCategory category, ConfidenceLevel confidence) {

  public GuardrailResult {
    if (!hrRelated) {
      category = null;
    } else if (category == null) {
      category = HrCategory.GENERAL_RH;
    }
  }
}
