import { ConversationMessage } from './conversation-message.model';

/**
 * Conversation - represents a chat session with multiple messages
 */
export interface Conversation {
  id: string;
  title: string;
  messages: ConversationMessage[];
  createdAt: Date;
  updatedAt: Date;
}
