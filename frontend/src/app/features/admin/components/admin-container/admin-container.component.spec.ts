import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminContainerComponent } from './admin-container.component';
import { DocumentService } from '../../../../core/services/document.service';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';

describe('AdminContainerComponent', () => {
  let component: AdminContainerComponent;
  let fixture: ComponentFixture<AdminContainerComponent>;
  let documentService: jasmine.SpyObj<DocumentService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    const documentServiceSpy = jasmine.createSpyObj('DocumentService', ['loadDocuments'], {
      documents: jasmine.createSpy().and.returnValue([]),
      isLoading: jasmine.createSpy().and.returnValue(false)
    });
    const messageServiceSpy = jasmine.createSpyObj('MessageService', ['add']);

    await TestBed.configureTestingModule({
      imports: [AdminContainerComponent],
      providers: [
        { provide: DocumentService, useValue: documentServiceSpy },
        { provide: MessageService, useValue: messageServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminContainerComponent);
    component = fixture.componentInstance;
    documentService = TestBed.inject(DocumentService) as jasmine.SpyObj<DocumentService>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load documents on init', () => {
    expect(documentService.loadDocuments).toHaveBeenCalled();
  });

  it('should show success toast on upload success', () => {
    const mockDocument = {
      id: '1',
      filename: 'test.pdf',
      size: 1024,
      type: 'application/pdf',
      status: 'pending',
      uploadedAt: new Date()
    };

    component.onUploadSuccess(mockDocument);

    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        severity: 'success',
        summary: 'Upload réussi'
      })
    );
  });

  it('should show error toast on upload error', () => {
    const error = { message: 'Upload failed', details: 'Network error' };

    component.onUploadError(error);

    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        severity: 'error',
        summary: 'Erreur d\'upload'
      })
    );
  });

  it('should show info toast on document deleted', () => {
    component.onDocumentDeleted('doc-123');

    expect(messageService.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        severity: 'info',
        summary: 'Document supprimé'
      })
    );
  });
});
