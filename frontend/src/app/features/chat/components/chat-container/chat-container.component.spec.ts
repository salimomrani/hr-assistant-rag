import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChatContainerComponent } from './chat-container.component';
import { ConversationService } from '../../../../core/services/conversation.service';
import { ApiService } from '../../../../core/services/api.service';
import { of, throwError } from 'rxjs';

describe('ChatContainerComponent', () => {
  let component: ChatContainerComponent;
  let fixture: ComponentFixture<ChatContainerComponent>;
  let conversationService: jasmine.SpyObj<ConversationService>;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const conversationSpy = jasmine.createSpyObj('ConversationService', ['addMessage'], {
      messages: jasmine.createSpy().and.returnValue([])
    });
    const apiSpy = jasmine.createSpyObj('ApiService', ['chatStream']);

    await TestBed.configureTestingModule({
      imports: [ChatContainerComponent],
      providers: [
        { provide: ConversationService, useValue: conversationSpy },
        { provide: ApiService, useValue: apiSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatContainerComponent);
    component = fixture.componentInstance;
    conversationService = TestBed.inject(ConversationService) as jasmine.SpyObj<ConversationService>;
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle question submission', () => {
    apiService.chatStream.and.returnValue(of('Test response'));

    component.onQuestionSubmitted('Test question');

    expect(apiService.chatStream).toHaveBeenCalledWith('Test question');
    expect(component.isLoading()).toBeTruthy();
  });

  it('should handle error during streaming', () => {
    apiService.chatStream.and.returnValue(throwError(() => new Error('Test error')));

    component.onQuestionSubmitted('Test question');

    expect(component.errorMessage()).toBeTruthy();
    expect(component.isLoading()).toBeFalsy();
  });

  it('should clear error message on close', () => {
    component.errorMessage.set('Test error');
    component.onErrorClosed();
    expect(component.errorMessage()).toBe('');
  });
});
