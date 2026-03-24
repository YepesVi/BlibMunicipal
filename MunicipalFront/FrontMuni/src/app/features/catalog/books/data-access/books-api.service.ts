import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_ENDPOINTS } from '../../../../core/api/api-endpoints';
import { getApiUrl } from '../../../../core/api/api.config';
import { PageResponse } from '../../../../models/common/page-response.model';
import {
  BookResponse,
  AttachBookImagesRequest,
  BooksQueryParams,
  BookSummaryResponse,
  CreateBookRequest,
  UpdateBookRequest,
} from './books.dto';

@Injectable({ providedIn: 'root' })
export class BooksApiService {
  private readonly http = inject(HttpClient);

  findAll(query: BooksQueryParams = {}): Observable<PageResponse<BookSummaryResponse>> {
    let params = new HttpParams();

    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    });

    return this.http.get<PageResponse<BookSummaryResponse>>(getApiUrl(API_ENDPOINTS.catalog.books), {
      params,
    });
  }

  create(payload: CreateBookRequest): Observable<BookResponse> {
    return this.http.post<BookResponse>(getApiUrl(API_ENDPOINTS.catalog.books), payload);
  }

  findById(bookId: number): Observable<BookResponse> {
    return this.http.get<BookResponse>(getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}`));
  }

  update(bookId: number, payload: UpdateBookRequest): Observable<BookResponse> {
    return this.http.put<BookResponse>(getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}`), payload);
  }

  delete(bookId: number): Observable<void> {
    return this.http.delete<void>(getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}`));
  }

  attachImages(bookId: number, payload: AttachBookImagesRequest): Observable<BookResponse> {
    return this.http.post<BookResponse>(
      getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}/images`),
      payload
    );
  }

  setPrimaryImage(bookId: number, bookImageId: number): Observable<BookResponse> {
    return this.http.patch<BookResponse>(
      getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}/images/${bookImageId}/primary`),
      {}
    );
  }

  removeImage(bookId: number, bookImageId: number): Observable<void> {
    return this.http.delete<void>(
      getApiUrl(`${API_ENDPOINTS.catalog.books}/${bookId}/images/${bookImageId}`)
    );
  }
}
