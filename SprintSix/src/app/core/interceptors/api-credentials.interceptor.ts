import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthSessionService } from '../services/auth-session.service';
import { CustomerService } from '../services/customer.service';

function getCookie(name: string): string | null {
  const cookie = document.cookie
    .split('; ')
    .find((row) => row.startsWith(`${name}=`));
  if (!cookie) {
    return null;
  }
  return decodeURIComponent(cookie.split('=')[1]);
}

export const apiCredentialsInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(AuthSessionService);
  const router = inject(Router);
  const customerService = inject(CustomerService);

  let authReq = req.clone({ withCredentials: true });

  const isMutating = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method);
  if (isMutating && typeof document !== 'undefined' && !authReq.headers.has('X-XSRF-TOKEN')) {
    const csrfToken = getCookie('XSRF-TOKEN');
    if (csrfToken) {
      authReq = authReq.clone({ setHeaders: { 'X-XSRF-TOKEN': csrfToken } });
    }
  }

  return next(authReq).pipe(
    catchError((error) => {
      const isMutating = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method);
      if (error?.status === 403 && isMutating && !req.url.includes('/api/auth/csrf')) {
        return customerService.csrf().pipe(
          switchMap(() => {
            const refreshedToken = getCookie('XSRF-TOKEN');
            if (!refreshedToken) {
              return throwError(() => error);
            }
            const retryReq = req.clone({
              withCredentials: true,
              setHeaders: { 'X-XSRF-TOKEN': refreshedToken }
            });
            return next(retryReq);
          })
        );
      }
      if (error?.status === 401) {
        session.clear();
        router.navigateByUrl('/login');
      }
      return throwError(() => error);
    })
  );
};
