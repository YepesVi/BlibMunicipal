import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { getApiUrl } from '../../../../core/api/api.config';
import { API_ENDPOINTS } from '../../../../core/api/api-endpoints';
import { CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest } from './categories.dto';

@Injectable({ providedIn: 'root' })
export class CategoriesApiService {
  private readonly http = inject(HttpClient);

  findAll(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(getApiUrl(API_ENDPOINTS.catalog.categories));
  }

  create(payload: CreateCategoryRequest): Observable<CategoryResponse> {
    return this.http.post<CategoryResponse>(getApiUrl(API_ENDPOINTS.catalog.categories), payload);
  }

  update(categoryId: number, payload: UpdateCategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(
      getApiUrl(`${API_ENDPOINTS.catalog.categories}/${categoryId}`),
      payload
    );
  }

  delete(categoryId: number): Observable<void> {
    return this.http.delete<void>(getApiUrl(`${API_ENDPOINTS.catalog.categories}/${categoryId}`));
  }
}
