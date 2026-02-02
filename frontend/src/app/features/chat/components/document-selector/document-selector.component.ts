import { Component, inject, signal, computed, OnInit, output, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DocumentService } from '../../../../core/services/document.service';
import { Document, DocumentStatus } from '../../../../core/models';
import { CheckboxModule } from 'primeng/checkbox';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';

/**
 * Document Selector Component - Allows users to filter RAG search by documents
 * Displays a collapsible list of indexed documents with checkboxes
 */
@Component({
  selector: 'app-document-selector',
  imports: [
    FormsModule,
    CheckboxModule,
    ButtonModule,
    TooltipModule
  ],
  templateUrl: './document-selector.component.html',
  styleUrl: './document-selector.component.css'
})
export class DocumentSelectorComponent implements OnInit {
  private documentService = inject(DocumentService);

  // Output event when selection changes
  selectionChanged = output<string[]>();

  // UI state
  isExpanded = signal<boolean>(false);

  // Track selected document IDs
  private selectedIds = signal<Set<string>>(new Set());

  // Track if initial selection has been done
  private initialSelectionDone = signal<boolean>(false);

  // Computed: indexed documents only
  indexedDocuments = computed(() =>
    this.documentService.documents().filter(d => d.status === DocumentStatus.INDEXED)
  );

  // Computed: selected document IDs as array
  selectedDocumentIds = computed(() => Array.from(this.selectedIds()));

  // Computed: selection summary text
  selectionSummary = computed(() => {
    const total = this.indexedDocuments().length;
    const selected = this.selectedIds().size;

    if (total === 0) {
      return 'No documents available';
    }
    if (selected === 0 || selected === total) {
      return `All documents (${total})`;
    }
    return `${selected} of ${total} documents`;
  });

  // Computed: are all documents selected?
  allSelected = computed(() => {
    const indexed = this.indexedDocuments();
    return indexed.length > 0 && this.selectedIds().size === indexed.length;
  });

  constructor() {
    // Effect to auto-select all documents when they load
    effect(() => {
      const indexed = this.indexedDocuments();
      if (indexed.length > 0 && !this.initialSelectionDone()) {
        // Auto-select all documents on first load
        const allIds = indexed.map(d => d.id);
        this.selectedIds.set(new Set(allIds));
        this.initialSelectionDone.set(true);
        this.emitSelection();
      }
    });
  }

  ngOnInit(): void {
    // Load documents if not already loaded
    if (this.documentService.documents().length === 0) {
      this.documentService.loadDocuments().subscribe();
    }
  }

  /**
   * Toggle the expanded/collapsed state
   */
  toggleExpanded(): void {
    this.isExpanded.update(v => !v);
  }

  /**
   * Check if a document is selected
   */
  isSelected(documentId: string): boolean {
    return this.selectedIds().has(documentId);
  }

  /**
   * Toggle selection for a single document
   */
  toggleDocument(documentId: string): void {
    this.selectedIds.update(ids => {
      const newIds = new Set(ids);
      if (newIds.has(documentId)) {
        newIds.delete(documentId);
      } else {
        newIds.add(documentId);
      }
      return newIds;
    });
    this.emitSelection();
  }

  /**
   * Select all indexed documents
   */
  selectAll(): void {
    const allIds = this.indexedDocuments().map(d => d.id);
    this.selectedIds.set(new Set(allIds));
    this.emitSelection();
  }

  /**
   * Clear all selections
   */
  clearAll(): void {
    this.selectedIds.set(new Set());
    this.emitSelection();
  }

  /**
   * Toggle between all selected and none selected
   */
  toggleAll(): void {
    if (this.allSelected()) {
      this.clearAll();
    } else {
      this.selectAll();
    }
  }

  /**
   * Emit the current selection
   * Empty array means "search all documents"
   */
  private emitSelection(): void {
    const selected = this.selectedDocumentIds();
    const total = this.indexedDocuments().length;

    console.log('[DocumentSelector] Selected:', selected.length, 'Total:', total, 'IDs:', selected);

    // If all selected or none selected, emit empty array (search all)
    if (selected.length === 0 || selected.length === total) {
      console.log('[DocumentSelector] Emitting empty array (all documents)');
      this.selectionChanged.emit([]);
    } else {
      console.log('[DocumentSelector] Emitting filtered:', selected);
      this.selectionChanged.emit(selected);
    }
  }
}
