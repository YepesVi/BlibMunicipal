import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { getApiUrl } from '../../../../core/api/api.config';
import { API_ENDPOINTS } from '../../../../core/api/api-endpoints';
import { AuthorResponse, CreateAuthorRequest, UpdateAuthorRequest } from './authors.dto';

@Injectable({ providedIn: 'root' })
export class AuthorsApiService {
  private readonly http = inject(HttpClient);

  findAll(fullName?: string): Observable<AuthorResponse[]> {
    let params = new HttpParams();
    if (fullName) {
      params = params.set('fullName', fullName);
    }

    return this.http.get<AuthorResponse[]>(getApiUrl(API_ENDPOINTS.catalog.authors), { params });
  }

  create(payload: CreateAuthorRequest): Observable<AuthorResponse> {
    return this.http.post<AuthorResponse>(getApiUrl(API_ENDPOINTS.catalog.authors), payload);
  }

  update(authorId: number, payload: UpdateAuthorRequest): Observable<AuthorResponse> {
    return this.http.put<AuthorResponse>(
      getApiUrl(`${API_ENDPOINTS.catalog.authors}/${authorId}`),
      payload
    );
  }

  delete(authorId: number): Observable<void> {
    return this.http.delete<void>(getApiUrl(`${API_ENDPOINTS.catalog.authors}/${authorId}`));
  }
}
