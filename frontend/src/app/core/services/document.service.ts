import { Injectable, inject, signal, computed } from '@angular/core';
import { ApiService } from './api.service';
import { Document, DocumentStatus, UploadProgress, UploadStatus } from '../models';
import { Observable } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';

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
  isLoading = this.loadingSignal.asReadonly();
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
   * Upload a new document file with progress tracking
   * @param file The file to upload (PDF or TXT, max 10MB)
   * @returns Observable emitting progress updates
   */
  uploadDocument(file: File): Observable<UploadProgress> {
    return this.api.uploadDocument(file).pipe(
      tap(progress => {
        // Update state when complete
        if (progress.status === UploadStatus.COMPLETE) {
          const progressWithDoc = progress as UploadProgress & { document: Document };
          if (progressWithDoc.document) {
            const parsedDocument = {
              ...progressWithDoc.document,
              uploadTimestamp: progressWithDoc.document.uploadTimestamp
                ? new Date(progressWithDoc.document.uploadTimestamp)
                : new Date()
            };
            this.documentsSignal.update(docs => [...docs, parsedDocument]);
          }
        }
      }),
      catchError(error => {
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
