import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingSpinnerComponent } from './loading-spinner.component';

describe('LoadingSpinnerComponent', () => {
  let component: LoadingSpinnerComponent;
  let fixture: ComponentFixture<LoadingSpinnerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingSpinnerComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display default loading message', () => {
    const compiled = fixture.nativeElement;
    const message = compiled.querySelector('.spinner-message p');
    expect(message?.textContent).toContain('Loading...');
  });

  it('should display custom message when provided', () => {
    fixture.componentRef.setInput('message', 'Please wait...');
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const message = compiled.querySelector('.spinner-message p');
    expect(message?.textContent).toContain('Please wait...');
  });

  it('should display inline spinner by default', () => {
    const compiled = fixture.nativeElement;
    const inlineSpinner = compiled.querySelector('.spinner-inline');
    expect(inlineSpinner).toBeTruthy();
  });

  it('should display overlay spinner when overlay is true', () => {
    fixture.componentRef.setInput('overlay', true);
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const overlaySpinner = compiled.querySelector('.spinner-overlay');
    expect(overlaySpinner).toBeTruthy();
  });

  it('should apply size attribute', () => {
    fixture.componentRef.setInput('size', 'large');
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const container = compiled.querySelector('[data-size]');
    expect(container?.getAttribute('data-size')).toBe('large');
  });

  it('should contain PrimeNG progress spinner', () => {
    const compiled = fixture.nativeElement;
    const spinner = compiled.querySelector('p-progressspinner');
    expect(spinner).toBeTruthy();
  });
});
