import { CommonModule } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../../core/auth/auth.service';
import { NotificationService } from '../../../../shared/services/notification.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificationService = inject(NotificationService);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(80)]],
    password: ['', [Validators.required]],
  });

  constructor() {
    effect(() => {
      if (this.authService.isAuthenticatedSignal()) {
        this.router.navigateByUrl('/books', { replaceUrl: true });
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      if (!this.isSubmitting()) {
        this.notificationService.error('Please complete username and password correctly');
      }
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => {
          this.notificationService.success('Welcome back');
          this.router.navigateByUrl('/books', { replaceUrl: true });
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Login failed';
          this.errorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }
}
