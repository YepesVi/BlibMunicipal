import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_ENDPOINTS } from '../../../core/api/api-endpoints';
import { getApiUrl } from '../../../core/api/api.config';
import { PageResponse } from '../../../models/common/page-response.model';
import { CreateUserRequest, UpdateUserRequest, UserResponse, UsersQueryParams } from './users.dto';

@Injectable({ providedIn: 'root' })
export class UsersApiService {
  private readonly http = inject(HttpClient);

  findAll(query: UsersQueryParams = {}): Observable<PageResponse<UserResponse>> {
    let params = new HttpParams();

    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });

    return this.http.get<PageResponse<UserResponse>>(getApiUrl(API_ENDPOINTS.users), { params });
  }

  create(payload: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(getApiUrl(API_ENDPOINTS.users), payload);
  }

  update(userId: number, payload: UpdateUserRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(getApiUrl(`${API_ENDPOINTS.users}/${userId}`), payload);
  }

  delete(userId: number): Observable<void> {
    return this.http.delete<void>(getApiUrl(`${API_ENDPOINTS.users}/${userId}`));
  }
}
