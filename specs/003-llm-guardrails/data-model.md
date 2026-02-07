# Data Model: LLM-Based Guardrails

**Date**: 2026-02-07
**Branch**: `003-llm-guardrails`

## New Entities

### 1. HrCategory (Enum)

Predefined HR domain categories for question classification.

| Value | Display Label (FR) | Description |
|-------|-------------------|-------------|
| `CONGES_ABSENCES` | Congés / Absences | Leave, PTO, sick days, holidays |
| `REMUNERATION_PAIE` | Rémunération / Paie | Salary, payslips, bonuses, deductions |
| `FORMATION_DEVELOPPEMENT` | Formation / Développement | Training, career development, certifications |
| `AVANTAGES_SOCIAUX` | Avantages sociaux | Benefits, insurance, retirement, perks |
| `CONTRAT_CONDITIONS` | Contrat / Conditions de travail | Employment contract, working hours, remote work |
| `RECRUTEMENT_INTEGRATION` | Recrutement / Intégration | Hiring, onboarding, probation |
| `REGLEMENT_DISCIPLINE` | Règlement intérieur / Discipline | Internal rules, disciplinary procedures, code of conduct |
| `GENERAL_RH` | Général RH | General HR questions not fitting other categories |

**Validation**: Must be one of the defined values. `GENERAL_RH` is the fallback when no specific category matches.

### 2. ConfidenceLevel (Enum)

Classification confidence indicator.

| Value | Description |
|-------|-------------|
| `HIGH` | LLM is highly confident in the classification |
| `MEDIUM` | LLM has moderate confidence; borderline question |
| `LOW` | LLM has low confidence; ambiguous question (still classified as HR per permissiveness rule) |

### 3. GuardrailResult (Record/DTO)

The outcome of input classification — returned by the guardrail service after LLM-based or fallback classification.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `hrRelated` | `boolean` | No | Whether the question is HR-related |
| `category` | `HrCategory` | Yes | HR category (null if off-topic) |
| `confidence` | `ConfidenceLevel` | No | Classification confidence level |

**Invariants**:
- If `hrRelated == false`, then `category == null`
- If `hrRelated == true` and `category == null`, default to `GENERAL_RH`

### 4. OutputGuardrailResult (Record/DTO)

The outcome of output validation — returned after checking the LLM response for harmful content.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `safe` | `boolean` | No | Whether the response passed all safety checks |
| `issues` | `List<String>` | No | List of detected issues (empty if safe) |
| `sanitizedContent` | `String` | Yes | Cleaned content if partially salvageable, null if fully blocked |

**Invariants**:
- If `safe == true`, then `issues` is empty and `sanitizedContent == null`
- If `safe == false` and `sanitizedContent != null`, the response was partially sanitized
- If `safe == false` and `sanitizedContent == null`, the response was fully replaced with fallback

### 5. ClassificationRequest (Internal Record)

Internal structure sent to the LLM for classification (used in prompt construction).

| Field | Type | Description |
|-------|------|-------------|
| `question` | `String` | The user's original question |

### 6. ClassificationResponse (Internal Record)

Structure expected from the LLM classification response (parsed via `BeanOutputConverter`).

| Field | Type | Description |
|-------|------|-------------|
| `hrRelated` | `boolean` | LLM's determination |
| `category` | `String` | Category name (mapped to `HrCategory` enum) |
| `confidence` | `String` | Confidence level (mapped to `ConfidenceLevel` enum) |

## Modified Entities

### GuardrailService (existing service, refactored)

**Current state**: Stateless service with `validateQuestion(String)` → void (throws exception).

**New state**: Service with two public methods:

| Method | Input | Output | Description |
|--------|-------|--------|-------------|
| `classifyQuestion(String)` | User question | `GuardrailResult` | LLM-based classification with keyword fallback |
| `validateOutput(String)` | LLM response text | `OutputGuardrailResult` | Pattern-based output safety check |
| `validateQuestion(String)` | User question | `void` (throws) | **Preserved for backward compatibility** — calls `classifyQuestion()` internally |

**New dependencies**:
- `ChatModel` (Spring AI) — for LLM classification calls
- Classification prompt resource — loaded from classpath

## Relationships

```
ChatRequest.question
    │
    ▼
GuardrailService.classifyQuestion()
    │
    ├─ LLM path: ChatModel.call() → ClassificationResponse → GuardrailResult
    │
    └─ Fallback path: keyword matching → GuardrailResult (confidence=LOW)
    │
    ▼
GuardrailResult { hrRelated, category, confidence }
    │
    ├─ hrRelated=false → HrAssistantException(INVALID_INPUT)
    │
    └─ hrRelated=true → RAG pipeline proceeds
                              │
                              ▼
                         LLM Response
                              │
                              ▼
                    GuardrailService.validateOutput()
                              │
                              ▼
                    OutputGuardrailResult { safe, issues, sanitizedContent }
                              │
                              ├─ safe=true → response sent to user
                              └─ safe=false → fallback message sent
```

## PII Detection Patterns

Regex patterns for French HR-relevant PII (used in output guardrails):

| Pattern | Description | Example Match |
|---------|-------------|---------------|
| French phone | `(?:(?:\+33\|0033)\s?\|0)[1-9](?:[\s.-]?\d{2}){4}` | 06 12 34 56 78, +33 6 12 34 56 78 |
| Email | `[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}` | jean.dupont@company.fr |
| French SSN | `[12]\s?\d{2}\s?\d{2}\s?\d{2}\s?\d{3}\s?\d{3}\s?\d{2}` | 1 85 12 75 123 456 78 |
| IBAN | `FR\d{2}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{3}` | FR76 3000 6000 0112 3456 7890 189 |
| Salary amount | `\d{1,3}(?:[\s.,]\d{3})*(?:[.,]\d{2})?\s?(?:euros?\|EUR\|€)` | 3 500,00 euros, 45000€ |
