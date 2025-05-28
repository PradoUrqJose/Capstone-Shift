import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Puesto {
  id?: number;
  nombre: string;
  descripcion?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PuestoService {
  private apiUrl = `${environment.apiUrl}/puestos`;

  constructor(private http: HttpClient) {}

  getPuestos(): Observable<Puesto[]> {
    return this.http.get<Puesto[]>(this.apiUrl).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar los puestos'));
      })
    );
  }

  getPuestoById(id: number): Observable<Puesto> {
    return this.http.get<Puesto>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Puesto no encontrado'));
      })
    );
  }

  addPuesto(puesto: Puesto): Observable<Puesto> {
    return this.http.post<Puesto>(this.apiUrl, puesto).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al agregar el puesto'));
      })
    );
  }

  updatePuesto(id: number, puesto: Puesto): Observable<Puesto> {
    return this.http.put<Puesto>(`${this.apiUrl}/${id}`, puesto).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al actualizar el puesto'));
      })
    );
  }

  deletePuesto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al eliminar el puesto'));
      })
    );
  }
}
