import { Component, input, signal } from '@angular/core';
import { SourceDocumentReference } from '../../../../core/models';

/**
 * Source List Component - Displays source documents used to generate an answer
 * Collapsible section showing document names with optional excerpts
 */
@Component({
  selector: 'app-source-list',
  templateUrl: './source-list.component.html',
  styleUrl: './source-list.component.css'
})
export class SourceListComponent {
  // Input: array of source document references
  sources = input<SourceDocumentReference[]>([]);

  // UI state: expanded/collapsed
  isExpanded = signal<boolean>(false);

  /**
   * Toggle expanded state
   */
  toggleExpanded(): void {
    this.isExpanded.update(v => !v);
  }

  /**
   * Get unique sources (deduplicated by document name)
   */
  uniqueSources(): SourceDocumentReference[] {
    const seen = new Set<string>();
    return this.sources().filter(source => {
      if (seen.has(source.documentName)) {
        return false;
      }
      seen.add(source.documentName);
      return true;
    });
  }
}
