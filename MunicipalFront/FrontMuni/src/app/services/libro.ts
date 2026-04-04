import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Libro, LibroSummary } from '../models/libro';
import { PageResponse } from '../models/page-response';

@Injectable({
  providedIn: 'root'
})
export class LibroService {
  private apiUrl = `${environment.apiBaseUrl}/catalog/books`;

  constructor(private http: HttpClient) { }

  getAll(page: number = 0, size: number = 10): Observable<PageResponse<LibroSummary>> {
    return this.http.get<PageResponse<LibroSummary>>(`${this.apiUrl}?page=${page}&size=${size}`);
  }

  create(libro: Omit<Libro, 'id'>): Observable<Libro> {
    return this.http.post<Libro>(this.apiUrl, libro);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
