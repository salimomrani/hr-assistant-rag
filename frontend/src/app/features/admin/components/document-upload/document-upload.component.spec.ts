import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentUploadComponent } from './document-upload.component';
import { DocumentService } from '../../../../core/services/document.service';
import { of, throwError } from 'rxjs';

describe('DocumentUploadComponent', () => {
  let component: DocumentUploadComponent;
  let fixture: ComponentFixture<DocumentUploadComponent>;
  let documentService: jasmine.SpyObj<DocumentService>;

  beforeEach(async () => {
    const serviceSpy = jasmine.createSpyObj('DocumentService', ['uploadDocument']);

    await TestBed.configureTestingModule({
      imports: [DocumentUploadComponent],
      providers: [
        { provide: DocumentService, useValue: serviceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentUploadComponent);
    component = fixture.componentInstance;
    documentService = TestBed.inject(DocumentService) as jasmine.SpyObj<DocumentService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should accept PDF files', () => {
    const file = new File(['test'], 'test.pdf', { type: 'application/pdf' });
    const event = { currentFiles: [file] };

    component.onSelect(event);

    expect(component.selectedFile()).toBe(file);
    expect(component.errorMessage()).toBe('');
  });

  it('should accept TXT files', () => {
    const file = new File(['test'], 'test.txt', { type: 'text/plain' });
    const event = { currentFiles: [file] };

    component.onSelect(event);

    expect(component.selectedFile()).toBe(file);
    expect(component.errorMessage()).toBe('');
  });

  it('should reject files larger than 10MB', () => {
    const largeFile = new File(['x'.repeat(11 * 1024 * 1024)], 'large.pdf', { type: 'application/pdf' });
    const event = { currentFiles: [largeFile] };

    component.onSelect(event);

    expect(component.selectedFile()).toBeNull();
    expect(component.errorMessage()).toContain('trop volumineux');
  });

  it('should reject invalid file types', () => {
    const invalidFile = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
    const event = { currentFiles: [invalidFile] };

    component.onSelect(event);

    expect(component.selectedFile()).toBeNull();
    expect(component.errorMessage()).toContain('non accepté');
  });

  it('should format file sizes correctly', () => {
    expect(component['formatBytes'](0)).toBe('0 B');
    expect(component['formatBytes'](1024)).toBe('1 KB');
    expect(component['formatBytes'](1024 * 1024)).toBe('1 MB');
  });

  it('should clear selected file on remove', () => {
    const file = new File(['test'], 'test.pdf', { type: 'application/pdf' });
    component.selectedFile.set(file);

    component.onRemove();

    expect(component.selectedFile()).toBeNull();
    expect(component.uploadProgress()).toBe(0);
  });
});
