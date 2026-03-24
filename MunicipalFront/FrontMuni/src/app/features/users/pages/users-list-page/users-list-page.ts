import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../../core/auth/auth.service';
import { ConfirmDialogService } from '../../../../shared/services/confirm-dialog.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { CreateUserRequest, UserResponse } from '../../data-access/users.dto';
import { UsersApiService } from '../../data-access/users-api.service';

@Component({
  selector: 'app-users-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './users-list-page.html',
  styleUrl: './users-list-page.scss',
})
export class UsersListPage {
  private readonly usersApiService = inject(UsersApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly formErrorMessage = signal<string | null>(null);
  readonly users = signal<UserResponse[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly usernameFilter = signal('');
  readonly sortBy = signal<'id' | 'username' | 'role'>('id');
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly showFormModal = signal(false);
  readonly editingUserId = signal<number | null>(null);
  readonly isAdmin = signal(this.authService.session()?.role === 'ADMIN');

  readonly form = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(4)]],
    password: ['', [Validators.minLength(8)]],
    role: this.formBuilder.nonNullable.control<'ADMIN' | 'EMPLOYEE'>('EMPLOYEE'),
  });

  constructor() {
    this.hydrateFromQueryParams();
    this.loadUsers();
  }

  loadUsers(targetPage?: number): void {
    if (typeof targetPage === 'number') {
      this.page.set(Math.max(targetPage, 0));
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.syncQueryParams();

    this.usersApiService
      .findAll({
        page: this.page(),
        size: 20,
        sortBy: this.sortBy(),
        sortDir: this.sortDir(),
        username: this.usernameFilter() || undefined,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.users.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to load users';
          this.errorMessage.set(message);
          this.notificationService.error(message);
          this.loading.set(false);
        },
      });
  }

  applyUsernameFilter(value: string): void {
    this.usernameFilter.set(value.trim());
    this.page.set(0);
    this.loadUsers();
  }

  changePage(delta: number): void {
    const nextPage = this.page() + delta;
    if (nextPage < 0 || nextPage >= this.totalPages()) {
      return;
    }
    this.loadUsers(nextPage);
  }

  startCreate(): void {
    this.editingUserId.set(null);
    this.formErrorMessage.set(null);
    this.form.reset({ username: '', password: '', role: 'EMPLOYEE' });
  }

  startEdit(user: UserResponse): void {
    this.editingUserId.set(user.id);
    this.formErrorMessage.set(null);
    this.form.reset({ username: user.username, password: '', role: user.role });
  }

  saveUser(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const editingId = this.editingUserId();

    if (!editingId && !value.password) {
      this.formErrorMessage.set('Password is required for a new user');
      return;
    }

    this.saving.set(true);
    this.formErrorMessage.set(null);

    const request$ = editingId
      ? this.usersApiService.update(editingId, {
          username: value.username,
          password: value.password || undefined,
          role: value.role,
        })
      : this.usersApiService.create({
          username: value.username,
          password: value.password,
          role: value.role,
        } as CreateUserRequest);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false))
      )
      .subscribe({
        next: () => {
          this.startCreate();
          this.closeFormModal();
          this.loadUsers();
          this.notificationService.success(
            editingId ? 'User updated successfully' : 'User created successfully'
          );
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to save user';
          this.formErrorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  deleteUser(user: UserResponse): void {
    if (!this.isAdmin()) {
      return;
    }

    this.confirmDialogService
      .open({
        title: 'Delete user',
        message: `Are you sure you want to delete "${user.username}"?`,
        confirmLabel: 'Delete',
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.usersApiService
          .delete(user.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              if (this.editingUserId() === user.id) {
                this.startCreate();
              }
              this.notificationService.success('User deleted successfully');
              this.loadUsers();
            },
            error: (error: unknown) => {
              const message = error instanceof Error ? error.message : 'Failed to delete user';
              this.errorMessage.set(message);
              this.notificationService.error(message);
            },
          });
      });
  }

  private hydrateFromQueryParams(): void {
    const queryParams = this.route.snapshot.queryParamMap;
    const page = Number(queryParams.get('usersPage') ?? '0');
    const username = queryParams.get('usersUsername') ?? '';
    const sortBy = queryParams.get('usersSortBy');
    const sortDir = queryParams.get('usersSortDir');

    this.page.set(Number.isNaN(page) ? 0 : Math.max(0, page));
    this.usernameFilter.set(username);
    if (sortBy === 'id' || sortBy === 'username' || sortBy === 'role') {
      this.sortBy.set(sortBy);
    }
    if (sortDir === 'asc' || sortDir === 'desc') {
      this.sortDir.set(sortDir);
    }
  }

  private syncQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        usersPage: this.page(),
        usersUsername: this.usernameFilter() || null,
        usersSortBy: this.sortBy(),
        usersSortDir: this.sortDir(),
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  applyFilters(): void {
    this.page.set(0);
    this.loadUsers();
  }

  openCreateModal(): void {
    this.startCreate();
    this.showFormModal.set(true);
  }

  openEditModal(user: UserResponse): void {
    this.startEdit(user);
    this.showFormModal.set(true);
  }

  closeFormModal(): void {
    this.showFormModal.set(false);
  }
}
