# Angular 21 Frontend Development Research
## HR Assistant RAG Application

**Date**: 2026-01-21
**Angular Version**: 18
**Purpose**: Technical decisions for HR Assistant chat/admin interface

---

## 1. UI Component Library Decision

### Overview
Evaluated three approaches for building the HR Assistant chat and admin interface with Angular 21:
1. **Angular Material** - Google's official Material Design implementation
2. **PrimeNG** - Enterprise-focused UI component library
3. **Custom Components** - Building from scratch

### Detailed Comparison

#### Angular Material

**Pros:**
- ✅ **Official Google Support**: Built by Google, guaranteed compatibility with Angular 21 and future versions
- ✅ **Smaller Bundle Size**: Typically results in smaller bundle sizes and faster rendering due to Angular CDK integration
- ✅ **Performance**: Built on top of Angular CDK, integrates efficiently with Angular's change detection
- ✅ **Material Design Consistency**: Strict Material Design guidelines ensure consistent, professional UI
- ✅ **Documentation**: Comprehensive official documentation and large community support
- ✅ **SSE Compatibility**: Works seamlessly with Angular's SSE implementation (handled at framework level)
- ✅ **Angular 21 Ready**: Among the first libraries to integrate new Angular features including standalone components

**Cons:**
- ❌ **Limited Component Variety**: Fewer advanced/niche components compared to PrimeNG (no built-in charts, advanced data grids)
- ❌ **Customization Constraints**: Highly opinionated with strict Material Design guidelines - limited visual freedom without extensive overrides
- ❌ **Performance Overhead**: Some components have additional overhead due to complexity and multiple dependencies
- ❌ **May Feel Slower**: Can experience slowdowns with large numbers of components due to deep Angular integration

**File Upload Components:**
- Standard file input with customization
- Requires custom implementation for advanced drag-and-drop
- Can be enhanced with third-party libraries like `@iplab/ngx-file-upload`

**Chat UI Patterns:**
- No native chat component
- Requires custom implementation using:
  - `mat-list` for message lists
  - `mat-card` for message bubbles
  - `mat-form-field` + `mat-input` for message input
  - Custom directives for scroll behavior

**Bundle Size Impact:** ~500KB (minified, with tree-shaking for typical enterprise app)

---

#### PrimeNG

**Pros:**
- ✅ **Extensive Component Library**: 90+ customizable components including data tables, charts, calendars, advanced forms
- ✅ **Flexible Theming**: Pre-built Flat and Material themes with theme designer for custom branding
- ✅ **Enterprise Features**: Built-in support for drag-and-drop, charts, tree views, advanced data grids
- ✅ **Angular 21 Compatible**: v17.18.0 supports Angular 21 with release cycle aligned to Angular (every 6 months)
- ✅ **2026 Roadmap**: Angular v22 support in Q2 2026, Angular v23 in Q4 2026
- ✅ **Rich Feature Set**: Includes advanced components not available in Angular Material
- ✅ **SSE Compatibility**: Works seamlessly with Angular's SSE implementation (handled at framework level)

**Cons:**
- ❌ **Larger Bundle Size**: Rich feature set results in larger library unless optimization techniques (tree-shaking) are used
- ❌ **Complex Theming**: Multiple CSS layers and complex UI behaviors can introduce bundle overhead
- ❌ **May Be Overkill**: Heavy if only a few basic components are needed
- ❌ **Less Immediate Alignment**: May not always be as closely aligned with latest Angular releases as Angular Material
- ❌ **Initial Load Time Impact**: Can affect loading time without proper optimization

**File Upload Components:**
- `p-fileUpload` with built-in features:
  - Drag-and-drop support
  - Multiple file selection
  - Progress bar
  - File preview
  - Custom templates
  - Auto upload mode

**Chat UI Patterns:**
- No native chat component
- Can be built using:
  - `p-scrollPanel` for message container
  - `p-card` or custom templates for message bubbles
  - `p-inputTextarea` for message input
  - Rich text editor (`p-editor`) option

**Bundle Size Impact:** ~800KB-1.2MB (minified, with tree-shaking for typical enterprise app)

---

#### Custom Components

**Pros:**
- ✅ **Full Control**: Complete control over design, features, and performance
- ✅ **Minimal Bundle Size**: Only include exactly what you need
- ✅ **Perfect Fit**: Tailored specifically to HR Assistant requirements
- ✅ **No External Dependencies**: No reliance on third-party library maintenance schedules

**Cons:**
- ❌ **High Development Cost**: Significant time investment to build and maintain
- ❌ **Accessibility Challenges**: Must implement WCAG compliance from scratch
- ❌ **No Community Support**: No existing solutions, documentation, or community help
- ❌ **Maintenance Burden**: Long-term maintenance and updates fall entirely on the team
- ❌ **Testing Overhead**: Must create comprehensive test suites for all components
- ❌ **Feature Parity**: Difficult to match feature richness of established libraries

**Estimated Development Time:**
- Chat interface: 40-60 hours
- File upload with drag-drop: 20-30 hours
- Admin dashboard: 60-80 hours
- Responsive design: 30-40 hours
- Accessibility: 40-50 hours
- **Total**: 190-260 hours minimum

---

### Recommendation: **Angular Material**

**Rationale:**

For the HR Assistant RAG MVP, **Angular Material is the optimal choice** based on:

1. **Performance First**: Smaller bundle size (~500KB vs ~1MB for PrimeNG) critical for initial load times
2. **Angular 21 Alignment**: Official Google support ensures immediate compatibility with Angular 21 features (standalone components, signals)
3. **SSE/Streaming Ready**: Works seamlessly with Server-Sent Events for chat streaming
4. **MVP Scope**: Component needs are basic (chat UI, file upload, simple admin) - don't need PrimeNG's 90+ components
5. **Development Speed**: Extensive documentation and community support accelerates MVP development
6. **Future-Proof**: Google maintains Angular Material in lock-step with Angular framework updates
7. **Professional Appearance**: Material Design provides clean, modern, enterprise-appropriate UI out of the box

**Implementation Strategy:**
- Use Angular Material as the base UI library
- Enhance file upload with `ngx-file-drop` or `@iplab/ngx-file-upload` for drag-and-drop
- Build custom chat components using Material primitives (`mat-list`, `mat-card`, etc.)
- Consider PrimeNG only if Phase 2+ requires advanced data visualization or complex data grids

**Trade-offs Accepted:**
- Will need custom chat component (neither library provides chat out-of-box)
- Less visual customization flexibility than PrimeNG
- May need to add specific libraries for advanced features later

---

## 2. State Management Approach

### Overview
Evaluated state management strategies for Angular 21 with the following constraints:
- MVP scope (no authentication, simple features)
- localStorage persistence required
- SSE streaming message handling
- Small to medium application complexity

### Options Evaluated

#### 1. Angular Signals (New Approach)

**Description:**
Angular Signals (introduced in Angular 16, improved in Angular 21) provide reactive state management with fine-grained reactivity and automatic change detection.

