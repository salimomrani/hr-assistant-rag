import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { UsageChartComponent } from './usage-chart.component';
import { DailyCount, DailyAverage } from '../../../../core/models';

@Component({
  imports: [UsageChartComponent],
  template: `
    <app-usage-chart [dailyUsage]="dailyUsage" [dailyResponseTimes]="dailyResponseTimes" />
  `,
})
class TestHostComponent {
  dailyUsage: DailyCount[] = [];
  dailyResponseTimes: DailyAverage[] = [];
}

describe('UsageChartComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
  });

  it('should create', () => {
    hostFixture.detectChanges();
    const el = hostFixture.nativeElement.querySelector('app-usage-chart');
    expect(el).toBeTruthy();
  });

  it('should render two chart panels', () => {
    hostFixture.detectChanges();
    const panels = hostFixture.nativeElement.querySelectorAll('.chart-panel');
    expect(panels.length).toBe(2);
  });

  it('should build usage chart data from inputs', () => {
    hostComponent.dailyUsage = [
      { date: '2026-02-08', count: 5 },
      { date: '2026-02-09', count: 12 },
    ];
    hostComponent.dailyResponseTimes = [];
    hostFixture.detectChanges();

    const component = hostFixture.debugElement.children[0].componentInstance as UsageChartComponent;
    const chartData = component.usageChartData();

    expect(chartData.labels).toHaveLength(2);
    expect(chartData.datasets[0].data).toEqual([5, 12]);
    expect(chartData.datasets[0].label).toBe('Questions');
  });

  it('should build response time chart data from inputs', () => {
    hostComponent.dailyUsage = [];
    hostComponent.dailyResponseTimes = [
      { date: '2026-02-08', averageMs: 1200 },
      { date: '2026-02-09', averageMs: 980 },
    ];
    hostFixture.detectChanges();

    const component = hostFixture.debugElement.children[0].componentInstance as UsageChartComponent;
    const chartData = component.responseTimeChartData();

    expect(chartData.labels).toHaveLength(2);
    expect(chartData.datasets[0].data).toEqual([1.2, 0.98]);
    expect(chartData.datasets[0].label).toBe('Avg Response Time (s)');
  });

  it('should handle empty chart data', () => {
    hostComponent.dailyUsage = [];
    hostComponent.dailyResponseTimes = [];
    hostFixture.detectChanges();

    const component = hostFixture.debugElement.children[0].componentInstance as UsageChartComponent;
    const usageData = component.usageChartData();
    const responseData = component.responseTimeChartData();

    expect(usageData.labels).toHaveLength(0);
    expect(usageData.datasets[0].data).toEqual([]);
    expect(responseData.labels).toHaveLength(0);
    expect(responseData.datasets[0].data).toEqual([]);
  });

  it('should convert response times from ms to seconds', () => {
    hostComponent.dailyResponseTimes = [{ date: '2026-02-08', averageMs: 5000 }];
    hostFixture.detectChanges();

    const component = hostFixture.debugElement.children[0].componentInstance as UsageChartComponent;
    const chartData = component.responseTimeChartData();
    expect(chartData.datasets[0].data[0]).toBe(5);
  });

  it('should format dates for chart labels', () => {
    hostComponent.dailyUsage = [{ date: '2026-02-08', count: 5 }];
    hostFixture.detectChanges();

    const component = hostFixture.debugElement.children[0].componentInstance as UsageChartComponent;
    const chartData = component.usageChartData();
    expect(chartData.labels[0]).toBe('08 Feb');
  });
});
