# HR Assistant RAG - Frontend

An Angular 21 web application providing an intelligent HR assistant interface with chat and document management features.

## Tech Stack

- **Framework**: Angular 21 with standalone components
- **Language**: TypeScript 5.6+
- **UI Library**: PrimeNG v20 (Aura theme)
- **Icons**: PrimeIcons
- **State Management**: Signals + RxJS
- **HTTP**: Angular HttpClient with SSE streaming
- **Storage**: Browser localStorage

## Prerequisites

- Node.js 20+ LTS
- npm 10+
- Backend API running on http://localhost:8080

## Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Start Development Server

```bash
# Start with backend proxy
ng serve --proxy-config proxy.conf.json
```

The application will open at http://localhost:4200

### 3. Build for Production

```bash
ng build --configuration=production
```

Build artifacts will be in `dist/frontend/`

## Project Structure

```
src/
├── app/
│   ├── core/              # Singleton services, models, interceptors
│   │   ├── services/      # API, conversation, document, storage services
│   │   ├── models/        # TypeScript interfaces
│   │   └── interceptors/  # HTTP interceptors
│   ├── shared/            # Reusable components, directives, pipes
│   ├── features/          # Feature modules (lazy-loaded)
│   │   ├── chat/          # Chat interface
│   │   └── admin/         # Document management
│   ├── layout/            # Layout components (header, navigation)
│   ├── app.config.ts      # Application configuration
│   └── app.routes.ts      # Routing configuration
├── environments/          # Environment configs
└── styles.css             # Global styles
```

## Development Commands

```bash
# Development
ng serve                    # Start dev server
ng serve --open            # Start and open browser
ng serve --port 4300       # Use custom port

# Code Generation
ng generate component <name>   # Create component
ng generate service <name>     # Create service
ng generate module <name>      # Create module

# Testing
ng test                    # Run unit tests
ng test --code-coverage    # Run with coverage
npx cypress run            # Run E2E tests

# Building
ng build                   # Development build
ng build --watch           # Watch mode
ng build --configuration=production  # Production build

# Linting
ng lint                    # Run ESLint
```

## Features

### Chat Interface (User)
- Ask HR questions through chat interface
- Streaming responses with SSE
- View source documents used for answers
- Conversation history (50 messages max in localStorage)

### Admin Interface
- Upload documents (PDF/TXT, max 10MB)
- View all indexed documents
- Edit document names and replace files
- Delete documents
- Monitor indexing status

## Architecture Highlights

### Angular 21 Patterns
- **Standalone Components**: No NgModules
- **Signals**: UI reactivity (`signal()`, `computed()`, `effect()`)
- **New Control Flow**: `@if`, `@for`, `@switch`
- **Input/Output Functions**: `input()`, `output()`
- **Dependency Injection**: `inject()` function
- **OnPush Change Detection**: Performance optimization

### State Management
- Services + RxJS for async operations
- Signals for UI reactivity
- `toSignal()` for Observable → Signal conversion

### SSE Streaming
- EventSource wrapped in `NgZone.run()` for change detection
- Real-time answer streaming from backend

## Configuration

### Environment Variables

**Development** (`src/environments/environment.ts`):
```typescript
{
  production: false,
  apiUrl: 'http://localhost:8080/api',
  localStoragePrefix: 'hr-assistant-',
  maxConversationMessages: 50,
  maxFileSizeMB: 10
}
```

**Production** (`src/environments/environment.prod.ts`):
```typescript
{
  production: true,
  apiUrl: '/api',  // Relative URL
  localStoragePrefix: 'hr-assistant-',
  maxConversationMessages: 50,
  maxFileSizeMB: 10
}
```

### Proxy Configuration

The `proxy.conf.json` file proxies `/api` requests to `http://localhost:8080` during development to avoid CORS issues.

## Backend API

The frontend consumes the following backend endpoints:

- `POST /api/chat` - Send question, get answer (blocking)
- `POST /api/chat/stream` - Send question, get streaming answer (SSE)
- `POST /api/documents` - Upload document (multipart)
- `GET /api/documents` - List all documents
- `DELETE /api/documents/{id}` - Delete document

## Troubleshooting

### CORS Errors
Ensure the dev server is started with proxy configuration:
```bash
ng serve --proxy-config proxy.conf.json
```

### Backend Unavailable
Verify the backend is running:
```bash
curl http://localhost:8080/api/health
```

### localStorage Not Persisting
Check if browser is in private/incognito mode (localStorage disabled).

## Resources

- [Angular 21 Documentation](https://angular.dev)
- [PrimeNG v20 Documentation](https://v20.primeng.org/)
- [RxJS Documentation](https://rxjs.dev/)
- [Angular Style Guide](https://angular.dev/style-guide)

## License

Internal project - All rights reserved
