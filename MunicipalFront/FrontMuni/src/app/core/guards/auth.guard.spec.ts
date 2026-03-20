import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  const createRouterStub = () => ({
    createUrlTree: (commands: unknown[]) => ({ commands }),
  });

  it('allows navigation when session is active', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => true },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBe(true);
  });

  it('redirects to login when session is missing', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => false },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toEqual({ commands: ['/login'] });
  });
});
