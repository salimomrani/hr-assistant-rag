import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageInputComponent } from './message-input.component';

describe('MessageInputComponent', () => {
  let component: MessageInputComponent;
  let fixture: ComponentFixture<MessageInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessageInputComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MessageInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate empty question as invalid', () => {
    component.questionText.set('');
    expect(component.isValid()).toBeFalsy();
  });

  it('should validate non-empty question as valid', () => {
    component.questionText.set('Test question');
    expect(component.isValid()).toBeTruthy();
  });

  it('should validate question over 1000 chars as invalid', () => {
    component.questionText.set('a'.repeat(1001));
    expect(component.isValid()).toBeFalsy();
    expect(component.isOverLimit()).toBeTruthy();
  });

  it('should emit questionSubmitted on submit', (done) => {
    component.questionText.set('Test question');
    component.questionSubmitted.subscribe((question: string) => {
      expect(question).toBe('Test question');
      done();
    });
    component.onSubmit();
  });

  it('should clear input after submit', () => {
    component.questionText.set('Test question');
    component.onSubmit();
    expect(component.questionText()).toBe('');
  });
});
