import { Component, inject, signal, viewChild } from '@angular/core';
import { CardModule } from 'primeng/card';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageInputComponent } from '../message-input/message-input.component';
import { ErrorMessageComponent } from '../../../../shared/components/error-message/error-message.component';
import { ConversationService } from '../../../../core/services/conversation.service';
import { ApiService } from '../../../../core/services/api.service';
import { Question, Answer, SourceDocumentReference } from '../../../../core/models';

/**
 * Chat Container Component - Main chat interface
 * Orchestrates message display, input, and conversation management
 */
@Component({
  selector: 'app-chat-container',
  imports: [
    CardModule,
    MessageListComponent,
    MessageInputComponent,
    ErrorMessageComponent
  ],
  templateUrl: './chat-container.component.html',
  styleUrl: './chat-container.component.css'
})
export class ChatContainerComponent {
  private conversationService = inject(ConversationService);
  private apiService = inject(ApiService);

  // Reference to message input for controlling disabled state
  private messageInput = viewChild(MessageInputComponent);

  // Signals for UI state
  isLoading = signal<boolean>(false);
  streamingContent = signal<string>('');
  errorMessage = signal<string>('');

  // Expose conversation service signals
  messages = this.conversationService.messages;

  /**
   * Handle question submission
   */
  onQuestionSubmitted(questionText: string): void {
    if (!questionText.trim() || this.isLoading()) {
      return;
    }

    // Create question object
    const question: Question = {
      text: questionText,
      timestamp: new Date()
    };

    // Set loading state
    this.isLoading.set(true);
    this.streamingContent.set('');
    this.errorMessage.set('');
    this.messageInput()?.setDisabled(true);

    // Call SSE streaming API
    this.apiService.chatStream(questionText).subscribe({
      next: (chunk: string) => {
        // Update streaming content with each chunk
        const currentContent = this.streamingContent();
        this.streamingContent.set(currentContent + chunk);
      },
      error: (error) => {
        // Handle error
        this.isLoading.set(false);
        this.streamingContent.set('');
        this.messageInput()?.setDisabled(false);

        const errorMsg = error.message || 'Une erreur est survenue. Veuillez réessayer.';
        this.errorMessage.set(errorMsg);

        // Clear error after 5 seconds
        setTimeout(() => this.errorMessage.set(''), 5000);
      },
      complete: () => {
        // When streaming completes, save the Q&A pair
        const finalContent = this.streamingContent();

        // TODO: Parse sources from final SSE event
        // For now, using empty sources array
        const answer: Answer = {
          content: finalContent,
          sources: [] as SourceDocumentReference[],
          timestamp: new Date(),
          isStreaming: false
        };

        // Add to conversation history
        this.conversationService.addMessage(question, answer);

        // Reset state
        this.isLoading.set(false);
        this.streamingContent.set('');
        this.messageInput()?.setDisabled(false);
      }
    });
  }

  /**
   * Handle error message close
   */
  onErrorClosed(): void {
    this.errorMessage.set('');
  }
}
