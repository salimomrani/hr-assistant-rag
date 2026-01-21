# Implementation Plan: HR RAG Frontend Interface

**Branch**: `002-hr-rag-frontend` | **Date**: 2026-01-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-hr-rag-frontend/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a responsive Angular web application with two main pages: a Chat interface for employees to ask HR questions with real-time streaming responses, and an Admin interface for document management (upload, view, edit, delete). The frontend consumes an existing Spring Boot REST API with SSE support, persists conversation history in browser localStorage (50 message limit), and implements document updates via delete+reupload pattern.

## Technical Context

**Language/Version**: TypeScript 5.6+ with Angular 21.x (latest stable, v21 available)
**Primary Dependencies**:
- Angular 21.x (framework with standalone components)
- RxJS 7.8+ (reactive programming for SSE streams)
- Angular Router (navigation with lazy loading)
- Angular Forms (reactive forms for validation)
- PrimeNG v20 (comprehensive UI component library - 80+ components, WCAG compliant)
- @primeuix/themes (PrimeNG theming system with Aura preset)
- PrimeIcons (icon library for PrimeNG components)
- State Management: Hybrid approach (Services + RxJS for async → Signals for UI reactivity)

**Storage**: Browser localStorage for conversation history (50 message limit)

**Testing**:
- Jasmine + Karma (unit tests)
- Cypress or Playwright (E2E tests)
- Angular Testing Library patterns

**Target Platform**: Modern web browsers (Chrome 100+, Firefox 100+, Safari 15+, Edge 100+)

**Project Type**: Web application (standalone frontend)

**Performance Goals**:
- Initial load < 3 seconds
- Time to interactive < 5 seconds
- SSE streaming response latency < 500ms
- Smooth 60fps UI interactions
- Bundle size < 500KB (main) + < 1MB (lazy-loaded)

**Constraints**:
- No authentication/authorization (MVP scope)
- Backend API fixed (cannot modify Spring Boot endpoints)
- Document updates must use delete+reupload pattern
- Must support mobile viewports (320px minimum width)
- Conversation history limited to 50 messages in localStorage

**Scale/Scope**:
- 2 main pages (Chat, Admin)
- ~15-20 components
- ~8 services
- 50+ concurrent conversations in localStorage
- 100+ documents in admin list

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: ✅ PASS (No project constitution defined - default gates applied)

**Default Gates Applied**:
- ✅ Separation of concerns: Components separated into .ts, .html, .css files (per user requirement)
- ✅ Best practices: Following Angular Style Guide and latest patterns
- ✅ Testability: Unit and E2E testing strategy defined
- ✅ Maintainability: Clear component/service boundaries
- ✅ Performance: Bundle optimization and lazy loading strategy

**Re-check after Phase 1**: ✅ PASS

