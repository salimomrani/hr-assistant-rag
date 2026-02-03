import { Component, input, output, effect, viewChild, ElementRef, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { MarkdownComponent } from 'ngx-markdown';
import { TooltipModule } from 'primeng/tooltip';
import { ConversationMessage } from '../../../../core/models';
import { SourceListComponent } from '../source-list/source-list.component';

/**
 * Message List Component - Displays conversation history with elegant message bubbles
 * Features auto-scroll, source citations, and streaming indicators
 */
@Component({
  selector: 'app-message-list',
  imports: [CommonModule, ScrollPanelModule, SourceListComponent, MarkdownComponent, TooltipModule],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.css'
})
export class MessageListComponent implements AfterViewInit {
  // Input: array of conversation messages
  messages = input<ConversationMessage[]>([]);

  // Input: current streaming content (partial response)
  streamingContent = input<string>('');

  // Input: loading state
  isLoading = input<boolean>(false);

  // Input: pending question being processed (shown during loading)
  pendingQuestion = input<string>('');

  // Output: emit when a suggested question is clicked
  suggestionClicked = output<string>();

  // Suggested questions for empty state
  readonly suggestedQuestions = [
    'Combien de jours de congés ai-je droit ?',
    'Comment poser une demande de télétravail ?',
    'Quels sont les avantages sociaux ?',
    'Comment fonctionne le remboursement des frais ?'
  ];

  // Reference to scroll panel for auto-scroll
  private scrollPanel = viewChild<ElementRef>('scrollContainer');

  ngAfterViewInit() {
    // Scroll to bottom when component initializes
    this.scrollToBottom();
  }

  constructor() {
    // Auto-scroll when new messages arrive or streaming content updates
    effect(() => {
      const msgs = this.messages();
      const streaming = this.streamingContent();

      if (msgs.length > 0 || streaming) {
        setTimeout(() => this.scrollToBottom(), 100);
      }
    });
  }

  /**
   * Scroll to the bottom of the message list
   */
  private scrollToBottom(): void {
    const container = this.scrollPanel()?.nativeElement;
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }

  /**
   * Format timestamp for display
   */
  formatTime(date: Date): string {
    return new Date(date).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format date for display
   */
  formatDate(date: Date): string {
    const d = new Date(date);
    const today = new Date();

    if (d.toDateString() === today.toDateString()) {
      return "Aujourd'hui";
    }

    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (d.toDateString() === yesterday.toDateString()) {
      return 'Hier';
    }

    return d.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: d.getFullYear() !== today.getFullYear() ? 'numeric' : undefined
    });
  }

  /**
   * Track message by ID for ngFor performance
   */
  trackByMessageId(index: number, message: ConversationMessage): string {
    return message.id;
  }

  // Track which message was just copied (for feedback)
  copiedMessageId = signal<string | null>(null);

  /**
   * Copy message content to clipboard
   */
  async copyToClipboard(content: string, messageId: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(content);
      this.copiedMessageId.set(messageId);

      // Reset after 2 seconds
      setTimeout(() => {
        if (this.copiedMessageId() === messageId) {
          this.copiedMessageId.set(null);
        }
      }, 2000);
    } catch (err) {
      console.error('Failed to copy text:', err);
    }
  }
}
