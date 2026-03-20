import { Injectable } from '@angular/core';

import { AuthSession } from './auth.types';

const STORAGE_KEY = 'muni_auth_session';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getSession(): AuthSession | null {
    const storage = this.getStorage();
    if (!storage) {
      return null;
    }

    const sessionRaw = storage.getItem(STORAGE_KEY);
    if (!sessionRaw) {
      return null;
    }

    try {
      return JSON.parse(sessionRaw) as AuthSession;
    } catch {
      this.clear();
      return null;
    }
  }

  setSession(session: AuthSession): void {
    const storage = this.getStorage();
    if (!storage) {
      return;
    }

    storage.setItem(STORAGE_KEY, JSON.stringify(session));
  }

  getAccessToken(): string | null {
    return this.getSession()?.accessToken ?? null;
  }

  getRefreshToken(): string | null {
    return this.getSession()?.refreshToken ?? null;
  }

  clear(): void {
    const storage = this.getStorage();
    if (!storage) {
      return;
    }

    storage.removeItem(STORAGE_KEY);
  }

  private getStorage(): Storage | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    return localStorage;
  }
}
