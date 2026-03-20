import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../../../core/auth/auth.service';
import { ConfirmDialogService } from '../../../../../shared/services/confirm-dialog.service';
import { NotificationService } from '../../../../../shared/services/notification.service';
import { AuthorsApiService } from '../../../authors/data-access/authors-api.service';
import { AuthorResponse } from '../../../authors/data-access/authors.dto';
import { CategoriesApiService } from '../../../categories/data-access/categories-api.service';
import { CategoryResponse } from '../../../categories/data-access/categories.dto';
import { BooksApiService } from '../../data-access/books-api.service';
import { BookSummaryResponse, CreateBookRequest } from '../../data-access/books.dto';

@Component({
  selector: 'app-books-list-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './books-list-page.html',
  styleUrl: './books-list-page.scss',
})
export class BooksListPage {
  private readonly booksApiService = inject(BooksApiService);
  private readonly authorsApiService = inject(AuthorsApiService);
  private readonly categoriesApiService = inject(CategoriesApiService);
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
  readonly books = signal<BookSummaryResponse[]>([]);
  readonly authors = signal<AuthorResponse[]>([]);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly titleFilter = signal('');
  readonly editingBookId = signal<number | null>(null);
  readonly isAdmin = signal(this.authService.session()?.role === 'ADMIN');

  readonly form = this.formBuilder.nonNullable.group({
    isbn: ['', [Validators.required, Validators.minLength(5)]],
    title: ['', [Validators.required, Validators.minLength(2)]],
    publisher: ['', [Validators.required, Validators.minLength(2)]],
    publicationYear: this.formBuilder.nonNullable.control<number | null>(null, [
      Validators.required,
      Validators.min(1000),
      Validators.max(9999),
    ]),
    description: [''],
    authorId: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
    categoryId: this.formBuilder.nonNullable.control<number | null>(null, [Validators.required]),
  });

  constructor() {
    this.hydrateFromQueryParams();
    this.loadCatalogData();
    this.loadBooks();
  }

  loadBooks(targetPage?: number): void {
    if (typeof targetPage === 'number') {
      this.page.set(Math.max(targetPage, 0));
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.syncQueryParams();

    this.booksApiService
      .findAll({
        page: this.page(),
        size: 10,
        sortBy: 'title',
        sortDir: 'asc',
        title: this.titleFilter() || undefined,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.books.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to load books';
          this.errorMessage.set(message);
          this.notificationService.error(message);
          this.loading.set(false);
        },
      });
  }

  applyTitleFilter(value: string): void {
    this.titleFilter.set(value.trim());
    this.page.set(0);
    this.loadBooks();
  }

  changePage(delta: number): void {
    const nextPage = this.page() + delta;
    if (nextPage < 0 || nextPage >= this.totalPages()) {
      return;
    }

    this.loadBooks(nextPage);
  }

  startCreate(): void {
    this.editingBookId.set(null);
    this.formErrorMessage.set(null);
    this.form.reset({
      isbn: '',
      title: '',
      publisher: '',
      publicationYear: null,
      description: '',
      authorId: null,
      categoryId: null,
    });
  }

  startEdit(book: BookSummaryResponse): void {
    this.editingBookId.set(book.id);
    this.formErrorMessage.set(null);
    this.form.reset({
      isbn: book.isbn,
      title: book.title,
      publisher: book.publisher,
      publicationYear: book.publicationYear,
      description: '',
      authorId: book.authorId,
      categoryId: book.categoryId,
    });
  }

  saveBook(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.form.getRawValue();
    if (!payload.publicationYear || !payload.authorId || !payload.categoryId) {
      this.formErrorMessage.set('Publication year, author and category are required');
      return;
    }

    this.saving.set(true);
    this.formErrorMessage.set(null);

    const requestBody: CreateBookRequest = {
      isbn: payload.isbn,
      title: payload.title,
      publisher: payload.publisher,
      publicationYear: payload.publicationYear,
      description: payload.description || undefined,
      authorId: payload.authorId,
      categoryId: payload.categoryId,
    };

    const editingBookId = this.editingBookId();
    const request$ = editingBookId
      ? this.booksApiService.update(editingBookId, requestBody)
      : this.booksApiService.create(requestBody);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false))
      )
      .subscribe({
        next: () => {
          this.startCreate();
          this.loadBooks();
          this.notificationService.success(
            editingBookId ? 'Book updated successfully' : 'Book created successfully'
          );
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to save book';
          this.formErrorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  deleteBook(book: BookSummaryResponse): void {
    if (!this.isAdmin()) {
      return;
    }

    this.confirmDialogService
      .open({
        title: 'Delete book',
        message: `Are you sure you want to delete "${book.title}"?`,
        confirmLabel: 'Delete',
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.booksApiService
          .delete(book.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notificationService.success('Book deleted successfully');
              this.loadBooks();
            },
            error: (error: unknown) => {
              const message = error instanceof Error ? error.message : 'Failed to delete book';
              this.errorMessage.set(message);
              this.notificationService.error(message);
            },
          });
      });
  }

  private loadCatalogData(): void {
    this.authorsApiService
      .findAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (authors) => this.authors.set(authors),
        error: () => this.notificationService.error('Could not load authors list for book form'),
      });

    this.categoriesApiService
      .findAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categories) => this.categories.set(categories),
        error: () => this.notificationService.error('Could not load categories list for book form'),
      });
  }

  private hydrateFromQueryParams(): void {
    const queryParams = this.route.snapshot.queryParamMap;
    const page = Number(queryParams.get('booksPage') ?? '0');
    const title = queryParams.get('booksTitle') ?? '';

    this.page.set(Number.isNaN(page) ? 0 : Math.max(0, page));
    this.titleFilter.set(title);
  }

  private syncQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        booksPage: this.page(),
        booksTitle: this.titleFilter() || null,
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}
