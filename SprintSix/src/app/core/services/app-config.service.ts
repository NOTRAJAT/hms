import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface AppConfig {
  apiBaseUrl: string;
}

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private readonly isBrowser: boolean;
  private config: AppConfig | null = null;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  load(): Promise<void> {
    if (!this.isBrowser) {
      this.config = { apiBaseUrl: '/api' };
      return Promise.resolve();
    }
    return firstValueFrom(this.http.get<AppConfig>('assets/env.json'))
      .then((config) => {
        this.config = config;
      });
  }

  get apiBaseUrl(): string {
    return this.config?.apiBaseUrl ?? '/api';
  }
}
