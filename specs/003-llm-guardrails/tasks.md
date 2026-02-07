# Tasks: LLM-Based Guardrails for HR Question Classification

**Input**: Design documents from `/specs/003-llm-guardrails/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/guardrail-api.md, quickstart.md

**Tests**: Included (explicitly requested in US4 of spec.md)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app monorepo**: `backend/src/main/java/com/hrassistant/` for source, `backend/src/test/java/com/hrassistant/` for tests

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add test dependencies and create test directory structure

- [x] T001 Add `spring-boot-starter-test` (includes JUnit 5 + Mockito) and `reactor-test` as test-scoped dependencies in `backend/pom.xml`
- [x] T002 Create test directory structure: `backend/src/test/java/com/hrassistant/service/`

**Checkpoint**: Test infrastructure ready — `mvn test` runs (no tests yet, zero failures)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create all shared model classes that multiple user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 [P] Create `HrCategory` enum with 8 HR categories and French display labels in `backend/src/main/java/com/hrassistant/model/HrCategory.java` — values: CONGES_ABSENCES, REMUNERATION_PAIE, FORMATION_DEVELOPPEMENT, AVANTAGES_SOCIAUX, CONTRAT_CONDITIONS, RECRUTEMENT_INTEGRATION, REGLEMENT_DISCIPLINE, GENERAL_RH; each with a `displayLabel` field (e.g., "Congés / Absences"); see data-model.md §1
- [x] T004 [P] Create `ConfidenceLevel` enum (HIGH, MEDIUM, LOW) in `backend/src/main/java/com/hrassistant/model/ConfidenceLevel.java` — see data-model.md §2
- [x] T005 [P] Create `GuardrailResult` record (boolean hrRelated, HrCategory category, ConfidenceLevel confidence) in `backend/src/main/java/com/hrassistant/model/GuardrailResult.java` — enforce invariants: if hrRelated=false then category=null; if hrRelated=true and category=null then default to GENERAL_RH; see data-model.md §3
- [x] T006 [P] Create `OutputGuardrailResult` record (boolean safe, List<String> issues, String sanitizedContent) in `backend/src/main/java/com/hrassistant/model/OutputGuardrailResult.java` — enforce invariants: if safe=true then issues is empty and sanitizedContent=null; see data-model.md §4
- [x] T007 [P] Create `ClassificationResponse` record (boolean hrRelated, String category, String confidence) in `backend/src/main/java/com/hrassistant/model/ClassificationResponse.java` — internal record used by BeanOutputConverter for LLM response parsing; see data-model.md §6

**Checkpoint**: All model classes compile — `mvn clean compile` passes

---

## Phase 3: User Story 1 — Intelligent Off-Topic Detection (Priority: P1) 🎯 MVP

**Goal**: Replace keyword-based off-topic detection with LLM-based classification using Spring AI `ChatModel.call()` with `BeanOutputConverter`. Maintain keyword fallback when LLM is unavailable (5s timeout).

**Independent Test**: Send HR and non-HR questions via `POST /api/chat` — HR questions get answers, off-topic returns 400, ambiguous questions pass (permissive), and when Ollama is stopped the system falls back to keyword detection.

### Implementation for User Story 1

- [x] T008 [US1] Create classification prompt template in `backend/src/main/resources/prompts/classification-prompt.txt` — French system prompt instructing llama3.2 to classify questions as HR-related or off-topic, return structured JSON matching ClassificationResponse schema (hrRelated, category, confidence); include the 8 HR categories with descriptions; instruct to err on permissiveness for ambiguous questions; handle any input language; see plan.md Phase 2 and research.md §6
- [x] T009 [US1] Refactor `GuardrailService.classifyQuestion()` method in `backend/src/main/java/com/hrassistant/service/GuardrailService.java` — inject `ChatModel` and prompt Resource; implement LLM classification path: build prompt with `PromptTemplate`, call `chatModel.call()` with `OllamaChatOptions(temperature=0.0)` and BeanOutputConverter format, parse response via `BeanOutputConverter<ClassificationResponse>`, map to `GuardrailResult`; wrap in `CompletableFuture.orTimeout(5, SECONDS)` for fallback; on any failure fall back to existing keyword detection returning `GuardrailResult(hrRelated, GENERAL_RH, LOW)`; log all classification decisions at INFO level; see plan.md Phase 3, research.md §1-§4, contracts/guardrail-api.md
- [x] T010 [US1] Update `validateQuestion()` method in `backend/src/main/java/com/hrassistant/service/GuardrailService.java` — call `classifyQuestion()` internally; if `hrRelated=false` throw `HrAssistantException(INVALID_INPUT)` with existing French error message; preserve existing empty/blank question validation; see contracts/guardrail-api.md

**Checkpoint**: US1 complete — HR questions classified by LLM, off-topic rejected, fallback works when Ollama is stopped. Verify with quickstart.md curl commands.

---

## Phase 4: User Story 2 — HR Question Categorization (Priority: P2)

**Goal**: Categorize HR-related questions into 8 predefined categories in the same LLM classification call. The categorization is already embedded in US1's single-call approach — this phase ensures correct category mapping and fallback behavior.

**Independent Test**: Send questions from each HR category and verify correct category assignment in logs (category is not yet exposed in API response). Send multi-category questions and verify the most relevant category is returned. Send questions matching no specific category and verify GENERAL_RH fallback.

### Implementation for User Story 2

- [x] T011 [US2] Enhance category mapping in `classifyQuestion()` in `backend/src/main/java/com/hrassistant/service/GuardrailService.java` — add robust mapping from ClassificationResponse.category string to HrCategory enum (case-insensitive, handle null/unknown → GENERAL_RH); log classified category at INFO level per logging contract; ensure keyword fallback returns GENERAL_RH for HR-related questions; see data-model.md §1, contracts/guardrail-api.md logging contract

**Checkpoint**: US2 complete — HR questions correctly categorized. Verify by checking application logs showing category for each classification.

---

## Phase 5: User Story 3 — Output Guardrails (Priority: P2)

**Goal**: Filter LLM output for PII (French phone, email, SSN, IBAN, salary) and harmful content before responses reach the user. Replace unsafe responses with a safe fallback message.

**Independent Test**: Inject test strings containing French PII patterns and harmful keywords — verify detection. Send normal responses — verify they pass through unmodified and quickly (<10ms).

### Implementation for User Story 3

- [x] T012 [US3] Implement `validateOutput(String)` method in `backend/src/main/java/com/hrassistant/service/GuardrailService.java` — regex-based PII detection for French phone (`(?:(?:\+33|0033)\s?|0)[1-9](?:[\s.-]?\d{2}){4}`), email, French SSN (15 digits), IBAN (FR prefix), salary amounts with currency indicators (€/euros/EUR); keyword-based harmful content filtering; return `OutputGuardrailResult(safe, issues, sanitizedContent=null for blocked)` per contracts/guardrail-api.md; log PII detections at WARN level; log passed responses at DEBUG level; see data-model.md §PII Detection Patterns, research.md §5
- [x] T013 [US3] Integrate output guardrails into `StreamingRagService.chatStream()` in `backend/src/main/java/com/hrassistant/service/StreamingRagService.java` — after LLM streaming completes, validate full response via `guardrailService.validateOutput()`; if unsafe replace response with safe fallback message ("Je ne suis pas en mesure de répondre à cette question. Veuillez contacter le service RH directement."); if safe pass through unchanged; see plan.md Phase 4, spec.md US3 acceptance scenario 4

**Checkpoint**: US3 complete — Output guardrails catch PII and harmful content. Normal responses pass through without noticeable delay.

---

## Phase 6: User Story 4 — Unit Test Coverage (Priority: P3)

**Goal**: Comprehensive unit tests for GuardrailService with mocked ChatModel covering classification, fallback, output filtering, and edge cases.

**Independent Test**: Run `mvn test` — all tests pass.

### Tests for User Story 4

- [x] T014 [P] [US4] Create `GuardrailServiceTest` test class in `backend/src/test/java/com/hrassistant/service/GuardrailServiceTest.java` — setup with `@ExtendWith(MockitoExtension.class)`, mock `ChatModel`, inject `GuardrailService`; load classification prompt from test resources; see research.md §7
- [x] T015 [P] [US4] Add LLM classification tests in `GuardrailServiceTest` — test HR question returns correct GuardrailResult (hrRelated=true, category, confidence=HIGH); test off-topic question returns hrRelated=false; test ambiguous question classified as HR-related (permissiveness); test prompt injection treated as off-topic; mock ChatModel to return expected ClassificationResponse JSON; see spec.md US1 acceptance scenarios 1-5
- [x] T016 [P] [US4] Add fallback tests in `GuardrailServiceTest` — test LLM timeout (>5s) falls back to keyword detection with confidence=LOW; test LLM exception falls back to keyword detection; test keyword fallback still correctly detects existing off-topic keywords; see spec.md US1 acceptance scenario 4
- [x] T017 [P] [US4] Add output guardrail tests in `GuardrailServiceTest` — test French phone number detected; test email detected; test French SSN detected; test IBAN detected; test salary amount detected; test safe response passes through; test multiple PII in single response; see spec.md US3 acceptance scenarios 1-3, data-model.md PII patterns
- [x] T018 [P] [US4] Add edge case tests in `GuardrailServiceTest` — test empty question throws HrAssistantException; test whitespace-only question throws HrAssistantException; test very long question (>5000 chars) handled gracefully; test unexpected LLM response format triggers fallback; test null output returns safe=true; see spec.md Edge Cases

**Checkpoint**: US4 complete — `mvn test` passes with all guardrail scenarios covered.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Validation, cleanup, and documentation

- [ ] T019 Run quickstart.md validation — **REQUIRES Ollama running locally, manual integration test** — start infrastructure with `docker compose up -d`, build with `mvn clean install`, run with `mvn spring-boot:run`, execute all 4 curl verification commands from `specs/003-llm-guardrails/quickstart.md` and confirm expected results
- [x] T020 Run full test suite — execute `mvn test` and confirm all tests pass with zero failures
- [x] T021 Review logging output — verify classification decisions logged at INFO, fallback triggers at WARN, output blocks at WARN, output passes at DEBUG per contracts/guardrail-api.md logging contract

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 completion — MVP, must be done first
- **US2 (Phase 4)**: Depends on US1 (T009 creates classifyQuestion, T011 enhances it)
- **US3 (Phase 5)**: Depends on Phase 2 completion — can run in parallel with US1/US2 (different methods)
- **US4 (Phase 6)**: Depends on US1 + US2 + US3 completion (tests cover all implemented features)
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Depends on Phase 2 only — core MVP
- **US2 (P2)**: Depends on US1 (enhances classifyQuestion method created in US1)
- **US3 (P2)**: Depends on Phase 2 only — independent from US1/US2 (validates output, not input)
- **US4 (P3)**: Depends on US1 + US2 + US3 (tests all of them)

### Within Each User Story

- Models before services
- Services before pipeline integration
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- **Phase 2**: All model classes (T003-T007) are independent files — all 5 can run in parallel
- **Phase 5**: US3 (output guardrails) can start as soon as Phase 2 completes — does not depend on US1
- **Phase 6**: All test tasks (T014-T018) write to the same file but cover independent test methods — can be authored in parallel by different agents
- **Cross-phase**: US1 (input classification) and US3 (output validation) modify different methods in GuardrailService and different files (StreamingRagService) — partial parallelism possible

---

## Parallel Example: Phase 2 (Foundational)

```bash
# Launch all model classes in parallel (5 independent files):
Task: "Create HrCategory enum in backend/.../model/HrCategory.java"
Task: "Create ConfidenceLevel enum in backend/.../model/ConfidenceLevel.java"
Task: "Create GuardrailResult record in backend/.../model/GuardrailResult.java"
Task: "Create OutputGuardrailResult record in backend/.../model/OutputGuardrailResult.java"
Task: "Create ClassificationResponse record in backend/.../model/ClassificationResponse.java"
```

## Parallel Example: Phase 6 (Tests)

```bash
# Launch all test groups in parallel (independent test methods in same file):
Task: "LLM classification tests in GuardrailServiceTest"
Task: "Fallback tests in GuardrailServiceTest"
Task: "Output guardrail tests in GuardrailServiceTest"
Task: "Edge case tests in GuardrailServiceTest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (test deps)
2. Complete Phase 2: Foundational (model classes)
3. Complete Phase 3: User Story 1 (LLM classification + fallback)
4. **STOP and VALIDATE**: Test with quickstart.md curl commands
5. Deploy/demo if ready — off-topic detection already improved

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 → LLM classification works → **MVP!**
3. Add US2 → Categories logged → Analytics-ready
4. Add US3 → Output safety → Production-ready
5. Add US4 → Tests pass → Regression-proof
6. Polish → Validated end-to-end

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- The classification prompt (T008) is the most critical creative task — it determines classification accuracy
- GuardrailService.java is modified by US1, US2, and US3 — these cannot fully parallelize on that file
