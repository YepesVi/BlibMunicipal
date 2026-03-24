import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs/operators';

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
  private readonly notificationService = inject(NotificationService);

  readonly loading = signal(false);
  readonly downloading = signal(false);
  readonly report = signal<BooksByAuthorReportResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.formBuilder.nonNullable.group({
    idCard: ['', [Validators.required, Validators.minLength(4)]],
  });

  generatePreview(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.reportsApiService
      .getBooksByAuthorIdCard(this.form.getRawValue().idCard.trim())
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
    const idCard = this.form.getRawValue().idCard.trim();
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
}
