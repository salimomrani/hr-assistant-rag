import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule, FileSelectEvent } from 'primeng/fileupload';
import { ProgressBarModule } from 'primeng/progressbar';
import { MessageService } from 'primeng/api';
import { DocumentUploadComponent } from '../document-upload/document-upload.component';
import { DocumentListComponent } from '../document-list/document-list.component';
import { PdfPreviewModalComponent } from '../pdf-preview-modal/pdf-preview-modal.component';
import { DocumentService } from '../../../../core/services/document.service';
import { Document, UploadStatus } from '../../../../core/models';

/**
 * Admin Container Component - Main orchestrator for document management
 * Coordinates upload and list components with centralized state
 */
@Component({
  selector: 'app-admin-container',
  imports: [
    FormsModule,
    CardModule,
    ToastModule,
    DialogModule,
    InputTextModule,
    ButtonModule,
    FileUploadModule,
    ProgressBarModule,
    DocumentUploadComponent,
    DocumentListComponent,
    PdfPreviewModalComponent
  ],
  providers: [MessageService],
  templateUrl: './admin-container.component.html',
  styleUrl: './admin-container.component.css'
})
export class AdminContainerComponent {
  private documentService = inject(DocumentService);
  private messageService = inject(MessageService);

  // State signals
  documents = this.documentService.documents;
  isLoading = this.documentService.isLoading;
  refreshTrigger = signal(0);

  // Edit dialog state
  editDialogVisible = signal(false);
  editingDocument = signal<Document | null>(null);
  newFilename = signal('');
  isRenaming = signal(false);

  // Replace dialog state
  replaceDialogVisible = signal(false);
  replacingDocument = signal<Document | null>(null);
  replaceFile = signal<File | null>(null);
  isReplacing = signal(false);
  replaceProgress = signal(0);

  // Preview modal state
  previewModalVisible = signal(false);
  previewDocument = signal<Document | null>(null);

  // Validation constants (same as document-upload)
  readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  readonly ACCEPTED_TYPES = ['application/pdf', 'text/plain'];

  constructor() {
    // Load documents on init
    this.loadDocuments();
  }

