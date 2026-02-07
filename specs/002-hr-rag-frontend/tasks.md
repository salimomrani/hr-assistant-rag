# Implementation Tasks: HR RAG Frontend Interface

**Feature**: HR RAG Frontend Interface
**Branch**: `002-hr-rag-frontend`
**Date**: 2026-01-21
**Tech Stack**: Angular 21, TypeScript 5.9, PrimeNG v21, RxJS 7.8+

## Overview

This document provides the complete task breakdown for implementing the HR RAG Frontend application. Tasks are organized by user story to enable independent implementation and testing of each feature increment.

**Total Stories**: 7 (2 x P1, 3 x P2, 2 x P3)
**Total Tasks**: 185 (159 completed, 26 pending)
**MVP Scope**: User Stories 1 + 3 (Chat + Document Upload) — **COMPLETE**

---

## Implementation Strategy

### Incremental Delivery

1. **MVP (Phase 3-4)**: US1 (Chat) + US3 (Upload) - Core value proposition ✅
2. **Enhancement 1 (Phase 5-6)**: US2 (Sources) + US4 (Document List) - Trust & oversight ✅
3. **Enhancement 2 (Phase 7)**: US7 (Delete) - Data hygiene ✅
4. **Enhancement 3 (Phase 8-9)**: US5 (Edit) + US6 (Replace) - Admin convenience ✅
5. **Polish (Phase 10)**: Cross-cutting concerns - Partially done

### Parallel Execution Strategy

- **Phase 1 (Setup)**: Sequential (project foundation)
- **Phase 2 (Foundational)**: Highly parallel (core services, models, navigation)
- **Phase 3+ (User Stories)**: Each story is independent - implement in any order within priority level
- **Within Each Story**: Components can be built in parallel after services are ready

---

## Dependencies Between User Stories

```
US1 (Chat - P1) ──┐
                  ├──> US2 (View Sources - P2) [depends on US1]
US3 (Upload - P1) ┘

US3 (Upload - P1) ──┐
                     ├──> US4 (Document List - P2) [depends on US3]
                     ├──> US7 (Delete - P2) [depends on US3]
                     ├──> US5 (Edit - P3) [depends on US3]
                     └──> US6 (Replace - P3) [depends on US3]
```

**Independent Stories**: US1 and US3 can be implemented in parallel
**Dependent Stories**: US2 depends on US1; US4, US5, US6, US7 depend on US3

---

## Phase 1: Project Setup ✅

**Goal**: Initialize Angular 21 project with PrimeNG v21 and core configuration

### Tasks

- [x] T001 Create Angular 21 project with CLI in frontend/ directory
- [x] T002 Install dependencies: primeng, @primeuix/themes, primeicons, rxjs
- [x] T003 Configure PrimeNG providers with Aura theme in src/app/app.config.ts
- [x] T004 Add PrimeIcons CSS to src/styles.css
- [x] T005 Create environment files in src/environments/ (development and production)
- [x] T006 Configure proxy for backend API in proxy.conf.json
- [x] T007 Update angular.json with proxy configuration
- [x] T008 Create project directory structure per implementation plan (core/, shared/, features/, layout/)
- [x] T009 Configure TypeScript strict mode in tsconfig.json
- [x] T010 Create README.md with project overview and setup instructions

**Parallel Opportunities**: None (sequential setup required)

---

## Phase 2: Foundational Layer ✅

**Goal**: Build core services, models, and shared infrastructure needed by all user stories

### Core Models (src/app/core/models/)

