import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthSessionService } from '../services/auth-session.service';

export const adminOrStaffGuard: CanActivateFn = () => {
  const sessionService = inject(AuthSessionService);
  const router = inject(Router);

  const canAccess = (role: string | undefined) => role === 'ADMIN' || role === 'STAFF';

  const current = sessionService.value;
  if (current) {
    return canAccess(current.role) ? true : router.createUrlTree(['/dashboard']);
  }

  return sessionService.hydrateFromServer().pipe(
    map((resolved) => {
      if (!resolved) {
        return router.createUrlTree(['/login']);
      }
      return canAccess(resolved.role) ? true : router.createUrlTree(['/dashboard']);
    })
  );
};

