import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthSessionService } from '../services/auth-session.service';

function landingRoute(role: string): string {
  if (role === 'ADMIN') {
    return '/admin/home';
  }
  if (role === 'STAFF') {
    return '/admin/complaints';
  }
  return '/dashboard';
}

export const authGuard: CanActivateFn = (_route, state) => {
  const sessionService = inject(AuthSessionService);
  const router = inject(Router);
  const targetUrl = state.url;

  const resolveRoute = () => {
    const current = sessionService.value;
    if (!current) {
      return router.createUrlTree(['/login']);
    }
    if (current.passwordChangeRequired && targetUrl !== '/change-password') {
      return router.createUrlTree(['/change-password']);
    }
    if (!current.passwordChangeRequired && targetUrl === '/change-password') {
      return router.createUrlTree([landingRoute(current.role)]);
    }
    return true;
  };

  if (sessionService.value) {
    return resolveRoute();
  }

  return sessionService.hydrateFromServer().pipe(
    map((session) => session ? resolveRoute() : router.createUrlTree(['/login']))
  );
};