- [x] T011 [P] Create Question interface in src/app/core/models/question.model.ts
- [x] T012 [P] Create Answer interface in src/app/core/models/answer.model.ts
- [x] T013 [P] Create SourceDocumentReference interface in src/app/core/models/source-document-reference.model.ts
- [x] T014 [P] Create ConversationMessage interface in src/app/core/models/conversation-message.model.ts
- [x] T015 [P] Create Document interface and DocumentStatus enum in src/app/core/models/document.model.ts
- [x] T016 [P] Create UploadProgress interface and UploadStatus enum in src/app/core/models/upload-progress.model.ts
- [x] T017 [P] Create ApiError interface in src/app/core/models/api-error.model.ts
- [x] T018 [P] Create Environment interface in src/app/core/models/environment.model.ts
- [x] T019 Create index.ts barrel file exporting all models in src/app/core/models/

### Core Services (src/app/core/services/)

- [x] T020 [P] Create ApiService with HttpClient injection in src/app/core/services/api.service.ts
- [x] T021 [P] Create StorageService for localStorage operations in src/app/core/services/storage.service.ts
- [x] T022 [P] Create ConversationService for managing chat history in src/app/core/services/conversation.service.ts
- [x] T023 [P] Create DocumentService for document management in src/app/core/services/document.service.ts
- [x] T024 Create index.ts barrel file exporting all services in src/app/core/services/

### HTTP Interceptors (src/app/core/interceptors/)

- [x] T025 Create ErrorInterceptor for global error handling in src/app/core/interceptors/error.interceptor.ts
- [x] T026 Create HeadersInterceptor for adding common headers in src/app/core/interceptors/headers.interceptor.ts
- [x] T027 Register interceptors in app.config.ts providers array

### Navigation & Layout

- [x] T028 Create HeaderComponent with separate files in src/app/layout/header/
- [x] T029 Implement header template with PrimeNG p-menubar in header.component.html
- [x] T030 Add navigation links (Chat, Admin) with active route indication in header template
- [x] T031 Style header component with responsive design in header.component.css
- [x] T032 Configure app routing in src/app/app.routes.ts with lazy-loaded feature routes
- [x] T033 Update AppComponent to include header and router-outlet in src/app/app.component.html

### Shared Components (src/app/shared/components/)

- [x] T034 [P] Create LoadingSpinnerComponent with PrimeNG p-progressSpinner in src/app/shared/components/loading-spinner/
- [x] T035 [P] Create ErrorMessageComponent with PrimeNG p-message in src/app/shared/components/error-message/
- [x] T036 Create index.ts barrel file exporting shared components in src/app/shared/components/

**Parallel Opportunities**:
- Models (T011-T018) can all be created in parallel
- Services (T020-T023) can be created in parallel
- Interceptors (T025-T026) can be created in parallel
- Shared components (T034-T035) can be created in parallel
- Navigation (T028-T031) can be done in parallel with services

**Blocking Tasks**: T032 (routing) depends on T028 (header); T033 (AppComponent) depends on T028 and T032

---

## Phase 3: User Story 1 - Ask HR Questions via Chat (P1) ✅

**Story Goal**: Enable employees to ask HR questions and receive streaming answers with source citations

**Priority**: P1 (Core value proposition)

**Independent Test**: Open chat page → Type "Combien de jours de congés?" → Submit → Verify answer appears with sources → Verify streaming display → Check conversation history persists after reload

### Chat Page Structure

- [x] T037 [US1] Create chat.routes.ts with lazy-loaded chat component in src/app/features/chat/
- [x] T038 [US1] Create ChatContainerComponent with separate files in src/app/features/chat/components/chat-container/
- [x] T039 [US1] Design chat container template with PrimeNG p-card in chat-container.component.html
- [x] T040 [US1] Style chat container for responsive layout in chat-container.component.css

### Message Display

- [x] T041 [P] [US1] Create MessageListComponent with separate files in src/app/features/chat/components/message-list/
- [x] T042 [P] [US1] Implement message list template with PrimeNG p-scrollPanel in message-list.component.html
- [x] T043 [P] [US1] Add message display logic with user/assistant differentiation in message-list.component.ts
- [x] T044 [P] [US1] Style message bubbles (user vs assistant) in message-list.component.css
- [x] T045 [P] [US1] Implement auto-scroll to latest message in message-list.component.ts
- [x] T046 [US1] Add loading indicator during response generation using LoadingSpinnerComponent

