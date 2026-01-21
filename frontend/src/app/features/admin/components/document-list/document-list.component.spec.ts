import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentListComponent } from './document-list.component';
import { DocumentService } from '../../../../core/services/document.service';
import { ConfirmationService } from 'primeng/api';
import { of, throwError } from 'rxjs';

describe('DocumentListComponent', () => {
  let component: DocumentListComponent;
  let fixture: ComponentFixture<DocumentListComponent>;
  let documentService: jasmine.SpyObj<DocumentService>;
  let confirmationService: jasmine.SpyObj<ConfirmationService>;

  beforeEach(async () => {
    const docServiceSpy = jasmine.createSpyObj('DocumentService', ['deleteDocument']);
    const confirmServiceSpy = jasmine.createSpyObj('ConfirmationService', ['confirm']);

    await TestBed.configureTestingModule({
      imports: [DocumentListComponent],
      providers: [
        { provide: DocumentService, useValue: docServiceSpy },
        { provide: ConfirmationService, useValue: confirmServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentListComponent);
    component = fixture.componentInstance;
    documentService = TestBed.inject(DocumentService) as jasmine.SpyObj<DocumentService>;
    confirmationService = TestBed.inject(ConfirmationService) as jasmine.SpyObj<ConfirmationService>;
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
    const mockDoc = {
      id: '1',
      filename: 'test.pdf',
      size: 1024,
      type: 'application/pdf',
      status: 'indexed',
      uploadedAt: new Date()
    };

    component.confirmDelete(mockDoc);

    expect(confirmationService.confirm).toHaveBeenCalled();
  });
});
