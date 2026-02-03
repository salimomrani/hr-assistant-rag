import { Component, input, output, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Document } from '../../../../core/models';
import { ApiService } from '../../../../core/services/api.service';

/**
 * PDF Preview Modal Component
 * Displays a document preview in an overlay dialog
 * Supports PDF files (via iframe) and TXT files (via fetch and display)
 */
@Component({
  selector: 'app-pdf-preview-modal',
  imports: [DialogModule, ButtonModule, ProgressSpinnerModule],
  templateUrl: './pdf-preview-modal.component.html',
  styleUrl: './pdf-preview-modal.component.css'
})
export class PdfPreviewModalComponent {
  private api = inject(ApiService);
  private sanitizer = inject(DomSanitizer);

  // Input: document to preview
  document = input<Document | null>(null);

  // Input: visibility control
  visible = input<boolean>(false);

  // Output: close event
  closed = output<void>();

  // Internal state
  isLoading = signal<boolean>(false);
  textContent = signal<string>('');
  errorMessage = signal<string>('');

  // Computed: raw file URL (for download link and fetch)
  fileUrl = computed(() => {
    const doc = this.document();
    if (!doc) return '';
    return this.api.getDocumentFileUrl(doc.id);
  });

  // Computed: safe file URL for iframe (sanitized)
  safeFileUrl = computed<SafeResourceUrl | null>(() => {
    const url = this.fileUrl();
    if (!url) return null;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  // Computed: is PDF file
  isPdf = computed(() => {
    const doc = this.document();
    if (!doc) return false;
    // Check both possible field names from backend
    const type = doc.type || doc.fileType;
    return type === 'PDF';
  });

  // Computed: dialog header
  dialogHeader = computed(() => {
    const doc = this.document();
    return doc?.filename || 'Apercu du document';
  });

  /**
   * Handle dialog visibility change
   */
  onVisibleChange(visible: boolean): void {
    if (!visible) {
      this.closed.emit();
      this.resetState();
    }
  }

  /**
   * Handle dialog show event - load content if TXT
   */
  onShow(): void {
    this.errorMessage.set('');

    if (this.isPdf()) {
      // For PDFs, the browser's built-in viewer handles loading
      // No need to show our own loading spinner
      this.isLoading.set(false);
    } else {
      // For TXT files, show loading while we fetch content
      this.isLoading.set(true);
      this.loadTextContent();
    }
  }

  /**
   * Load text content for TXT files
   */
  private loadTextContent(): void {
    const url = this.fileUrl();
    if (!url) return;

    fetch(url)
      .then(response => {
        if (!response.ok) {
          throw new Error('Failed to load file');
        }
        return response.text();
      })
      .then(text => {
        this.textContent.set(text);
        this.isLoading.set(false);
      })
      .catch(() => {
        this.errorMessage.set('Impossible de charger le document');
        this.isLoading.set(false);
      });
  }

  /**
   * Reset internal state
   */
  private resetState(): void {
    this.isLoading.set(false);
    this.textContent.set('');
    this.errorMessage.set('');
  }

  /**
   * Close the dialog
   */
  close(): void {
    this.closed.emit();
  }
}
