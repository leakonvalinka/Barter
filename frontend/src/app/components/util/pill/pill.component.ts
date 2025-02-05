import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'pill',
  standalone: true,
  imports: [],
  templateUrl: './pill.component.html',
  styleUrl: './pill.component.scss'
})
export class PillComponent {
  @Input() label: string = '';
  @Input() count?: number;
  @Input() selected: boolean = false;
  @Output() onClick = new EventEmitter<void>();
}
