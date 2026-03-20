import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { guestGuard } from './guest.guard';

describe('guestGuard', () => {
  const createRouterStub = () => ({
    createUrlTree: (commands: unknown[]) => ({ commands }),
  });

  it('allows access when user is not authenticated', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => false },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
    expect(result).toBe(true);
  });

  it('redirects authenticated users to books', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => true },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
    expect(result).toEqual({ commands: ['/books'] });
  });
});
