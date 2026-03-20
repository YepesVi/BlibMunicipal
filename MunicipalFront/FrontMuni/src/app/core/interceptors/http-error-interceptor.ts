import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { ApiErrorResponse } from '../../models/common/api-error.model';

let sessionExpiryHandled = false;

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const isAuthRequest = req.url.includes('/api/auth/login') || req.url.includes('/api/auth/refresh');

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      if (error.status === 401 && !isAuthRequest) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            const refreshedToken = authService.getAccessToken();
            if (!refreshedToken) {
              return throwError(() => new Error('Session expired'));
            }

            return next(
              req.clone({
                setHeaders: {
                  Authorization: `Bearer ${refreshedToken}`,
                },
              })
            );
          }),
          catchError(() => {
            if (!sessionExpiryHandled) {
              sessionExpiryHandled = true;
              authService.logout().subscribe({
                next: () => {
                  router.navigateByUrl('/login');
                  sessionExpiryHandled = false;
                },
                error: () => {
                  router.navigateByUrl('/login');
                  sessionExpiryHandled = false;
                },
              });
            }
            return throwError(() => new Error('Session expired. Please sign in again.'));
          })
        );
      }

      const apiError = error.error as ApiErrorResponse | undefined;

      if (apiError?.message) {
        return throwError(() => new Error(apiError.message));
      }

      if (error.message) {
        return throwError(() => new Error(error.message));
      }

      return throwError(() => error);
    })
  );
};
