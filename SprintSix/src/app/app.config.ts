import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideClientHydration } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { apiCredentialsInterceptor } from './core/interceptors/api-credentials.interceptor';
import { AppConfigService } from './core/services/app-config.service';
import { AuthSessionService } from './core/services/auth-session.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideClientHydration(),
    provideHttpClient(withInterceptors([apiCredentialsInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: (config: AppConfigService, session: AuthSessionService) => async () => {
        await config.load();
        await session.initialize();
      },
      deps: [AppConfigService, AuthSessionService],
      multi: true
    }
  ]
};
