import { Component, input, output, inject, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { SkeletonModule } from 'primeng/skeleton';
import { Document } from '../../../../core/models';
import { DocumentService } from '../../../../core/services/document.service';

/**
 * Document List Component - Displays documents in a table with actions
 * Features: status badges, delete action, empty state, loading skeleton
 */
@Component({
  selector: 'app-document-list',
  imports: [TableModule, ButtonModule, TagModule, ConfirmDialogModule, SkeletonModule],
  providers: [ConfirmationService],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css'
})
export class DocumentListComponent {
  private documentService = inject(DocumentService);
  private confirmationService = inject(ConfirmationService);

  // Input properties
  documents = input<Document[]>([]);
  isLoading = input<boolean>(false);

  // Output events
  documentDeleted = output<string>();
  deleteError = output<string>();

  // Local state
  deletingId = signal<string | null>(null);

  /**
   * Confirm and delete document
   */
  confirmDelete(document: Document): void {
    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer "${document.filename}" ?`,
      header: 'Confirmer la suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.deleteDocument(document.id);
      }
    });
  }

  /**
   * Delete document
   */
  private deleteDocument(documentId: string): void {
    this.deletingId.set(documentId);

    this.documentService.deleteDocument(documentId).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.documentDeleted.emit(documentId);
      },
      error: (error) => {
        this.deletingId.set(null);
        this.deleteError.emit(
          error.error?.message || 'Erreur lors de la suppression du document'
        );
      }
    });
  }

  /**
   * Check if document is being deleted
   */
  isDeleting(documentId: string): boolean {
    return this.deletingId() === documentId;
  }

  /**
   * Get severity for status badge
   */
  getStatusSeverity(status: string): 'success' | 'warn' | 'danger' | 'secondary' {
    switch (status?.toLowerCase()) {
      case 'indexed':
        return 'success';
      case 'pending':
        return 'warn';
      case 'failed':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  /**
   * Get label for status badge
   */
  getStatusLabel(status: string): string {
    switch (status?.toLowerCase()) {
      case 'indexed':
        return 'Indexé';
      case 'pending':
        return 'En cours';
      case 'failed':
        return 'Échec';
      default:
        return status || 'Inconnu';
    }
  }

  /**
   * Format file size
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';

    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Format date
   */
  formatDate(date: Date | string): string {
    const dateObj = typeof date === 'string' ? new Date(date) : date;

    return new Intl.DateTimeFormat('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(dateObj);
  }

  /**
   * Get file type display
   */
  getFileType(filename: string): string {
    const ext = filename.split('.').pop()?.toUpperCase();
    return ext || 'Inconnu';
  }
}
