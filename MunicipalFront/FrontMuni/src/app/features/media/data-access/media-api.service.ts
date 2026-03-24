import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { getApiUrl } from '../../../core/api/api.config';
import { API_ENDPOINTS } from '../../../core/api/api-endpoints';
import { MediaAssetResponse, MediaFolder } from './media.dto';

@Injectable({ providedIn: 'root' })
export class MediaApiService {
  private readonly http = inject(HttpClient);

  uploadImage(file: File, folder: MediaFolder): Observable<MediaAssetResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<MediaAssetResponse>(getApiUrl(`${API_ENDPOINTS.media}/images?folder=${folder}`), formData);
  }
}
