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
   * Uses XMLHttpRequest for streaming support with POST method
   * @param question The user's question
   * @returns Observable emitting text chunks as they arrive
   */
  chatStream(question: string): Observable<string> {
    return new Observable(observer => {
      const xhr = new XMLHttpRequest();
      let lastIndex = 0;

      xhr.open('POST', `${this.apiUrl}/chat/stream`, true);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.setRequestHeader('Accept', 'text/event-stream');

      // Handle progressive response
      xhr.onprogress = () => {
        this.ngZone.run(() => {
          const responseText = xhr.responseText.substring(lastIndex);
          lastIndex = xhr.responseText.length;

          // Parse SSE format (data: ...)
          const lines = responseText.split('\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const data = line.substring(5).trim();
              if (data) {
                observer.next(data);
              }
            }
          }
        });
      };

      // Handle completion
      xhr.onload = () => {
        this.ngZone.run(() => {
          if (xhr.status >= 200 && xhr.status < 300) {
            observer.complete();
          } else {
            observer.error(new Error(`HTTP error! status: ${xhr.status}`));
          }
        });
      };

      // Handle errors
      xhr.onerror = () => {
        this.ngZone.run(() => {
          observer.error(new Error('Network error occurred'));
        });
      };

      // Handle abort
      xhr.onabort = () => {
        this.ngZone.run(() => {
          observer.error(new Error('Request aborted'));
        });
      };

      // Send request
      xhr.send(JSON.stringify({ question }));

      // Cleanup function
      return () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
          xhr.abort();
        }
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
