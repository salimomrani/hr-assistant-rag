# Quickstart: HR RAG Frontend

**Feature**: HR RAG Frontend Interface
**Date**: 2026-01-21
**Prerequisites**: Node.js 20+ LTS, npm 10+, Backend API running on port 8080

## Overview

This guide walks you through setting up and running the Angular 21 HR RAG frontend application locally.

## Prerequisites Check

```bash
# Verify Node.js version (requires 20+ LTS)
node --version  # Should output v20.x.x or higher

# Verify npm version
npm --version   # Should output 10.x.x or higher

# Verify backend is running
curl http://localhost:8080/api/health
# Should return: {"status":"UP","ollama":"connected"}
```

## Initial Setup

### 1. Install Angular CLI (if not already installed)

```bash
npm install -g @angular/cli@latest
# This will install Angular CLI 19.x (latest stable)
```

### 2. Create Angular 21 Project

```bash
# Navigate to project root
cd /path/to/hr-assistant-rag

# Create Angular application with routing and CSS
ng new frontend --routing --style=css --skip-git --standalone

# Navigate into frontend directory
cd frontend
```

### 3. Install Dependencies

```bash
# Install PrimeNG, themes, and icons
npm install primeng @primeuix/themes primeicons

# Install additional dependencies
npm install --save rxjs@^7.8.0
npm install --save-dev @types/node
```

### 4. Configure PrimeNG

Update `src/app/app.config.ts` to include PrimeNG providers:

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimationsAsync(),
    providePrimeNG({
      theme: {
        preset: Aura
      }
    })
  ]
};
```

Add PrimeIcons CSS to `angular.json` or `src/styles.css`:

**Option 1 - In `src/styles.css`:**
```css
@import 'primeicons/primeicons.css';
```

**Option 2 - In `angular.json`:**
```json
"styles": [
  "src/styles.css",
  "node_modules/primeicons/primeicons.css"
]
```

### 5. Configure Environment

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  localStoragePrefix: 'hr-assistant-',
  maxConversationMessages: 50,
  maxFileSizeMB: 10
};
```

Create `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: '/api',  // Relative URL for production
  localStoragePrefix: 'hr-assistant-',
  maxConversationMessages: 50,
  maxFileSizeMB: 10
};
```

### 6. Configure Proxy for Development

Create `proxy.conf.json` in project root:

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

Update `angular.json` to use proxy:

```json
{
  "projects": {
    "frontend": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          }
        }
      }
    }
  }
}
```

## Project Structure Setup

### 1. Generate Core Structure

```bash
# Create core module structure
ng generate module core
ng generate service core/services/api
ng generate service core/services/conversation
ng generate service core/services/document
ng generate service core/services/storage

# Create shared module
ng generate module shared
ng generate component shared/components/loading-spinner --standalone
ng generate component shared/components/error-message --standalone

# Create feature modules
ng generate module features/chat --route chat --module app.routes.ts
ng generate module features/admin --route admin --module app.routes.ts

# Create layout components
ng generate component layout/header --standalone
ng generate component layout/navigation --standalone
```

### 2. Create Component Directories with Separation

For each feature component, create separate .ts, .html, .css files:

```bash
# Chat feature components
mkdir -p src/app/features/chat/components/chat-container
mkdir -p src/app/features/chat/components/message-list
mkdir -p src/app/features/chat/components/message-input
mkdir -p src/app/features/chat/components/source-list

# Admin feature components
mkdir -p src/app/features/admin/components/document-list
mkdir -p src/app/features/admin/components/document-upload
mkdir -p src/app/features/admin/components/document-item
mkdir -p src/app/features/admin/components/document-actions
```

## Development Workflow

### 1. Start Backend (Terminal 1)

```bash
cd backend
mvn spring-boot:run

# Wait for "Started HrAssistantRagApplication" message
# Backend runs on http://localhost:8080
```

### 2. Start Frontend (Terminal 2)

```bash
cd frontend
ng serve --proxy-config proxy.conf.json

# Application runs on http://localhost:4200
# Opens automatically in browser
```

### 3. Verify Setup

Navigate to http://localhost:4200 and verify:

- ✅ Navigation header displays with "Chat" and "Admin" links
- ✅ Chat page loads at `/chat`
- ✅ Admin page loads at `/admin`
- ✅ No console errors related to API calls

## Testing Setup

### 1. Unit Tests

```bash
# Run unit tests
ng test

# Run tests with coverage
ng test --code-coverage

# Run tests in headless mode (CI)
ng test --watch=false --browsers=ChromeHeadless
```

