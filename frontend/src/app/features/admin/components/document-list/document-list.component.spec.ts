import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentListComponent } from './document-list.component';
import { DocumentService } from '../../../../core/services/document.service';
import { ConfirmationService } from 'primeng/api';
import { vi } from 'vitest';
import { Document, DocumentStatus } from '../../../../core/models';

describe('DocumentListComponent', () => {
  let component: DocumentListComponent;
  let fixture: ComponentFixture<DocumentListComponent>;
  let documentServiceMock: { deleteDocument: ReturnType<typeof vi.fn> };
  let confirmationService: ConfirmationService;

  beforeEach(async () => {
    documentServiceMock = { deleteDocument: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [DocumentListComponent],
      providers: [{ provide: DocumentService, useValue: documentServiceMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentListComponent);
    component = fixture.componentInstance;

    // Get the real ConfirmationService from the component's own injector (component-level provider)
    confirmationService = fixture.debugElement.injector.get(ConfirmationService);
    vi.spyOn(confirmationService, 'confirm');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should format file sizes correctly', () => {
    expect(component.formatFileSize(0)).toBe('0 B');
    expect(component.formatFileSize(1024)).toBe('1 KB');
    expect(component.formatFileSize(1024 * 1024)).toBe('1 MB');
  });

  it('should get correct status severity', () => {
    expect(component.getStatusSeverity('indexed')).toBe('success');
    expect(component.getStatusSeverity('pending')).toBe('warn');
    expect(component.getStatusSeverity('failed')).toBe('danger');
  });

  it('should get correct status label', () => {
    expect(component.getStatusLabel('indexed')).toBe('Indexé');
    expect(component.getStatusLabel('pending')).toBe('En cours');
    expect(component.getStatusLabel('failed')).toBe('Échec');
  });

  it('should extract file type from filename', () => {
    expect(component.getFileType('document.pdf')).toBe('PDF');
    expect(component.getFileType('notes.txt')).toBe('TXT');
  });

  it('should format dates in French locale', () => {
    const date = new Date('2024-01-21T14:30:00');
    const formatted = component.formatDate(date);

    expect(formatted).toContain('janvier');
    expect(formatted).toContain('2024');
  });

  it('should confirm before deleting', () => {
    const mockDoc: Document = {
      id: '1',
      filename: 'test.pdf',
      fileType: 'PDF',
      fileSizeBytes: 1024,
      status: DocumentStatus.INDEXED,
      uploadTimestamp: new Date(),
    };

    component.confirmDelete(mockDoc);

    expect(confirmationService.confirm).toHaveBeenCalled();
  });
});
