import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<{ token: string }> {
    const payload = { username, password };
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    console.log('🔍 Enviando solicitud de login:', JSON.stringify(payload));

    return this.http.post(`${this.apiUrl}/login`, payload, { headers, responseType: 'text' }).pipe(
      tap(response => console.log('✅ Respuesta del backend (texto):', response)),
      map(response => {
        // La respuesta es el token como string
        if (!response || typeof response !== 'string') {
          console.error('❌ Respuesta inválida:', response);
          throw new Error('La respuesta no es un token válido');
        }
        return { token: response };
      }),
      tap(response => {
        console.log('💾 Guardando token:', response.token);
        localStorage.setItem('token', response.token);
      }),
      catchError(err => {
        console.error('🚨 Error en la solicitud de login:', err);
        const errorMessage = err.error?.message || err.message || 'Error al iniciar sesión';
        console.error('🚨 Mensaje de error:', errorMessage);
        return throwError(() => new Error(errorMessage));
      })
    );
  }

  logout(): void {
    console.log('🔒 Cerrando sesión, eliminando token');
    localStorage.removeItem('token');
  }

  getToken(): string | null {
    const token = localStorage.getItem('token');
    console.log('🔑 Obteniendo token:', token ? 'Token presente' : 'No hay token');
    return token;
  }

  isLoggedIn(): boolean {
    const loggedIn = !!this.getToken();
    console.log('🔍 Verificando login:', loggedIn);
    return loggedIn;
  }
}
