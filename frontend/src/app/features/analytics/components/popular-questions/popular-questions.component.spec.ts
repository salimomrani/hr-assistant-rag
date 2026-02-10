import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { PopularQuestionsComponent } from './popular-questions.component';
import { QuestionFrequency } from '../../../../core/models';

@Component({
  imports: [PopularQuestionsComponent],
  template: `<app-popular-questions [questions]="questions" />`,
})
class TestHostComponent {
  questions: QuestionFrequency[] = [];
}

describe('PopularQuestionsComponent', () => {
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
    const el = hostFixture.nativeElement.querySelector('app-popular-questions');
    expect(el).toBeTruthy();
  });

  it('should show empty state when no questions', () => {
    hostComponent.questions = [];
    hostFixture.detectChanges();

    const emptyState = hostFixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
    expect(emptyState.textContent).toContain('No questions recorded yet');
  });

  it('should render question list when questions exist', () => {
    hostComponent.questions = [
      { question: 'What is the leave policy?', count: 15 },
      { question: 'How to request time off?', count: 8 },
    ];
    hostFixture.detectChanges();

    const items = hostFixture.nativeElement.querySelectorAll('.question-item');
    expect(items.length).toBe(2);
  });

  it('should display question text and count', () => {
    hostComponent.questions = [{ question: 'What is the leave policy?', count: 15 }];
    hostFixture.detectChanges();

    const questionText = hostFixture.nativeElement.querySelector('.question-text');
    const countBadge = hostFixture.nativeElement.querySelector('.count-badge');
    expect(questionText.textContent).toContain('What is the leave policy?');
    expect(countBadge.textContent.trim()).toBe('15');
  });

  it('should display rank numbers', () => {
    hostComponent.questions = [
      { question: 'Q1', count: 10 },
      { question: 'Q2', count: 5 },
    ];
    hostFixture.detectChanges();

    const ranks = hostFixture.nativeElement.querySelectorAll('.rank');
    expect(ranks[0].textContent.trim()).toBe('#1');
    expect(ranks[1].textContent.trim()).toBe('#2');
  });

  it('should truncate long questions', () => {
    const longQuestion = 'A'.repeat(150);
    hostComponent.questions = [{ question: longQuestion, count: 5 }];
    hostFixture.detectChanges();

    const questionText = hostFixture.nativeElement.querySelector('.question-text');
    expect(questionText.textContent).toContain('...');
    expect(questionText.textContent.length).toBeLessThan(150);
  });

  it('should not truncate short questions', () => {
    hostComponent.questions = [{ question: 'Short question', count: 5 }];
    hostFixture.detectChanges();

    const questionText = hostFixture.nativeElement.querySelector('.question-text');
    expect(questionText.textContent).toContain('Short question');
    expect(questionText.textContent).not.toContain('...');
  });

  it('should not show empty state when questions exist', () => {
    hostComponent.questions = [{ question: 'Q1', count: 10 }];
    hostFixture.detectChanges();

    const emptyState = hostFixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeFalsy();
  });
});
