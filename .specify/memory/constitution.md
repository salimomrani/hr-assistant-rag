<!--
Sync Impact Report
- Version change: 0.0.0 → 1.0.0
- Modified principles: N/A (initial creation)
- Added sections: Core Principles (5), Coding Standards, Development Workflow, Governance
- Removed sections: All template placeholders replaced
- Templates requiring updates:
  - .specify/templates/plan-template.md — ✅ no update needed (Constitution Check gate already present)
  - .specify/templates/spec-template.md — ✅ no update needed (priorities align with Principle V)
  - .specify/templates/tasks-template.md — ✅ no update needed (user story grouping aligns with Principle V)
- Follow-up TODOs: none
-->

# HR Assistant RAG Constitution

## Core Principles

### I. Library-First

Every feature MUST use existing, well-maintained libraries instead of custom implementations.

- Use Spring AI for LLM integration, MapStruct for mappings, Lombok for boilerplate reduction
- Use PrimeNG components for all UI needs (80+ components available)
- Use Spring utilities (`StringUtils`, etc.) over hand-rolled helpers
- A custom implementation is only acceptable when no suitable library exists AND the justification is documented in the feature's research.md

### II. English-Only Code Artifacts

All code artifacts MUST be written in English. This includes comments, Javadoc, variable names, commit messages, PR titles, and documentation files.

- User-facing UI strings and LLM prompts MAY be in French (the application's target language)
- Spec documents MAY be in French when the user writes them that way
- Enum display labels for French HR categories are acceptable (domain data, not code artifact)

### III. Incremental Validation

Implementation MUST proceed one file or one method at a time with user validation at each step.

- Never implement multiple tasks without explicit user approval
- Each user story MUST be independently testable and deliverable
- Stop at checkpoints defined in tasks.md to validate before proceeding
- Features follow MVP-first delivery: P1 story working before P2 begins

### IV. Artifact Synchronization

Speckit artifacts (tasks.md, spec.md, plan.md) MUST stay synchronized with actual implementation state.

- Mark tasks `[x]` in tasks.md as work completes
- Document architectural deviations from the original plan with inline notes
- Add extra components as `T###a`, `T###b` entries when implementation creates unplanned files
- Update tech versions in tasks.md headers when actual dependencies differ from planned

### V. Lean Configuration

Project configuration files (CLAUDE.md, MEMORY.md, settings.json) MUST remain concise and free of redundancy.

- CLAUDE.md contains project structure, commands, and coding standards only — no tutorials or code examples
- MEMORY.md captures lessons learned and patterns, organized by topic
- No duplicate tool configurations (e.g., same MCP server registered twice)
- Stale artifacts (old todos, outdated refs) MUST be cleaned periodically

## Coding Standards

- **Java 21**: Records for immutable DTOs, pattern matching, sealed classes, Jakarta Bean Validation, SLF4J logging
- **Angular 21**: Standalone components, `input()`/`output()` functions, `inject()`, signals, `@if`/`@for` control flow, `ChangeDetectionStrategy.OnPush`
- **Separation of concerns**: Separate .ts, .html, .css files for Angular components; layered architecture (controller → service → repository) for backend
- **Testing**: Vitest for frontend, JUnit 5 + Mockito for backend; tests cover acceptance scenarios from spec.md

## Development Workflow

- **Git**: Conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`), feature branches (`feature/`, `fix/`, `refactor/`, `chore/`), PRs required for master
- **Speckit flow**: spec.md → plan.md (with research.md, data-model.md, contracts/, quickstart.md) → tasks.md → implement
- **Feature branches**: Named `{NNN}-{feature-name}` matching the spec directory
- **Infrastructure**: Docker Compose for local services (PostgreSQL + pgvector, Redis), Ollama for LLM

## Governance

This constitution defines non-negotiable project standards. All implementation work MUST verify compliance with these principles.

- The plan-template.md "Constitution Check" gate enforces principle review before and after design
- Amendments require: documentation of the change, version bump, and update to this file
- Version follows semantic versioning: MAJOR for principle removals/redefinitions, MINOR for new principles, PATCH for clarifications
- Use `CLAUDE.md` for runtime development guidance; use this file for governance principles

**Version**: 1.0.0 | **Ratified**: 2026-02-07 | **Last Amended**: 2026-02-07
