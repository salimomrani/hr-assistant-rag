import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
  effect,
  viewChild,
  ElementRef,
  AfterViewInit,
  signal,
  inject,
  DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { MarkdownComponent } from 'ngx-markdown';
import { TooltipModule } from 'primeng/tooltip';
import { ConversationMessage } from '../../../../core/models';
import { SourceListComponent } from '../source-list/source-list.component';

/**
 * Message List Component - Handles message display with intelligent auto-scroll.
 */
@Component({
  selector: 'app-message-list',
  imports: [CommonModule, ScrollPanelModule, SourceListComponent, MarkdownComponent, TooltipModule],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MessageListComponent implements AfterViewInit {
  // Inputs via Signal API
  messages = input<ConversationMessage[]>([]);
  streamingContent = input<string>('');
  isLoading = input<boolean>(false);
  pendingQuestion = input<string>('');

  // Outputs
  suggestionClicked = output<string>();

  // State
  copiedMessageId = signal<string | null>(null);
  private destroyRef = inject(DestroyRef);

  readonly suggestedQuestions = [
    'Combien de jours de congés ai-je droit ?',
    'Comment poser une demande de télétravail ?',
    'Quels sont les avantages sociaux ?',
    'Comment fonctionne le remboursement des frais ?',
  ];

  // Scroll container reference
  private scrollContainer = viewChild<ElementRef>('scrollContainer');

  constructor() {
    // Reactive effect to trigger scroll on content changes
    effect(() => {
      // Track dependencies
      this.messages();
      this.streamingContent();
      this.isLoading();

      // Use requestAnimationFrame to ensure the DOM is ready
      requestAnimationFrame(() => this.scrollToBottom());
    });
  }

  ngAfterViewInit() {
    this.scrollToBottom();
  }

  /**
   * Smooth scroll to the bottom of the container
   */
  private scrollToBottom(): void {
    const element = this.scrollContainer()?.nativeElement;
    if (element) {
      // Target the inner element for library components
      const scrollEl = element.querySelector('.p-scrollpanel-content') || element;
      scrollEl.scrollTo({
        top: scrollEl.scrollHeight,
        behavior: 'smooth',
      });
    }
  }

  /**
   * Copy content to clipboard with visual feedback
   */
  async copyToClipboard(content: string, messageId: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(content);
      this.copiedMessageId.set(messageId);

      const timeout = setTimeout(() => {
        if (this.copiedMessageId() === messageId) {
          this.copiedMessageId.set(null);
        }
      }, 2000);

      this.destroyRef.onDestroy(() => clearTimeout(timeout));
    } catch (err) {
      console.error('Failed to copy to clipboard:', err);
    }
  }
}