  /**
   * Handle successful document upload
   * Shows success toast and refreshes document list
   */
  onUploadSuccess(document: Document): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Upload réussi',
      detail: `${document.filename} a été uploadé avec succès`,
      life: 5000
    });

    // Refresh document list
    this.loadDocuments();
  }

  /**
   * Handle upload error
   * Shows error toast with details
   */
  onUploadError(error: { message: string; details?: string }): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erreur d\'upload',
      detail: error.details || error.message,
      life: 7000
    });
  }

  /**
   * Handle document deletion
   * Shows success toast and refreshes list
   */
  onDocumentDeleted(documentId: string): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Document supprimé',
      detail: 'Le document a été supprimé avec succès',
      life: 5000
    });

    // Refresh document list
    this.loadDocuments();
  }

  /**
   * Handle delete error
   */
  onDeleteError(error: string): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Erreur de suppression',
      detail: error,
      life: 7000
    });
  }

  /**
   * Open edit dialog for a document
   */
  onEditDocument(document: Document): void {
    this.editingDocument.set(document);
    this.newFilename.set(document.filename);
    this.editDialogVisible.set(true);
  }

  /**
   * Close edit dialog
   */
  closeEditDialog(): void {
    this.editDialogVisible.set(false);
    this.editingDocument.set(null);
    this.newFilename.set('');
  }

  /**
   * Save renamed document
   */
  saveRename(): void {
    const document = this.editingDocument();
    const filename = this.newFilename().trim();

    if (!document || !filename) {
      return;
    }

    // Validate filename
    if (filename.length > 255) {
      this.messageService.add({
        severity: 'error',
        summary: 'Erreur',
        detail: 'Le nom du fichier ne peut pas dépasser 255 caractères',
        life: 5000
      });
      return;
    }

    this.isRenaming.set(true);

    this.documentService.renameDocument(document.id, filename).subscribe({
      next: () => {
        this.isRenaming.set(false);
        this.closeEditDialog();
        this.messageService.add({
          severity: 'success',
          summary: 'Document renommé',
          detail: `Le document a été renommé en "${filename}"`,
          life: 5000
        });
        // Refresh document list
        this.loadDocuments();
      },
      error: (error) => {
        this.isRenaming.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur de renommage',
          detail: error.error?.message || 'Impossible de renommer le document',
          life: 7000
        });
      }
    });
  }

  /**
   * Check if save button should be disabled
   */
  isSaveDisabled(): boolean {
    const filename = this.newFilename().trim();
    const original = this.editingDocument()?.filename;
    return !filename || filename === original || this.isRenaming();
  }

  /**
   * Open replace dialog for a document
   */
  onReplaceDocument(document: Document): void {
    this.replacingDocument.set(document);
    this.replaceFile.set(null);
    this.replaceProgress.set(0);
    this.replaceDialogVisible.set(true);
  }

  /**
   * Close replace dialog
   */
  closeReplaceDialog(): void {
    this.replaceDialogVisible.set(false);
    this.replacingDocument.set(null);
    this.replaceFile.set(null);
    this.replaceProgress.set(0);
  }

  /**
   * Handle file selection for replacement
   */
  onReplaceFileSelect(event: FileSelectEvent): void {
    if (event.currentFiles && event.currentFiles.length > 0) {
      const file = event.currentFiles[0];

      // Validate file
      if (!this.ACCEPTED_TYPES.includes(file.type)) {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Type de fichier non accepté. Formats acceptés: PDF, TXT',
          life: 5000
        });
        return;
      }

      if (file.size > this.MAX_FILE_SIZE) {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Fichier trop volumineux. Taille maximale: 10 MB',
          life: 5000
        });
        return;
      }

      this.replaceFile.set(file);
    }
  }

  /**
   * Clear selected replace file
   */
  onReplaceFileClear(): void {
    this.replaceFile.set(null);
    this.replaceProgress.set(0);
  }

  /**
   * Execute document replacement
   * Deletes old document, uploads new one with same filename
   */
  executeReplace(): void {
    const document = this.replacingDocument();
    const file = this.replaceFile();

    if (!document || !file) {
      return;
    }

    this.isReplacing.set(true);
    this.replaceProgress.set(0);

    // Step 1: Delete old document
    this.documentService.deleteDocument(document.id).subscribe({
      next: () => {
        // Step 2: Upload new file with original filename
        const renamedFile = new File([file], document.filename, { type: file.type });

        this.documentService.uploadDocument(renamedFile).subscribe({
          next: (progress) => {
            if (progress.status === UploadStatus.UPLOADING || progress.status === UploadStatus.PROCESSING) {
              this.replaceProgress.set(progress.percentComplete);
            } else if (progress.status === UploadStatus.COMPLETE) {
              this.replaceProgress.set(100);
              this.isReplacing.set(false);
              this.closeReplaceDialog();

              this.messageService.add({
                severity: 'success',
                summary: 'Document remplacé',
                detail: `"${document.filename}" a été remplacé avec succès`,
                life: 5000
              });

              // Refresh document list
              this.loadDocuments();
            }
          },
          error: (error) => {
            this.isReplacing.set(false);
            this.replaceProgress.set(0);
            this.messageService.add({
              severity: 'error',
              summary: 'Erreur d\'upload',
              detail: error.error?.message || 'Impossible d\'uploader le nouveau fichier',
              life: 7000
            });
          }
        });
      },
      error: (error) => {
        this.isReplacing.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur de suppression',
          detail: error.error?.message || 'Impossible de supprimer l\'ancien document',
          life: 7000
        });
      }
    });
  }

  /**
   * Check if replace button should be disabled
   */
  isReplaceDisabled(): boolean {
    return !this.replaceFile() || this.isReplacing();
  }

  /**
   * Open preview modal for a document
   */
  onPreviewDocument(document: Document): void {
    this.previewDocument.set(document);
    this.previewModalVisible.set(true);
  }

  /**
   * Close preview modal
   */
  closePreviewModal(): void {
    this.previewModalVisible.set(false);
    this.previewDocument.set(null);
  }

  /**
   * Format bytes to human-readable string
   */
  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Load documents from service
   */
  private loadDocuments(): void {
    this.documentService.loadDocuments().subscribe({
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur de chargement',
          detail: 'Impossible de charger les documents',
          life: 5000
        });
        console.error('Error loading documents:', error);
      }
    });
  }
}
