import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, startWith } from 'rxjs';

import { AuthService } from './core/auth/auth.service';
import { NotificationService } from './shared/services/notification.service';
import { ThemePreference, ThemeService } from './shared/services/theme.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    protected readonly authService: AuthService,
    private readonly router: Router,
    private readonly notificationService: NotificationService,
    protected readonly themeService: ThemeService
  ) {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        startWith(null),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        if (this.authService.isAuthenticated() && this.router.url.startsWith('/login')) {
          this.router.navigateByUrl('/books', { replaceUrl: true });
        }
      });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.notificationService.success('Session closed successfully');
        this.router.navigateByUrl('/login');
      },
    });
  }

  updateTheme(preference: ThemePreference): void {
    this.themeService.setPreference(preference);
  }
}
