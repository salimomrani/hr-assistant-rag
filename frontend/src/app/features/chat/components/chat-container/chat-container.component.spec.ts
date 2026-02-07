import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChatContainerComponent } from './chat-container.component';
import { ConversationService } from '../../../../core/services/conversation.service';
import { ApiService } from '../../../../core/services/api.service';
import { DocumentService } from '../../../../core/services/document.service';
import { vi } from 'vitest';
import { of, throwError, EMPTY } from 'rxjs';
import { signal } from '@angular/core';

describe('ChatContainerComponent', () => {
  let component: ChatContainerComponent;
  let fixture: ComponentFixture<ChatContainerComponent>;
  let conversationService: {
    addMessage: ReturnType<typeof vi.fn>;
    messages: ReturnType<typeof signal>;
  };
  let apiService: { chatStream: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    conversationService = {
      addMessage: vi.fn(),
      messages: signal([]),
    };
    apiService = { chatStream: vi.fn() };

    const documentServiceMock = {
      documents: signal([]),
      isLoading: signal(false),
      loadDocuments: vi.fn().mockReturnValue(EMPTY),
    };

    await TestBed.configureTestingModule({
      imports: [ChatContainerComponent],
      providers: [
        { provide: ConversationService, useValue: conversationService },
        { provide: ApiService, useValue: apiService },
        { provide: DocumentService, useValue: documentServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ChatContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle question submission', () => {
    apiService.chatStream.mockReturnValue(of('Test response'));

    component.onQuestionSubmitted('Test question');

    expect(apiService.chatStream).toHaveBeenCalledWith('Test question', undefined);
  });

  it('should handle error during streaming', () => {
    apiService.chatStream.mockReturnValue(throwError(() => new Error('Test error')));

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