### Message Input

- [x] T047 [P] [US1] Create MessageInputComponent with separate files in src/app/features/chat/components/message-input/
- [x] T048 [P] [US1] Implement input form with PrimeNG p-inputTextarea and p-button in message-input.component.html
- [x] T049 [P] [US1] Add form validation (non-empty, max 1000 chars) in message-input.component.ts
- [x] T050 [P] [US1] Style input area with responsive design in message-input.component.css
- [x] T051 [US1] Implement form submit with output event emitter in message-input.component.ts

### Chat Service & SSE Streaming

- [x] T052 [US1] Implement POST /api/chat method in ApiService (blocking mode)
- [x] T053 [US1] Implement POST /api/chat/stream method with Fetch API in ApiService (SSE streaming)
- [x] T054 [US1] Wrap SSE callbacks in NgZone.run() for change detection in ApiService
- [x] T055 [US1] Add error handling for network interruptions in SSE stream
- [x] T056 [US1] Parse SSE events and emit text chunks as RxJS Observable

### Conversation State Management

- [x] T057 [US1] Create conversation history signal in ConversationService
- [x] T058 [US1] Implement addMessage method in ConversationService
- [x] T059 [US1] Implement loadHistory method from localStorage in ConversationService
- [x] T060 [US1] Implement saveHistory method to localStorage with 50-message limit in ConversationService
- [x] T061 [US1] Implement FIFO eviction when message limit exceeded in ConversationService
- [x] T062 [US1] Add currentStreamingResponse signal for partial answers in ConversationService

### Integration

- [x] T063 [US1] Connect ChatContainerComponent to ConversationService signals
- [x] T064 [US1] Implement question submission flow: input → service → API → display in ChatContainerComponent
- [x] T065 [US1] Handle streaming response chunks and update UI progressively
- [x] T066 [US1] Persist completed Q&A pairs to localStorage after each answer
- [x] T067 [US1] Load conversation history on component init and display in MessageListComponent
- [x] T068 [US1] Add error handling with ErrorMessageComponent for API failures

### Extra Components (not originally planned, added during implementation)

- [x] T068a [US1] Create ChatPageComponent layout wrapper with sidebar toggle in src/app/features/chat/components/chat-page/
- [x] T068b [US1] Create ChatSidebarComponent for conversation list (ChatGPT-style) in src/app/features/chat/components/chat-sidebar/
- [x] T068c [US1] Create DocumentSelectorComponent for filtering RAG search by specific documents in src/app/features/chat/components/document-selector/

**Parallel Opportunities**:
- MessageListComponent (T041-T046) and MessageInputComponent (T047-T051) can be built in parallel
- After T052-T056 (API methods) complete, T057-T062 (ConversationService) can proceed
- T041-T051 (components) can be built while T052-T056 (API) is in progress

**Blocking Dependencies**:
- T063-T068 (integration) require completion of components (T041-T051) and services (T052-T062)

**Independent Test Criteria**:
1. ✅ Chat page loads without errors
2. ✅ User can type question and submit
3. ✅ Answer streams progressively (visible chunks appearing)
4. ✅ Completed Q&A appears in history
5. ✅ History persists after page reload (localStorage check)
6. ✅ Empty question shows validation error

---

## Phase 4: User Story 3 - Upload HR Documents (P1) ✅

**Story Goal**: Enable HR administrators to upload PDF/TXT documents for indexing

**Priority**: P1 (Foundational infrastructure)

**Independent Test**: Navigate to admin page → Click upload → Select valid PDF < 10MB → Verify upload progress → Verify document appears in list with "pending" status

### Admin Page Structure

