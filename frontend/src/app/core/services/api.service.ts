import { Injectable, inject, NgZone } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Document, UploadProgress, UploadStatus } from '../models';

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
   * Uses Fetch API with ReadableStream for proper SSE handling
   * @param question The user's question
   * @returns Observable emitting text chunks as they arrive
   */
  chatStream(question: string): Observable<string> {
    return new Observable(observer => {
      const abortController = new AbortController();

      // Use direct backend URL to bypass proxy buffering for SSE
      const streamUrl = 'http://localhost:8080/api/chat/stream';

      fetch(streamUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ question }),
        signal: abortController.signal
      })
        .then(async response => {
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const reader = response.body?.getReader();
          const decoder = new TextDecoder();

          if (!reader) {
            throw new Error('Response body is null');
          }

          console.log('[SSE] Starting to read stream...');
          let buffer = '';

          while (true) {
            const { done, value } = await reader.read();
            console.log('[SSE] Read chunk, done:', done, 'value length:', value?.length);

            if (done) {
              this.ngZone.run(() => observer.complete());
              break;
            }

            // Decode chunk and add to buffer
            buffer += decoder.decode(value, { stream: true });

            // Split by double newlines (SSE event separator)
            const events = buffer.split('\n\n');

            // Keep last incomplete event in buffer
            buffer = events.pop() || '';

            // Process complete events
            for (const event of events) {
              if (event.trim()) {
                console.log('[SSE] Raw event:', event);
                // Parse data: lines
                const lines = event.split('\n');

                for (const line of lines) {
                  if (line.startsWith('data:')) {
                    // Extract data after 'data:' prefix (5 chars)
                    const data = line.substring(5);
                    console.log('[SSE] Parsed data:', JSON.stringify(data));

                    // Empty data line represents a newline in the original text
                    if (data === '') {
                      this.ngZone.run(() => observer.next('\n'));
                    } else {
                      // Emit content as-is (spaces are part of LLM tokens)
                      this.ngZone.run(() => observer.next(data));
                    }
                  }
                }
              }
            }
          }
        })
        .catch(error => {
          this.ngZone.run(() => observer.error(error));
        });

      // Cleanup: abort fetch on unsubscribe
      return () => abortController.abort();
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
   * Upload a document file with progress tracking
   * @param file The file to upload (PDF or TXT)
   * @returns Observable emitting upload progress updates
   */
  uploadDocument(file: File): Observable<UploadProgress> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<Document>(
      `${this.apiUrl}/documents`,
      formData,
      {
        reportProgress: true,
        observe: 'events'
      }
    ).pipe(
      map((event: HttpEvent<Document>) => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            const percentComplete = event.total
              ? Math.round((100 * event.loaded) / event.total)
              : 0;
            return {
              filename: file.name,
              percentComplete,
              status: UploadStatus.UPLOADING,
              bytesTransferred: event.loaded,
              totalBytes: event.total || 0
            };

          case HttpEventType.Response:
            return {
              filename: file.name,
              percentComplete: 100,
              status: UploadStatus.COMPLETE,
              bytesTransferred: file.size,
              totalBytes: file.size,
              document: event.body!
            } as UploadProgress & { document: Document };

          default:
            return {
              filename: file.name,
              percentComplete: 0,
              status: UploadStatus.UPLOADING,
              bytesTransferred: 0,
              totalBytes: file.size
            };
        }
      })
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

  /**
   * Rename a document
   * @param documentId The document ID to rename
   * @param newFilename The new filename
   * @returns Observable with updated document
   */
  renameDocument(documentId: string, newFilename: string): Observable<Document> {
    return this.http.patch<Document>(
      `${this.apiUrl}/documents/${documentId}`,
      { newFilename }
    );
  }
}
