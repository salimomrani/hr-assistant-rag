import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AnalyticsPageComponent } from './analytics-page.component';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { DashboardAnalytics } from '../../../../core/models';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

const mockAnalytics: DashboardAnalytics = {
  totalQuestionsToday: 42,
  averageResponseTimeMs: 1500,
  totalDocuments: 10,
  conversationsThisWeek: 120,
  popularQuestions: [
    { question: 'What is the leave policy?', count: 15 },
    { question: 'How to request time off?', count: 8 },
  ],
  topDocuments: [{ documentId: 'doc-1', documentName: 'handbook.pdf', referenceCount: 25 }],
  dailyUsage: [{ date: '2026-02-08', count: 5 }],
  dailyResponseTimes: [{ date: '2026-02-08', averageMs: 1200.5 }],
};

describe('AnalyticsPageComponent', () => {
  let component: AnalyticsPageComponent;
  let fixture: ComponentFixture<AnalyticsPageComponent>;
  let analyticsServiceMock: { getDashboardAnalytics$: ReturnType<typeof vi.fn> };

  function createComponent() {
    fixture = TestBed.createComponent(AnalyticsPageComponent);
    component = fixture.componentInstance;
  }

  describe('with data', () => {
    beforeEach(async () => {
      analyticsServiceMock = {
        getDashboardAnalytics$: vi.fn().mockReturnValue(of(mockAnalytics)),
      };

      await TestBed.configureTestingModule({
        imports: [AnalyticsPageComponent],
        providers: [
          { provide: AnalyticsService, useValue: analyticsServiceMock },
          provideRouter([]),
          provideHttpClient(),
          provideHttpClientTesting(),
        ],
      }).compileComponents();

      createComponent();
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should set loading to false after data loads', () => {
      expect(component.loading()).toBe(false);
    });

    it('should set analytics data', () => {
      expect(component.analytics()).toEqual(mockAnalytics);
    });

    it('should not have error', () => {
      expect(component.error()).toBeNull();
    });

    it('should render kpi-cards component', () => {
      const kpiCards = fixture.nativeElement.querySelector('app-kpi-cards');
      expect(kpiCards).toBeTruthy();
    });

    it('should render popular-questions component', () => {
      const popularQuestions = fixture.nativeElement.querySelector('app-popular-questions');
      expect(popularQuestions).toBeTruthy();
    });

    it('should render top-documents component', () => {
      const topDocuments = fixture.nativeElement.querySelector('app-top-documents');
      expect(topDocuments).toBeTruthy();
    });

    it('should render usage-chart component', () => {
      const usageChart = fixture.nativeElement.querySelector('app-usage-chart');
      expect(usageChart).toBeTruthy();
    });

    it('should not show loading spinner', () => {
      const loadingState = fixture.nativeElement.querySelector('.loading-state');
      expect(loadingState).toBeFalsy();
    });

    it('should not show error state', () => {
      const errorState = fixture.nativeElement.querySelector('.error-state');
      expect(errorState).toBeFalsy();
    });
  });

  describe('loading state', () => {
    beforeEach(async () => {
      // Never-completing observable to keep loading state
      analyticsServiceMock = {
        getDashboardAnalytics$: vi.fn().mockReturnValue(new (await import('rxjs')).Subject()),
      };

      await TestBed.configureTestingModule({
        imports: [AnalyticsPageComponent],
        providers: [
          { provide: AnalyticsService, useValue: analyticsServiceMock },
          provideRouter([]),
          provideHttpClient(),
          provideHttpClientTesting(),
        ],
      }).compileComponents();

      createComponent();
      fixture.detectChanges();
    });

    it('should show loading state initially', () => {
      expect(component.loading()).toBe(true);
    });

    it('should display loading spinner', () => {
      const loadingState = fixture.nativeElement.querySelector('.loading-state');
      expect(loadingState).toBeTruthy();
    });
  });

  describe('error state', () => {
    beforeEach(async () => {
      analyticsServiceMock = {
        getDashboardAnalytics$: vi
          .fn()
          .mockReturnValue(throwError(() => new Error('Server error'))),
      };

      await TestBed.configureTestingModule({
        imports: [AnalyticsPageComponent],
        providers: [
          { provide: AnalyticsService, useValue: analyticsServiceMock },
          provideRouter([]),
          provideHttpClient(),
          provideHttpClientTesting(),
        ],
      }).compileComponents();

      createComponent();
      fixture.detectChanges();
    });

    it('should set loading to false on error', () => {
      expect(component.loading()).toBe(false);
    });

    it('should set error message', () => {
      expect(component.error()).toBe('Failed to load analytics data');
    });

    it('should display error state', () => {
      const errorState = fixture.nativeElement.querySelector('.error-state');
      expect(errorState).toBeTruthy();
    });

    it('should not render dashboard components', () => {
      const kpiCards = fixture.nativeElement.querySelector('app-kpi-cards');
      expect(kpiCards).toBeFalsy();
    });
  });
});
