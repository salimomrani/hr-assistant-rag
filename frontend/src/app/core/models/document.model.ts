/**
 * Document Status enum - Indexing status of an uploaded document
 */
export enum DocumentStatus {
  PENDING = 'pending',   // Being indexed
  INDEXED = 'indexed',   // Ready for use
  FAILED = 'failed'      // Indexing error occurred
}

/**
 * Document model - An uploaded file (PDF or TXT) stored in the backend system
 */
export interface Document {
  id: string;                       // Unique identifier from backend
  filename: string;                 // Display name (editable)
  fileType: 'PDF' | 'TXT';         // File type enum
  fileSizeBytes: number;            // File size in bytes
  status: DocumentStatus;           // Indexing status
  uploadTimestamp: Date;            // When document was uploaded
  failureReason?: string;           // Error message if status is FAILED
}
