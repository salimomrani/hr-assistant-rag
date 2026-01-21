import { Routes } from '@angular/router';

/**
 * Application routes with lazy-loaded feature modules
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: '/chat',
    pathMatch: 'full'
  },
  {
    path: 'chat',
    loadComponent: () =>
      import('./features/chat/components/chat-container/chat-container.component').then(
        m => m.ChatContainerComponent
      )
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./features/admin/components/document-list/document-list.component').then(
        m => m.DocumentListComponent
      )
  },
  {
    path: '**',
    redirectTo: '/chat'
  }
];
