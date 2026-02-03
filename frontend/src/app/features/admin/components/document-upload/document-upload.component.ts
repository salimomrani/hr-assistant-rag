import { Component, inject, output, signal, computed } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FileUploadModule, FileUploadHandlerEvent, FileSelectEvent } from 'primeng/fileupload';
import { ProgressBarModule } from 'primeng/progressbar';
import { ButtonModule } from 'primeng/button';
import { DocumentService } from '../../../../core/services/document.service';
import { Document, UploadStatus } from '../../../../core/models';
import { ErrorMessageComponent } from '../../../../shared/components/error-message/error-message.component';

/**
 * Document Upload Component - Handles file upload with validation and progress
 * Features: drag-and-drop, file type validation, size validation, progress tracking
 */
@Component({
  selector: 'app-document-upload',
  imports: [FileUploadModule, ProgressBarModule, ButtonModule, ErrorMessageComponent],
  templateUrl: './document-upload.component.html',
  styleUrl: './document-upload.component.css'
})
export class DocumentUploadComponent {
  private documentService = inject(DocumentService);

  // Validation constants
  readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  readonly ACCEPTED_TYPES = ['application/pdf', 'text/plain'];
  readonly ACCEPTED_EXTENSIONS = ['.pdf', '.txt'];

  // State signals
  uploadProgress = signal<number>(0);
  isUploading = signal<boolean>(false);
  errorMessage = signal<string>('');
  selectedFile = signal<File | null>(null);

  // Computed properties
  hasError = computed(() => this.errorMessage().length > 0);
  formattedFileSize = computed(() => {
    const file = this.selectedFile();
    return file ? this.formatBytes(file.size) : '';
  });

  // Output events
  uploadSuccess = output<Document>();
  uploadError = output<{ message: string; details?: string }>();

  /**
   * Trigger manual upload
   */
  triggerUpload(): void {
    const file = this.selectedFile();
    if (!file) {
      return;
    }

    // Call onUpload with FileUploadHandlerEvent format
    this.onUpload({ files: [file] } as FileUploadHandlerEvent);
  }

  /**
   * Handle file selection from file upload component
   */
  onSelect(event: FileSelectEvent): void {
    this.errorMessage.set('');

    if (event.currentFiles && event.currentFiles.length > 0) {
      const file = event.currentFiles[0];
      this.selectedFile.set(file);

      // Validate file
      const validationError = this.validateFile(file);
      if (validationError) {
        this.errorMessage.set(validationError);
        this.selectedFile.set(null);
      }
    }
  }

  /**
   * Handle file removal
   */
  onRemove(): void {
    this.selectedFile.set(null);
    this.errorMessage.set('');
    this.uploadProgress.set(0);
  }

  /**
   * Handle file upload with custom handler
   */
  onUpload(event: FileUploadHandlerEvent): void {
    const file = event.files[0];

    if (!file) {
      return;
    }

    // Validate before upload
    const validationError = this.validateFile(file);
    if (validationError) {
      this.errorMessage.set(validationError);
      this.uploadError.emit({
        message: 'Validation échouée',
        details: validationError
      });
      return;
    }

    this.isUploading.set(true);
    this.uploadProgress.set(0);
    this.errorMessage.set('');

    this.documentService.uploadDocument(file).subscribe({
      next: (progress) => {
        if (progress.status === UploadStatus.UPLOADING || progress.status === UploadStatus.PROCESSING) {
          this.uploadProgress.set(progress.percentComplete);
        } else if (progress.status === UploadStatus.COMPLETE) {
          this.uploadProgress.set(100);
          const progressWithDoc = progress as typeof progress & { document: Document };
          if (progressWithDoc.document) {
            this.uploadSuccess.emit(progressWithDoc.document);
          }
          this.selectedFile.set(null);
          this.isUploading.set(false);

          // Reset progress after delay
          setTimeout(() => this.uploadProgress.set(0), 2000);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.isUploading.set(false);
        this.uploadProgress.set(0);

        const errorMsg = this.getErrorMessage(error);
        this.errorMessage.set(errorMsg);
        this.uploadError.emit({
          message: 'Upload échoué',
          details: errorMsg
        });
      }
    });
  }

  /**
   * Validate file type and size
   */
  private validateFile(file: File): string | null {
    // Check file type
    if (!this.ACCEPTED_TYPES.includes(file.type)) {
      return `Type de fichier non accepté. Formats acceptés: PDF, TXT`;
    }

    // Check file size
    if (file.size > this.MAX_FILE_SIZE) {
      return `Fichier trop volumineux. Taille maximale: ${this.formatBytes(this.MAX_FILE_SIZE)}`;
    }

    return null;
  }

  /**
   * Format bytes to human-readable string
   */
  private formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';

    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Extract error message from HTTP error
   */
  private getErrorMessage(error: HttpErrorResponse): string {
    switch (error.status) {
      case 413:
        return 'Fichier trop volumineux pour le serveur';
      case 400:
        return error.error?.message || 'Requête invalide';
      case 415:
        return 'Type de fichier non supporté par le serveur';
      default:
        return error.error?.message || 'Erreur serveur lors de l\'upload';
    }
  }

  /**
   * Clear error message
   */
  onErrorClosed(): void {
    this.errorMessage.set('');
  }
}
