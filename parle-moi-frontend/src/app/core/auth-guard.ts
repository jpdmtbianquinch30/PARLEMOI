import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Role } from './auth';

export function creerGuardRole(roleRequis: Role): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.aLeRole(roleRequis)) {
      return true;
    }

    const pageLogin = roleRequis === 'ADMIN' ? '/admin/login' : '/ecoutant/login';
    router.navigate([pageLogin]);
    return false;
  };
}