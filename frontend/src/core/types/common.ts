export interface SortInfo {
  sorted: boolean;
  unsorted: boolean;
  empty: boolean;
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: SortInfo;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

export interface PageMeta {
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface Page<T> {
  content: T[];
  page: PageMeta;
}

export interface ApiError {
  status: number;
  errorCode: string;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
}
