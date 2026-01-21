import { Injectable, inject, signal, computed } from '@angular/core';
import { ApiService } from './api.service';
import { Document, DocumentStatus, UploadProgress, UploadStatus } from '../models';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

/**
 * Document Service - Manages document operations and state
 * Provides CRUD operations for documents with reactive state management
 */
@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private api = inject(ApiService);

  // Reactive state using signals
  private documentsSignal = signal<Document[]>([]);
  private loadingSignal = signal<boolean>(false);
  private uploadProgressSignal = signal<UploadProgress | null>(null);

  // Public readonly signals
  documents = this.documentsSignal.asReadonly();
  loading = this.loadingSignal.asReadonly();
  uploadProgress = this.uploadProgressSignal.asReadonly();

  // Computed signals
  documentCount = computed(() => this.documents().length);
  indexedCount = computed(() =>
    this.documents().filter(d => d.status === DocumentStatus.INDEXED).length
  );
  pendingCount = computed(() =>
    this.documents().filter(d => d.status === DocumentStatus.PENDING).length
  );
  failedCount = computed(() =>
    this.documents().filter(d => d.status === DocumentStatus.FAILED).length
  );

  /**
   * Load all documents from the backend
   * @returns Observable that completes when documents are loaded
   */
  loadDocuments(): Observable<Document[]> {
    this.loadingSignal.set(true);

    return this.api.getDocuments().pipe(
      tap(documents => {
        // Convert ISO string timestamps to Date objects
        const parsedDocuments = documents.map(doc => ({
          ...doc,
          uploadTimestamp: new Date(doc.uploadTimestamp)
        }));
        this.documentsSignal.set(parsedDocuments);
        this.loadingSignal.set(false);
      }),
      catchError(error => {
        this.loadingSignal.set(false);
        throw error;
      })
    );
  }

  /**
   * Upload a new document file
   * @param file The file to upload (PDF or TXT, max 10MB)
   * @returns Observable with the created document
   */
  uploadDocument(file: File): Observable<Document> {
    // Initialize upload progress
    this.uploadProgressSignal.set({
      filename: file.name,
      percentComplete: 0,
      status: UploadStatus.UPLOADING,
      bytesTransferred: 0,
      totalBytes: file.size
    });

    return this.api.uploadDocument(file).pipe(
      tap(document => {
        // Update upload progress to complete
        this.uploadProgressSignal.set({
          filename: file.name,
          percentComplete: 100,
          status: UploadStatus.COMPLETE,
          bytesTransferred: file.size,
          totalBytes: file.size
        });

        // Add new document to list
        const parsedDocument = {
          ...document,
          uploadTimestamp: new Date(document.uploadTimestamp)
        };
        this.documentsSignal.update(docs => [...docs, parsedDocument]);

        // Clear upload progress after short delay
        setTimeout(() => this.uploadProgressSignal.set(null), 2000);
      }),
      catchError(error => {
        // Update upload progress to error
        this.uploadProgressSignal.set({
          filename: file.name,
          percentComplete: 0,
          status: UploadStatus.ERROR,
          bytesTransferred: 0,
          totalBytes: file.size,
          errorMessage: error.message || 'Upload failed'
        });

        // Clear upload progress after delay
        setTimeout(() => this.uploadProgressSignal.set(null), 5000);
        throw error;
      })
    );
  }

  /**
   * Delete a document by ID
   * @param documentId The document ID to delete
   * @returns Observable that completes when delete is successful
   */
  deleteDocument(documentId: string): Observable<void> {
    return this.api.deleteDocument(documentId).pipe(
      tap(() => {
        // Remove document from list
        this.documentsSignal.update(docs =>
          docs.filter(d => d.id !== documentId)
        );
      })
    );
  }

  /**
   * Get a specific document by ID
   * @param documentId The document ID
   * @returns The document or undefined if not found
   */
  getDocumentById(documentId: string): Document | undefined {
    return this.documents().find(d => d.id === documentId);
  }
}