> **Note**: No separate `admin.routes.ts` — admin route is defined directly in `src/app/app.routes.ts` with lazy-loaded `AdminContainerComponent`.

- [x] T069 [US3] Configure admin route with lazy-loaded admin component in src/app/app.routes.ts
- [x] T070 [US3] Create AdminContainerComponent with separate files in src/app/features/admin/components/admin-container/
- [x] T071 [US3] Design admin container template with PrimeNG p-card in admin-container.component.html
- [x] T072 [US3] Style admin container for responsive layout in admin-container.component.css

### Document Upload

- [x] T073 [P] [US3] Create DocumentUploadComponent with separate files in src/app/features/admin/components/document-upload/
- [x] T074 [P] [US3] Implement file upload UI with PrimeNG p-fileUpload and drag-and-drop zone in document-upload.component.html
- [x] T075 [P] [US3] Configure file upload with accept=".pdf,.txt" and maxFileSize validation in template
- [x] T076 [P] [US3] Add client-side validation (file type PDF/TXT, size <= 10MB) in document-upload.component.ts
- [x] T077 [P] [US3] Display validation errors with custom error messages per HTTP status code
- [x] T078 [US3] Implement upload progress tracking with progress signal in document-upload.component.ts
- [x] T079 [US3] Style upload component with responsive design in document-upload.component.css

### Document Service Integration

- [x] T080 [US3] Implement POST /api/documents with multipart/form-data in ApiService
- [x] T081 [US3] Add upload progress tracking using HttpEventType.UploadProgress in DocumentService
- [x] T082 [US3] Create uploadDocument method returning Observable<UploadProgress> in DocumentService
- [x] T083 [US3] Add error handling for upload failures (413 Payload Too Large, 400 Bad Request)

### Document List (Basic)

- [x] T084 [P] [US3] Create DocumentListComponent with separate files in src/app/features/admin/components/document-list/
- [x] T085 [P] [US3] Implement basic table with PrimeNG p-table in document-list.component.html
- [x] T086 [P] [US3] Display columns: filename, type, category, size, status, upload date, actions in template
- [x] T087 [US3] Fetch documents on init using GET /api/documents via DocumentService in admin-container.component.ts
- [x] T088 [US3] Add empty state message when no documents exist
- [x] T089 [US3] Style document list table in document-list.component.css

### Integration

- [x] T090 [US3] Connect AdminContainerComponent to DocumentService
- [x] T091 [US3] Implement upload flow: select file → validate → upload → update list
- [x] T092 [US3] Add PrimeNG p-toast for upload success/failure notifications
- [x] T093 [US3] Refresh document list after successful upload
- [x] T094 [US3] Display upload progress using PrimeNG p-progressBar

### Extra Components (not originally planned, added during implementation)

- [x] T094a [US3] Create PdfPreviewModalComponent for PDF/TXT preview in src/app/features/admin/components/pdf-preview-modal/
- [x] T094b [US3] Add category autocomplete (p-autoComplete) to DocumentUploadComponent for optional categorization

**Parallel Opportunities**:
- DocumentUploadComponent (T073-T079) and DocumentListComponent (T084-T089) can be built in parallel
- T080-T083 (DocumentService upload methods) can be implemented while components are built

**Blocking Dependencies**:
- T090-T094 (integration) require completion of upload component (T073-T079), list component (T084-T089), and service (T080-T083)

**Independent Test Criteria**:
1. ✅ Admin page loads without errors
2. ✅ Upload button visible and clickable
3. ✅ File validation prevents invalid files (wrong type, > 10MB)
4. ✅ Valid file uploads with progress indicator
5. ✅ Uploaded document appears in list with metadata
6. ✅ Toast notification shows upload success

---

## Phase 5: User Story 2 - View Answer Sources (P2) ✅

**Story Goal**: Display source documents with citations for each answer to build trust

**Priority**: P2 (Trust & transparency)

**Dependencies**: Requires US1 (Chat) to be complete

