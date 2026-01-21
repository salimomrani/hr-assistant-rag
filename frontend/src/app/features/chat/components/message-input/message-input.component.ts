import { Component, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';

/**
 * Message Input Component - Text input for submitting questions
 * Features validation, character counter, and keyboard shortcuts
 */
@Component({
  selector: 'app-message-input',
  imports: [FormsModule, TextareaModule, ButtonModule],
  templateUrl: './message-input.component.html',
  styleUrl: './message-input.component.css'
})
export class MessageInputComponent {
  // Question text signal
  questionText = signal<string>('');

  // Disabled state (during submission)
  isDisabled = signal<boolean>(false);

  // Max character limit (public for template access)
  readonly MAX_LENGTH = 1000;

  // Computed: character count
  charCount = computed(() => this.questionText().length);

  // Computed: is over limit
  isOverLimit = computed(() => this.charCount() > this.MAX_LENGTH);

  // Computed: is valid (non-empty and under limit)
  isValid = computed(() => {
    const text = this.questionText().trim();
    return text.length > 0 && text.length <= this.MAX_LENGTH;
  });

  // Output: emitted when question is submitted
  questionSubmitted = output<string>();

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (!this.isValid() || this.isDisabled()) {
      return;
    }

    const question = this.questionText().trim();
    this.questionSubmitted.emit(question);
    this.questionText.set('');
  }

  /**
   * Handle keyboard shortcuts
   * Enter = submit, Shift+Enter = new line
   */
  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSubmit();
    }
  }

  /**
   * Set disabled state from parent
   */
  setDisabled(disabled: boolean): void {
    this.isDisabled.set(disabled);
  }
}
