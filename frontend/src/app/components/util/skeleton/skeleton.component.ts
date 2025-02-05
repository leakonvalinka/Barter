import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  templateUrl: './skeleton.component.html',
  styleUrls: ['./skeleton.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class SkeletonComponent {
  @Input() width = '100%';
  @Input() height = '20px';
  @Input() borderRadius = '4px';
  @Input() animation = true;
} 