**Independent Test**: Ask question → Receive answer → Verify source documents displayed with names and excerpts → Verify "no sources" message when applicable

### Source Display Component

> **Note**: Implementation uses a collapsible custom component (not p-accordion). Shows "Sources (N)" with expand/collapse toggle and deduplicates sources by document name. Empty sources array hides the component entirely.

- [x] T095 [P] [US2] Create SourceListComponent with separate files in src/app/features/chat/components/source-list/
- [x] T096 [P] [US2] Implement source display template with collapsible header in source-list.component.html
- [x] T097 [P] [US2] Add input signal for sources array in source-list.component.ts
- [x] T098 [P] [US2] Display document name for each unique source in template
- [x] T099 [P] [US2] Handle empty sources array (component hidden when no sources)
- [x] T100 [US2] Style source list component in source-list.component.css

### Integration with Chat

- [x] T101 [US2] Update MessageListComponent to include SourceListComponent for each answer
- [x] T102 [US2] Pass sources array from ConversationMessage to SourceListComponent
- [x] T103 [US2] Update message display template to show sources below answer text
- [x] T104 [US2] Add visual separator between answer and sources section

**Parallel Opportunities**: All T095-T100 (SourceListComponent) can proceed together

**Blocking Dependencies**: T101-T104 (integration) require US1 (Chat) and SourceListComponent (T095-T100)

**Independent Test Criteria**:
1. ✅ Sources section appears below each answer
2. ✅ Document names are clearly displayed
3. ✅ Multiple sources are shown distinctly (deduplicated)
4. ✅ Component hidden when no sources (clean UX)
5. ✅ Sources are collapsible/expandable

---

## Phase 6: User Story 4 - View and Manage Documents (P2) — Partially Complete

**Story Goal**: Display comprehensive document list with metadata and status indicators

**Priority**: P2 (Admin oversight)

**Dependencies**: Requires US3 (Upload) to be complete

**Independent Test**: Navigate to admin page → Verify all documents displayed with metadata → Verify status indicators (pending/indexed/failed) → Verify pagination/infinite scroll for large lists

### Enhanced Document List

- [x] T105 [P] [US4] Add status badge display with PrimeNG p-tag in document-list.component.html
- [x] T106 [P] [US4] Implement status-based styling (success=indexed, warn=pending, danger=failed) in document-list.component.ts
- [ ] T107 [P] [US4] Add sortable column headers for table columns (filename, size, date) in document-list.component.html
- [ ] T108 [P] [US4] Add filtering functionality for status (pending/indexed/failed) in document-list.component.html
- [x] T109 [US4] Implement pagination with PrimeNG p-table paginator feature (triggers at >10 docs)
- [x] T110 [US4] Add rows-per-page selector (10, 20, 50) in template
- [x] T111 [US4] Format file size display (bytes → KB/MB/GB) using formatFileSize() in document-list.component.ts
- [x] T112 [US4] Format upload date with French locale (dd month yyyy, HH:mm) in document-list.component.ts

### Status Updates

- [ ] T113 [US4] Add polling mechanism to check status changes for "pending" documents in document-list.component.ts
- [ ] T114 [US4] Update document status in real-time when indexing completes
- [ ] T115 [US4] Add manual refresh button with PrimeNG p-button in admin-container template
- [ ] T116 [US4] Display last refresh time in document list header

> **Note**: Auto-refresh occurs after upload/delete/rename/replace actions. Manual refresh button and polling for pending documents are not yet implemented.

**Parallel Opportunities**: All T105-T112 (enhanced list features) can be implemented in parallel

**Blocking Dependencies**: Requires US3 (T084-T089 basic list) to be complete

**Independent Test Criteria**:
1. ✅ All documents display with complete metadata
2. ✅ Status badges show correct colors and labels
3. ❌ Table sorting not yet configurable via column headers
4. ❌ Status filtering not yet available
5. ✅ Pagination handles 10+ documents
6. ✅ File sizes and dates are formatted correctly

