export const API_ENDPOINTS = {
  auth: {
    login: '/api/auth/login',
    refresh: '/api/auth/refresh',
    logout: '/api/auth/logout',
  },
  catalog: {
    books: '/api/catalog/books',
    authors: '/api/catalog/authors',
    categories: '/api/catalog/categories',
  },
  users: '/api/users',
  media: '/api/media',
  reports: {
    booksByAuthorIdCard: '/api/reports/books/by-author-id-card',
  },
} as const;
