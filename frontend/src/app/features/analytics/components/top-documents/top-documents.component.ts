import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DocumentReference } from '../../../../core/models';

@Component({
  selector: 'app-top-documents',
  imports: [],
  templateUrl: './top-documents.component.html',
  styleUrl: './top-documents.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopDocumentsComponent {
  documents = input.required<DocumentReference[]>();
}
