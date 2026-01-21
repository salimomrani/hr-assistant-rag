/**
 * Environment configuration interface
 * Used for type-safe access to environment variables
 */
export interface Environment {
  production: boolean;
  apiUrl: string;
  localStoragePrefix: string;
  maxConversationMessages: number;
  maxFileSizeMB: number;
}