### 2. E2E Tests (Cypress)

```bash
# Install Cypress
npm install --save-dev cypress

# Initialize Cypress
npx cypress open

# Run E2E tests
npx cypress run
```

Create `cypress/e2e/chat.cy.ts`:

```typescript
describe('Chat Page', () => {
  beforeEach(() => {
    cy.visit('/chat');
  });

  it('should display chat interface', () => {
    cy.contains('Chat').should('be.visible');
    cy.get('input[placeholder*="question"]').should('be.visible');
  });

  it('should submit a question', () => {
    cy.get('input[placeholder*="question"]').type('Combien de jours de congés?');
    cy.get('button[type="submit"]').click();
    cy.contains('Combien de jours de congés?').should('be.visible');
  });
});
```

## Build for Production

```bash
# Build optimized production bundle
ng build --configuration=production

# Output: dist/frontend/
# Deploy contents to web server (Nginx, Apache, etc.)
```

### Production Configuration

Ensure `dist/frontend/index.html` is served for all routes (SPA routing):

**Nginx example**:
```nginx
server {
  listen 80;
  server_name your-domain.com;
  root /var/www/frontend/dist/frontend;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }

  location /api {
    proxy_pass http://localhost:8080/api;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

## Common Issues & Solutions

### Issue: CORS errors when calling backend

**Solution**: Ensure proxy.conf.json is configured and Angular dev server is started with `--proxy-config`:

```bash
ng serve --proxy-config proxy.conf.json
```

### Issue: Backend returns 503 "Service unavailable"

**Solution**: Verify Ollama is running:

```bash
ollama list
# Should show llama3.2 and nomic-embed-text

# If not running:
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

### Issue: localStorage not persisting

**Solution**: Check browser's private/incognito mode (localStorage disabled). Use regular browser window.

### Issue: File upload fails with 413 error

**Solution**: File exceeds 10MB limit. Backend validates max 10MB (10,485,760 bytes). Check file size:

```bash
ls -lh your-file.pdf
```

### Issue: SSE streaming not working

**Solution**: Ensure EventSource is wrapped in NgZone:

```typescript
import { NgZone } from '@angular/core';

constructor(private ngZone: NgZone) {}

streamChat(question: string): Observable<string> {
  return new Observable(observer => {
    const eventSource = new EventSource(`/api/chat/stream`);

    eventSource.onmessage = (event) => {
      this.ngZone.run(() => {
        observer.next(event.data);
      });
    };
  });
}
```

## Development Commands Reference

```bash
# Development
ng serve                              # Start dev server
ng serve --open                       # Start and open browser
ng serve --port 4300                  # Use custom port

# Code Generation
ng generate component <name>          # Create component
ng generate service <name>            # Create service
ng generate module <name>             # Create module
ng generate guard <name>              # Create route guard

# Testing
ng test                               # Run unit tests
ng test --watch=false                 # Run once
ng e2e                                # Run E2E tests

# Building
ng build                              # Development build
ng build --configuration=production   # Production build
ng build --watch                      # Watch mode

# Linting
ng lint                               # Run ESLint

# Code Analysis
ng analytics                          # Bundle size analysis
```

## Next Steps

1. **Implement Core Services**: Start with `ApiService`, `ConversationService`, `DocumentService`
2. **Build Chat Feature**: Create chat-container, message-list, message-input components
3. **Build Admin Feature**: Create document-list, document-upload components
4. **Add PrimeNG Components**: Use p-card, p-button, p-table, p-fileUpload, p-progressSpinner, p-toast
5. **Implement SSE Streaming**: Use RxJS Observable with EventSource
6. **Add Error Handling**: Create HTTP interceptor for global error handling
7. **Implement localStorage**: Create StorageService for conversation persistence
8. **Write Tests**: Unit tests for services, E2E tests for user flows

## Resources

- [Angular 21 Documentation](https://angular.io/docs)
- [PrimeNG v20 Documentation](https://v20.primeng.org/)
- [PrimeNG Showcase](https://v20.primeng.org/showcase/) - Live examples of all components
- [PrimeIcons](https://primeng.org/icons) - Complete icon library
- [RxJS Documentation](https://rxjs.dev/)
- [Angular Style Guide](https://angular.io/guide/styleguide)
- [Server-Sent Events API](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)

## Support

For issues with:
- **Frontend**: Check browser console, Angular dev tools
- **Backend**: Check Spring Boot logs (`mvn spring-boot:run` output)
- **Ollama**: Check Ollama service (`ollama list`, `curl http://localhost:11434`)

**Happy coding! 🚀**
