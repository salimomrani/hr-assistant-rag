import { Component, inject, signal } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
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
  imports: [CardModule, ToastModule, DocumentUploadComponent, DocumentListComponent],
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
