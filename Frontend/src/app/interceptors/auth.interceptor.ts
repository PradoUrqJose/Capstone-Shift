import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  console.log('🔍 Interceptor: URL:', req.url, 'Token:', token ? 'Presente' : 'Ausente');

  if (token && !req.url.includes('/auth/login')) {
    console.log('🔑 Interceptor: Añadiendo Authorization: Bearer', token);
    const cloned = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(cloned);
  }
  console.log('🚫 Interceptor: Sin token o URL de login, pasando sin cambios');
  return next(req);
};
