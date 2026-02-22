import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { LoginResult } from '../models/auth.model';
import { CustomerService } from './customer.service';

export interface SessionUser extends LoginResult {
  email: string;
  mobile: string;
  address: string;
}

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private subject = new BehaviorSubject<SessionUser | null>(null);
  session$ = this.subject.asObservable();

  get value(): SessionUser | null {
    return this.subject.value;
  }

  constructor(private customerService: CustomerService) {}

  set(session: SessionUser): void {
    this.subject.next(session);
  }

  clear(): void {
    this.subject.next(null);
  }

  invalidateServerSession(): Observable<void> {
    this.clear();
    return this.customerService.logout().pipe(
      map(() => void 0),
      catchError(() => of(void 0))
    );
  }

  initialize(): Promise<void> {
    return new Promise((resolve) => {
      this.customerService.csrf().subscribe({
        next: () => {
          this.hydrateFromServer().subscribe({
            next: () => resolve(),
            error: () => resolve()
          });
        },
        error: () => {
          this.hydrateFromServer().subscribe({
            next: () => resolve(),
            error: () => resolve()
          });
        }
      });
    });
  }

  hydrateFromServer(): Observable<SessionUser | null> {
    return this.customerService.me().pipe(
      tap((user) => this.subject.next(user)),
      map((user) => user as SessionUser),
      catchError(() => {
        this.subject.next(null);
        return of(null);
      })
    );
  }
}
