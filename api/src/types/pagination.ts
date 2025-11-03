export interface PaginatedResponse<T> {
  data: T[];
  page: number;
  per_page: number;
  total: number;
}

export function createPaginationResponse<T>(
  data: T[],
  total: number,
  page: number | undefined,
  perPage: number | undefined
): PaginatedResponse<T> {
  const safePerPage = perPage && perPage > 0 ? perPage : 10;
  const safePage = page && page > 0 ? page : 1;
  return {
    data,
    page: safePage,
    per_page: safePerPage,
    total,
  };
}