**Pros:**
- ✅ **Simplified Reactivity**: Easier to understand than RxJS for simple state
- ✅ **Performance**: Fine-grained change detection - only re-renders what actually changed
- ✅ **Less Boilerplate**: No subscription management overhead
- ✅ **Angular 21+ Default**: Angular 21+ makes Signals the default for local state
- ✅ **Type Safety**: Strong TypeScript support with computed signals
- ✅ **Synchronous**: Perfect for internal component state and UI reactivity

**Cons:**
- ❌ **Not for Async**: Not designed for asynchronous data streams or event handling
- ❌ **Limited Operators**: Lacks RxJS's rich operator ecosystem
- ❌ **Learning Curve**: New mental model for developers familiar with RxJS
- ❌ **SSE Challenge**: Requires RxJS integration for Server-Sent Events

**Best Use Cases:**
- Component-local state (form state, UI toggles)
- Derived/computed values
- Simple counter/flag management
- Synchronous state updates

**Example Pattern:**
```typescript
export class ChatComponent {
  messages = signal<Message[]>([]);
  isTyping = signal(false);
  messageCount = computed(() => this.messages().length);

  addMessage(message: Message) {
    this.messages.update(msgs => [...msgs, message]);
  }
}
```

---

#### 2. Services with RxJS (Traditional Approach)

**Description:**
Service-based state stores using RxJS Observables and BehaviorSubjects for reactive state management.

**Pros:**
- ✅ **Async Excellence**: Perfect for HTTP requests, SSE streams, WebSocket connections
- ✅ **Rich Operators**: Powerful operator library (map, filter, switchMap, debounceTime, etc.)
- ✅ **Proven Pattern**: Mature, well-understood approach with extensive community knowledge
- ✅ **SSE Integration**: Natural fit for Server-Sent Events with Observable streams
- ✅ **Complex Event Handling**: Excels at combining multiple streams, event-driven programming
- ✅ **No Additional Dependencies**: Built into Angular core

**Cons:**
- ❌ **Subscription Management**: Must manually unsubscribe to prevent memory leaks
- ❌ **Complexity**: Steeper learning curve for beginners
- ❌ **Boilerplate**: More code compared to Signals for simple state
- ❌ **Change Detection**: Triggers Angular change detection even when values don't change (without optimization)

**Best Use Cases:**
- HTTP API calls
- Server-Sent Events / WebSocket streams
- Complex async workflows
- Combining multiple data sources
- Debouncing/throttling user input

**Example Pattern:**
```typescript
@Injectable({ providedIn: 'root' })
export class ChatStateService {
  private messagesSubject = new BehaviorSubject<Message[]>([]);
  messages$ = this.messagesSubject.asObservable();

  private sseConnection?: EventSource;

  connectToStream(chatId: string): Observable<Message> {
    return new Observable(observer => {
      this.sseConnection = new EventSource(`/api/chat/stream?id=${chatId}`);

      this.sseConnection.onmessage = (event) => {
        observer.next(JSON.parse(event.data));
      };

      this.sseConnection.onerror = (error) => {
        observer.error(error);
      };

      return () => this.sseConnection?.close();
    });
  }

  addMessage(message: Message) {
    const current = this.messagesSubject.value;
    this.messagesSubject.next([...current, message]);
  }
}
```

---

#### 3. NgRx (Redux Pattern)

**Description:**
Full-featured state management using Redux pattern with Actions, Reducers, Effects, and Selectors. NgRx now offers Signal-based APIs (Signal State, Signal Store).

**Pros:**
- ✅ **Predictable State**: Single source of truth with immutable state updates
- ✅ **Time-Travel Debugging**: Redux DevTools for debugging state changes
- ✅ **Scalability**: Excellent for large applications with complex state
- ✅ **Signal Integration**: New Signal State and Signal Store APIs in recent versions
- ✅ **Side Effect Management**: Structured approach to async operations via Effects

**Cons:**
- ❌ **Overkill for MVP**: Massive overhead for small applications
- ❌ **Boilerplate Heavy**: Requires actions, reducers, effects, selectors for every feature
- ❌ **Learning Curve**: Steep learning curve, requires understanding Redux concepts
- ❌ **Bundle Size**: Additional dependency increases application size
- ❌ **Development Speed**: Slows down MVP development

**Best Use Cases:**
- Large enterprise applications
- Complex state with many interdependencies
- Teams requiring strict state management patterns
- Applications needing time-travel debugging

**Not Recommended** for HR Assistant MVP due to complexity overhead.

---

#### 4. Akita

**Description:**
State management pattern built on RxJS with a simpler API than NgRx, focusing on entities and queries.

**Pros:**
- ✅ **Simpler than NgRx**: Less boilerplate, easier to learn
- ✅ **Entity Management**: Good for managing collections of entities
- ✅ **DevTools**: Built-in Redux DevTools support
- ✅ **Performance**: Efficient query system

**Cons:**
- ❌ **Additional Dependency**: Requires external library
- ❌ **Still Overkill**: Too complex for MVP scope
- ❌ **Smaller Community**: Less community support than NgRx
- ❌ **Learning Curve**: Still requires learning library-specific concepts

**Not Recommended** for HR Assistant MVP - unnecessary complexity.

---

### Recommendation: **Hybrid Approach (Services + RxJS → Signals)**

**Strategy:**
Use **Services with RxJS** for async operations and SSE streaming, then convert to **Signals** at the UI boundary.

**Rationale:**

1. **Best of Both Worlds**: RxJS handles async complexity, Signals simplify UI reactivity
2. **SSE Perfect Fit**: RxJS Observables are ideal for Server-Sent Events streams
3. **MVP Simplicity**: Avoids heavy libraries (NgRx, Akita) while maintaining clean architecture
4. **localStorage Integration**: Simple to implement with service-based pattern
5. **Angular 21 Alignment**: Follows modern Angular best practices (2026 guidance)
6. **Future-Proof**: Aligns with Angular's direction (Signals for state, RxJS for async)
7. **No Subscription Management**: Use `toSignal()` to eliminate manual unsubscribe logic

**Implementation Pattern:**

```typescript
// Service Layer (RxJS for async operations)
@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);

  // SSE streaming
  streamChat(question: string): Observable<string> {
    return new Observable(observer => {
      const eventSource = new EventSource(
        `/api/chat/stream?question=${encodeURIComponent(question)}`
      );

      eventSource.onmessage = (event) => observer.next(event.data);
      eventSource.onerror = (error) => observer.error(error);

      return () => eventSource.close();
    });
  }

  // Regular HTTP
  uploadDocument(file: File): Observable<DocumentInfo> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DocumentInfo>('/api/documents', formData);
  }
}

// Component Layer (Signals for UI state)
@Component({
  selector: 'app-chat',
  standalone: true,
  template: `
    <div class="messages">
      @for (msg of messages(); track msg.id) {
        <div class="message">{{ msg.content }}</div>
      }
    </div>
    <div>{{ currentResponse() }}</div>
  `
})
export class ChatComponent {
  private chatService = inject(ChatService);

  // Signals for UI state
  messages = signal<Message[]>([]);
  currentResponse = signal('');
  isStreaming = signal(false);

  sendMessage(question: string) {
    this.isStreaming.set(true);
    this.currentResponse.set('');

    this.chatService.streamChat(question).subscribe({
      next: (chunk) => {
        this.currentResponse.update(current => current + chunk);
      },
      complete: () => {
        this.messages.update(msgs => [...msgs, {
          id: Date.now(),
          content: this.currentResponse()
        }]);
        this.currentResponse.set('');
        this.isStreaming.set(false);
      }
    });
  }
}
```

