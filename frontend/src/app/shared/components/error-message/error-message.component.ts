import { Component, input, output } from '@angular/core';
import { MessageModule } from 'primeng/message';

/**
 * Error Message Component - Elegant notification component
 * Displays contextual messages with severity levels and optional close action
 */
@Component({
  selector: 'app-error-message',
  imports: [MessageModule],
  templateUrl: './error-message.component.html',
  styleUrl: './error-message.component.css'
})
export class ErrorMessageComponent {
  // Message content
  message = input.required<string>();

  // Severity level
  severity = input<'success' | 'info' | 'warn' | 'error'>('error');

  // Can be closed by user
  closeable = input<boolean>(true);

  // Emitted when close button is clicked
  closed = output<void>();

  /**
   * Handle close button click
   */
  onClose(): void {
    this.closed.emit();
  }

  /**
   * Get icon for severity level
   */
  getIcon(): string {
    const icons = {
      success: 'pi-check-circle',
      info: 'pi-info-circle',
      warn: 'pi-exclamation-triangle',
      error: 'pi-times-circle'
    };
    return icons[this.severity()];
  }

  /**
   * Get title for severity level
   */
  getTitle(): string {
    const titles = {
      success: 'Success',
      info: 'Information',
      warn: 'Warning',
      error: 'Error'
    };
    return titles[this.severity()];
  }
}
