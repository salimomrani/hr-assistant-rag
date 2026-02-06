# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HR Assistant RAG - An intelligent HR assistant that answers employee questions using RAG (Retrieval-Augmented Generation) on internal documents.

**Monorepo Structure:**
```
hr-assistant-rag/
├── backend/          # Spring Boot API
├── frontend/         # Angular 21 frontend (PrimeNG)
├── specs/            # Specifications and documentation
├── CLAUDE.md         # This file
└── README.md         # Project documentation
```

## Tech Stack

- **Backend**: Spring Boot 4.0.1, Java 21
- **AI**: Spring AI 2.0.0-M1
- **LLM**: Ollama (llama3.2 + nomic-embed-text) running locally on port 11434
- **Streaming**: WebFlux / SSE
- **Vector Store**: PostgreSQL 16 + pgvector (HNSW index, cosine distance)
- **Cache**: Redis 7.4 (semantic caching)
- **Infrastructure**: Docker Compose (pgvector + Redis), Spring Boot Docker Compose Support
- **Frontend**: Angular 21, TypeScript 5.9, PrimeNG v21, RxJS 7.8+, ngx-markdown

## Build & Run Commands

**Backend:**
```bash
cd backend

# Build
mvn clean install

# Run application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Kill process on port 8080 if needed
lsof -ti:8080 | xargs kill -9
```

**Frontend:**
```bash
cd frontend

# Install dependencies
npm install

# Start development server (with backend proxy)
npm start

# Run unit tests (Vitest)
ng test

# Build for production
ng build --configuration=production
```

## Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 22+
- Docker (for PostgreSQL pgvector + Redis)
- Ollama running locally:

```bash
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

## Architecture

The backend application (`backend/`) follows a layered architecture:

1. **Controllers** (`backend/src/main/java/com/hrassistant/controller/`) - REST endpoints for chat and document management
2. **Services** (`backend/src/main/java/com/hrassistant/service/`) - Business logic
   - `RagService` - Orchestrates the RAG pipeline (blocking)
   - `StreamingRagService` - Orchestrates the RAG pipeline (streaming)
   - `CachingStreamingRagService` - RAG pipeline with semantic caching
   - `EmbeddingService` - Converts text to vectors via Spring AI
   - `VectorStoreService` - Stores and searches embeddings (pgvector)
   - `DocumentService` - Handles document upload and chunking
   - `CacheService` - Semantic response caching (Redis)
   - `GuardrailService` - Input/output filtering
3. **Config** (`backend/src/main/java/com/hrassistant/config/`) - Spring configuration (Redis, Web, Logging)
4. **Model** (`backend/src/main/java/com/hrassistant/model/`) - DTOs and JPA entities
5. **Mapper** (`backend/src/main/java/com/hrassistant/mapper/`) - MapStruct mappers
6. **Repository** (`backend/src/main/java/com/hrassistant/repository/`) - Spring Data JPA repositories

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/chat` | Send question, get full response (blocking) |
| POST | `/api/chat/stream` | Send question, get streaming SSE response |
| POST | `/api/documents` | Upload document |
| GET | `/api/documents` | List indexed documents |
| DELETE | `/api/documents/{id}` | Delete document |

## Documentation

- **Specifications**: `specs/001-hr-rag-assistant/`
- **Quickstart Guide**: `specs/001-hr-rag-assistant/quickstart.md`
- **Tasks**: `specs/001-hr-rag-assistant/tasks.md`
- **Backend README**: `backend/README.md`

## Coding Standards

- **Comments**: ALWAYS write comments in English, NEVER in French
- **Javadoc**: Use English for all documentation
- **Code**: Use Lombok annotations to reduce boilerplate
- **Utilities**: Use Spring utilities like StringUtils for string operations
- **Mappings**: Use MapStruct for object mappings (Entity ↔ DTO conversions)
- **Don't Reinvent the Wheel**: ALWAYS use existing libraries and utilities instead of writing custom implementations. If a well-maintained library exists for a task, use it.

## Development Workflow

**IMPORTANT**: Work step-by-step with user validation at each step

1. **Implement ONE file or ONE method at a time**
2. **Show the code to the user**
3. **Wait for user validation before proceeding to the next step**
4. **Never implement multiple tasks without explicit approval**

Example workflow:
- Create DTO → Show → Wait for validation
- Create Service → Show → Wait for validation
- Create Controller → Show → Wait for validation

## Spring AI Patterns

- Spring AI BOM version managed via `${spring-ai.version}` property
- Ollama chat and embedding models are auto-configured via `spring.ai.ollama.*` properties
- Vector store uses pgvector via `spring-ai-starter-vector-store-pgvector`
- Semantic caching uses Redis via `CacheService` and `CachingStreamingRagService`

## Active Technologies
- Java 21 + Spring Boot 4.0.1, Spring AI 2.0.0-M1, Spring WebFlux (backend)
- PostgreSQL 16 + pgvector, Redis 7.4, Docker Compose (infrastructure)
- Angular 21 + TypeScript 5.9, PrimeNG v21, RxJS 7.8+ (frontend)

## Frontend Architecture (002-hr-rag-frontend)