**localStorage Service Pattern:**

```typescript
@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly CHAT_HISTORY_KEY = 'hr_chat_history';

  saveChatHistory(messages: Message[]): void {
    localStorage.setItem(this.CHAT_HISTORY_KEY, JSON.stringify(messages));
  }

  loadChatHistory(): Message[] {
    const stored = localStorage.getItem(this.CHAT_HISTORY_KEY);
    return stored ? JSON.parse(stored) : [];
  }

  clearChatHistory(): void {
    localStorage.removeItem(this.CHAT_HISTORY_KEY);
  }
}
```

**Key Benefits:**
- ✅ No NgRx/Akita overhead
- ✅ No manual subscription management (can use `toSignal()` or `async` pipe)
- ✅ Perfect for SSE streaming
- ✅ Simple localStorage integration
- ✅ Scales to medium complexity
- ✅ Aligns with Angular 21+ best practices

**Trade-offs Accepted:**
- Manual service creation (vs. auto-generated NgRx boilerplate)
- No time-travel debugging (not needed for MVP)
- Less structured than Redux pattern (acceptable for MVP scope)

---

## 3. Angular 21 Best Practices

### A. Standalone Components (Angular 14+, Standard in 18+)

**Overview:**
Standalone components eliminate the need for NgModules, simplifying application architecture and enabling better tree-shaking.

**Key Benefits:**
- ✅ Simpler mental model (no NgModule declarations)
- ✅ Better tree-shaking (smaller bundles)
- ✅ Faster compilation
- ✅ Angular 21+ default (future-proof)

**Basic Pattern:**

```typescript
@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,           // For *ngIf, *ngFor, async pipe
    ReactiveFormsModule,    // For reactive forms
    MatCardModule,          // Material components
    MatButtonModule,
    ChatMessageComponent    // Other standalone components
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent {
  // Component logic
}
```

**Bootstrap Configuration:**

```typescript
// main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/interceptors/auth.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    // Other providers
  ]
}).catch(err => console.error(err));
```

**Best Practices:**
1. Keep root component minimal - delegate feature logic to child components
2. Import only what you need in each component
3. Use `providedIn: 'root'` for services (tree-shakable)
4. Handle bootstrap errors for debugging

