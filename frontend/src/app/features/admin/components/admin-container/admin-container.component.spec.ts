import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminContainerComponent } from './admin-container.component';
import { DocumentService } from '../../../../core/services/document.service';
import { MessageService } from 'primeng/api';
import { vi } from 'vitest';
import { EMPTY } from 'rxjs';
import { signal } from '@angular/core';
import { Document, DocumentStatus } from '../../../../core/models';

describe('AdminContainerComponent', () => {
  let component: AdminContainerComponent;
  let fixture: ComponentFixture<AdminContainerComponent>;
  let documentServiceMock: {
    loadDocuments: ReturnType<typeof vi.fn>;
    documents: ReturnType<typeof signal>;
    isLoading: ReturnType<typeof signal>;
  };
  let messageService: MessageService;

  beforeEach(async () => {
    documentServiceMock = {
      loadDocuments: vi.fn().mockReturnValue(EMPTY),
      documents: signal([]),
      isLoading: signal(false),
    };

    await TestBed.configureTestingModule({
      imports: [AdminContainerComponent],
      providers: [{ provide: DocumentService, useValue: documentServiceMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminContainerComponent);
    component = fixture.componentInstance;

    // Get the real MessageService from the component's own injector (component-level provider)
    messageService = fixture.debugElement.injector.get(MessageService);
    vi.spyOn(messageService, 'add');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load documents on init', () => {
    expect(documentServiceMock.loadDocuments).toHaveBeenCalled();
  });

  it('should show success toast on upload success', () => {
    const mockDocument: Document = {
      id: '1',
      filename: 'test.pdf',
      fileType: 'PDF',
      fileSizeBytes: 1024,
      status: DocumentStatus.PENDING,
      uploadTimestamp: new Date(),
    };

    component.onUploadSuccess(mockDocument);

    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'success',
        summary: 'Upload réussi',
      }),
    );
  });

  it('should show error toast on upload error', () => {
    const error = { message: 'Upload failed', details: 'Network error' };

    component.onUploadError(error);

    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'error',
        summary: "Erreur d'upload",
      }),
    );
  });

  it('should show info toast on document deleted', () => {
    component.onDocumentDeleted('doc-123');

    expect(messageService.add).toHaveBeenCalledWith(
      expect.objectContaining({
        severity: 'info',
        summary: 'Document supprimé',
      }),
    );
  });
});
