import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-kpi-cards',
  imports: [],
  templateUrl: './kpi-cards.component.html',
  styleUrl: './kpi-cards.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KpiCardsComponent {
  totalQuestionsToday = input.required<number>();
  averageResponseTimeMs = input.required<number>();
  totalDocuments = input.required<number>();
  conversationsThisWeek = input.required<number>();

  formattedResponseTime = computed(() => {
    const ms = this.averageResponseTimeMs();
    if (ms === 0) return '0s';
    return (ms / 1000).toFixed(1) + 's';
  });
}
