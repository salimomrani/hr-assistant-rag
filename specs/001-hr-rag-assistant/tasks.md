# Tasks: HR RAG Assistant

**Input**: Design documents from `/specs/001-hr-rag-assistant/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Tests not explicitly requested - implementation tasks only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4)
- Paths use `src/main/java/com/hrassistant/` for source code

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependencies, and configuration

- [x] T001 Add PDFBox dependency to pom.xml for PDF parsing
- [x] T002 [P] Create application.yml with Ollama and RAG configuration in src/main/resources/application.yml
- [X] T003 [P] Create RAG system prompt template in src/main/resources/prompts/rag-prompt.txt
- [x] T004 [P] Create package directories for config, controller, service, model, exception

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T005 Create OllamaConfig.java with ChatLanguageModel and EmbeddingModel beans in src/main/java/com/hrassistant/config/OllamaConfig.java
- [X] T006 [P] Create HrAssistantException.java custom exception in src/main/java/com/hrassistant/exception/HrAssistantException.java
- [X] T007 [P] Create ErrorInfo.java DTO in src/main/java/com/hrassistant/model/ErrorInfo.java
- [X] T008 [P] Create DocumentType.java enum (PDF, TXT) in src/main/java/com/hrassistant/model/DocumentType.java
- [X] T009 [P] Create DocumentStatus.java enum (PENDING, INDEXED, FAILED) in src/main/java/com/hrassistant/model/DocumentStatus.java
- [x] T010 [P] Create Document.java entity in src/main/java/com/hrassistant/model/Document.java
- [x] T011 [P] Create DocumentChunk.java entity in src/main/java/com/hrassistant/model/DocumentChunk.java
- [x] T012 Create EmbeddingService.java for text to vector conversion in src/main/java/com/hrassistant/service/EmbeddingService.java
- [x] T013 Create VectorStoreService.java with InMemoryEmbeddingStore in src/main/java/com/hrassistant/service/VectorStoreService.java
- [x] T014 Create GlobalExceptionHandler.java for error handling in src/main/java/com/hrassistant/config/GlobalExceptionHandler.java
- [x] T015 Create HealthController.java with GET /api/health endpoint in src/main/java/com/hrassistant/controller/HealthController.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Poser une question RH (Priority: P1) 🎯 MVP

**Goal**: Permettre aux employés de poser des questions et recevoir des réponses basées sur les documents indexés

**Independent Test**: Poser "Combien de jours de congés ai-je droit ?" et vérifier que la réponse cite les sources

### Implementation for User Story 1

- [x] T016 [P] [US1] Create ChatRequest.java DTO in src/main/java/com/hrassistant/model/ChatRequest.java
- [x] T017 [P] [US1] Create ChatResponse.java DTO in src/main/java/com/hrassistant/model/ChatResponse.java
- [x] T018 [US1] Create GuardrailService.java for question validation and off-topic detection in src/main/java/com/hrassistant/service/GuardrailService.java
- [ ] T019 [US1] Create RagService.java for RAG orchestration (retrieve + generate) in src/main/java/com/hrassistant/service/RagService.java
- [ ] T020 [US1] Create ChatController.java with POST /api/chat endpoint in src/main/java/com/hrassistant/controller/ChatController.java
- [ ] T021 [US1] Implement off-topic question handling (FR-013) in GuardrailService.java
- [ ] T022 [US1] Implement "no relevant info" response (FR-010) in RagService.java
- [ ] T023 [US1] Implement LLM unavailability handling (FR-012) in RagService.java

**Checkpoint**: User Story 1 complete - questions can be asked and answered with sources

---

## Phase 4: User Story 2 - Indexer des documents RH (Priority: P2)

**Goal**: Permettre l'upload et l'indexation de documents PDF et TXT

**Independent Test**: Uploader un PDF, vérifier qu'il apparaît dans la liste avec statut INDEXED

### Implementation for User Story 2

- [ ] T024 [P] [US2] Create DocumentInfo.java DTO in src/main/java/com/hrassistant/model/DocumentInfo.java
- [ ] T025 [US2] Create DocumentService.java for upload, chunking, and indexing in src/main/java/com/hrassistant/service/DocumentService.java
- [ ] T026 [US2] Implement PDF text extraction using PDFBox in DocumentService.java
- [ ] T027 [US2] Implement TXT file reading in DocumentService.java
- [ ] T028 [US2] Implement chunking logic (500 chars, 50 overlap) in DocumentService.java
- [ ] T029 [US2] Implement file validation (type, size) in DocumentService.java
- [ ] T030 [US2] Create DocumentController.java with POST /api/documents endpoint in src/main/java/com/hrassistant/controller/DocumentController.java
- [ ] T031 [US2] Implement DELETE /api/documents/{id} endpoint in DocumentController.java
- [ ] T032 [US2] Handle corrupted/unreadable document errors in DocumentService.java

**Checkpoint**: User Story 2 complete - documents can be uploaded, indexed, and deleted

---

## Phase 5: User Story 3 - Recevoir les réponses en streaming (Priority: P3)

**Goal**: Afficher les réponses progressivement via Server-Sent Events

**Independent Test**: Poser une question via /api/chat/stream et vérifier que les tokens arrivent progressivement

### Implementation for User Story 3

- [ ] T033 [US3] Create StreamingRagService.java for streaming generation in src/main/java/com/hrassistant/service/StreamingRagService.java
- [ ] T034 [US3] Configure StreamingChatLanguageModel bean in OllamaConfig.java
- [ ] T035 [US3] Implement GET /api/chat/stream SSE endpoint in ChatController.java
- [ ] T036 [US3] Implement error handling during streaming in StreamingRagService.java
- [ ] T037 [US3] Add sources metadata at end of stream in StreamingRagService.java

**Checkpoint**: User Story 3 complete - streaming responses work with proper error handling

---

## Phase 6: User Story 4 - Consulter les documents indexés (Priority: P4)

**Goal**: Afficher la liste des documents avec leurs métadonnées

**Independent Test**: Appeler GET /api/documents et vérifier que tous les documents uploadés sont listés

### Implementation for User Story 4

- [ ] T038 [US4] Implement GET /api/documents endpoint in DocumentController.java
- [ ] T039 [US4] Implement document listing with metadata in DocumentService.java
- [ ] T040 [US4] Handle empty document list response in DocumentController.java

**Checkpoint**: User Story 4 complete - document list is accessible

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements affecting multiple user stories

- [ ] T041 [P] Add logging statements across all services using SLF4J
- [ ] T042 [P] Add input validation annotations (@NotBlank, @Size) to DTOs
- [ ] T043 Verify quickstart.md instructions work end-to-end
- [ ] T044 Review and optimize embedding search performance
- [ ] T045 Add request/response logging in controllers

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational - Independent of US1 (can pre-load docs for testing US1)
- **User Story 3 (P3)**: Can start after Foundational - Extends US1 chat functionality
- **User Story 4 (P4)**: Can start after Foundational - Shares DocumentService with US2

### Within Each User Story

- DTOs before services
- Services before controllers
- Core implementation before edge cases
- Story complete before moving to next priority

### Parallel Opportunities

**Setup Phase:**
- T002, T003, T004 can run in parallel

**Foundational Phase:**
- T006, T007, T008, T009, T010, T011 can run in parallel (enums and models)
- T012, T013, T014, T015 depend on OllamaConfig (T005)

**User Story 1:**
- T016, T017 can run in parallel (DTOs)

**User Story 2:**
- T024 can start immediately (DTO)

**Polish Phase:**
- T041, T042 can run in parallel

---

## Parallel Example: Foundational Phase

```bash
# After T005 (OllamaConfig) completes, launch these in parallel:
Task: "Create HrAssistantException.java" (T006)
Task: "Create ErrorInfo.java" (T007)
Task: "Create DocumentType.java enum" (T008)
Task: "Create DocumentStatus.java enum" (T009)
Task: "Create Document.java entity" (T010)
Task: "Create DocumentChunk.java entity" (T011)
```

## Parallel Example: User Story 1

```bash
# Launch DTOs in parallel:
Task: "Create ChatRequest.java" (T016)
Task: "Create ChatResponse.java" (T017)

# Then services sequentially:
Task: "Create GuardrailService.java" (T018)
Task: "Create RagService.java" (T019)
Task: "Create ChatController.java" (T020)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test with pre-loaded document
5. Deploy/demo MVP

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. User Story 1 → Chat works → MVP! (T001-T023)
3. User Story 2 → Document upload works (T024-T032)
4. User Story 3 → Streaming works (T033-T037)
5. User Story 4 → Document listing works (T038-T040)
6. Polish → Production ready (T041-T045)

### Suggested MVP Scope

**Minimum viable: Tasks T001-T023** (Setup + Foundational + User Story 1)
- Total: 23 tasks
- Result: Working Q&A with pre-loaded documents

---

## Notes

- [P] tasks = different files, no dependencies within that phase
- [US#] label maps task to specific user story
- Each user story should be independently testable
- Commit after each task or logical group
- Pre-load test documents for US1 testing before US2 is complete
- Avoid: same file conflicts across parallel tasks
