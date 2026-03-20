import { Injectable, inject } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, finalize, map, shareReplay, tap } from 'rxjs/operators';

import { AuthApiService } from '../../features/auth/data-access/auth-api.service';
import { AuthResponse, LoginRequest } from '../../features/auth/data-access/auth.dto';
import { AuthSessionStore } from './auth-session.store';
import { AuthSession } from './auth.types';
import { TokenStorageService } from './token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authApiService = inject(AuthApiService);
  private readonly authSessionStore = inject(AuthSessionStore);
  private readonly tokenStorageService = inject(TokenStorageService);
  private refreshInFlight$: Observable<AuthSession> | null = null;

  constructor() {
    this.bootstrapSession();
  }

  readonly session = this.authSessionStore.session;
  readonly isAuthenticatedSignal = this.authSessionStore.isAuthenticated;

  login(credentials: LoginRequest): Observable<AuthSession> {
    return this.authApiService.login(credentials).pipe(
      map((response) => this.toSession(response)),
      tap((session) => this.persistSession(session))
    );
  }

  refreshToken(): Observable<AuthSession> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    const refreshToken = this.tokenStorageService.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('Refresh token not available'));
    }

    this.refreshInFlight$ = this.authApiService.refresh({ refreshToken }).pipe(
      map((response) => this.toSession(response)),
      tap((session) => this.persistSession(session)),
      finalize(() => {
        this.refreshInFlight$ = null;
      }),
      shareReplay(1)
    );

    return this.refreshInFlight$;
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenStorageService.getRefreshToken();

    if (!refreshToken) {
      this.clearSession();
      return of(void 0);
    }

    return this.authApiService.logout({ refreshToken }).pipe(
      catchError(() => of(void 0)),
      finalize(() => this.clearSession())
    );
  }

  getAccessToken(): string | null {
    return this.tokenStorageService.getAccessToken();
  }

  isAuthenticated(): boolean {
    return this.authSessionStore.isAuthenticated();
  }

  private bootstrapSession(): void {
    const session = this.tokenStorageService.getSession();
    if (session) {
      this.authSessionStore.setSession(session);
    }
  }

  private persistSession(session: AuthSession): void {
    this.tokenStorageService.setSession(session);
    this.authSessionStore.setSession(session);
  }

  private clearSession(): void {
    this.tokenStorageService.clear();
    this.authSessionStore.clearSession();
  }

  private toSession(response: AuthResponse): AuthSession {
    return {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      tokenType: response.tokenType,
      userId: response.userId,
      username: response.username,
      role: response.role,
    };
  }
}
