# Implementation Plan: LLM-Based Guardrails for HR Question Classification

**Branch**: `003-llm-guardrails` | **Date**: 2026-02-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/003-llm-guardrails/spec.md`

## Summary

Replace the keyword-based off-topic detection in `GuardrailService` with LLM-based intelligent classification using Spring AI `ChatModel.call()` (synchronous) with `BeanOutputConverter` for structured JSON output. Add output guardrails using regex-based PII detection and keyword-based harmful content filtering. Categorize HR questions into 8 predefined categories in a single LLM call. Maintain keyword-based fallback when LLM is unavailable (5s timeout). Add comprehensive unit tests with mocked `ChatModel`.

## Technical Context

**Language/Version**: Java 21 (Amazon Corretto)
**Primary Dependencies**: Spring Boot 4.0.1, Spring AI 2.0.0-M1 (Ollama starter), Lombok, MapStruct
**Storage**: PostgreSQL 16 + pgvector (existing, unchanged), Redis 7.4 (existing, unchanged)
**Testing**: JUnit 5 + Mockito (via spring-boot-starter-test, **new dependency**), reactor-test (**new dependency**)
**Target Platform**: Linux/macOS server, Docker Compose
**Project Type**: Web application (backend only for this feature)
**Performance Goals**: Classification adds <2s latency; output guardrail <10ms (regex-based)
**Constraints**: LLM classification timeout 5s, fallback to keywords on failure
**Scale/Scope**: Single-user local deployment, ~30 Java files in backend

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

No project-specific constitution defined (template placeholders only). No gates to enforce. Proceeding.

**Post-Phase 1 re-check**: Design uses existing patterns (service layer, exception handling, Spring AI ChatModel). No new architectural patterns introduced. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/003-llm-guardrails/
├── spec.md              # Feature specification
├── plan.md              # This file
├── research.md          # Phase 0: technology decisions
├── data-model.md        # Phase 1: entity definitions
├── quickstart.md        # Phase 1: build & verify guide
├── contracts/
│   └── guardrail-api.md # Phase 1: internal service contracts
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
backend/
├── pom.xml                                          # MODIFIED: add test dependencies
├── src/main/java/com/hrassistant/
│   ├── model/
│   │   ├── HrCategory.java                          # NEW: HR category enum
│   │   ├── ConfidenceLevel.java                     # NEW: confidence level enum
│   │   ├── GuardrailResult.java                     # NEW: classification result record
│   │   └── OutputGuardrailResult.java               # NEW: output validation result record
│   ├── service/
│   │   ├── GuardrailService.java                    # MODIFIED: LLM classification + output filtering
│   │   └── StreamingRagService.java                 # MODIFIED: integrate output guardrails
│   └── ...
├── src/main/resources/
│   └── prompts/
│       └── classification-prompt.txt                # NEW: LLM classification prompt template
└── src/test/java/com/hrassistant/
    └── service/
        └── GuardrailServiceTest.java                # NEW: comprehensive unit tests
```

**Structure Decision**: Backend-only changes. This feature modifies the existing backend service layer and adds new model classes. No frontend changes. No new packages — all new files go into existing `model/` and `service/` packages.

## Implementation Approach

### Phase 1: New Model Classes (no dependencies)

Create the 4 new model files:
1. `HrCategory` enum — 8 HR categories with French display labels
2. `ConfidenceLevel` enum — HIGH, MEDIUM, LOW
3. `GuardrailResult` record — classification outcome (hrRelated, category, confidence)
4. `OutputGuardrailResult` record — output validation outcome (safe, issues, sanitizedContent)

### Phase 2: Classification Prompt

Create `classification-prompt.txt` — a French system prompt that instructs llama3.2 to:
- Classify questions as HR-related or off-topic
- Assign an HR category from the predefined list
- Return a confidence level
- Return structured JSON matching `ClassificationResponse` schema
- Err on permissiveness for ambiguous questions

### Phase 3: Refactor GuardrailService (core change)

Refactor `GuardrailService` to:
1. Inject `ChatModel` (Spring AI)
2. Add `classifyQuestion(String)` method:
   - Build classification prompt with `PromptTemplate`
   - Call `chatModel.call()` with `OllamaChatOptions(temperature=0.0)`
   - Parse response via `BeanOutputConverter<ClassificationResponse>`
   - Map to `GuardrailResult`
   - Wrap in `CompletableFuture.orTimeout(5s)` for fallback
   - On any failure: fall back to existing keyword detection
3. Add `validateOutput(String)` method:
   - Run regex patterns for French PII (phone, email, SSN, IBAN, salary)
   - Check for harmful content keywords
   - Return `OutputGuardrailResult`
4. Update `validateQuestion()` to use `classifyQuestion()` internally
5. Preserve existing `HrAssistantException(INVALID_INPUT)` behavior for off-topic

### Phase 4: Integrate Output Guardrails in Pipeline

Modify `StreamingRagService.chatStream()` to:
1. After LLM streaming completes, validate the full response via `guardrailService.validateOutput()`
2. If unsafe: replace response with fallback message
3. If safe: pass through unchanged

### Phase 5: Test Infrastructure + Unit Tests

1. Add `spring-boot-starter-test` and `reactor-test` to `pom.xml`
2. Create `GuardrailServiceTest` with mocked `ChatModel`:
   - Test LLM classification (HR question → correct category)
   - Test LLM classification (off-topic → rejected)
   - Test LLM fallback (timeout → keyword detection)
   - Test LLM fallback (exception → keyword detection)
   - Test output guardrails (PII detection)
   - Test output guardrails (safe response passes)
   - Test edge cases (empty question, long question, prompt injection)
   - Test keyword fallback accuracy (existing keywords still work)

## Key Design Decisions

| Decision | Choice | See |
|----------|--------|-----|
| Classification call type | Synchronous `ChatModel.call()` | [research.md #1](research.md) |
| Response parsing | `BeanOutputConverter` + Ollama `.format()` | [research.md #2](research.md) |
| Temperature override | Per-call `0.0` via `OllamaChatOptions` | [research.md #3](research.md) |
| Timeout strategy | `CompletableFuture.orTimeout(5s)` + keyword fallback | [research.md #4](research.md) |
| Output guardrails | Regex PII + keyword harmful content | [research.md #5](research.md) |
| Prompt language | French prompt, English enum values | [research.md #6](research.md) |
| Test approach | Mocked ChatModel, no real Ollama needed | [research.md #7](research.md) |
| Single vs dual call | Single call for classification + categorization | [research.md #8](research.md) |

## Complexity Tracking

No constitution violations. No complexity justifications needed.
