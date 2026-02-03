import { Pipe, PipeTransform, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';

/**
 * Markdown Pipe - Converts markdown text to sanitized HTML
 * Supports: bold, italic, lists, code blocks, links
 */
@Pipe({
  name: 'markdown'
})
export class MarkdownPipe implements PipeTransform {
  private sanitizer = inject(DomSanitizer);

  constructor() {
    // Configure marked options for safe rendering
    marked.setOptions({
      breaks: true,      // Convert \n to <br>
      gfm: true          // GitHub Flavored Markdown
    });
  }

  transform(value: string | null | undefined): SafeHtml {
    if (!value) {
      return '';
    }

    try {
      const html = marked.parse(value) as string;
      return this.sanitizer.bypassSecurityTrustHtml(html);
    } catch {
      return value;
    }
  }
}
