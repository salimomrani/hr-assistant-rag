import { Component, input } from '@angular/core';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

/**
 * Loading Spinner Component - Elegant loading indicator
 * Displays animated spinner with optional message
 */
@Component({
  selector: 'app-loading-spinner',
  imports: [ProgressSpinnerModule],
  templateUrl: './loading-spinner.component.html',
  styleUrl: './loading-spinner.component.css'
})
export class LoadingSpinnerComponent {
  // Optional loading message
  message = input<string>('Loading...');

  // Spinner size (small, medium, large)
  size = input<'small' | 'medium' | 'large'>('medium');

  // Show overlay backdrop
  overlay = input<boolean>(false);
}
