import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { getApiUrl } from '../../../core/api/api.config';
import { API_ENDPOINTS } from '../../../core/api/api-endpoints';
import { AuthResponse, LoginRequest, LogoutRequest, RefreshTokenRequest } from './auth.dto';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(getApiUrl(API_ENDPOINTS.auth.login), payload);
  }

  refresh(payload: RefreshTokenRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(getApiUrl(API_ENDPOINTS.auth.refresh), payload);
  }

  logout(payload: LogoutRequest): Observable<void> {
    return this.http.post<void>(getApiUrl(API_ENDPOINTS.auth.logout), payload);
  }
}
