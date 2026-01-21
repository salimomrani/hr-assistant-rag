import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageListComponent } from './message-list.component';

describe('MessageListComponent', () => {
  let component: MessageListComponent;
  let fixture: ComponentFixture<MessageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MessageListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(MessageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display empty state when no messages', () => {
    fixture.componentRef.setInput('messages', []);
    fixture.componentRef.setInput('isLoading', false);
    fixture.componentRef.setInput('streamingContent', '');
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const emptyState = compiled.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
  });

  it('should format time correctly', () => {
    const date = new Date('2024-01-21T14:30:00');
    const formatted = component.formatTime(date);
    expect(formatted).toMatch(/\d{2}:\d{2}/);
  });

  it('should format date as "Aujourd\'hui" for today', () => {
    const today = new Date();
    const formatted = component.formatDate(today);
    expect(formatted).toBe("Aujourd'hui");
  });
});
