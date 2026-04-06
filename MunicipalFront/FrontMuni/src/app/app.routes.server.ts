import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'login',
    renderMode: RenderMode.Client
  },
  {
    path: 'books/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'authors/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'categories/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'users/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'reports/**',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
