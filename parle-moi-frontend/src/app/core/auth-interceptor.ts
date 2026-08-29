import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.token();

  // N'ajoute le token que sur les routes protegees - inutile et sans effet sur les routes
  // publiques (conversations, services...), mais evite d'exposer le token a des appels
  // qui n'en ont pas besoin.
  const estRouteProtegee = req.url.includes('/api/ecoutant') || req.url.includes('/api/admin');

  if (token && estRouteProtegee) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req);
};