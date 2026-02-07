# API Contracts: Guardrail Service

**Date**: 2026-02-07
**Branch**: `003-llm-guardrails`

## Overview

This feature does **not** add new HTTP endpoints. The guardrail improvements are internal to the backend service layer. The existing `/api/chat` and `/api/chat/stream` endpoints remain unchanged from the client's perspective.

The changes are:
1. More accurate off-topic detection (fewer false positives/negatives)
2. HR category attached to the classification result (internal, not exposed in API response yet)
3. Output safety filtering before responses reach the user

## Existing Endpoints (unchanged)

### POST `/api/chat`

**Request** (unchanged):
```json
{
  "question": "Comment poser mes jours de congés ?",
  "conversationId": "abc-123",
  "documentIds": ["doc-1", "doc-2"]
}
```

**Success Response** (unchanged):
```json
{
  "answer": "Pour poser vos jours de congés...",
  "sources": ["conges.pdf"],
  "conversationId": "abc-123"
}
```

**Off-topic Response** (unchanged HTTP contract, improved accuracy):
```
HTTP 400 Bad Request

{
  "timestamp": "2026-02-07T10:30:00",
  "status": 400,
  "error": "INVALID_INPUT",
  "message": "Cette question ne concerne pas les ressources humaines. Veuillez contacter directement le service RH pour des questions non liées aux politiques RH."
}
```

### POST `/api/chat/stream`

Same request/response contract as above, but streaming via SSE.

## Internal Service Contracts

### GuardrailService.classifyQuestion(String) → GuardrailResult

**Input**: User question string (non-null, non-blank)
**Output**: `GuardrailResult` record

```java
// Success - HR related
GuardrailResult(hrRelated=true, category=CONGES_ABSENCES, confidence=HIGH)

// Success - Off-topic
GuardrailResult(hrRelated=false, category=null, confidence=HIGH)

// Fallback mode (LLM unavailable)
GuardrailResult(hrRelated=true, category=GENERAL_RH, confidence=LOW)
```

**Error conditions**:
- Empty/blank question → `HrAssistantException(INVALID_INPUT)`
- LLM timeout (>5s) → Falls back to keyword detection, returns result with `confidence=LOW`
- LLM error → Falls back to keyword detection, returns result with `confidence=LOW`

### GuardrailService.validateOutput(String) → OutputGuardrailResult

**Input**: LLM response text (non-null)
**Output**: `OutputGuardrailResult` record

```java
// Safe response
OutputGuardrailResult(safe=true, issues=[], sanitizedContent=null)

// Unsafe response - PII detected
OutputGuardrailResult(safe=false, issues=["PII_DETECTED: email address"], sanitizedContent=null)

// Unsafe response - harmful content
OutputGuardrailResult(safe=false, issues=["DISCRIMINATORY_LANGUAGE"], sanitizedContent=null)
```

**Error conditions**:
- Null input → returns safe=true (empty response is not harmful)
- Pattern matching errors → logged, returns safe=true (fail-open to avoid blocking valid responses)

## LLM Classification Prompt Contract

### Input (sent to Ollama via ChatModel)

System prompt instructs the LLM to classify user questions and return structured JSON.

**Expected LLM response format**:
```json
{
  "hrRelated": true,
  "category": "CONGES_ABSENCES",
  "confidence": "HIGH"
}
```

**Valid `category` values**: `CONGES_ABSENCES`, `REMUNERATION_PAIE`, `FORMATION_DEVELOPPEMENT`, `AVANTAGES_SOCIAUX`, `CONTRAT_CONDITIONS`, `RECRUTEMENT_INTEGRATION`, `REGLEMENT_DISCIPLINE`, `GENERAL_RH`, `null` (when off-topic)

**Valid `confidence` values**: `HIGH`, `MEDIUM`, `LOW`

## Logging Contract

| Event | Level | Format |
|-------|-------|--------|
| Classification decision (any) | INFO | `"Question classified: hrRelated={}, category={}, confidence={}"` |
| LLM fallback triggered | WARN | `"LLM classification failed, falling back to keyword detection: {}"` |
| Output guardrail blocked | WARN | `"Output guardrail blocked response: issues={}"` |
| Output guardrail passed | DEBUG | `"Output guardrail passed"` |
| PII pattern matched | WARN | `"PII detected in output: {patternType}"` |
