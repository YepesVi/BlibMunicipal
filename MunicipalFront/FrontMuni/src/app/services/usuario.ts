import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Usuario } from '../models/usuario';
import { PageResponse } from '../models/page-response';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private apiUrl = `${environment.apiBaseUrl}/usuarios`;

  constructor(private http: HttpClient) { }

  getAll(page: number = 0, size: number = 10): Observable<PageResponse<Usuario>> {
    return this.http.get<PageResponse<Usuario>>(`${this.apiUrl}?page=${page}&size=${size}`);
  }
}
