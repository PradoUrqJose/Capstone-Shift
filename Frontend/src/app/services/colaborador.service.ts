import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Colaborador {
  id: number;
  nombre: string;
  apellido: string;
  dni: string;
  email: string;
  telefono: string;
  empresaId: number;
  empresaNombre: string;
  fotoUrl?: string;
  habilitado: boolean;
  fechaNacimiento?: string;
  puestoId?: number;
  puestoNombre?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ColaboradorService {
  private apiUrl = `${environment.apiUrl}/colaboradores`;

  constructor(private http: HttpClient) {}

  getColaboradores(): Observable<Colaborador[]> {
    return this.http.get<Colaborador[]>(this.apiUrl).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar los colaboradores'));
      })
    );
  }

  getColaboradoresByEmpresa(empresaId: number): Observable<Colaborador[]> {
    return this.http.get<Colaborador[]>(`${this.apiUrl}/empresa/${empresaId}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar los colaboradores'));
      })
    );
  }

  addColaborador(colaborador: Colaborador, file?: File): Observable<Colaborador> {
    const formData = new FormData();
    formData.append('colaborador', new Blob([JSON.stringify(colaborador)], { type: 'application/json' }));
    if (file) {
      formData.append('file', file);
    }
    return this.http.post<Colaborador>(this.apiUrl, formData).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al agregar el colaborador'));
      })
    );
  }

  updateColaborador(id: number, colaborador: Colaborador, file?: File): Observable<Colaborador> {
    const formData = new FormData();
    formData.append('colaborador', new Blob([JSON.stringify(colaborador)], { type: 'application/json' }));
    if (file) {
      formData.append('file', file);
    }
    return this.http.put<Colaborador>(`${this.apiUrl}/${id}`, formData).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al actualizar el colaborador'));
      })
    );
  }

  deleteColaborador(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al eliminar el colaborador'));
      })
    );
  }

  toggleHabilitacion(id: number, habilitado: boolean): Observable<Colaborador> {
    return this.http.put<Colaborador>(`${this.apiUrl}/${id}/habilitacion`, null, {
      params: { habilitado: habilitado.toString() }
    }).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al cambiar la habilitación'));
      })
    );
  }

  getColaboradoresPorHabilitacion(habilitado: boolean): Observable<Colaborador[]> {
    return this.http.get<Colaborador[]>(`${this.apiUrl}/filtro`, {
      params: { habilitado: habilitado.toString() }
    }).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar los colaboradores'));
      })
    );
  }

  getColaboradorById(id: number): Observable<Colaborador> {
    return this.http.get<Colaborador>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Colaborador no encontrado'));
      })
    );
  }
}
