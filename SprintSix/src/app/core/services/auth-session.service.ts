import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject, Observable } from 'rxjs';
import { LoginResult } from '../models/auth.model';

export interface SessionUser extends LoginResult {
  email: string;
  mobile: string;
  address: string;
}

const STORAGE_KEY = 'hms_session';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private subject: BehaviorSubject<SessionUser | null>;
  session$!: Observable<SessionUser | null>;
  private readonly isBrowser: boolean;

  get value(): SessionUser | null {
    return this.subject.value;
  }

  set(session: SessionUser): void {
    this.subject.next(session);
    if (this.isBrowser) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    }
  }

  clear(): void {
    this.subject.next(null);
    if (this.isBrowser) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  constructor(@Inject(PLATFORM_ID) platformId: object) {
    this.isBrowser = isPlatformBrowser(platformId);
    this.subject = new BehaviorSubject<SessionUser | null>(this.load());
    this.session$ = this.subject.asObservable();
  }

  private load(): SessionUser | null {
    if (!this.isBrowser) {
      return null;
    }
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as SessionUser;
    } catch {
      return null;
    }
  }
}
