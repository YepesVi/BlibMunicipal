import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../../../core/auth/auth.service';
import { ConfirmDialogService } from '../../../../../shared/services/confirm-dialog.service';
import { NotificationService } from '../../../../../shared/services/notification.service';
import { AuthorResponse } from '../../data-access/authors.dto';
import { AuthorsApiService } from '../../data-access/authors-api.service';

@Component({
  selector: 'app-authors-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './authors-list-page.html',
  styleUrl: './authors-list-page.scss',
})
export class AuthorsListPage {
  private readonly authorsApiService = inject(AuthorsApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly notificationService = inject(NotificationService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fullNameFilter = signal('');
  readonly authors = signal<AuthorResponse[]>([]);
  readonly showFormModal = signal(false);
  readonly editingAuthorId = signal<number | null>(null);
  readonly isAdmin = signal(this.authService.session()?.role === 'ADMIN');

  readonly form = this.formBuilder.nonNullable.group({
    idCard: ['', [Validators.required, Validators.minLength(5)]],
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    nationality: ['', [Validators.required, Validators.minLength(2)]],
    biography: [''],
  });

  constructor() {
    this.loadAuthors();
  }

  loadAuthors(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.authorsApiService
      .findAll(this.fullNameFilter() || undefined)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (authors) => {
          this.authors.set(authors);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(error instanceof Error ? error.message : 'Failed to load authors');
          if (error instanceof Error) {
            this.notificationService.error(error.message);
          }
          this.loading.set(false);
        },
      });
  }

  applyFilters(): void {
    this.loadAuthors();
  }

  startCreate(): void {
    this.editingAuthorId.set(null);
    this.form.reset({ idCard: '', fullName: '', nationality: '', biography: '' });
  }

  openCreateModal(): void {
    this.startCreate();
    this.showFormModal.set(true);
  }

  startEdit(author: AuthorResponse): void {
    this.editingAuthorId.set(author.id);
    this.form.reset({
      idCard: author.idCard,
      fullName: author.fullName,
      nationality: author.nationality,
      biography: author.biography ?? '',
    });
  }

  openEditModal(author: AuthorResponse): void {
    this.startEdit(author);
    this.showFormModal.set(true);
  }

  closeFormModal(): void {
    this.showFormModal.set(false);
  }

  saveAuthor(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const editingId = this.editingAuthorId();
    const payload = this.form.getRawValue();

    this.saving.set(true);

    const request$ = editingId
      ? this.authorsApiService.update(editingId, payload)
      : this.authorsApiService.create(payload);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false))
      )
      .subscribe({
        next: () => {
          this.startCreate();
          this.closeFormModal();
          this.loadAuthors();
          this.notificationService.success(
            editingId ? 'Author updated successfully' : 'Author created successfully'
          );
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to save author';
          this.errorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  deleteAuthor(author: AuthorResponse): void {
    if (!this.isAdmin()) {
      return;
    }

    this.confirmDialogService
      .open({
        title: 'Delete author',
        message: `Are you sure you want to delete "${author.fullName}"?`,
        confirmLabel: 'Delete',
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.authorsApiService
          .delete(author.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notificationService.success('Author deleted successfully');
              this.loadAuthors();
            },
            error: (error: unknown) => {
              const message = error instanceof Error ? error.message : 'Failed to delete author';
              this.errorMessage.set(message);
              this.notificationService.error(message);
            },
          });
      });
  }
}
