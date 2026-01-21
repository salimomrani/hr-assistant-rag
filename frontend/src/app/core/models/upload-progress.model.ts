/**
 * Upload Status enum - Current status of a file upload operation
 */
export enum UploadStatus {
  UPLOADING = 'uploading',
  PROCESSING = 'processing', // Backend is processing
  COMPLETE = 'complete',
  ERROR = 'error'
}

/**
 * Upload Progress - Temporary state tracking file upload completion
 * Lifecycle: Created on file selection → Updated during upload → Converted to Document on completion → Discarded
 */
export interface UploadProgress {
  filename: string;         // File being uploaded
  percentComplete: number;  // Upload percentage (0-100)
  status: UploadStatus;     // Current upload status
  bytesTransferred: number; // Bytes uploaded so far
  totalBytes: number;       // Total file size
  errorMessage?: string;    // Error if status is ERROR
}
