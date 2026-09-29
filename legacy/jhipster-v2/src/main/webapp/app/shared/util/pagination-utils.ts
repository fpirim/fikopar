export interface IPaginationState {
  search: string;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDir: string;
}

export const getUrlParameter = (name: string, search: string): string => {
  const url = new URL(`http://localhost${search}`); // using a dummy url for parsing
  return url.searchParams.get(name) || '';
};

export const getSortState = (
  location: { search: string },
  pageSize: number,
  search = '',
  sortBy = 'id',
  sortDir = 'asc'
): IPaginationState => {
  const searchParam = getUrlParameter('search', location.search);
  const pageParam = getUrlParameter('page', location.search);
  const sortByParam = getUrlParameter('sortBy', location.search);
  const sortDirParam = getUrlParameter('sortDir', location.search);
  let newSearch = search;
  let newSortBy = sortBy;
  let newSortDir = sortDir;
  let newPage = 0;
  if (searchParam !== null && searchParam !== undefined) {
    newSearch = searchParam;
  }
  if (pageParam !== '' && !isNaN(parseInt(pageParam, 10))) {
    newPage = parseInt(pageParam, 10);
  }
  if (sortByParam !== '') {
    newSortBy = sortByParam;
  }
  if (sortDirParam !== '') {
    newSortDir = sortDirParam;
  }
  return {
    search: newSearch,
    page: newPage,
    pageSize,
    sortBy: newSortBy,
    sortDir: newSortDir,
  };
};

export const overridePaginationStateWithQueryParams = (paginationBaseState: IPaginationState, locationSearch: string) => {
  const params = new URLSearchParams(locationSearch);
  const search = params.get('search');
  const page = params.get('page');
  const sortBy = params.get('sortBy');
  const sortDir = params.get('sortDir');
  if (search !== null && search !== undefined) {
    paginationBaseState.search = search;
  }
  if (page) {
    paginationBaseState.page = +page;
  }
  if (sortBy) {
    paginationBaseState.sortBy = sortBy;
  }
  if (sortDir) {
    paginationBaseState.sortDir = sortDir;
  }
  return paginationBaseState;
};
