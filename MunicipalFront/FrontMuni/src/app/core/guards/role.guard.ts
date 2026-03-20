import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { UserRole } from '../../features/auth/data-access/auth.dto';
import { AuthService } from '../auth/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const allowedRoles = (route.data['roles'] as UserRole[] | undefined) ?? [];
  const role = authService.session()?.role;

  if (allowedRoles.length === 0 || (role && allowedRoles.includes(role))) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
