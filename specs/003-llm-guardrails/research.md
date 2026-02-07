# Research: LLM-Based Guardrails for HR Question Classification

**Date**: 2026-02-07
**Branch**: `003-llm-guardrails`

## 1. LLM Classification Call Strategy

**Decision**: Use `ChatModel.call(Prompt)` (synchronous, blocking) for classification instead of `chatModel.stream()`.

**Rationale**: Classification is a short, atomic operation that returns a single structured result. Streaming adds unnecessary complexity for a response that is typically under 100 tokens. The blocking call simplifies error handling and timeout management.

**Alternatives considered**:
- `chatModel.stream()` + `collectList().block()` — Adds reactive overhead for no benefit
- Separate HTTP call to Ollama REST API — Bypasses Spring AI abstractions, harder to maintain

## 2. Structured Output Parsing

**Decision**: Use Spring AI `BeanOutputConverter<T>` with a Java record to parse LLM classification responses into strongly-typed objects.

**Rationale**: `BeanOutputConverter` auto-generates JSON schema instructions for the LLM prompt and deserializes the response. Combined with Ollama's `.format()` option in `OllamaChatOptions`, this constrains the model at the token-generation level to only produce valid JSON matching the schema — more reliable than prompt-only instructions.

**Alternatives considered**:
- Manual JSON parsing with `ObjectMapper` — Error-prone, no schema enforcement at model level
- `MapOutputConverter` — Loses type safety, requires manual casting
- Regex-based parsing — Fragile, breaks with unexpected LLM output

## 3. Per-Call Temperature Override

**Decision**: Use `OllamaChatOptions.builder().temperature(0.0).build()` as per-call options for classification, separate from the global `0.7` temperature used for RAG responses.

**Rationale**: Classification requires deterministic output. A temperature of 0.0 ensures the same question always produces the same classification. The global RAG temperature of 0.7 remains unchanged for creative, natural-sounding answers.

**Alternatives considered**:
- Global temperature change — Would degrade RAG response quality
- Separate `ChatModel` bean — Over-engineered for a single parameter override

## 4. Timeout and Fallback Strategy

**Decision**: Use a `CompletableFuture` with `orTimeout(5, TimeUnit.SECONDS)` wrapping the `chatModel.call()`, falling back to keyword-based detection on timeout or exception.

**Rationale**: Spring AI's `OllamaChatOptions` does not expose a per-call timeout. Wrapping the synchronous call in a `CompletableFuture` with timeout provides reliable fallback behavior without requiring a separate `ChatModel` bean with different HTTP client settings.

**Alternatives considered**:
- Separate `OllamaChatModel` bean with custom HTTP timeout — Over-engineered, two beans to manage
- `WebClient` with timeout calling Ollama REST API — Bypasses Spring AI, loses structured output support
- No timeout (rely on global HTTP timeout) — Risky, could block the pipeline indefinitely

## 5. Output Guardrails Approach

**Decision**: Use regex-based pattern matching for PII detection and keyword lists for harmful content detection. No second LLM call for output validation.

**Rationale**: Pattern-based detection is fast (sub-millisecond), deterministic, and easy to test. A second LLM call would double latency and introduce another failure point. The RAG prompt already constrains the LLM significantly; output guardrails are a safety net for edge cases.

**Alternatives considered**:
- LLM-based output classification — Too slow (adds 2-5 seconds), doubles failure surface
- External moderation API — Adds external dependency, network latency, and cost
- No output guardrails — Unacceptable for HR context where incorrect advice has legal consequences

## 6. Classification Prompt Language

**Decision**: Write the classification system prompt in French with English enum values for categories and confidence levels.

**Rationale**: The primary user base speaks French, and LLM classification accuracy improves when the system prompt matches the input language. Using English enum values (e.g., `LEAVE_ABSENCE`, `HIGH`) keeps the code consistent with Java naming conventions and avoids encoding issues.

**Alternatives considered**:
- Fully English prompt — Lower accuracy on French input questions
- Fully French including enum values — Requires translation layer, complicates code

## 7. Test Infrastructure

**Decision**: Add `spring-boot-starter-test` (includes JUnit 5 + Mockito) and `reactor-test` to `pom.xml`. Mock `ChatModel` in unit tests instead of calling Ollama.

**Rationale**: No test infrastructure currently exists in the backend. Unit tests should mock the LLM to be fast, deterministic, and runnable without Ollama. Integration tests with a real LLM are out of scope for this feature.

**Alternatives considered**:
- Integration tests with real Ollama — Slow, non-deterministic, requires running infrastructure
- Testcontainers for Ollama — Adds complexity, slower CI
- No tests — Violates spec requirement US4

## 8. Single vs Separate Classification and Categorization

**Decision**: Single LLM call returning both `isHrRelated` (boolean) and `category` (enum) in one structured JSON response.

**Rationale**: Per spec clarification, a single call minimizes latency. The classification prompt instructs the LLM to return a JSON object with all fields. If the question is off-topic, the category is set to `null`.

**Alternatives considered**:
- Two sequential calls (classify then categorize) — Doubles latency unnecessarily
- Classification only, categorize later in pipeline — Wastes the context already available to the LLM
