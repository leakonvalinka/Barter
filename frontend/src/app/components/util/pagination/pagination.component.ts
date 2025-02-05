import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIcon } from '@ng-icons/core';
import {
  heroChevronDoubleLeft,
  heroChevronDoubleRight,
  heroChevronLeft,
  heroChevronRight,
} from '@ng-icons/heroicons/outline';
import { PaginationRangePipe } from '../../../pipes/pagination-range.pipe';
import { NgpPagination, NgpPaginationButton, NgpPaginationFirst, NgpPaginationLast, NgpPaginationNext, NgpPaginationPrevious } from 'ng-primitives/pagination';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [
    CommonModule,
    NgIcon,
    PaginationRangePipe,
    NgpPagination,
    NgpPaginationButton,
    NgpPaginationFirst,
    NgpPaginationLast,
    NgpPaginationNext,
    NgpPaginationPrevious,
  ],
  templateUrl: './pagination.component.html',
})
export class PaginationComponent {
  @Input() currentPage: number = 1;
  @Input() totalPages: number = 1;
  @Output() pageChange = new EventEmitter<number>();

  isLargeScreen(): boolean {
    return window.innerWidth >= 640; // 640px is the 'sm' breakpoint in Tailwind
  }
} 