**Sources:**
- [Mastering Angular 21 Standalone Components](https://medium.com/@aalam-info-solutions-llp/mastering-angular-18-comprehensive-guide-to-standalone-components-routing-lazy-loading-and-d70bf8ea39ec)
- [Lazy Loading with Standalone Architecture](https://medium.com/@vishalini.sharma/lazy-loading-with-standalone-architecture-a74ca90b1703)

---

### B. Signals vs RxJS (When to Use Each)

**2026 Consensus:** Use both technologies together based on their strengths.

#### Use Signals For:

✅ **Synchronous, internal state**
- Component-local state (toggles, counters, flags)
- Form state that doesn't involve async validation
- UI-specific state (expanded/collapsed, selected items)

✅ **Derived/computed values**
```typescript
export class ProductComponent {
  quantity = signal(1);
  price = signal(29.99);
  total = computed(() => this.quantity() * this.price());
}
```

✅ **Simple state updates**
```typescript
count = signal(0);
increment() {
  this.count.update(c => c + 1);
}
```

✅ **Performance-critical UI reactivity**
- Fine-grained change detection
- Only re-renders what changed

#### Use RxJS For:

✅ **Asynchronous operations**
- HTTP requests
- Server-Sent Events / WebSocket streams
- Timer-based operations

✅ **Complex event handling**
```typescript
search$ = this.searchInput$.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term => this.searchService.search(term))
);
```

✅ **Combining multiple streams**
```typescript
vm$ = combineLatest([
  this.user$,
  this.settings$,
  this.notifications$
]).pipe(
  map(([user, settings, notifications]) => ({
    user,
    settings,
    notifications
  }))
);
```

✅ **Advanced operators**
- `debounceTime`, `throttleTime` for input handling
- `retry`, `catchError` for error handling
- `shareReplay` for caching
- `switchMap`, `mergeMap`, `concatMap` for request management

#### Recommended Pattern: RxJS → Signals

**Convert Observables to Signals at the UI boundary:**

```typescript
export class ChatComponent {
  private chatService = inject(ChatService);

  // RxJS for async operation
  private messages$ = this.chatService.getMessages();

  // Convert to Signal for UI consumption
  messages = toSignal(this.messages$, { initialValue: [] });

  // Or manually in constructor
  constructor() {
    this.messages$.subscribe(msgs => this.messagesSignal.set(msgs));
  }
}
```

**Key Insight:** "Start with RxJS to reap the benefits of operators and finish with signals to remove the burdens of subscription management."

**Sources:**
- [Angular State Management: RxJS vs Signals](https://medium.com/@thecodingdon/angular-state-management-best-practices-with-rxjs-vs-signals-db629de24888)
- [From RxJS to Signals: The Future of State Management](https://hackernoon.com/from-rxjs-to-signals-the-future-of-state-management-in-angular)
- [Will Signals Replace RxJS?](https://angularexperts.io/blog/signals-vs-rxjs/)
- [Angular State Management for 2025](https://nx.dev/blog/angular-state-management-2025)

---

### C. Lazy Loading with New Routing

**Overview:**
Lazy loading with standalone components significantly reduces initial load time by deferring component loading until needed.

**Benefits:**
- ✅ Smaller initial bundle size
- ✅ Faster Time to Interactive (TTI)
- ✅ Each lazy-loaded component gets its own tiny bundle
- ✅ More efficient than traditional NgModule approach

**Basic Lazy Loading:**

```typescript
// app.routes.ts
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'chat',
    loadComponent: () => import('./chat/chat.component').then(m => m.ChatComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin.component').then(m => m.AdminComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
```

**Lazy Loading Child Routes:**

```typescript
// admin.routes.ts
import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: 'documents',
    loadComponent: () => import('./documents/documents.component')
      .then(m => m.DocumentsComponent)
  },
  {
    path: 'settings',
    loadComponent: () => import('./settings/settings.component')
      .then(m => m.SettingsComponent)
  }
];

// app.routes.ts
export const routes: Routes = [
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.routes').then(m => m.adminRoutes)
  }
];
```

**Route Guards with Lazy Loading:**

```typescript
export const routes: Routes = [
  {
    path: 'admin',
    canActivate: [authGuard],  // Functional guard
    loadComponent: () => import('./admin/admin.component')
      .then(m => m.AdminComponent)
  }
];

// Functional guard (Angular 14+)
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  return authService.isAuthenticated();
};
```

**Preloading Strategy:**

```typescript
// main.ts
import { PreloadAllModules } from '@angular/router';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(
      routes,
      withPreloading(PreloadAllModules)  // Preload in background after initial load
    )
  ]
});
```

**Best Practices:**
1. Lazy load feature routes, not the home page
2. Group related features into shared lazy-loaded sections
3. Use preloading strategy for better UX after initial load
4. Handle loading errors gracefully

**Sources:**
- [Routing and Lazy Loading with Standalone Components](https://www.angulararchitects.io/en/blog/routing-and-lazy-loading-with-standalone-components/)
- [Lazy Load Standalone Components with loadComponent](https://ultimatecourses.com/blog/lazy-load-standalone-components-via-load-component)
- [Angular Lazy Loading Guide](https://zerotomastery.io/blog/angular-lazy-loading/)

---

### D. SSE Integration Patterns

**Overview:**
Server-Sent Events (SSE) is simpler than WebSockets for scenarios where only the server needs to send updates (like chat responses, live notifications).

**Why SSE over WebSockets:**
- ✅ Simpler implementation (native browser EventSource API)
- ✅ Automatic reconnection
- ✅ Works over standard HTTP/HTTPS
- ✅ No need for bidirectional communication
- ✅ Built-in error handling

**Pattern 1: Service-Based SSE with RxJS Observable**

```typescript
// sse.service.ts
@Injectable({ providedIn: 'root' })
export class SseService {
  private zone = inject(NgZone);  // Critical for change detection!

  /**
   * Creates an Observable from Server-Sent Events
   * @param url - The SSE endpoint URL
   * @returns Observable that emits SSE messages
   */
  createEventSource(url: string): Observable<MessageEvent> {
    return new Observable(observer => {
      const eventSource = new EventSource(url);

      // Run inside Angular zone for proper change detection
      eventSource.onmessage = (event) => {
        this.zone.run(() => {
          observer.next(event);
        });
      };

      eventSource.onerror = (error) => {
        this.zone.run(() => {
          observer.error(error);
          eventSource.close();
        });
      };

      // Cleanup on unsubscribe
      return () => {
        eventSource.close();
      };
    });
  }
}
```

**Pattern 2: Chat Component with Streaming**

```typescript
// chat.component.ts
@Component({
  selector: 'app-chat',
  standalone: true,
  template: `
    <div class="chat-container">
      @for (msg of messages(); track msg.id) {
        <div class="message" [class.user]="msg.isUser">
          {{ msg.content }}
        </div>
      }

      @if (isStreaming()) {
        <div class="message assistant streaming">
          {{ currentResponse() }}
          <span class="cursor">|</span>
        </div>
      }

      <form (submit)="sendMessage()">
        <input [(ngModel)]="userInput" [disabled]="isStreaming()" />
        <button type="submit" [disabled]="isStreaming()">Send</button>
      </form>
    </div>
  `
})
export class ChatComponent implements OnDestroy {
  private sseService = inject(SseService);
  private cdr = inject(ChangeDetectorRef);
  private subscription?: Subscription;

  // Signals for UI state
  messages = signal<ChatMessage[]>([]);
  currentResponse = signal('');
  isStreaming = signal(false);
  userInput = '';

  sendMessage() {
    if (!this.userInput.trim()) return;

    // Add user message
    this.messages.update(msgs => [...msgs, {
      id: Date.now(),
      content: this.userInput,
      isUser: true
    }]);

    const question = this.userInput;
    this.userInput = '';
    this.isStreaming.set(true);
    this.currentResponse.set('');

    // Connect to SSE stream
    const url = `/api/chat/stream?question=${encodeURIComponent(question)}`;

    this.subscription = this.sseService.createEventSource(url)
      .pipe(
        map(event => event.data),
        catchError(error => {
          console.error('SSE Error:', error);
          this.isStreaming.set(false);
          return EMPTY;
        })
      )
      .subscribe({
        next: (chunk: string) => {
          // Accumulate streaming response
          this.currentResponse.update(current => current + chunk);
        },
        complete: () => {
          // Add complete response to messages
          this.messages.update(msgs => [...msgs, {
            id: Date.now(),
            content: this.currentResponse(),
            isUser: false
          }]);
          this.currentResponse.set('');
          this.isStreaming.set(false);
        }
      });
  }

  ngOnDestroy() {
    // Clean up SSE connection
    this.subscription?.unsubscribe();
  }
}
```

**Pattern 3: Using ngx-sse-client Library**

```typescript
// Install: npm install ngx-sse-client

// app.config.ts
import { provideSseClient } from 'ngx-sse-client';

export const appConfig: ApplicationConfig = {
  providers: [
    provideSseClient()
  ]
};

// chat.service.ts
import { SseClient } from 'ngx-sse-client';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private sseClient = inject(SseClient);

  streamChat(question: string): Observable<string> {
    return this.sseClient.get(`/api/chat/stream?question=${encodeURIComponent(question)}`)
      .pipe(
        map((event: MessageEvent) => event.data),
        catchError(error => {
          console.error('SSE Error:', error);
          return throwError(() => error);
        })
      );
  }
}
```

**Pattern 4: NgRx ComponentStore with SSE**

```typescript
@Injectable()
export class ChatStore extends ComponentStore<ChatState> {
  private sseService = inject(SseService);

  readonly messages$ = this.select(state => state.messages);
  readonly isStreaming$ = this.select(state => state.isStreaming);

  readonly connectToStream = this.effect((question$: Observable<string>) => {
    return question$.pipe(
      tap(() => this.patchState({ isStreaming: true, currentResponse: '' })),
      switchMap(question =>
        this.sseService.createEventSource(`/api/chat/stream?question=${question}`)
          .pipe(
            map(event => event.data),
            tap(chunk => this.updateResponse(chunk)),
            catchError(error => {
              this.patchState({ isStreaming: false });
              return EMPTY;
            })
          )
      )
    );
  });

  private updateResponse(chunk: string) {
    this.patchState(state => ({
      currentResponse: state.currentResponse + chunk
    }));
  }
}
```

**Critical: Angular Zone Integration**

SSE implementations **must** run inside Angular Zone to trigger change detection:

```typescript
eventSource.onmessage = (event) => {
  this.zone.run(() => {
    // This ensures Angular knows to update the UI
    observer.next(event);
  });
};
```

**Best Practices:**
1. Always wrap EventSource callbacks in `NgZone.run()`
2. Clean up connections in `ngOnDestroy` or Observable teardown
3. Handle errors gracefully with retry logic
4. Show loading/streaming indicators in the UI
5. Accumulate chunks in a Signal or BehaviorSubject
6. Consider `ngx-sse-client` for interceptor support (headers, auth)

**Sources:**
- [Implementing Server-Sent Events in Angular](https://medium.com/@andrewkoliaka/implementing-server-sent-events-in-angular-a5e40617cb78)
- [Server-Sent Events with NestJS and Angular](https://medium.com/@piotrkorowicki/server-sent-events-sse-with-nestjs-and-angular-d90635783d8c)
- [Angular and Server Sent Events](https://dev.to/bartoszgajda55/angular-and-server-sent-events-sse-58de)
- [ngx-sse-client npm](https://www.npmjs.com/package/ngx-sse-client)

---

### E. localStorage Service Patterns

**Overview:**
localStorage provides persistent client-side storage for user data, chat history, and application state.

**Service Pattern:**

```typescript
// storage.service.ts
@Injectable({ providedIn: 'root' })
export class StorageService {
  private readonly PREFIX = 'hr_assistant_';

  /**
   * Set item in localStorage with type safety
   */
  setItem<T>(key: string, value: T): void {
    try {
      const serialized = JSON.stringify(value);
      localStorage.setItem(this.PREFIX + key, serialized);
    } catch (error) {
      console.error('Failed to save to localStorage:', error);
    }
  }

  /**
   * Get item from localStorage with type safety
   */
  getItem<T>(key: string, defaultValue: T): T {
    try {
      const item = localStorage.getItem(this.PREFIX + key);
      return item ? JSON.parse(item) : defaultValue;
    } catch (error) {
      console.error('Failed to read from localStorage:', error);
      return defaultValue;
    }
  }

  /**
   * Remove item from localStorage
   */
  removeItem(key: string): void {
    localStorage.removeItem(this.PREFIX + key);
  }

  /**
   * Clear all app-specific items
   */
  clear(): void {
    Object.keys(localStorage)
      .filter(key => key.startsWith(this.PREFIX))
      .forEach(key => localStorage.removeItem(key));
  }

  /**
   * Check if localStorage is available
   */
  isAvailable(): boolean {
    try {
      const test = '__storage_test__';
      localStorage.setItem(test, test);
      localStorage.removeItem(test);
      return true;
    } catch {
      return false;
    }
  }
}
```

**Usage in Feature Service:**

```typescript
// chat-history.service.ts
@Injectable({ providedIn: 'root' })
export class ChatHistoryService {
  private storage = inject(StorageService);
  private readonly CHAT_KEY = 'chat_history';
  private readonly MAX_MESSAGES = 100;

  private messagesSubject = new BehaviorSubject<ChatMessage[]>(
    this.loadFromStorage()
  );
  messages$ = this.messagesSubject.asObservable();

  private loadFromStorage(): ChatMessage[] {
    return this.storage.getItem<ChatMessage[]>(this.CHAT_KEY, []);
  }

  private saveToStorage(messages: ChatMessage[]): void {
    // Keep only last N messages to prevent localStorage overflow
    const trimmed = messages.slice(-this.MAX_MESSAGES);
    this.storage.setItem(this.CHAT_KEY, trimmed);
  }

  addMessage(message: ChatMessage): void {
    const current = this.messagesSubject.value;
    const updated = [...current, message];
    this.messagesSubject.next(updated);
    this.saveToStorage(updated);
  }

  clearHistory(): void {
    this.messagesSubject.next([]);
    this.storage.removeItem(this.CHAT_KEY);
  }

  exportHistory(): string {
    return JSON.stringify(this.messagesSubject.value, null, 2);
  }
}
```

**Component Integration:**

```typescript
@Component({
  selector: 'app-chat',
  standalone: true,
  template: `
    <button (click)="clearHistory()">Clear History</button>
    <button (click)="exportHistory()">Export History</button>

    @for (msg of messages(); track msg.id) {
      <div class="message">{{ msg.content }}</div>
    }
  `
})
export class ChatComponent {
  private chatHistory = inject(ChatHistoryService);

  // Convert Observable to Signal
  messages = toSignal(this.chatHistory.messages$, { initialValue: [] });

  clearHistory() {
    if (confirm('Clear all chat history?')) {
      this.chatHistory.clearHistory();
    }
  }

  exportHistory() {
    const data = this.chatHistory.exportHistory();
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `chat-history-${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
```

**Best Practices:**
1. Use a prefix for all keys to avoid conflicts
2. Always wrap in try-catch (localStorage can fail in private browsing)
3. Check availability before using
4. Limit stored data size (5-10MB limit in most browsers)
5. Sanitize/validate data when reading
6. Consider compression for large data (use libraries like `lz-string`)
7. Provide fallback behavior when localStorage is unavailable

**localStorage Quotas:**
- Chrome/Edge: 10MB per origin
- Firefox: 10MB per origin
- Safari: 5MB per origin

**Alternative: IndexedDB for Large Data**

For larger datasets (>5MB), consider IndexedDB:

```typescript
// For complex use cases, use libraries like:
// - Dexie.js (most popular)
// - localForage (localStorage-like API with IndexedDB backend)
```

**Sources:**
- [Angular HTTP Interceptor Token Tutorial](https://angular.love/how-to-implement-automatic-token-insertion-in-requests-using-http-interceptor-angular-tutorials/)
- [Error Handling with Angular Interceptors](https://dev.to/cezar-plescan/error-handling-with-angular-interceptors-2548)

---

### F. File Upload Handling

**Overview:**
File uploads in Angular 21 require handling file selection, validation, progress tracking, and integration with backend APIs.

**Basic File Upload Component:**

```typescript
// file-upload.component.ts
@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatProgressBarModule],
  template: `
    <div class="upload-container">
      <input
        #fileInput
        type="file"
        [accept]="acceptedTypes"
        [multiple]="multiple"
        (change)="onFileSelected($event)"
        hidden
      />

      <button mat-raised-button color="primary" (click)="fileInput.click()">
        Choose File{{ multiple ? 's' : '' }}
      </button>

      @if (uploading()) {
        <mat-progress-bar mode="determinate" [value]="uploadProgress()"></mat-progress-bar>
        <p>Uploading: {{ uploadProgress() }}%</p>
      }

      @if (error()) {
        <div class="error">{{ error() }}</div>
      }

      @if (selectedFiles().length > 0) {
        <div class="selected-files">
          <h3>Selected Files:</h3>
          <ul>
            @for (file of selectedFiles(); track file.name) {
              <li>
                {{ file.name }} ({{ formatFileSize(file.size) }})
                <button (click)="removeFile(file)">Remove</button>
              </li>
            }
          </ul>
        </div>
      }

      @if (selectedFiles().length > 0 && !uploading()) {
        <button mat-raised-button color="accent" (click)="uploadFiles()">
          Upload
        </button>
      }
    </div>
  `,
  styles: [`
    .upload-container { padding: 20px; }
    .error { color: red; margin-top: 10px; }
    .selected-files { margin-top: 20px; }
  `]
})
export class FileUploadComponent {
  private http = inject(HttpClient);

  @Input() acceptedTypes = '.pdf,.doc,.docx,.txt';
  @Input() multiple = true;
  @Input() maxSizeMB = 10;
  @Input() uploadUrl = '/api/documents';
  @Output() uploadComplete = new EventEmitter<DocumentInfo[]>();
  @Output() uploadError = new EventEmitter<string>();

  selectedFiles = signal<File[]>([]);
  uploading = signal(false);
  uploadProgress = signal(0);
  error = signal<string | null>(null);

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const files = Array.from(input.files);
    const validFiles: File[] = [];

    for (const file of files) {
      // Validate file size
      if (file.size > this.maxSizeMB * 1024 * 1024) {
        this.error.set(`File ${file.name} exceeds ${this.maxSizeMB}MB limit`);
        continue;
      }

      validFiles.push(file);
    }

    this.selectedFiles.set(validFiles);
    this.error.set(null);
  }

  removeFile(file: File) {
    this.selectedFiles.update(files => files.filter(f => f !== file));
  }

  uploadFiles() {
    const files = this.selectedFiles();
    if (files.length === 0) return;

    const formData = new FormData();
    files.forEach(file => formData.append('files', file));

    this.uploading.set(true);
    this.uploadProgress.set(0);
    this.error.set(null);

    this.http.post<DocumentInfo[]>(this.uploadUrl, formData, {
      reportProgress: true,
      observe: 'events'
    }).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress) {
          const progress = event.total
            ? Math.round((100 * event.loaded) / event.total)
            : 0;
          this.uploadProgress.set(progress);
        } else if (event.type === HttpEventType.Response) {
          this.uploading.set(false);
          this.selectedFiles.set([]);
          this.uploadComplete.emit(event.body!);
        }
      },
      error: (error) => {
        this.uploading.set(false);
        this.error.set('Upload failed: ' + (error.message || 'Unknown error'));
        this.uploadError.emit(error.message);
      }
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
```

**Drag-and-Drop File Upload:**

```typescript
// Install: npm install ngx-file-drop
// Or use custom directive:

@Directive({
  selector: '[appFileDrop]',
  standalone: true
})
export class FileDropDirective {
  @Output() filesDropped = new EventEmitter<File[]>();
  @HostBinding('class.file-over') fileOver = false;

  @HostListener('dragover', ['$event'])
  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.fileOver = true;
  }

  @HostListener('dragleave', ['$event'])
  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.fileOver = false;
  }

  @HostListener('drop', ['$event'])
  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.fileOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.filesDropped.emit(Array.from(files));
    }
  }
}

// Usage:
@Component({
  template: `
    <div
      appFileDrop
      (filesDropped)="onFilesDropped($event)"
      class="drop-zone"
    >
      <p>Drag and drop files here or click to browse</p>
      <input
        type="file"
        multiple
        (change)="onFileSelected($event)"
        hidden
        #fileInput
      />
      <button (click)="fileInput.click()">Browse</button>
    </div>
  `,
  styles: [`
    .drop-zone {
      border: 2px dashed #ccc;
      border-radius: 8px;
      padding: 40px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
    }
    .drop-zone.file-over {
      border-color: #2196F3;
      background-color: #E3F2FD;
    }
  `]
})
export class FileDropComponent {
  onFilesDropped(files: File[]) {
    console.log('Files dropped:', files);
    // Handle files
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.onFilesDropped(Array.from(input.files));
    }
  }
}
```

**File Upload Service:**

```typescript
@Injectable({ providedIn: 'root' })
export class FileUploadService {
  private http = inject(HttpClient);

  uploadDocument(file: File): Observable<DocumentInfo> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<DocumentInfo>('/api/documents', formData, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      map(event => {
        if (event.type === HttpEventType.UploadProgress) {
          const progress = event.total
            ? Math.round((100 * event.loaded) / event.total)
            : 0;
          return { type: 'progress', progress };
        } else if (event.type === HttpEventType.Response) {
          return { type: 'complete', data: event.body };
        }
        return { type: 'unknown' };
      }),
      filter(result => result.type !== 'unknown')
    );
  }

  uploadMultipleDocuments(files: File[]): Observable<DocumentInfo[]> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));

    return this.http.post<DocumentInfo[]>('/api/documents/batch', formData);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`/api/documents/${id}`);
  }
}
```

**Best Practices:**
1. Validate file types and sizes before upload
2. Show progress indicators for better UX
3. Handle errors gracefully with user-friendly messages
4. Support both click-to-browse and drag-and-drop
5. Allow file removal before upload
6. Disable upload button during upload
7. Use FormData for multipart/form-data uploads
8. Consider chunked uploads for large files (>100MB)

**Libraries:**
- `@iplab/ngx-file-upload` - Full-featured with reactive/template forms
- `ngx-file-drop` - Lightweight drag-and-drop
- Custom directives for simple use cases

**Sources:**
- [Angular File Upload Tutorial](https://www.djamware.com/post/685f4b8ee1a9ac448f9a759d/angular-file-upload-tutorial-with-draganddrop-and-progress-bar)
- [Custom File Uploader Angular 21](https://medium.com/@paul.pietzko/custom-file-uploader-angular-18-ca566131f128)
- [Angular File Upload Guide](https://blog.angular-university.io/angular-file-upload/)
- [ngx-file-drop npm](https://www.npmjs.com/package/ngx-file-drop)

---

### G. Error Handling and Interceptors

**Overview:**
HTTP interceptors provide centralized error handling, authentication, logging, and request/response transformation.

**Functional Interceptor (Recommended for Angular 21+):**

```typescript
// error.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = 'Bad Request: ' + (error.error?.message || 'Invalid data');
            break;
          case 401:
            errorMessage = 'Unauthorized: Please log in';
            // Optional: Clear localStorage and redirect
            localStorage.removeItem('authToken');
            router.navigate(['/login']);
            break;
          case 403:
            errorMessage = 'Forbidden: You do not have permission';
            break;
          case 404:
            errorMessage = 'Not Found: Resource does not exist';
            break;
          case 500:
            errorMessage = 'Server Error: Please try again later';
            break;
          default:
            errorMessage = `Error ${error.status}: ${error.statusText}`;
        }
      }

      console.error('HTTP Error:', errorMessage, error);

      // Optional: Show toast/snackbar notification
      // const snackBar = inject(MatSnackBar);
      // snackBar.open(errorMessage, 'Close', { duration: 5000 });

      return throwError(() => new Error(errorMessage));
    })
  );
};
```

**Authentication Interceptor:**

```typescript
// auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip adding token for public endpoints
  if (req.url.includes('/auth/login') || req.url.includes('/public')) {
    return next(req);
  }

  // Retrieve token from localStorage
  const token = localStorage.getItem('authToken');

  if (token) {
    // Clone request and add authorization header
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }

  return next(req);
};
```

**Logging Interceptor:**

```typescript
// logging.interceptor.ts
import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { tap } from 'rxjs';

