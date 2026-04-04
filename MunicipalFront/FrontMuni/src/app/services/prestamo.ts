import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// WARNING: There is no 'Prestamo' (Loan) entity or controller in the backend!
// The backend is currently a catalog only (Books, Authors, Categories, Users).
// You will either need to add Prestamo to the Spring Boot backend or remove this.
interface Prestamo {
  id?: number;
  libroId: number;
  usuarioId: number;
}

@Injectable({
  providedIn: 'root'
})
export class PrestamoService {
  private apiUrl = `${environment.apiBaseUrl}/prestamos`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(this.apiUrl);
  }
}