**Phase 1 Validation**:
- ✅ Data model (data-model.md) defines clear TypeScript interfaces with validation rules
- ✅ API contracts (contracts/api-contract.yaml) specify all endpoints with OpenAPI 3.0 spec
- ✅ Component separation enforced in project structure (separate .ts/.html/.css files)
- ✅ Angular 21 best practices followed (standalone components, signals, lazy loading)
- ✅ State management pattern defined (Services + RxJS → Signals)
- ✅ Testing strategy defined (Jasmine/Karma for unit, Cypress for E2E)
- ✅ Quickstart guide (quickstart.md) provides clear setup instructions

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
frontend/                           # Angular 21 application
├── src/
│   ├── app/
│   │   ├── core/                  # Singleton services, guards, interceptors
│   │   │   ├── services/          # API, storage, conversation services
│   │   │   ├── models/            # TypeScript interfaces/types
│   │   │   └── interceptors/      # HTTP interceptors for headers/errors
│   │   │
│   │   ├── shared/                # Reusable components, directives, pipes
│   │   │   ├── components/        # Message bubble, file upload, loading spinner
│   │   │   ├── directives/
│   │   │   └── pipes/
│   │   │
│   │   ├── features/              # Feature modules (lazy-loaded)
│   │   │   ├── chat/              # Chat page module
│   │   │   │   ├── components/    # Chat-specific components
│   │   │   │   │   ├── chat-container/
│   │   │   │   │   │   ├── chat-container.component.ts
│   │   │   │   │   │   ├── chat-container.component.html
│   │   │   │   │   │   ├── chat-container.component.css
│   │   │   │   │   │   └── chat-container.component.spec.ts
│   │   │   │   │   ├── message-list/
│   │   │   │   │   ├── message-input/
│   │   │   │   │   └── source-list/
│   │   │   │   ├── services/      # Chat-specific services
│   │   │   │   └── chat.routes.ts
│   │   │   │
│   │   │   └── admin/             # Admin page module
│   │   │       ├── components/    # Admin-specific components
│   │   │       │   ├── document-list/
│   │   │       │   ├── document-upload/
│   │   │       │   ├── document-item/
│   │   │       │   └── document-actions/
│   │   │       ├── services/      # Admin-specific services
│   │   │       └── admin.routes.ts
│   │   │
│   │   ├── layout/                # Layout components
│   │   │   ├── header/
│   │   │   │   ├── header.component.ts
│   │   │   │   ├── header.component.html
│   │   │   │   ├── header.component.css
│   │   │   │   └── header.component.spec.ts
│   │   │   └── navigation/
│   │   │
│   │   ├── app.component.ts       # Root component
│   │   ├── app.component.html
│   │   ├── app.component.css
│   │   ├── app.routes.ts          # App routing configuration
│   │   └── app.config.ts          # App configuration (providers, etc.)
│   │
│   ├── assets/                    # Static assets
│   ├── environments/              # Environment configs
│   ├── index.html
│   ├── main.ts                    # Bootstrap
│   └── styles.css                 # Global styles
│
├── cypress/                       # E2E tests
│   ├── e2e/
│   │   ├── chat.cy.ts
│   │   └── admin.cy.ts
│   └── support/
│
├── angular.json                   # Angular CLI config
├── package.json
├── tsconfig.json
└── README.md

backend/                           # Existing Spring Boot API (not modified)
├── src/
└── ...
```

**Structure Decision**: Following Angular 21 best practices with standalone components, feature-based organization, and strict separation of .ts/.html/.css files as requested. Each component has its own directory with separate files for TypeScript logic, HTML template, CSS styles, and spec tests. Core services are singletons, shared components are reusable, and feature modules are lazy-loaded for performance.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**Status**: No violations - all complexity justified by requirements

---

## Planning Phase Complete

**Date Completed**: 2026-01-21

### Phase 0: Research ✅

**Output**: `research.md`

**Decisions Made**:
1. **UI Component Library**: PrimeNG v20 exclusively (80+ components, WCAG compliant, excellent Angular 21 support, comprehensive feature set)
2. **State Management**: Hybrid approach (Services + RxJS for async → Signals for UI)
3. **Angular 21 Patterns**: Standalone components, lazy loading, SSE integration, functional interceptors

**Rationale**: See research.md for detailed comparison and alternatives considered

### Phase 1: Design & Contracts ✅

**Outputs**:
- `data-model.md` - 9 TypeScript interfaces with validation rules and lifecycle documentation
- `contracts/api-contract.yaml` - OpenAPI 3.0 spec with 6 endpoints, request/response schemas, error handling
- `quickstart.md` - Complete setup guide with prerequisites, commands, troubleshooting

**Key Design Decisions**:
- **Component Separation**: Separate .ts/.html/.css files for all feature components (per user requirement)
- **Lazy Loading**: Chat and Admin modules lazy-loaded for performance
- **localStorage**: Conversation history with 50-message FIFO limit
- **Document Updates**: Implemented as delete+reupload (no backend update API)
- **Error Handling**: HTTP interceptors for global error handling
- **SSE Streaming**: RxJS Observable with EventSource wrapped in NgZone

**Agent Context Updated**: CLAUDE.md updated with Angular 21 technology stack and coding standards

### Next Step

Run `/speckit.tasks` to generate implementation tasks from this plan.

### Important Notes for Implementation

1. **Angular Version**: Using Angular 21 (latest stable release as of January 2026)

2. **Frontend Development Skill**: When implementing frontend components, pages, and UI elements, ALWAYS use the `/frontend-design` skill. This skill is optimized for:
   - Creating production-grade, distinctive interfaces
   - High design quality avoiding generic AI aesthetics
   - Angular 21 best practices and patterns
   - Component architecture with proper separation (.ts/.html/.css)

3. **Context7 Integration**: Use Context7 for latest Angular 21/21 documentation and patterns during development.