export const loggingInterceptor: HttpInterceptorFn = (req, next) => {
  const started = Date.now();

  console.log(`🚀 Request: ${req.method} ${req.url}`);

  return next(req).pipe(
    tap({
      next: (event) => {
        if (event instanceof HttpResponse) {
          const elapsed = Date.now() - started;
          console.log(`✅ Response: ${req.method} ${req.url} (${elapsed}ms)`, event.status);
        }
      },
      error: (error) => {
        const elapsed = Date.now() - started;
        console.error(`❌ Error: ${req.method} ${req.url} (${elapsed}ms)`, error);
      }
    })
  );
};
```

**Retry Interceptor:**

```typescript
// retry.interceptor.ts
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { retry, timer } from 'rxjs';

export const retryInterceptor: HttpInterceptorFn = (req, next) => {
  // Only retry GET requests
  if (req.method !== 'GET') {
    return next(req);
  }

  return next(req).pipe(
    retry({
      count: 3,
      delay: (error: HttpErrorResponse, retryCount: number) => {
        // Exponential backoff: 1s, 2s, 4s
        const delay = Math.pow(2, retryCount) * 1000;
        console.log(`Retrying request (${retryCount}/3) after ${delay}ms...`);
        return timer(delay);
      },
      resetOnSuccess: true
    })
  );
};
```

**Configuration:**

```typescript
// app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { loggingInterceptor } from './core/interceptors/logging.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withInterceptors([
        loggingInterceptor,  // Order matters! Logging first
        authInterceptor,     // Then auth
        retryInterceptor,    // Then retry logic
        errorInterceptor     // Error handling last
      ])
    )
  ]
};
```

**Global Error Handler Service:**

```typescript
// global-error-handler.service.ts
import { ErrorHandler, Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private snackBar = inject(MatSnackBar);

  handleError(error: Error): void {
    console.error('Global Error:', error);

    // Show user-friendly message
    this.snackBar.open(
      'An unexpected error occurred. Please try again.',
      'Close',
      { duration: 5000, panelClass: ['error-snackbar'] }
    );

    // Optional: Send to error tracking service (Sentry, LogRocket, etc.)
    // this.errorTrackingService.logError(error);
  }
}

