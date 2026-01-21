/**
 * Question model - A text query submitted by a user through the chat interface
 */
export interface Question {
  text: string;           // The question text (validated: non-empty, max 1000 chars)
  timestamp: Date;        // When the question was asked
}
