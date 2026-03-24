import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

import { AuthorsApiService } from '../../../catalog/authors/data-access/authors-api.service';
import { AuthorResponse } from '../../../catalog/authors/data-access/authors.dto';
import { NotificationService } from '../../../../shared/services/notification.service';
import { ReportsApiService } from '../../data-access/reports-api.service';
import { BooksByAuthorReportResponse } from '../../data-access/reports.dto';

@Component({
  selector: 'app-books-by-author-report-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './books-by-author-report-page.html',
  styleUrl: './books-by-author-report-page.scss',
})
export class BooksByAuthorReportPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly reportsApiService = inject(ReportsApiService);
  private readonly authorsApiService = inject(AuthorsApiService);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = signal(false);
  readonly downloading = signal(false);
  readonly loadingAuthors = signal(false);
  readonly authors = signal<AuthorResponse[]>([]);
  readonly report = signal<BooksByAuthorReportResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.formBuilder.group({
    authorId: this.formBuilder.control<number | null>(null, [Validators.required]),
  });

  constructor() {
    this.loadAuthors();
  }

  generatePreview(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      if (!this.loading()) {
        this.notificationService.error('Please select an author to generate the report');
      }
      return;
    }

    const idCard = this.getSelectedAuthorIdCard();
    if (!idCard) {
      this.notificationService.error('Please select a valid author');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.reportsApiService
      .getBooksByAuthorIdCard(idCard)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (report) => {
          this.report.set(report);
          this.notificationService.success('Report preview generated');
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Could not generate report preview';
          this.errorMessage.set(message);
          this.report.set(null);
          this.notificationService.error(message);
        },
      });
  }

  downloadPdf(): void {
    const idCard = this.getSelectedAuthorIdCard();
    if (!idCard || this.downloading()) {
      return;
    }

    this.downloading.set(true);
    this.reportsApiService
      .downloadBooksByAuthorIdCardPdf(idCard)
      .pipe(finalize(() => this.downloading.set(false)))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const anchor = document.createElement('a');
          anchor.href = url;
          anchor.download = `books-by-author-${idCard}.pdf`;
          anchor.click();
          window.URL.revokeObjectURL(url);
          this.notificationService.success('Report downloaded');
        },
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Could not download report PDF';
          this.notificationService.error(message);
        },
      });
  }

  private loadAuthors(): void {
    this.loadingAuthors.set(true);
    this.authorsApiService
      .findAll()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loadingAuthors.set(false))
      )
      .subscribe({
        next: (authors) => this.authors.set(authors),
        error: (error: unknown) => {
          const message = error instanceof Error ? error.message : 'Could not load authors';
          this.notificationService.error(message);
        },
      });
  }

  private getSelectedAuthorIdCard(): string | null {
    const authorId = this.form.getRawValue().authorId;
    if (!authorId) {
      return null;
    }

    return this.authors().find((author) => author.id === authorId)?.idCard ?? null;
  }
}
