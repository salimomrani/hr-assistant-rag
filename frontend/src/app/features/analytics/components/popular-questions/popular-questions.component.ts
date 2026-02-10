import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { TooltipModule } from 'primeng/tooltip';
import { QuestionFrequency } from '../../../../core/models';

@Component({
  selector: 'app-popular-questions',
  imports: [TooltipModule],
  templateUrl: './popular-questions.component.html',
  styleUrl: './popular-questions.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PopularQuestionsComponent {
  questions = input.required<QuestionFrequency[]>();

  truncate(text: string, maxLength = 100): string {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  }

  needsTruncation(text: string, maxLength = 100): boolean {
    return text.length > maxLength;
  }
}
