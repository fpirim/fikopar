export interface IListState {
  search: string;
  sortBy: string;
  sortDir: string;
}

export const getUrlParameter = (name: string, search: string): string => {
  const url = new URL(`http://localhost${search}`); // using a dummy url for parsing
  return url.searchParams.get(name) || '';
};

export const getSortState = (location: { search: string }, search = '', sortBy = 'id', sortDir = 'asc'): IListState => {
  const searchParam = getUrlParameter('search', location.search);
  const sortByParam = getUrlParameter('sortBy', location.search);
  const sortDirParam = getUrlParameter('sortDir', location.search);
  let newSearch = search;
  let newSortBy = sortBy;
  let newSortDir = sortDir;
  let newPage = 0;
  if (searchParam !== null && searchParam !== undefined) {
    newSearch = searchParam;
  }
  if (sortByParam !== '') {
    newSortBy = sortByParam;
  }
  if (sortDirParam !== '') {
    newSortDir = sortDirParam;
  }
  return {
    search: newSearch,
    sortBy: newSortBy,
    sortDir: newSortDir,
  };
};

export const overrideListStateWithQueryParams = (listBaseState: IListState, locationSearch: string) => {
  const params = new URLSearchParams(locationSearch);
  const search = params.get('search');
  const sortBy = params.get('sortBy');
  const sortDir = params.get('sortDir');
  if (search !== null && search !== undefined) {
    listBaseState.search = search;
  }
  if (sortBy) {
    listBaseState.sortBy = sortBy;
  }
  if (sortDir) {
    listBaseState.sortDir = sortDir;
  }
  return listBaseState;
};
