import { Component, inject, output } from '@angular/core';
import { ConversationService } from '../../../../core/services/conversation.service';
import { Conversation } from '../../../../core/models';

/**
 * Chat Sidebar Component - displays conversation list like ChatGPT
 */
@Component({
  selector: 'app-chat-sidebar',
  templateUrl: './chat-sidebar.component.html',
  styleUrl: './chat-sidebar.component.css'
})
export class ChatSidebarComponent {
  private conversationService = inject(ConversationService);

  // Expose service signals
  conversations = this.conversationService.conversations;
  activeConversationId = this.conversationService.activeConversationId;

  // Disable new chat button if most recent conversation is empty
  canCreateNewChat = this.conversationService.canCreateNewConversation;

  // Output event for mobile sidebar close
  sidebarClosed = output<void>();

  /**
   * Create a new conversation only if the most recent one has messages
   */
  onNewChat(): void {
    if (this.conversationService.canCreateNewConversation()) {
      this.conversationService.createConversation();
    }
  }

  /**
   * Switch to a conversation
   */
  onSelectConversation(conversation: Conversation): void {
    this.conversationService.switchConversation(conversation.id);
  }

  /**
   * Delete a conversation
   */
  onDeleteConversation(event: Event, conversation: Conversation): void {
    event.stopPropagation();
    this.conversationService.deleteConversation(conversation.id);
  }

  /**
   * Check if a conversation is active
   */
  isActive(conversation: Conversation): boolean {
    return conversation.id === this.activeConversationId();
  }
}
