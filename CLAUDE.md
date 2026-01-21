# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HR Assistant RAG - An intelligent HR assistant that answers employee questions using RAG (Retrieval-Augmented Generation) on internal documents.

**Monorepo Structure:**
```
hr-assistant-rag/
├── backend/          # Spring Boot API
├── frontend/         # Frontend application (to be implemented)
├── specs/            # Specifications and documentation
├── CLAUDE.md         # This file
└── README.md         # Project documentation
```

## Tech Stack

- **Backend**: Spring Boot 4.0.1, Java 17
- **AI**: LangChain4j 1.10.0
- **LLM**: Ollama (llama3.2) running locally on port 11434
- **Streaming**: WebFlux / SSE
- **Vector Store**: In-Memory (planned: pgvector)
- **Frontend**: To be defined

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
# Commands to be defined
```

## Prerequisites

Ollama must be running locally:
```bash
ollama run llama3.2
```

## Architecture

The backend application (`backend/`) follows a layered architecture:

1. **Controllers** (`backend/src/main/java/com/hrassistant/controller/`) - REST endpoints for chat and document management
2. **Services** (`backend/src/main/java/com/hrassistant/service/`) - Business logic
   - `RagService` - Orchestrates the RAG pipeline (blocking)
   - `StreamingRagService` - Orchestrates the RAG pipeline (streaming)
   - `EmbeddingService` - Converts text to vectors
   - `VectorStoreService` - Stores and searches embeddings
   - `DocumentService` - Handles document upload and chunking
   - `GuardrailService` - Input/output filtering
3. **Config** (`backend/src/main/java/com/hrassistant/config/`) - Spring configuration beans for LangChain4j
4. **Model** (`backend/src/main/java/com/hrassistant/model/`) - DTOs (ChatRequest, ChatResponse, DocumentInfo)
5. **Mapper** (`backend/src/main/java/com/hrassistant/mapper/`) - MapStruct mappers

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

## LangChain4j Patterns

- Use `${langchain4j.version}` property for version consistency
- Embedding models and chat models are configured as Spring beans in `OllamaConfig`
- Vector store uses `InMemoryEmbeddingStore` for development

## Active Technologies
- Java 17 + Spring Boot 4.0.1, LangChain4j 1.10.0, Spring WebFlux (001-hr-rag-assistant)
- In-Memory (InMemoryEmbeddingStore) - MVP, pgvector prévu ultérieuremen (001-hr-rag-assistant)

## Recent Changes
- 001-hr-rag-assistant: Added Java 17 + Spring Boot 4.0.1, LangChain4j 1.10.0, Spring WebFlux
