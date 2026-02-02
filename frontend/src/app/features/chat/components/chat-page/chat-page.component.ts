import { Component, signal } from '@angular/core';
import { ChatSidebarComponent } from '../chat-sidebar/chat-sidebar.component';
import { ChatContainerComponent } from '../chat-container/chat-container.component';

/**
 * Chat Page Component - Layout wrapper with sidebar and chat area
 */
@Component({
  selector: 'app-chat-page',
  imports: [
    ChatSidebarComponent,
    ChatContainerComponent
  ],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.css'
})
export class ChatPageComponent {
  // Mobile sidebar toggle
  sidebarOpen = signal<boolean>(false);

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