// Register in app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    { provide: ErrorHandler, useClass: GlobalErrorHandler }
  ]
};
```

**Best Practices:**
1. Use **functional interceptors** (Angular 14+) - more predictable than class-based
2. Order matters - apply interceptors in logical sequence
3. Always return `next(req)` or modified clone
4. Handle both client-side and server-side errors
5. Provide user-friendly error messages
6. Log errors for debugging
7. Consider retry logic for transient failures (network issues)
8. Clear sensitive data (tokens) on 401 errors
9. Use global error handler for uncaught exceptions

**Sources:**
- [Angular HTTP Interceptors Guide](https://angular.dev/guide/http/interceptors)
- [Implementing Angular 21 HTTP Interceptors](https://blogsoverflow.com/12026/implementing-angular-18-http-interceptors/)
- [Error Handling with Angular Interceptors](https://dev.to/cezar-plescan/error-handling-with-angular-interceptors-2548)
- [Using Interceptors in Angular](https://medium.com/@matheusluna96/using-interceptors-in-angular-a-complete-guide-5f1568e22c95)

---

### H. Component Separation (.ts / .html / .css)

**Overview:**
Angular 21 supports both inline and separate template/style files. The choice depends on component complexity and team preferences.

**When to Use Separate Files:**

✅ **Use separate .html/.css files when:**
- Template is >15 lines
- Complex HTML structure with multiple @if/@for blocks
- Significant styling (>20 lines of CSS)
- Multiple developers working on same component
- Need syntax highlighting/IntelliSense in templates
- Reusable styles across components

**When to Use Inline:**

✅ **Use inline template/styles when:**
- Simple components (<15 lines template, <10 lines styles)
- Quick prototypes
- Utility components (directives, pipes)
- No complex styling needed

**Recommended Structure:**

```
src/app/
├── features/
│   ├── chat/
│   │   ├── components/
│   │   │   ├── chat-message/
│   │   │   │   ├── chat-message.component.ts
│   │   │   │   ├── chat-message.component.html
│   │   │   │   ├── chat-message.component.css
│   │   │   │   └── chat-message.component.spec.ts
│   │   │   ├── chat-input/
│   │   │   │   ├── chat-input.component.ts
│   │   │   │   ├── chat-input.component.html
│   │   │   │   └── chat-input.component.css
│   │   ├── chat.component.ts
│   │   ├── chat.component.html
│   │   ├── chat.component.css
│   │   └── chat.routes.ts
│   │
│   └── admin/
│       ├── components/
│       │   ├── document-list/
│       │   └── document-upload/
│       ├── admin.component.ts
│       ├── admin.component.html
│       └── admin.component.css
│
├── shared/
│   ├── components/        # Reusable UI components
│   ├── directives/        # Reusable directives
│   ├── pipes/             # Reusable pipes
│   └── models/            # Shared interfaces/types
│
└── core/
    ├── services/          # Singleton services
    ├── interceptors/      # HTTP interceptors
    └── guards/            # Route guards
