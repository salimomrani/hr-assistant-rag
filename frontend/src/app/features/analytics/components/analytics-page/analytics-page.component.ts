import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { DashboardAnalytics } from '../../../../core/models';
import { KpiCardsComponent } from '../kpi-cards/kpi-cards.component';
import { PopularQuestionsComponent } from '../popular-questions/popular-questions.component';
import { TopDocumentsComponent } from '../top-documents/top-documents.component';
import { UsageChartComponent } from '../usage-chart/usage-chart.component';

@Component({
  selector: 'app-analytics-page',
  imports: [
    KpiCardsComponent,
    PopularQuestionsComponent,
    TopDocumentsComponent,
    UsageChartComponent,
  ],
  templateUrl: './analytics-page.component.html',
  styleUrl: './analytics-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalyticsPageComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);

  analytics = signal<DashboardAnalytics | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.analyticsService.getDashboardAnalytics$().subscribe({
      next: (data) => {
        this.analytics.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Failed to load analytics data');
        this.loading.set(false);
        console.error('Error loading analytics:', err);
      },
    });
  }
}
