import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Empresa {
  id: number;
  nombre: string;
  ruc: string;
  numeroEmpleados: number;
  habilitada: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class EmpresaService {
  private apiUrl = `${environment.apiUrl}/empresas`;

  constructor(private http: HttpClient) {}

  getEmpresas(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(this.apiUrl).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar las empresas'));
      })
    );
  }

  addEmpresa(empresa: Empresa): Observable<Empresa> {
    return this.http.post<Empresa>(this.apiUrl, empresa).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al agregar la empresa'));
      })
    );
  }

  updateEmpresa(id: number, empresa: Empresa): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.apiUrl}/${id}`, empresa).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al actualizar la empresa'));
      })
    );
  }

  deleteEmpresa(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al eliminar la empresa'));
      })
    );
  }

  toggleHabilitacion(id: number, habilitada: boolean): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.apiUrl}/${id}/habilitacion`, null, {
      params: { habilitada: habilitada.toString() }
    }).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'Error al cambiar la habilitación'));
      })
    );
  }

  getEmpresasPorHabilitacion(habilitada: boolean): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(`${this.apiUrl}/filtro`, {
      params: { habilitada: habilitada.toString() }
    }).pipe(
      catchError(err => {
        return throwError(() => new Error(err.error?.message || 'No se pudieron cargar las empresas'));
      })
    );
  }
}
