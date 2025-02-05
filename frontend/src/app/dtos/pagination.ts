import {HttpParams} from '@angular/common/http';

/**
 * represents parameters for paginated query-endpoints
 */
export interface PaginationParams{
  page: number;
  pageSize: number;
}

export function paginationToHttpQueryParams(pagination: PaginationParams): HttpParams {
  return new HttpParams()
    .set('page', pagination.page.toString())
    .set('pageSize', pagination.pageSize.toString());
}

export interface PaginatedResults<T>{
  page: number;
  pageSize: number;
  total: number;

  hasMore: boolean;
  items: T[];
}
