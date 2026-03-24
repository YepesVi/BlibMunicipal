import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { App } from './app';
import { AuthService } from './core/auth/auth.service';
import { NotificationService } from './shared/services/notification.service';
import { ThemeService } from './shared/services/theme.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            session: () => null,
            isAuthenticated: () => false,
            isAuthenticatedSignal: () => false,
            logout: () => of(void 0),
          },
        },
        {
          provide: NotificationService,
          useValue: {
            success: () => undefined,
            error: () => undefined,
          },
        },
        {
          provide: ThemeService,
          useValue: {
            preference: () => 'auto',
            setPreference: () => undefined,
          },
        },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
