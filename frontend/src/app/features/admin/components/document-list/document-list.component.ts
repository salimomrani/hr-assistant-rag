import { Component } from '@angular/core';

/**
 * Document List Component - Document management interface (Placeholder)
 * Will be implemented in Phase 4+
 */
@Component({
  selector: 'app-document-list',
  imports: [],
  template: `
    <div class="placeholder-container">
      <h2>Admin Interface</h2>
      <p>Coming soon in Phase 4...</p>
    </div>
  `,
  styles: [`
    .placeholder-container {
      padding: 3rem;
      text-align: center;
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }
    h2 {
      font-family: 'Cormorant Garamond', serif;
      font-size: 2rem;
      margin: 0 0 1rem 0;
      color: #1a202c;
    }
    p {
      font-family: 'DM Sans', sans-serif;
      color: #64748b;
      margin: 0;
    }
  `]
})
export class DocumentListComponent {}
