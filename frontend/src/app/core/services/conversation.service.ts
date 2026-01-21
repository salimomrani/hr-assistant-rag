import { Injectable, inject, signal, computed } from '@angular/core';
import { StorageService } from './storage.service';
import { ConversationMessage, Question, Answer } from '../models';
import { environment } from '../../../environments/environment';

/**
 * Conversation Service - Manages chat history with localStorage persistence
 * Implements FIFO eviction when max message limit (50) is exceeded
 */
@Injectable({
  providedIn: 'root'
})
export class ConversationService {
  private storage = inject(StorageService);
  private readonly STORAGE_KEY = 'conversations';
  private readonly MAX_MESSAGES = environment.maxConversationMessages;

  // Reactive state using signals
  private messagesSignal = signal<ConversationMessage[]>([]);

  // Public readonly signals
  messages = this.messagesSignal.asReadonly();
  messageCount = computed(() => this.messages().length);
  isEmpty = computed(() => this.messages().length === 0);

  constructor() {
    this.loadFromStorage();
  }

  /**
   * Add a new question-answer pair to the conversation history
   * Automatically persists to localStorage and applies FIFO eviction
   * @param question The user's question
   * @param answer The system's answer
   */
  addMessage(question: Question, answer: Answer): void {
    const message: ConversationMessage = {
      id: crypto.randomUUID(),
      question,
      answer,
      timestamp: question.timestamp
    };

    const currentMessages = this.messagesSignal();
    let updatedMessages = [...currentMessages, message];

    // Apply FIFO eviction if limit exceeded
    if (updatedMessages.length > this.MAX_MESSAGES) {
      const excess = updatedMessages.length - this.MAX_MESSAGES;
      updatedMessages = updatedMessages.slice(excess);
    }

    this.messagesSignal.set(updatedMessages);
    this.saveToStorage(updatedMessages);
  }

  /**
   * Clear all conversation history
   */
  clearHistory(): void {
    this.messagesSignal.set([]);
    this.storage.remove(this.STORAGE_KEY);
  }

  /**
   * Get a specific message by ID
   * @param messageId The message ID
   * @returns The message or undefined if not found
   */
  getMessageById(messageId: string): ConversationMessage | undefined {
    return this.messages().find(m => m.id === messageId);
  }

  /**
   * Load conversation history from localStorage
   */
  private loadFromStorage(): void {
    const stored = this.storage.get<ConversationMessage[]>(this.STORAGE_KEY);

    if (stored && Array.isArray(stored)) {
      // Convert ISO string timestamps back to Date objects
      const messages = stored.map(msg => ({
        ...msg,
        timestamp: new Date(msg.timestamp),
        question: {
          ...msg.question,
          timestamp: new Date(msg.question.timestamp)
        },
        answer: {
          ...msg.answer,
          timestamp: new Date(msg.answer.timestamp)
        }
      }));

      this.messagesSignal.set(messages);
    }
  }

  /**
   * Save conversation history to localStorage
   */
  private saveToStorage(messages: ConversationMessage[]): void {
    this.storage.set(this.STORAGE_KEY, messages);
  }
}
