import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthSessionService } from '../services/auth-session.service';

export const adminGuard: CanActivateFn = () => {
  const sessionService = inject(AuthSessionService);
  const router = inject(Router);

  const session = sessionService.value;
  if (session) {
    return session.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
  }

  return sessionService.hydrateFromServer().pipe(
    map((resolved) => {
      if (!resolved) {
        return router.createUrlTree(['/login']);
      }
      return resolved.role === 'ADMIN' ? true : router.createUrlTree(['/dashboard']);
    })
  );
};
