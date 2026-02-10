import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { TopDocumentsComponent } from './top-documents.component';
import { DocumentReference } from '../../../../core/models';

@Component({
  imports: [TopDocumentsComponent],
  template: `<app-top-documents [documents]="documents" />`,
})
class TestHostComponent {
  documents: DocumentReference[] = [];
}

describe('TopDocumentsComponent', () => {
  let hostFixture: ComponentFixture<TestHostComponent>;
  let hostComponent: TestHostComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();

    hostFixture = TestBed.createComponent(TestHostComponent);
    hostComponent = hostFixture.componentInstance;
  });

  it('should create', () => {
    hostFixture.detectChanges();
    const el = hostFixture.nativeElement.querySelector('app-top-documents');
    expect(el).toBeTruthy();
  });

  it('should show empty state when no documents', () => {
    hostComponent.documents = [];
    hostFixture.detectChanges();

    const emptyState = hostFixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
    expect(emptyState.textContent).toContain('No document references recorded yet');
  });

  it('should render document list when documents exist', () => {
    hostComponent.documents = [
      { documentId: 'doc-1', documentName: 'handbook.pdf', referenceCount: 25 },
      { documentId: 'doc-2', documentName: 'leave-policy.pdf', referenceCount: 12 },
    ];
    hostFixture.detectChanges();

    const items = hostFixture.nativeElement.querySelectorAll('.document-item');
    expect(items.length).toBe(2);
  });

  it('should display document name and reference count', () => {
    hostComponent.documents = [
      { documentId: 'doc-1', documentName: 'handbook.pdf', referenceCount: 25 },
    ];
    hostFixture.detectChanges();

    const docName = hostFixture.nativeElement.querySelector('.document-name');
    const countBadge = hostFixture.nativeElement.querySelector('.count-badge');
    expect(docName.textContent).toContain('handbook.pdf');
    expect(countBadge.textContent.trim()).toBe('25');
  });

  it('should display rank numbers', () => {
    hostComponent.documents = [
      { documentId: 'doc-1', documentName: 'handbook.pdf', referenceCount: 25 },
      { documentId: 'doc-2', documentName: 'leave-policy.pdf', referenceCount: 12 },
    ];
    hostFixture.detectChanges();

    const ranks = hostFixture.nativeElement.querySelectorAll('.rank');
    expect(ranks[0].textContent.trim()).toBe('#1');
    expect(ranks[1].textContent.trim()).toBe('#2');
  });

  it('should not show empty state when documents exist', () => {
    hostComponent.documents = [
      { documentId: 'doc-1', documentName: 'handbook.pdf', referenceCount: 25 },
    ];
    hostFixture.detectChanges();

    const emptyState = hostFixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeFalsy();
  });
});
