import { CommonModule } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../../../../core/auth/auth.service';
import { ConfirmDialogService } from '../../../../../shared/services/confirm-dialog.service';
import { NotificationService } from '../../../../../shared/services/notification.service';
import { CategoriesGraphqlService } from '../../data-access/categories-graphql.service';
import { CategoryResponse } from '../../data-access/categories.dto';

interface CategoryTreeNode {
  id: number;
  name: string;
  children: CategoryTreeNode[];
}

@Component({
  selector: 'app-categories-list-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categories-list-page.html',
  styleUrl: './categories-list-page.scss',
})
export class CategoriesListPage {
  private readonly categoriesApiService = inject(CategoriesGraphqlService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly notificationService = inject(NotificationService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly editingCategoryId = signal<number | null>(null);
  readonly showFormModal = signal(false);
  readonly expandedIds = signal<Set<number>>(new Set());
  readonly isAdmin = signal(this.authService.session()?.role === 'ADMIN');
  readonly categoryTree = computed(() => this.buildTree(this.categories()));

  readonly form = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(500)]],
    parentId: [''],
  });

  constructor() {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.categoriesApiService
      .findAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (categories) => {
          this.categories.set(categories);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(error instanceof Error ? error.message : 'Failed to load categories');
          if (error instanceof Error) {
            this.notificationService.error(error.message);
          }
          this.loading.set(false);
        },
      });
  }

  startCreate(): void {
    this.editingCategoryId.set(null);
    this.form.reset({ name: '', description: '', parentId: '' });
  }

  openCreateModal(): void {
    this.startCreate();
    this.showFormModal.set(true);
  }

  startEdit(category: CategoryResponse): void {
    this.editingCategoryId.set(category.id);
    this.form.reset({
      name: category.name,
      description: category.description ?? '',
      parentId: category.parentId ? String(category.parentId) : '',
    });
  }

  openEditModal(category: CategoryResponse): void {
    this.startEdit(category);
    this.showFormModal.set(true);
  }

  closeFormModal(): void {
    this.showFormModal.set(false);
  }

  saveCategory(): void {
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      if (!this.saving()) {
        this.notificationService.error('Please fix the highlighted category form fields');
      }
      return;
    }

    const editingId = this.editingCategoryId();
    const value = this.form.getRawValue();
    const payload = {
      name: value.name,
      description: value.description || undefined,
      parentId: value.parentId ? Number(value.parentId) : null,
    };

    this.saving.set(true);
    const request$ = editingId
      ? this.categoriesApiService.update(editingId, payload)
      : this.categoriesApiService.create(payload);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false))
      )
      .subscribe({
        next: () => {
          this.startCreate();
          this.closeFormModal();
          this.loadCategories();
          this.notificationService.success(
            editingId ? 'Category updated successfully' : 'Category created successfully'
          );
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Failed to save category';
          this.errorMessage.set(message);
          this.notificationService.error(message);
        },
      });
  }

  deleteCategory(category: CategoryResponse): void {
    if (!this.isAdmin()) {
      return;
    }

    this.confirmDialogService
      .open({
        title: 'Delete category',
        message: `Are you sure you want to delete "${category.name}"?`,
        confirmLabel: 'Delete',
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) {
          return;
        }

        this.categoriesApiService
          .delete(category.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.notificationService.success('Category deleted successfully');
              this.loadCategories();
            },
            error: (error: unknown) => {
              const message = error instanceof Error ? error.message : 'Failed to delete category';
              this.errorMessage.set(message);
              this.notificationService.error(message);
            },
          });
      });
  }

  toggleExpand(id: number): void {
    const expanded = new Set(this.expandedIds());
    if (expanded.has(id)) {
      expanded.delete(id);
    } else {
      expanded.add(id);
    }
    this.expandedIds.set(expanded);
  }

  isExpanded(id: number): boolean {
    return this.expandedIds().has(id);
  }

  private buildTree(categories: CategoryResponse[]): CategoryTreeNode[] {
    const byParent = new Map<number | null, CategoryResponse[]>();

    for (const category of categories) {
      const list = byParent.get(category.parentId) ?? [];
      list.push(category);
      byParent.set(category.parentId, list);
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