---

## Phase 7: User Story 7 - Delete Documents (P2) ✅

**Story Goal**: Enable administrators to permanently remove documents with confirmation

**Priority**: P2 (Data hygiene)

**Dependencies**: Requires US3 (Upload) to be complete

**Independent Test**: Select document → Click delete → Verify confirmation dialog → Confirm → Verify document removed from list

> **Architecture Note**: Tasks originally planned a separate `DocumentActionsComponent`. Implementation embeds action buttons (edit, delete, replace, preview) directly in `DocumentListComponent` via output events — cleaner architecture, fewer files.

### Delete Functionality

- [x] T117 [US7] Add delete action button to each row in document-list.component.html (replaces planned DocumentActionsComponent T117-T121)
- [x] T118 [US7] Add output event emitters (documentDeleted, deleteError) in document-list.component.ts
- [x] T119 [US7] Add loading state per document (deletingId signal) in document-list.component.ts
- [x] T120 [US7] Implement DELETE /api/documents/{id} method in ApiService
- [x] T121 [US7] Add deleteDocument method returning Observable in DocumentService
- [x] T122 [US7] Add confirmation dialog using PrimeNG ConfirmationService in document-list.component.ts
- [x] T123 [US7] Configure confirmation dialog with French warning message
- [x] T124 [US7] Implement delete flow: click → confirm → API call → emit event
- [x] T125 [US7] Add error handling for delete failures
- [x] T126 [US7] Display success/error toast notification via AdminContainerComponent

### Integration

- [x] T127 [US7] Connect documentDeleted event to AdminContainerComponent handler
- [x] T128 [US7] Refresh document list after successful deletion
- [x] T129 [US7] Update document count display via DocumentService computed signals (documentCount, indexedCount, etc.)

**Independent Test Criteria**:
1. ✅ Delete button appears for each document
2. ✅ Confirmation dialog shows warning message
3. ✅ Cancel button preserves document
4. ✅ Confirm button deletes document
5. ✅ Document removed from list after delete
6. ✅ Toast notification confirms deletion

---

## Phase 8: User Story 5 - Edit Document Metadata (P3) ✅

**Story Goal**: Enable administrators to rename documents

**Priority**: P3 (Admin convenience)

**Dependencies**: Requires US3 (Upload) to be complete

**Independent Test**: Select document → Click edit → Change name → Save → Verify new name in list

> **Implementation Note**: Rename uses a dedicated `PATCH /api/documents/{id}` endpoint via `DocumentService.renameDocument()`, not the originally planned delete+reupload approach. This is cleaner and preserves document metadata.

### Edit Dialog

- [x] T130 [US5] Add edit button to document-list.component.html with editDocument output event
- [x] T131 [US5] Create edit dialog using PrimeNG p-dialog in admin-container.component.html
- [x] T132 [US5] Add form with PrimeNG p-inputText for new filename (maxlength=255) in dialog template
- [x] T133 [US5] Implement form validation (non-empty, unchanged detection) in admin-container.component.ts
- [x] T134 [US5] Add save and cancel buttons to dialog

### Edit Implementation

- [x] T135 [US5] Create renameDocument method in DocumentService calling PATCH /api/documents/{id}
- [x] T136 [US5] Implement rename flow: edit button → dialog → validate → API call → refresh
- [x] T137 [US5] Add loading indicator during rename operation (renamingDocument signal)
- [x] T138 [US5] Handle rename errors with error toast
- [x] T139 [US5] Display success toast after rename operation

### Integration

- [x] T140 [US5] Connect edit button event from DocumentListComponent to dialog open in AdminContainerComponent
- [x] T141 [US5] Pre-fill form with current filename
- [x] T142 [US5] Refresh document list after successful rename
- [x] T143 [US5] Close dialog on success or cancel

