import { Injectable, computed, signal } from '@angular/core';

import { AuthSession } from './auth.types';

@Injectable({ providedIn: 'root' })
export class AuthSessionStore {
  private readonly sessionSignal = signal<AuthSession | null>(null);

  readonly session = this.sessionSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.sessionSignal() !== null);

  setSession(session: AuthSession): void {
    this.sessionSignal.set(session);
  }

  clearSession(): void {
    this.sessionSignal.set(null);
  }
}
