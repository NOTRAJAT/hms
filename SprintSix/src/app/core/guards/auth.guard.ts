import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthSessionService } from '../services/auth-session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(AuthSessionService).value;
  const router = inject(Router);

  if (!session) {
    return router.createUrlTree(['/login']);
  }

  return true;
};