**Independent Test Criteria**:
1. ✅ Edit button appears for each document
2. ✅ Dialog opens with current filename pre-filled
3. ✅ Form validation prevents empty/unchanged names
4. ✅ Save triggers rename API call
5. ✅ New name appears in document list
6. ✅ Cancel button preserves original document

---

## Phase 9: User Story 6 - Replace Document File (P3) ✅

**Story Goal**: Enable administrators to replace document files via delete+reupload

**Priority**: P3 (Admin convenience)

**Dependencies**: Requires US3 (Upload) and US7 (Delete) to be complete

**Independent Test**: Select document → Click replace → Upload new file → Verify document re-indexed with new content

### Replace Dialog

- [x] T144 [US6] Add replace button to document-list.component.html with replaceDocument output event
- [x] T145 [US6] Create replace dialog using PrimeNG p-dialog in admin-container.component.html
- [x] T146 [US6] Add p-fileUpload in dialog with same validation (PDF/TXT, <= 10MB)
- [x] T147 [US6] Display current document info and warning message in dialog
- [x] T148 [US6] Add upload progress indicator (p-progressBar) in dialog

### Replace Implementation (Delete + Reupload)

- [x] T149 [US6] Implement executeReplace() in AdminContainerComponent — two-step: delete old → upload new with original filename
- [x] T150 [US6] Handle replace errors for both delete and upload steps
- [x] T151 [US6] Display success/error toast after replace operation

### Integration

- [x] T152 [US6] Connect replace button event from DocumentListComponent to dialog open in AdminContainerComponent
- [x] T153 [US6] Refresh document list after successful replace (shows "pending" status during re-indexing)
- [x] T154 [US6] Close dialog on success or cancel

**Independent Test Criteria**:
1. ✅ Replace button appears for each document
2. ✅ Dialog shows current document info and warning
3. ✅ File validation prevents invalid uploads
4. ✅ Replace triggers delete+reupload sequence
5. ✅ Document shows "pending" status during re-indexing
6. ✅ New file content is indexed correctly

---

## Phase 10: Polish & Cross-Cutting Concerns — Partially Complete

**Goal**: Add final polish, error handling, and cross-cutting features

### Error Handling & Validation

- [x] T155 [P] Enhance ErrorInterceptor with specific error messages per status code (400, 404, 413, 500, 503) in error.interceptor.ts
- [ ] T156 [P] Add retry logic for transient errors (503 Service Unavailable) in ApiService
- [ ] T157 [P] Implement exponential backoff for SSE reconnection in chat service
- [ ] T158 [P] Add network status detection and offline mode indicator
- [ ] T159 Create global error handler service for uncaught exceptions

### Responsive Design

- [x] T160 [P] Test chat interface on mobile (320px) and adjust styles — media queries implemented across chat components
- [x] T161 [P] Test admin interface on tablet (768px) and adjust styles — media queries implemented across admin components
- [x] T162 [P] Implement collapsible sidebar for small screens in ChatPageComponent (sidebarOpen signal + toggle)
- [x] T163 [P] Add touch-friendly button sizes for mobile devices
- [ ] T164 Test keyboard navigation and accessibility (WCAG 2.0)

### Performance Optimization

- [ ] T165 [P] Implement OnPush change detection strategy for all components
- [ ] T166 [P] Ensure all @for loops use proper track expressions in templates
- [ ] T167 [P] Lazy-load PrimeNG modules to reduce initial bundle size
- [ ] T168 [P] Add bundle size analysis and optimize imports
- [ ] T169 Configure production build optimizations in angular.json

### Testing (optional, not explicitly requested)

- [ ] T170 [P] Configure Vitest for unit tests (framework already installed)
- [ ] T171 [P] Write unit tests for ConversationService
- [ ] T172 [P] Write unit tests for DocumentService
- [ ] T173 [P] Write unit tests for StorageService
- [ ] T174 [P] Write unit tests for ApiService
- [ ] T175 Configure test coverage reporting