**IMPORTANT**: When developing the frontend, ALWAYS use the `/frontend-design` skill for creating components, pages, and UI elements. This skill is specifically designed for building production-grade, distinctive frontend interfaces with high design quality.

The frontend (`frontend/`) follows Angular 21 best practices with standalone components:

**Structure:**
- **Core Module** (`src/app/core/`) - Singleton services, guards, interceptors
  - `services/` - API, conversation, document, storage services
  - `models/` - TypeScript interfaces and types
  - `interceptors/` - HTTP interceptors for headers/errors

- **Shared Module** (`src/app/shared/`) - Reusable components, directives, pipes

- **Feature Modules** (`src/app/features/`) - Lazy-loaded feature modules
  - `chat/` - Chat page with streaming SSE responses
  - `admin/` - Document management (upload, edit, delete)

- **Layout Components** (`src/app/layout/`) - Header, navigation

**Key Patterns:**
- **Standalone Components**: No NgModules, simpler architecture
- **Signals**: UI reactivity (replacing RxJS subscriptions where possible)
- **RxJS**: Async operations (HTTP, SSE streams)
- **Separation**: Each component has separate .ts, .html, .css files
- **localStorage**: Conversation history (50 message limit)
- **State Management**: Services + RxJS → Signals at UI boundary

## Frontend Coding Standards (Angular 21)

- **Comments**: ALWAYS write comments in English, NEVER in French
- **Component Separation**: ALWAYS separate .ts, .html, .css files (NEVER inline templates/styles for feature components)
- **Naming**: Use kebab-case for file names (`chat-message.component.ts`)
- **Angular Style Guide**: Follow official Angular style guide

### Angular 21 Specific Best Practices

- **Standalone Components**: Use standalone components (default in Angular 20+)
  - Do NOT set `standalone: true` in decorators (it's the default)

- **Inputs/Outputs**: Use `input()` and `output()` functions instead of decorators
  ```typescript
  // GOOD (Angular 21)
  firstName = input<string>();           // Signal<string|undefined>
  lastName = input.required<string>();   // Signal<string>
  age = input(0);                        // Signal<number>
  clicked = output<void>();              // EventEmitter

  // BAD (old style)
  @Input() firstName?: string;
  @Output() clicked = new EventEmitter<void>();
  ```

- **Dependency Injection**: Use `inject()` function instead of constructor injection
  ```typescript
  // GOOD (Angular 21)
  private http = inject(HttpClient);
  private router = inject(Router);

  // BAD (old style)
  constructor(private http: HttpClient) {}
  ```

- **Control Flow**: Use native control flow (`@if`, `@for`, `@switch`) instead of structural directives
  ```typescript
  // GOOD (Angular 21)
  @if (isLoggedIn) { <p>Welcome</p> }
  @for (item of items; track item.id) { <p>{{item.name}}</p> }

  // BAD (old style)
  *ngIf="isLoggedIn"
  *ngFor="let item of items; trackBy: trackById"
  ```

- **Signals**: Use signals for state management
  - Use `signal()` for writable state
  - Use `computed()` for derived state
  - Use `effect()` for side effects
  - Prefer signals over subscriptions where possible
  - Use `toSignal()` to convert Observables to Signals

- **Change Detection**: Set `changeDetection: ChangeDetectionStrategy.OnPush` in `@Component` decorator

- **Host Bindings**: Use `host` object in decorator instead of `@HostBinding`/`@HostListener`
  ```typescript
  // GOOD (Angular 21)
  @Component({
    host: {
      '(click)': 'onClick()',
      '[class.active]': 'isActive()'
    }
  })

  // BAD (old style)
  @HostListener('click') onClick() {}
  @HostBinding('class.active') isActive = false;
  ```

- **Templates**:
  - Do NOT use `ngClass`, use `class` bindings instead
  - Do NOT use `ngStyle`, use `style` bindings instead
  - Do NOT write arrow functions in templates (not supported)
  - Keep templates simple, avoid complex logic

- **Images**: Use `NgOptimizedImage` for all static images (not for inline base64)

- **Lazy Loading**: Feature modules should be lazy-loaded with `loadComponent`

- **Error Handling**: Use functional HTTP interceptors (Angular 14+)

- **Don't Reinvent the Wheel**: Use PrimeNG v21 components exclusively (80+ components available)

### PrimeNG v21 Best Practices

- **Installation**: Install with `npm install primeng @primeuix/themes`
- **Theme Configuration**: Use Aura preset theme via `providePrimeNG` in app.config.ts
- **Animations**: Enable with `provideAnimationsAsync()`
- **Component Import**: Import standalone PrimeNG components directly
  ```typescript
  import { ButtonModule } from 'primeng/button';
  import { CardModule } from 'primeng/card';
  import { TableModule } from 'primeng/table';
  ```
- **80+ Components Available**: Use rich component library for complex UI needs
  - Forms: InputText, Dropdown, Calendar, FileUpload
  - Data: Table, DataView, Tree
  - Overlay: Dialog, Menu, Tooltip
  - Messages: Toast, Message
  - And many more...
- **Accessibility**: PrimeNG is WCAG 2.0 compliant
- **Theming**: Design-agnostic API with Material, Bootstrap, custom themes
- **TypeScript**: First-class TypeScript support with types
