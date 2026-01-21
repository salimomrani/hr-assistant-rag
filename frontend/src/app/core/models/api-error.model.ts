/**
 * API Error - Standard error response from the backend API
 *
 * Common HTTP status codes:
 * - 400: Bad Request (invalid file, question too long)
 * - 404: Not Found (document doesn't exist)
 * - 413: Payload Too Large (file > 10MB)
 * - 500: Internal Server Error (backend failure)
 * - 503: Service Unavailable (backend down, Ollama unavailable)
 */
export interface ApiError {
  status: number;         // HTTP status code
  message: string;        // User-friendly error message
  details?: string;       // Technical details (optional)
}
