# HR Assistant RAG - Frontend

An Angular 21 web application providing an intelligent HR assistant interface with chat and document management features.

## Tech Stack

- **Framework**: Angular 21 with standalone components
- **Language**: TypeScript 5.9
- **UI Library**: PrimeNG v21 (Aura theme)
- **Icons**: PrimeIcons
- **State Management**: Signals + RxJS 7.8+
- **Markdown**: ngx-markdown
- **HTTP**: Angular HttpClient + Fetch API (SSE streaming)
- **Storage**: Browser localStorage
- **Testing**: Vitest

## Prerequisites

- Node.js 22+ LTS
- npm 10+
- Backend API running on http://localhost:8080

## Quick Start

```bash
npm install
npm start        # Dev server with proxy (http://localhost:4200)
ng test          # Run unit tests (Vitest)
ng build --configuration=production
```

## Project Structure

```
src/app/
├── core/                  # Singleton services, models, interceptors
│   ├── services/          # ApiService, ConversationService, DocumentService, StorageService
│   ├── models/            # TypeScript interfaces (Document, ConversationMessage, etc.)
│   └── interceptors/      # ErrorInterceptor, HeadersInterceptor
├── shared/components/     # LoadingSpinnerComponent, ErrorMessageComponent
├── features/
│   ├── chat/              # Chat interface (ChatPage, ChatContainer, MessageList, MessageInput, ChatSidebar, DocumentSelector, SourceList)
│   └── admin/             # Document management (AdminContainer, DocumentUpload, DocumentList, PdfPreviewModal)
├── layout/header/         # HeaderComponent with navigation
├── app.config.ts          # PrimeNG providers, interceptors, routing
└── app.routes.ts          # Lazy-loaded feature routes
```

## Core Services

### ApiService

HTTP communication with the backend. All methods return Observables.

| Method | Description |
|--------|-------------|
| `chatStream$(question, documentIds?)` | SSE streaming Q&A via Fetch API |
| `chat$(question)` | Blocking Q&A |
| `uploadDocument$(file, category?)` | Upload with progress tracking |
| `getDocuments$()` | List all documents |
| `deleteDocument$(documentId)` | Delete a document |
| `renameDocument$(documentId, newFilename)` | Rename a document |
| `getCategories$()` | List document categories |
| `getDocumentFileUrl(documentId)` | Get file download URL |

### DocumentService

Document state management with signals. Wraps ApiService and maintains reactive state.

| Signal | Type | Description |
|--------|------|-------------|
| `documents` | `Document[]` | All loaded documents |
| `isLoading` | `boolean` | Loading state |
| `documentCount` | `number` | Total document count |
| `indexedCount` | `number` | Documents with INDEXED status |
| `pendingCount` | `number` | Documents with PENDING status |
| `failedCount` | `number` | Documents with FAILED status |
| `categories` | `string[]` | Unique sorted categories |

### ConversationService

Multi-conversation chat management with localStorage persistence.

| Method | Description |
|--------|-------------|
| `createConversation()` | Create new conversation, returns ID |
| `switchConversation(id)` | Switch active conversation |
| `deleteConversation(id)` | Delete a conversation |
| `addMessage(question, answer)` | Add Q&A pair with FIFO eviction |
| `clearAllHistory()` | Reset all conversations |

### StorageService

localStorage wrapper with app prefix and error handling.

| Method | Description |
|--------|-------------|
| `set<T>(key, value)` | Save JSON-serialized data |
| `get<T>(key)` | Get parsed data or null |
| `remove(key)` | Remove a key |
| `clear()` | Remove all prefixed keys |
| `isAvailable()` | Check localStorage access |

## localStorage Schema

All keys are prefixed with `hr-assistant-`.

| Key | Type | Description |
|-----|------|-------------|
| `hr-assistant-conversations` | `Conversation[]` | All chat conversations with messages |
| `hr-assistant-activeConversationId` | `string` | Currently active conversation ID |

### Limits

- **Max messages per conversation**: 50 (FIFO eviction - oldest messages removed first)
- **Conversation title**: Auto-generated from first question (max 50 chars)
- **Storage quota**: ~5MB (browser limit). Exceeding quota fails silently.

### Data Structure

```
Conversation {
  id: string (UUID)
  title: string
  messages: ConversationMessage[]
  createdAt: Date
  updatedAt: Date
}

ConversationMessage {
  id: string (UUID)
  question: { text, timestamp }
  answer: { text, sources: SourceDocumentReference[], timestamp }
  timestamp: Date
}
```

## Backend API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/chat` | Blocking Q&A |
| POST | `/api/chat/stream` | Streaming SSE Q&A |
| POST | `/api/documents` | Upload document (multipart) |
| GET | `/api/documents` | List documents |
| GET | `/api/documents/categories` | List categories |
| GET | `/api/documents/{id}` | Document details |
| GET | `/api/documents/{id}/file` | Download file |
| PATCH | `/api/documents/{id}` | Rename document |
| DELETE | `/api/documents/{id}` | Delete document |

## Troubleshooting

### Backend unavailable / CORS errors

```bash
# Verify backend is running
curl http://localhost:8080/api/health

# Dev server uses proxy (proxy.conf.json) - start with:
npm start
```

### SSE streaming not working

The chat uses **Fetch API** (not EventSource) for SSE streaming to avoid proxy buffering issues. In development, `streamUrl` points directly to `http://localhost:8080/api` to bypass the Angular proxy.

If streaming hangs, check that Ollama is running:
```bash
curl http://localhost:11434/api/tags
```

### localStorage not persisting

- Check if browser is in **private/incognito mode** (localStorage disabled)
- Check quota: `JSON.stringify(localStorage).length` in dev console
- Reset all data: call `StorageService.clear()` or delete keys prefixed with `hr-assistant-`

### Tests failing

```bash
# Run tests (Vitest, not Karma)
npx ng test

# Common issue: mock names must match service method names ($ suffix convention)
# Example: loadDocuments$ (not loadDocuments)
```

### Ollama models not loaded

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

### Docker containers not running

```bash
cd ../backend && docker compose up -d   # pgvector + Redis
```

## License

Internal project - All rights reserved
