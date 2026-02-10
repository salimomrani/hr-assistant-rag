import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { ChartModule } from 'primeng/chart';
import { DailyCount, DailyAverage } from '../../../../core/models';

@Component({
  selector: 'app-usage-chart',
  imports: [ChartModule],
  templateUrl: './usage-chart.component.html',
  styleUrl: './usage-chart.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsageChartComponent {
  dailyUsage = input.required<DailyCount[]>();
  dailyResponseTimes = input.required<DailyAverage[]>();

  usageChartData = computed(() => {
    const usage = this.dailyUsage();
    return {
      labels: usage.map((d) => this.formatDate(d.date)),
      datasets: [
        {
          label: 'Questions',
          data: usage.map((d) => d.count),
          backgroundColor: 'rgba(34, 211, 238, 0.6)',
          borderColor: '#22d3ee',
          borderWidth: 1,
          borderRadius: 4,
        },
      ],
    };
  });

  usageChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: {
          color: '#9199b0',
          font: { family: "'Plus Jakarta Sans', system-ui, sans-serif", size: 12 },
        },
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#6b7494',
          font: { size: 11 },
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.04)',
        },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#6b7494',
          font: { size: 11 },
          stepSize: 1,
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.04)',
        },
      },
    },
  };

  responseTimeChartData = computed(() => {
    const times = this.dailyResponseTimes();
    return {
      labels: times.map((d) => this.formatDate(d.date)),
      datasets: [
        {
          label: 'Avg Response Time (s)',
          data: times.map((d) => d.averageMs / 1000),
          borderColor: '#fbbf24',
          backgroundColor: 'rgba(251, 191, 36, 0.1)',
          tension: 0.3,
          fill: true,
          pointBackgroundColor: '#fbbf24',
          pointBorderColor: '#fbbf24',
          pointRadius: 3,
        },
      ],
    };
  });

  responseTimeChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        labels: {
          color: '#9199b0',
          font: { family: "'Plus Jakarta Sans', system-ui, sans-serif", size: 12 },
        },
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#6b7494',
          font: { size: 11 },
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.04)',
        },
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#6b7494',
          font: { size: 11 },
          callback: (value: number) => value + 's',
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.04)',
        },
      },
    },
  };

  private formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const day = date.getDate().toString().padStart(2, '0');
    const months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec',
    ];
    return `${day} ${months[date.getMonth()]}`;
  }
}
