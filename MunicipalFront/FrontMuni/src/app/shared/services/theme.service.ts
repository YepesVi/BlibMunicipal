import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type ThemePreference = 'auto' | 'light' | 'dark';

const THEME_KEY = 'muni_theme_preference';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  readonly preference = signal<ThemePreference>('auto');

  constructor() {
    this.bootstrap();
  }

  setPreference(preference: ThemePreference): void {
    this.preference.set(preference);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(THEME_KEY, preference);
    }
    this.applyTheme(preference);
  }

  private bootstrap(): void {
    const saved = typeof localStorage !== 'undefined' ? localStorage.getItem(THEME_KEY) : null;
    const preference = this.toPreference(saved);
    this.preference.set(preference);
    this.applyTheme(preference);

    if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        if (this.preference() === 'auto') {
          this.applyTheme('auto');
        }
      });
    }
  }

  private applyTheme(preference: ThemePreference): void {
    const isDark =
      preference === 'dark' ||
      (preference === 'auto' &&
        typeof window !== 'undefined' &&
        typeof window.matchMedia === 'function' &&
        window.matchMedia('(prefers-color-scheme: dark)').matches);

    const documentElement = this.document?.documentElement;
    if (!documentElement) {
      return;
    }

    documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
  }

  private toPreference(value: string | null): ThemePreference {
    if (value === 'light' || value === 'dark' || value === 'auto') {
      return value;
    }
    return 'auto';
  }
}