```

**Separate Files Example:**

```typescript
// chat.component.ts
@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    ChatMessageComponent,
    ChatInputComponent
  ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']  // Note: styleUrls (plural)
})
export class ChatComponent {
  messages = signal<Message[]>([]);
  isStreaming = signal(false);

  // Component logic...
}
```

```html
<!-- chat.component.html -->
<div class="chat-container">
  <div class="messages-list">
    @for (msg of messages(); track msg.id) {
      <app-chat-message
        [message]="msg"
        [isStreaming]="isStreaming() && $last"
      />
    }
  </div>

  <app-chat-input
    [disabled]="isStreaming()"
    (messageSent)="sendMessage($event)"
  />
</div>
```

```css
/* chat.component.css */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f5f5;
}
```

**Inline Example (for simple components):**

```typescript
@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <div class="spinner">
      <mat-spinner diameter="40"></mat-spinner>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [`
    .spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() message = 'Loading...';
}
```

**Best Practices:**

1. **Consistency**: Choose one approach per project and stick with it
2. **Naming**: Use kebab-case for files (`chat-message.component.ts`)
3. **Colocation**: Keep related files together in same directory
4. **Imports**: Always list all dependencies in `imports` array for standalone
5. **Styles Scope**: Component styles are scoped by default (no CSS leakage)
6. **Shared Styles**: Use global styles in `styles.css` for app-wide rules
7. **SCSS**: Consider SCSS for variables, mixins, nesting:

```typescript
@Component({
  styleUrls: ['./chat.component.scss']  // .scss instead of .css
})
```

**Component Size Guidelines:**

- ✅ **Small**: <100 lines (.ts), <50 lines (.html) - Single file OK
- ✅ **Medium**: 100-300 lines - Separate files recommended
- ⚠️ **Large**: 300+ lines - Consider breaking into sub-components

**Angular CLI Commands:**

```bash
# Generate component with separate files (default)
ng generate component features/chat

# Generate component with inline template
ng generate component features/loading --inline-template

# Generate component with inline styles
ng generate component features/badge --inline-style

# Generate component with both inline
ng generate component features/icon --inline-template --inline-style
```

**Key Insight:** For the HR Assistant MVP, use **separate files** for feature components (chat, admin) and **inline templates** for small utility components (spinners, badges).

---

## Summary of Recommendations

### 1. UI Component Library: **Angular Material**
- Smaller bundle size (~500KB vs ~1MB)
- Official Google support, Angular 21 ready
- Perfect for MVP scope
- SSE compatible
- Enhance with `ngx-file-drop` for drag-and-drop uploads

### 2. State Management: **Hybrid (Services + RxJS → Signals)**
- Services with RxJS for async operations (HTTP, SSE)
- Convert to Signals at UI boundary with `toSignal()`
- Simple localStorage service for persistence
- No NgRx/Akita overhead
- Scales to medium complexity

### 3. Key Angular 21 Patterns:
- ✅ Use **standalone components** (Angular 21+ default)
- ✅ Lazy load routes with `loadComponent`
- ✅ **Signals** for sync state, **RxJS** for async operations
- ✅ **Functional interceptors** for error handling
- ✅ **NgZone.run()** for SSE change detection
- ✅ **Separate .html/.css** files for feature components
- ✅ localStorage service with try-catch error handling
- ✅ Drag-and-drop file upload with validation

---

## Next Steps

1. **Setup Project:**
   ```bash
   ng new hr-assistant-frontend --standalone --routing --style=css
   npm install @angular/material
   npm install ngx-file-drop
   ```

2. **Generate Core Structure:**
   ```bash
   ng generate component features/chat --standalone
   ng generate component features/admin --standalone
   ng generate service core/services/sse --skip-tests
   ng generate service core/services/storage --skip-tests
   ng generate interceptor core/interceptors/error --functional
   ```

3. **Implement in Order:**
   - Basic routing with lazy loading
   - SSE service for chat streaming
   - localStorage service for chat history
   - Chat UI with Angular Material components
   - File upload with drag-and-drop
   - Admin dashboard for document management
   - Error handling interceptors
   - Responsive design with Angular Flex Layout or CSS Grid

4. **Testing Strategy:**
   - Unit tests for services (Jasmine/Karma or Jest)
   - Component tests with TestBed
   - E2E tests with Playwright or Cypress
   - SSE integration tests with mock EventSource

---

## Sources

### UI Component Library Research
- [PrimeNG for Angular 21](https://www.primefaces.org/blog/primeng-for-angular-18/)
- [PrimeNG vs Angular Material 2025](https://developerchandan.medium.com/primeng-vs-angular-material-in-2025-which-ui-library-is-better-for-angular-projects-d98aef4c5465)
- [Angular Material vs PrimeNG – Enterprise Comparison](https://www.infragistics.com/blogs/angular-material-vs-primeng/)
- [ngx-sse-client npm](https://www.npmjs.com/package/ngx-sse-client)
- [Stream Chat Angular SDK](https://getstream.io/chat/sdk/angular/)
- [Syncfusion Angular Chat UI](https://www.syncfusion.com/angular-components/angular-chat-ui)
- [7 Best Angular UI Libraries 2026](https://www.thefrontendcompany.com/posts/angular-ui-library)

### State Management Research
- [Angular State Management: RxJS vs Signals](https://medium.com/@thecodingdon/angular-state-management-best-practices-with-rxjs-vs-signals-db629de24888)
- [From RxJS to Signals: Future of State Management](https://hackernoon.com/from-rxjs-to-signals-the-future-of-state-management-in-angular)
- [Will Signals Replace RxJS?](https://angularexperts.io/blog/signals-vs-rxjs/)
- [Angular State Management for 2025](https://nx.dev/blog/angular-state-management-2025)
- [State Management Comparison: NgRx vs Signals vs RxJS](https://medium.com/@roshannavale7/state-management-in-angular-ngrx-vs-signals-vs-rxjs-172869302a9a)

### Angular 21 Best Practices
- [Mastering Angular 21: Standalone Components & Routing](https://medium.com/@aalam-info-solutions-llp/mastering-angular-18-comprehensive-guide-to-standalone-components-routing-lazy-loading-and-d70bf8ea39ec)
- [Routing and Lazy Loading with Standalone Components](https://www.angulararchitects.io/en/blog/routing-and-lazy-loading-with-standalone-components/)
- [Lazy Load Standalone Components with loadComponent](https://ultimatecourses.com/blog/lazy-load-standalone-components-via-load-component)
- [Angular Lazy Loading Guide](https://zerotomastery.io/blog/angular-lazy-loading/)

### SSE Integration
- [Implementing Server-Sent Events in Angular](https://medium.com/@andrewkoliaka/implementing-server-sent-events-in-angular-a5e40617cb78)
- [Server-Sent Events with NestJS and Angular](https://medium.com/@piotrkorowicki/server-sent-events-sse-with-nestjs-and-angular-d90635783d8c)
- [Angular and Server Sent Events](https://dev.to/bartoszgajda55/angular-and-server-sent-events-sse-58de)
- [Leveraging SSE with Angular and Node.js](https://www.c-sharpcorner.com/article/leveraging-server-sent-events-sse-with-angular-and-node-js/)

### File Upload
- [Angular File Upload Tutorial with Drag-and-Drop](https://www.djamware.com/post/685f4b8ee1a9ac448f9a759d/angular-file-upload-tutorial-with-draganddrop-and-progress-bar)
- [Custom File Uploader Angular 21](https://medium.com/@paul.pietzko/custom-file-uploader-angular-18-ca566131f128)
- [Angular File Upload - Complete Guide](https://blog.angular-university.io/angular-file-upload/)
- [ngx-file-drop npm](https://www.npmjs.com/package/ngx-file-drop)

### Error Handling & Interceptors
- [Angular HTTP Interceptors Official Guide](https://angular.dev/guide/http/interceptors)
- [Implementing Angular 21 HTTP Interceptors](https://blogsoverflow.com/12026/implementing-angular-18-http-interceptors/)
- [Error Handling with Angular Interceptors](https://dev.to/cezar-plescan/error-handling-with-angular-interceptors-2548)
- [Using Interceptors in Angular: Complete Guide](https://medium.com/@matheusluna96/using-interceptors-in-angular-a-complete-guide-5f1568e22c95)
- [Angular HTTP Interceptor Token Tutorial](https://angular.love/how-to-implement-automatic-token-insertion-in-requests-using-http-interceptor-angular-tutorials/)

---

**Document Version**: 1.0
**Last Updated**: 2026-01-21
**Angular Version**: 18
**Next Review**: After Phase 1 MVP completion