### Documentation

- [ ] T176 [P] Add JSDoc comments to all public methods in services
- [ ] T177 [P] Create component usage examples in README.md
- [ ] T178 [P] Document localStorage schema and limits in README.md
- [ ] T179 Add troubleshooting guide for common issues

**Parallel Opportunities**: All polish tasks can be executed in parallel

---

## Task Summary

### Total Tasks by Phase

- **Phase 1 (Setup)**: 10 tasks (10 done)
- **Phase 2 (Foundational)**: 26 tasks (26 done)
- **Phase 3 (US1 - Chat)**: 35 tasks (35 done, includes 3 extra components)
- **Phase 4 (US3 - Upload)**: 28 tasks (28 done, includes 2 extra components)
- **Phase 5 (US2 - Sources)**: 10 tasks (10 done)
- **Phase 6 (US4 - Document List)**: 12 tasks (6 done, 6 pending)
- **Phase 7 (US7 - Delete)**: 13 tasks (13 done, renumbered from original)
- **Phase 8 (US5 - Edit)**: 14 tasks (14 done, renumbered from original)
- **Phase 9 (US6 - Replace)**: 11 tasks (11 done, renumbered from original)
- **Phase 10 (Polish)**: 25 tasks (5 done, 20 pending)

**Total**: 184 tasks — **158 completed** ✅, **26 pending** ⏳

### Completed by User Story

- **US1 (Chat - P1)**: 35/35 ✅
- **US2 (Sources - P2)**: 10/10 ✅
- **US3 (Upload - P1)**: 28/28 ✅
- **US4 (Document List - P2)**: 6/12 (sorting, filtering, polling pending)
- **US5 (Edit - P3)**: 14/14 ✅
- **US6 (Replace - P3)**: 11/11 ✅
- **US7 (Delete - P2)**: 13/13 ✅
- **Polish**: 5/25 (error interceptor + responsive done)

### Remaining Work (26 tasks)

**US4 enhancements** (6 tasks):
- T107-T108: Sortable columns, status filter dropdown
- T113-T116: Polling for pending docs, manual refresh button, last refresh time

**Polish** (20 tasks):
- T156-T159: Retry logic, SSE reconnection, offline indicator, global error handler
- T164: Accessibility testing
- T165-T169: Performance (OnPush, track expressions, lazy loading, bundle analysis)
- T170-T175: Unit tests
- T176-T179: Documentation

---

## Notes

- **ALWAYS use `/frontend-design` skill** when implementing UI components, pages, and templates
- **Use Context7** for latest Angular 21 and PrimeNG v21 documentation during implementation
- **Component Separation**: All components MUST have separate .ts, .html, .css files
- **Signals**: Use signals for state management, convert RxJS Observables with `toSignal()`
- **PrimeNG**: Use PrimeNG v21 components exclusively (no Angular Material)
- **Error Handling**: All API calls must have proper error handling with user-friendly messages
- **localStorage**: Respect 50-message limit with FIFO eviction
- **Testing**: Project uses Vitest (not Karma/Jasmine/Cypress)

### Architectural Deviations from Original Plan

1. **No separate `DocumentActionsComponent`**: Action buttons (edit, delete, replace, preview) are embedded directly in `DocumentListComponent` via output events — cleaner, fewer files
2. **Rename uses API endpoint**: `PATCH /api/documents/{id}` instead of delete+reupload
3. **SSE uses Fetch API**: Not EventSource — avoids buffering issues, supports AbortController
4. **Extra components added**: ChatPageComponent (layout), ChatSidebarComponent (conversation list), DocumentSelectorComponent (RAG filter), PdfPreviewModalComponent (preview)

---

**Generated**: 2026-01-21
**Last Updated**: 2026-02-07
**Status**: Feature implementation complete — polish tasks remaining
