import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/pages/dashboard-page/dashboard-page').then(
        (m) => m.DashboardPage
      ),
  },
  {
    path: 'books',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/catalog/books/pages/books-list-page/books-list-page').then(
        (m) => m.BooksListPage
      ),
  },
  {
    path: 'authors',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/catalog/authors/pages/authors-list-page/authors-list-page').then(
        (m) => m.AuthorsListPage
      ),
  },
  {
    path: 'categories',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/catalog/categories/pages/categories-list-page/categories-list-page').then(
        (m) => m.CategoriesListPage
      ),
  },
  {
    path: 'users',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/users/pages/users-list-page/users-list-page').then((m) => m.UsersListPage),
  },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' },
];
