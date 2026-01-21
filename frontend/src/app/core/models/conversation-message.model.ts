import { Question } from './question.model';
import { Answer } from './answer.model';

/**
 * Conversation Message - A single question-answer pair in the chat history
 * Storage: Persisted in browser localStorage (max 50 messages, FIFO eviction)
 */
export interface ConversationMessage {
  id: string;             // Unique identifier (UUID)
  question: Question;     // The user's question
  answer: Answer;         // The system's answer
  timestamp: Date;        // Message pair timestamp (inherited from question)
}
