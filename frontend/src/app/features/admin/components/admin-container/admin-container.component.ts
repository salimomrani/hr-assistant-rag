import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { DocumentUploadComponent } from '../document-upload/document-upload.component';
import { DocumentListComponent } from '../document-list/document-list.component';
import { DocumentService } from '../../../../core/services/document.service';
import { Document } from '../../../../core/models';

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
    DocumentUploadComponent,
    DocumentListComponent
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
