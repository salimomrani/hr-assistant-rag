import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { KpiCardsComponent } from './kpi-cards.component';

@Component({
  imports: [KpiCardsComponent],
  template: `
    <app-kpi-cards
      [totalQuestionsToday]="totalQuestionsToday"
      [averageResponseTimeMs]="averageResponseTimeMs"
      [totalDocuments]="totalDocuments"
      [conversationsThisWeek]="conversationsThisWeek"
    />
  `,
})
class TestHostComponent {
  totalQuestionsToday = 42;
  averageResponseTimeMs = 1500;
  totalDocuments = 10;
  conversationsThisWeek = 120;
}

describe('KpiCardsComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
    hostFixture.detectChanges();
  });

  it('should create', () => {
    const kpiCards = hostFixture.nativeElement.querySelector('app-kpi-cards');
    expect(kpiCards).toBeTruthy();
  });

  it('should display total questions today', () => {
    const values = hostFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[0].textContent.trim()).toBe('42');
  });

  it('should display formatted response time in seconds', () => {
    const values = hostFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[1].textContent.trim()).toBe('1.5s');
  });

  it('should display total documents', () => {
    const values = hostFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[2].textContent.trim()).toBe('10');
  });

  it('should display conversations this week', () => {
    const values = hostFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[3].textContent.trim()).toBe('120');
  });

  it('should format zero response time as 0s', () => {
    const zeroFixture = TestBed.createComponent(TestHostComponent);
    zeroFixture.componentInstance.averageResponseTimeMs = 0;
    zeroFixture.detectChanges();

    const values = zeroFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[1].textContent.trim()).toBe('0s');
  });

  it('should format large response time', () => {
    const largeFixture = TestBed.createComponent(TestHostComponent);
    largeFixture.componentInstance.averageResponseTimeMs = 12345;
    largeFixture.detectChanges();

    const values = largeFixture.nativeElement.querySelectorAll('.kpi-value');
    expect(values[1].textContent.trim()).toBe('12.3s');
  });

  it('should render four kpi cards', () => {
    const cards = hostFixture.nativeElement.querySelectorAll('.kpi-card');
    expect(cards.length).toBe(4);
  });

  it('should display correct labels', () => {
    const labels = hostFixture.nativeElement.querySelectorAll('.kpi-label');
    expect(labels[0].textContent.trim()).toBe('Questions Today');
    expect(labels[1].textContent.trim()).toBe('Avg Response Time');
    expect(labels[2].textContent.trim()).toBe('Total Documents');
    expect(labels[3].textContent.trim()).toBe('Conversations This Week');
  });
});
