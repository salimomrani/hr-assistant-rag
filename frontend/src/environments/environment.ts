export const environment = {
  production: false,
  apiUrl: '/api', // Uses Angular proxy to localhost:8080
  streamUrl: 'http://localhost:8080/api', // Direct URL for SSE (bypasses proxy buffering)
  localStoragePrefix: 'hr-assistant-',
  maxConversationMessages: 50,
  maxFileSizeMB: 10
};
