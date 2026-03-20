import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { AuthService } from '../auth/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  const createRouterStub = () => ({
    createUrlTree: (commands: unknown[]) => ({ commands }),
  });

  it('allows navigation when role is included', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { session: () => ({ role: 'ADMIN' }) },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() =>
      roleGuard({ data: { roles: ['ADMIN'] } } as never, {} as never)
    );
    expect(result).toBe(true);
  });

  it('redirects when role is not included', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: createRouterStub() },
        {
          provide: AuthService,
          useValue: { session: () => ({ role: 'EMPLOYEE' }) },
        },
      ],
    });

    const result = TestBed.runInInjectionContext(() =>
      roleGuard({ data: { roles: ['ADMIN'] } } as never, {} as never)
    );
    expect(result).toEqual({ commands: ['/dashboard'] });
  });
});
