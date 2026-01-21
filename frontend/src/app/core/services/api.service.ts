import { Injectable, inject, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Document } from '../models';

/**
 * API Service - Handles all HTTP communication with the backend
 * Provides methods for chat and document management endpoints
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private ngZone = inject(NgZone);
  private readonly apiUrl = environment.apiUrl;

  /**
   * Send a chat question and get streaming response via SSE
   * @param question The user's question
   * @returns Observable emitting text chunks as they arrive
   */
  chatStream(question: string): Observable<string> {
    return new Observable(observer => {
      const eventSource = new EventSource(
        `${this.apiUrl}/chat/stream?question=${encodeURIComponent(question)}`
      );

      eventSource.onmessage = (event) => {
        this.ngZone.run(() => {
          observer.next(event.data);
        });
      };

      eventSource.onerror = (error) => {
        this.ngZone.run(() => {
          observer.error(error);
          eventSource.close();
        });
      };

      // Cleanup function
      return () => {
        eventSource.close();
      };
    });
  }

  /**
   * Send a chat question and get complete response (blocking)
   * @param question The user's question
   * @returns Observable with answer and sources
   */
  chat(question: string): Observable<{ answer: string; sources: any[] }> {
    return this.http.post<{ answer: string; sources: any[] }>(
      `${this.apiUrl}/chat`,
      { question }
    );
  }

  /**
   * Upload a document file
   * @param file The file to upload (PDF or TXT)
   * @returns Observable with the created document
   */
  uploadDocument(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<Document>(
      `${this.apiUrl}/documents`,
      formData
    );
  }

  /**
   * Get list of all documents
   * @returns Observable with array of documents
   */
  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/documents`);
  }

  /**
   * Delete a document by ID
   * @param documentId The document ID to delete
   * @returns Observable that completes when delete is successful
   */
  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${documentId}`);
  }
}
