import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { getApiUrl } from '../../../core/api/api.config';
import { API_ENDPOINTS } from '../../../core/api/api-endpoints';
import { BooksByAuthorReportResponse } from './reports.dto';

@Injectable({ providedIn: 'root' })
export class ReportsApiService {
  private readonly http = inject(HttpClient);

  getBooksByAuthorIdCard(idCard: string): Observable<BooksByAuthorReportResponse> {
    return this.http.get<BooksByAuthorReportResponse>(
      getApiUrl(`${API_ENDPOINTS.reports.booksByAuthorIdCard}/${encodeURIComponent(idCard)}`)
    );
  }

  downloadBooksByAuthorIdCardPdf(idCard: string): Observable<Blob> {
    return this.http.get(
      getApiUrl(`${API_ENDPOINTS.reports.booksByAuthorIdCard}/${encodeURIComponent(idCard)}/pdf`),
      { responseType: 'blob' }
    );
  }
}
