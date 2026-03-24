import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { finalize, map, switchMap } from 'rxjs/operators';

import { AuthService } from '../../../../../core/auth/auth.service';
import { ConfirmDialogService } from '../../../../../shared/services/confirm-dialog.service';
import { NotificationService } from '../../../../../shared/services/notification.service';
import { MediaApiService } from '../../../../media/data-access/media-api.service';
import { AuthorsApiService } from '../../../authors/data-access/authors-api.service';
import { AuthorResponse } from '../../../authors/data-access/authors.dto';
import { CategoriesApiService } from '../../../categories/data-access/categories-api.service';
import { CategoryResponse } from '../../../categories/data-access/categories.dto';
import { BooksApiService } from '../../data-access/books-api.service';
import {
  BookImageResponse,
  BookSummaryResponse,
  CreateBookRequest,
} from '../../data-access/books.dto';

type BooksViewMode = 'list' | 'cards';
type SortByOption = 'title' | 'publicationYear' | 'createdAt';
type SortDirectionOption = 'asc' | 'desc';

interface CategoryTreeNode {
  id: number;
  name: string;
  children: CategoryTreeNode[];
}

@Component({
  selector: 'app-books-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './books-list-page.html',
  styleUrl: './books-list-page.scss',
})
export class BooksListPage {
  private readonly booksApiService = inject(BooksApiService);
  private readonly authorsApiService = inject(AuthorsApiService);
  private readonly categoriesApiService = inject(CategoriesApiService);
  private readonly mediaApiService = inject(MediaApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly notificationService = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly modalLoading = signal(false);
  readonly imageActionLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly formErrorMessage = signal<string | null>(null);
  readonly books = signal<BookSummaryResponse[]>([]);
  readonly authors = signal<AuthorResponse[]>([]);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly existingImages = signal<BookImageResponse[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  readonly titleFilter = signal('');
  readonly authorFilter = signal<number | null>(null);
  readonly categoryFilter = signal<number | null>(null);
  readonly publicationYearFilter = signal<number | null>(null);
  readonly sortBy = signal<SortByOption>('title');
  readonly sortDir = signal<SortDirectionOption>('asc');

  readonly viewMode = signal<BooksViewMode>('cards');
  readonly categoryTreeExpanded = signal<Set<number>>(new Set());
  readonly showFormModal = signal(false);
  readonly editingBookId = signal<number | null>(null);
  readonly pendingImageFile = signal<File | null>(null);
  readonly pendingImageAltText = signal('');
  readonly pendingImageName = signal<string | null>(null);

  readonly isAdmin = signal(this.authService.session()?.role === 'ADMIN');

  readonly categoryTree = computed(() => this.buildCategoryTree(this.categories()));

  readonly form = this.formBuilder.nonNullable.group({
    isbn: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(30)]],
    title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
    publisher: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
    publicationYear: this.formBuilder.nonNullable.control<number | null>(null, [
      Validators.required,
      Validators.min(1000),
      Validators.max(9999),
    ]),
    description: ['', [Validators.maxLength(2000)]],
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
        size: 12,
        sortBy: this.sortBy(),
        sortDir: this.sortDir(),
        title: this.titleFilter() || undefined,
        authorId: this.authorFilter() ?? undefined,
        categoryId: this.categoryFilter() ?? undefined,
        publicationYear: this.publicationYearFilter() ?? undefined,
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

  applyFilters(): void {
    this.page.set(0);
    this.loadBooks();
  }

  clearFilters(): void {
    this.titleFilter.set('');
    this.authorFilter.set(null);
    this.categoryFilter.set(null);
    this.publicationYearFilter.set(null);
    this.sortBy.set('title');
    this.sortDir.set('asc');
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

  setViewMode(mode: BooksViewMode): void {
    this.viewMode.set(mode);
    this.syncQueryParams();
  }

  toggleCategoryTreeNode(categoryId: number): void {
    const expanded = new Set(this.categoryTreeExpanded());
    if (expanded.has(categoryId)) {
      expanded.delete(categoryId);
    } else {
      expanded.add(categoryId);
    }
    this.categoryTreeExpanded.set(expanded);
  }

  filterByCategoryFromTree(categoryId: number): void {
    this.categoryFilter.set(categoryId);
    this.applyFilters();
  }

  openCreateModal(): void {
    this.editingBookId.set(null);
    this.formErrorMessage.set(null);
    this.modalLoading.set(false);
    this.pendingImageFile.set(null);
    this.pendingImageAltText.set('');
    this.pendingImageName.set(null);
    this.existingImages.set([]);
    this.form.reset({
      isbn: '',
      title: '',
      publisher: '',
      publicationYear: null,
      description: '',
      authorId: null,
      categoryId: null,
    });
    this.showFormModal.set(true);
  }

  openEditModal(book: BookSummaryResponse): void {
    this.editingBookId.set(book.id);
    this.formErrorMessage.set(null);
    this.modalLoading.set(true);
    this.pendingImageFile.set(null);
    this.pendingImageAltText.set('');
    this.pendingImageName.set(null);
    this.existingImages.set([]);
    this.form.reset({
      isbn: book.isbn,
      title: book.title,
      publisher: book.publisher,
      publicationYear: book.publicationYear,
      description: '',
      authorId: book.authorId,
      categoryId: book.categoryId,
    });
    this.showFormModal.set(true);

    this.booksApiService
      .findById(book.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (bookDetail) => {
          this.form.reset({
            isbn: bookDetail.isbn,
            title: bookDetail.title,
            publisher: bookDetail.publisher,
            publicationYear: bookDetail.publicationYear,
            description: bookDetail.description ?? '',
            authorId: bookDetail.authorId,
            categoryId: bookDetail.categoryId,
          });
          this.existingImages.set(bookDetail.images);
          this.modalLoading.set(false);
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to load book details';
          this.formErrorMessage.set(message);
          this.notificationService.error(message);
          this.modalLoading.set(false);
        },
      });
  }

  closeFormModal(): void {
    this.showFormModal.set(false);
    this.modalLoading.set(false);
  }

  onBookImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const selected = input.files?.[0] ?? null;
    this.pendingImageFile.set(selected);
    this.pendingImageName.set(selected?.name ?? null);
  }

  clearPendingImage(): void {
    this.pendingImageFile.set(null);
    this.pendingImageName.set(null);
    this.pendingImageAltText.set('');
  }

  setPrimaryImage(imageId: number): void {
    const bookId = this.editingBookId();
    if (!bookId || this.imageActionLoading()) {
      return;
    }

    this.imageActionLoading.set(true);
    this.booksApiService
      .setPrimaryImage(bookId, imageId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.imageActionLoading.set(false))
      )
      .subscribe({
        next: (updatedBook) => {
          this.existingImages.set(updatedBook.images);
          this.loadBooks(this.page());
          this.notificationService.success('Primary image updated');
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to set primary image';
          this.formErrorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  removeImage(imageId: number): void {
    const bookId = this.editingBookId();
    if (!bookId || this.imageActionLoading()) {
      return;
    }

    this.imageActionLoading.set(true);
    this.booksApiService
      .removeImage(bookId, imageId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.imageActionLoading.set(false))
      )
      .subscribe({
        next: () => {
          this.existingImages.update((images) => images.filter((image) => image.id !== imageId));
          this.loadBooks(this.page());
          this.notificationService.success('Image removed');
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to remove image';
          this.formErrorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  saveBook(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      if (!this.saving()) {
        this.notificationService.error('Please fix the highlighted book form fields');
      }
      return;
    }

    if (this.pendingImageAltText().trim().length > 255) {
      const message = 'Image alt text must not exceed 255 characters';
      this.formErrorMessage.set(message);
      this.notificationService.error(message);
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
        switchMap((book) => {
          const imageFile = this.pendingImageFile();
          if (!imageFile) {
            return of(book);
          }

          return this.mediaApiService.uploadImage(imageFile, 'BOOKS').pipe(
            switchMap((mediaAsset) =>
              this.booksApiService.attachImages(book.id, {
                images: [
                  {
                    mediaAssetId: mediaAsset.id,
                    primaryImage: !editingBookId && this.existingImages().length === 0,
                    altText: this.pendingImageAltText() || undefined,
                  },
                ],
              })
            ),
            map((updatedBook) => {
              if (editingBookId) {
                this.existingImages.set(updatedBook.images);
              }
              return book;
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false))
      )
      .subscribe({
        next: () => {
          this.closeFormModal();
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

  isCategoryExpanded(categoryId: number): boolean {
    return this.categoryTreeExpanded().has(categoryId);
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

    this.page.set(Math.max(0, Number(queryParams.get('booksPage') ?? '0') || 0));
    this.titleFilter.set(queryParams.get('booksTitle') ?? '');
    this.authorFilter.set(this.toNullableNumber(queryParams.get('booksAuthorId')));
    this.categoryFilter.set(this.toNullableNumber(queryParams.get('booksCategoryId')));
    this.publicationYearFilter.set(this.toNullableNumber(queryParams.get('booksYear')));

    const sortBy = queryParams.get('booksSortBy');
    if (sortBy === 'title' || sortBy === 'publicationYear' || sortBy === 'createdAt') {
      this.sortBy.set(sortBy);
    }

    const sortDir = queryParams.get('booksSortDir');
    if (sortDir === 'asc' || sortDir === 'desc') {
      this.sortDir.set(sortDir);
    }

    const mode = queryParams.get('booksMode');
    if (mode === 'list' || mode === 'cards') {
      this.viewMode.set(mode);
    }
  }

  private syncQueryParams(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        booksPage: this.page(),
        booksTitle: this.titleFilter() || null,
        booksAuthorId: this.authorFilter() ?? null,
        booksCategoryId: this.categoryFilter() ?? null,
        booksYear: this.publicationYearFilter() ?? null,
        booksSortBy: this.sortBy(),
        booksSortDir: this.sortDir(),
        booksMode: this.viewMode(),
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  private toNullableNumber(value: string | null): number | null {
    if (!value) {
      return null;
    }

    const parsed = Number(value);
    return Number.isNaN(parsed) ? null : parsed;
  }

  private buildCategoryTree(categories: CategoryResponse[]): CategoryTreeNode[] {
    const byParent = new Map<number | null, CategoryResponse[]>();

    for (const category of categories) {
      const group = byParent.get(category.parentId) ?? [];
      group.push(category);
      byParent.set(category.parentId, group);
    }

    const buildNode = (category: CategoryResponse): CategoryTreeNode => ({
      id: category.id,
      name: category.name,
      children: (byParent.get(category.id) ?? [])
        .sort((a, b) => a.name.localeCompare(b.name))
        .map(buildNode),
    });

    return (byParent.get(null) ?? []).sort((a, b) => a.name.localeCompare(b.name)).map(buildNode);
  }
}
