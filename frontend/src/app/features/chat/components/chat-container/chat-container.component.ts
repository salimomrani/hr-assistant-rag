import { Component, inject, signal, viewChild } from '@angular/core';
import { MessageListComponent } from '../message-list/message-list.component';
import { MessageInputComponent } from '../message-input/message-input.component';
import { DocumentSelectorComponent } from '../document-selector/document-selector.component';
import { ConversationService } from '../../../../core/services/conversation.service';
import { ApiService } from '../../../../core/services/api.service';
import { Question, Answer, SourceDocumentReference } from '../../../../core/models';

/**
 * Chat Container Component - Main chat interface
 * Orchestrates message display, input, and conversation management
 * Design inspired by Google Gemini - minimal, clean, flat
 */
@Component({
  selector: 'app-chat-container',
  imports: [
    MessageListComponent,
    MessageInputComponent,
    DocumentSelectorComponent
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

  // Selected document IDs for filtering RAG search
  selectedDocumentIds = signal<string[]>([]);

  // Expose conversation service signals
  messages = this.conversationService.messages;

  /**
   * Handle document selection change from DocumentSelectorComponent
   */
  onDocumentSelectionChanged(documentIds: string[]): void {
    this.selectedDocumentIds.set(documentIds);
  }

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

    // Get selected document IDs (empty array means search all)
    const documentIds = this.selectedDocumentIds();

    // Call SSE streaming API with optional document filter
    this.apiService.chatStream(questionText, documentIds.length > 0 ? documentIds : undefined).subscribe({
      next: (chunk: string) => {
        // Update streaming content with each chunk
        this.streamingContent.update(current => current + chunk);
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

        // Backend handles all formatting (spacing, sources, etc.)
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
