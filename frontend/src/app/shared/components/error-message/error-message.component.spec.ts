import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorMessageComponent } from './error-message.component';

describe('ErrorMessageComponent', () => {
  let component: ErrorMessageComponent;
  let fixture: ComponentFixture<ErrorMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorMessageComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorMessageComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('message', 'Test error message');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display message', () => {
    const compiled = fixture.nativeElement;
    const messageBody = compiled.querySelector('.message-body');
    expect(messageBody?.textContent).toContain('Test error message');
  });

  it('should display error severity by default', () => {
    const compiled = fixture.nativeElement;
    const container = compiled.querySelector('[data-severity]');
    expect(container?.getAttribute('data-severity')).toBe('error');
  });

  it('should display custom severity', () => {
    fixture.componentRef.setInput('severity', 'success');
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const container = compiled.querySelector('[data-severity]');
    expect(container?.getAttribute('data-severity')).toBe('success');
  });

  it('should display close button when closeable is true', () => {
    const compiled = fixture.nativeElement;
    const closeButton = compiled.querySelector('.message-close');
    expect(closeButton).toBeTruthy();
  });

  it('should not display close button when closeable is false', () => {
    fixture.componentRef.setInput('closeable', false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const closeButton = compiled.querySelector('.message-close');
    expect(closeButton).toBeFalsy();
  });

  it('should emit closed event when close button is clicked', () => {
    let closedEmitted = false;
    component.closed.subscribe(() => {
      closedEmitted = true;
    });

    const compiled = fixture.nativeElement;
    const closeButton = compiled.querySelector('.message-close') as HTMLButtonElement;
    closeButton?.click();

    expect(closedEmitted).toBeTruthy();
  });

  it('should display correct icon for error severity', () => {
    fixture.componentRef.setInput('severity', 'error');
    fixture.detectChanges();

    expect(component.getIcon()).toBe('pi-times-circle');
  });

  it('should display correct icon for success severity', () => {
    fixture.componentRef.setInput('severity', 'success');
    fixture.detectChanges();

    expect(component.getIcon()).toBe('pi-check-circle');
  });

  it('should display correct icon for warning severity', () => {
    fixture.componentRef.setInput('severity', 'warn');
    fixture.detectChanges();

    expect(component.getIcon()).toBe('pi-exclamation-triangle');
  });

  it('should display correct icon for info severity', () => {
    fixture.componentRef.setInput('severity', 'info');
    fixture.detectChanges();

    expect(component.getIcon()).toBe('pi-info-circle');
  });

  it('should display correct title for each severity', () => {
    const severities: Array<'success' | 'info' | 'warn' | 'error'> = ['success', 'info', 'warn', 'error'];
    const expectedTitles = ['Success', 'Information', 'Warning', 'Error'];

    severities.forEach((severity, index) => {
      fixture.componentRef.setInput('severity', severity);
      fixture.detectChanges();

      const compiled = fixture.nativeElement;
      const title = compiled.querySelector('.message-title');
      expect(title?.textContent).toBe(expectedTitles[index]);
    });
  });

  it('should display message icon', () => {
    const compiled = fixture.nativeElement;
    const icon = compiled.querySelector('.message-icon i');
    expect(icon).toBeTruthy();
  });
});
