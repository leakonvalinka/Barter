import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'paginationRange',
  standalone: true
})
export class PaginationRangePipe implements PipeTransform {
  transform(totalPages: number, currentPage: number = 1): (number | string)[] {
    const isMobile = window.innerWidth < 640; // 640px is the 'sm' breakpoint in Tailwind
    const maxPagesShown = isMobile ? 3 : 7; // Show fewer pages on mobile
    const range: (number | string)[] = [];
    
    if (totalPages <= maxPagesShown) {
      // If total pages is less than max shown, just show all pages
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    // Always add first page
    range.push(1);

    if (isMobile) {
      // Simplified mobile view
      if (currentPage <= 2) {
        range.push(2);
        range.push('...');
      } else if (currentPage >= totalPages - 1) {
        range.push('...');
        range.push(totalPages - 1);
      } else {
        range.push('...');
        range.push(currentPage);
        range.push('...');
      }
      range.push(totalPages);
      return range;
    }

    const leftSide = currentPage - 1;
    const rightSide = totalPages - currentPage;

    // If current page is close to start
    if (leftSide <= 3) {
      for (let i = 2; i <= Math.min(5, totalPages - 1); i++) {
        range.push(i);
      }
      if (totalPages > 6) {
        range.push('...');
      }
      range.push(totalPages);
      return range;
    }

    // If current page is close to end
    if (rightSide <= 3) {
      if (totalPages > 6) {
        range.push('...');
      }
      for (let i = Math.max(totalPages - 4, 2); i <= totalPages; i++) {
        range.push(i);
      }
      return range;
    }

    // If current page is in middle
    range.push('...');
    range.push(currentPage - 1);
    range.push(currentPage);
    range.push(currentPage + 1);
    range.push('...');
    range.push(totalPages);

    return range;
  }
} 