import { Injectable, inject, signal, computed } from '@angular/core';
import { StorageService } from './storage.service';
import { ConversationMessage, Question, Answer, Conversation } from '../models';
import { environment } from '../../../environments/environment';

/**
 * Conversation Service - Manages multiple chat sessions with localStorage persistence
 * Supports creating, switching, and deleting conversations
 */
@Injectable({
  providedIn: 'root'
})
export class ConversationService {
  private storage = inject(StorageService);
  private readonly STORAGE_KEY = 'conversations';
  private readonly ACTIVE_KEY = 'activeConversationId';
  private readonly MAX_MESSAGES_PER_CONVERSATION = environment.maxConversationMessages;
  private readonly MAX_TITLE_LENGTH = 50;

  // Reactive state using signals
  private conversationsSignal = signal<Conversation[]>([]);
  private activeConversationIdSignal = signal<string | null>(null);

  // Public readonly signals
  conversations = this.conversationsSignal.asReadonly();
  activeConversationId = this.activeConversationIdSignal.asReadonly();

  // Computed signals
  activeConversation = computed(() => {
    const id = this.activeConversationIdSignal();
    if (!id) return null;
    return this.conversationsSignal().find(c => c.id === id) || null;
  });

  messages = computed(() => {
    const conversation = this.activeConversation();
    return conversation?.messages || [];
  });

  messageCount = computed(() => this.messages().length);
  isEmpty = computed(() => this.messages().length === 0);
  hasConversations = computed(() => this.conversationsSignal().length > 0);

  // Check if the most recent conversation (first in list) has messages
  canCreateNewConversation = computed(() => {
    const conversations = this.conversationsSignal();
    if (conversations.length === 0) return true;
    return conversations[0].messages.length > 0;
  });

  constructor() {
    this.loadFromStorage();
  }

  /**
   * Create a new conversation and set it as active
   * @returns The new conversation ID
   */
  createConversation(): string {
    const newConversation: Conversation = {
      id: crypto.randomUUID(),
      title: 'Nouvelle conversation',
      messages: [],
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const conversations = [newConversation, ...this.conversationsSignal()];
    this.conversationsSignal.set(conversations);
    this.activeConversationIdSignal.set(newConversation.id);
    this.saveToStorage();

    return newConversation.id;
  }

  /**
   * Switch to an existing conversation
   * @param conversationId The conversation ID to switch to
   */
  switchConversation(conversationId: string): void {
    const conversation = this.conversationsSignal().find(c => c.id === conversationId);
    if (conversation) {
      this.activeConversationIdSignal.set(conversationId);
      this.storage.set(this.ACTIVE_KEY, conversationId);
    }
  }

  /**
   * Delete a conversation
   * @param conversationId The conversation ID to delete
   */
  deleteConversation(conversationId: string): void {
    const conversations = this.conversationsSignal().filter(c => c.id !== conversationId);
    this.conversationsSignal.set(conversations);

    // If we deleted the active conversation, switch to another or create new
    if (this.activeConversationIdSignal() === conversationId) {
      if (conversations.length > 0) {
        this.activeConversationIdSignal.set(conversations[0].id);
      } else {
        this.createConversation();
      }
    }

    this.saveToStorage();
  }

  /**
   * Add a new question-answer pair to the active conversation
   * Automatically persists to localStorage and applies FIFO eviction
   * @param question The user's question
   * @param answer The system's answer
   */
  addMessage(question: Question, answer: Answer): void {
    const activeId = this.activeConversationIdSignal();
    if (!activeId) {
      // Create a new conversation if none exists
      this.createConversation();
    }

    const message: ConversationMessage = {
      id: crypto.randomUUID(),
      question,
      answer,
      timestamp: question.timestamp
    };

    const conversations = this.conversationsSignal().map(conv => {
      if (conv.id === this.activeConversationIdSignal()) {
        let updatedMessages = [...conv.messages, message];

        // Apply FIFO eviction if limit exceeded
        if (updatedMessages.length > this.MAX_MESSAGES_PER_CONVERSATION) {
          const excess = updatedMessages.length - this.MAX_MESSAGES_PER_CONVERSATION;
          updatedMessages = updatedMessages.slice(excess);
        }

        // Update title from first question if it's the default
        let title = conv.title;
        if (conv.messages.length === 0) {
          title = this.generateTitle(question.text);
        }

        return {
          ...conv,
          title,
          messages: updatedMessages,
          updatedAt: new Date()
        };
      }
      return conv;
    });

    this.conversationsSignal.set(conversations);
    this.saveToStorage();
  }

  /**
   * Clear all conversation history (for migration or reset)
   */
  clearAllHistory(): void {
    this.conversationsSignal.set([]);
    this.activeConversationIdSignal.set(null);
    this.storage.remove(this.STORAGE_KEY);
    this.storage.remove(this.ACTIVE_KEY);
    this.createConversation();
  }

  /**
   * Generate a title from the question text
   */
  private generateTitle(questionText: string): string {
    const trimmed = questionText.trim();
    if (trimmed.length <= this.MAX_TITLE_LENGTH) {
      return trimmed;
    }
    return trimmed.substring(0, this.MAX_TITLE_LENGTH - 3) + '...';
  }

  /**
   * Load conversations from localStorage
   */
  private loadFromStorage(): void {
    const stored = this.storage.get<Conversation[]>(this.STORAGE_KEY);
    const activeId = this.storage.get<string>(this.ACTIVE_KEY);

    if (stored && Array.isArray(stored) && stored.length > 0) {
      // Convert ISO string timestamps back to Date objects
      const conversations = stored.map(conv => ({
        ...conv,
        createdAt: new Date(conv.createdAt),
        updatedAt: new Date(conv.updatedAt),
        messages: conv.messages.map(msg => ({
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
        }))
      }));

      this.conversationsSignal.set(conversations);

      // Restore active conversation or use the first one
      if (activeId && conversations.find(c => c.id === activeId)) {
        this.activeConversationIdSignal.set(activeId);
      } else {
        this.activeConversationIdSignal.set(conversations[0].id);
      }
    } else {
      // No stored conversations, create a new one
      this.createConversation();
    }
  }

  /**
   * Save conversations to localStorage
   */
  private saveToStorage(): void {
    this.storage.set(this.STORAGE_KEY, this.conversationsSignal());
    const activeId = this.activeConversationIdSignal();
    if (activeId) {
      this.storage.set(this.ACTIVE_KEY, activeId);
    }
  }
}
