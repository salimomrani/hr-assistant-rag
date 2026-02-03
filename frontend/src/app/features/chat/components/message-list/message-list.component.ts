import { 
  Component, input, output, effect, viewChild, 
  ElementRef, AfterViewInit, signal, inject, DestroyRef 
} from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { MarkdownComponent } from 'ngx-markdown';
import { TooltipModule } from 'primeng/tooltip';
import { ConversationMessage } from '../../../../core/models';
import { SourceListComponent } from '../source-list/source-list.component';

/**
 * Message List Component - Gère l'affichage des messages avec auto-scroll intelligent.
 */
@Component({
  selector: 'app-message-list',
  standalone: true,
  imports: [
    CommonModule, 
    ScrollPanelModule, 
    SourceListComponent, 
    MarkdownComponent, 
    TooltipModule,
    DatePipe
  ],
  templateUrl: './message-list.component.html',
  styleUrl: './message-list.component.css'
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
    'Comment fonctionne le remboursement des frais ?'
  ];

  // Accès au container de scroll
  private scrollContainer = viewChild<ElementRef>('scrollContainer');

  constructor() {
    // Effet réactif pour déclencher le scroll
    effect(() => {
      // On surveille les dépendances
      this.messages();
      this.streamingContent();
      this.isLoading();
      
      // Utilisation de requestAnimationFrame pour s'assurer que le DOM est prêt
      requestAnimationFrame(() => this.scrollToBottom());
    });
  }

  ngAfterViewInit() {
    this.scrollToBottom();
  }

  /**
   * Scroll fluide vers le bas du conteneur
   */
  private scrollToBottom(): void {
    const element = this.scrollContainer()?.nativeElement;
    if (element) {
      // On cible souvent l'élément interne pour les composants de librairie
      const scrollEl = element.querySelector('.p-scrollpanel-content') || element;
      scrollEl.scrollTo({
        top: scrollEl.scrollHeight,
        behavior: 'smooth'
      });
    }
  }

  /**
   * Copie le contenu dans le presse-papier avec feedback visuel
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
      console.error('Erreur lors de la copie :', err);
    }
  }
}