import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthorsGraphqlService } from '../../../catalog/authors/data-access/authors-graphql.service';
import { NotificationService } from '../../../../shared/services/notification.service';
import { ReportsApiService } from '../../data-access/reports-api.service';
import { ReportsGraphqlService } from '../../data-access/reports-graphql.service';
import { BooksByAuthorReportPage } from './books-by-author-report-page';

describe('BooksByAuthorReportPage', () => {
  let authorsGraphqlService: {
    findAll: ReturnType<typeof vi.fn>;
  };

  let reportsGraphqlService: {
    getBooksByAuthorIdCard: ReturnType<typeof vi.fn>;
  };

  let reportsApiService: {
    downloadBooksByAuthorIdCardPdf: ReturnType<typeof vi.fn>;
  };

  let notificationService: {
    success: ReturnType<typeof vi.fn>;
    error: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    authorsGraphqlService = {
      findAll: vi.fn().mockReturnValue(
        of([
          {
            id: 1,
            idCard: '0102',
            fullName: 'Author Name',
            nationality: 'CO',
            biography: null,
            createdAt: '2026-03-20T10:15:30Z',
            updatedAt: '2026-03-20T10:15:30Z',
          },
        ])
      ),
    };

    reportsGraphqlService = {
      getBooksByAuthorIdCard: vi.fn(),
    };

    reportsApiService = {
      downloadBooksByAuthorIdCardPdf: vi.fn(),
    };

    notificationService = {
      success: vi.fn(),
      error: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [BooksByAuthorReportPage],
      providers: [
        { provide: AuthorsGraphqlService, useValue: authorsGraphqlService },
        { provide: ReportsGraphqlService, useValue: reportsGraphqlService },
        { provide: ReportsApiService, useValue: reportsApiService },
        { provide: NotificationService, useValue: notificationService },
      ],
    }).compileComponents();
  });

  it('generates preview and stores report data', () => {
    const fixture = TestBed.createComponent(BooksByAuthorReportPage);
    const component = fixture.componentInstance;
    const mockReport = {
      authorIdCard: '0102',
      authorName: 'Author Name',
      generatedAt: '2026-03-20T10:15:30Z',
      totalBooks: 1,
      books: [
        {
          bookId: 1,
          isbn: '9780307474728',
          title: 'One Hundred Years of Solitude',
          publisher: 'Harper',
          publicationYear: 1967,
          categoryName: 'Novel',
          primaryImageUrl: null,
        },
      ],
    };

    reportsGraphqlService.getBooksByAuthorIdCard.mockReturnValue(of(mockReport));
    component.form.controls.authorId.setValue(1);

    component.generatePreview();

    expect(reportsGraphqlService.getBooksByAuthorIdCard).toHaveBeenCalledWith('0102');
    expect(component.report()?.authorName).toBe('Author Name');
    expect(component.errorMessage()).toBeNull();
    expect(notificationService.success).toHaveBeenCalledWith('Report preview generated');
  });

  it('shows error and clears report when preview fails', () => {
    const fixture = TestBed.createComponent(BooksByAuthorReportPage);
    const component = fixture.componentInstance;

    reportsGraphqlService.getBooksByAuthorIdCard.mockReturnValue(
      throwError(() => new Error('Could not load report'))
    );
    component.form.controls.authorId.setValue(1);

    component.generatePreview();

    expect(component.report()).toBeNull();
    expect(component.errorMessage()).toBe('Could not load report');
    expect(notificationService.error).toHaveBeenCalledWith('Could not load report');
  });

  it('downloads report pdf and triggers browser download', () => {
    const fixture = TestBed.createComponent(BooksByAuthorReportPage);
    const component = fixture.componentInstance;
    const clickSpy = vi.fn();
    const createObjectUrlSpy = vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:report');
    const revokeObjectUrlSpy = vi.spyOn(window.URL, 'revokeObjectURL');
    const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue({
      href: '',
      download: '',
      click: clickSpy,
    } as unknown as HTMLAnchorElement);

    reportsApiService.downloadBooksByAuthorIdCardPdf.mockReturnValue(
      of(new Blob(['test'], { type: 'application/pdf' }))
    );
    component.form.controls.authorId.setValue(1);

    component.downloadPdf();

    expect(reportsApiService.downloadBooksByAuthorIdCardPdf).toHaveBeenCalledWith('0102');
    expect(createElementSpy).toHaveBeenCalledWith('a');
    expect(createObjectUrlSpy).toHaveBeenCalled();
    expect(clickSpy).toHaveBeenCalled();
    expect(revokeObjectUrlSpy).toHaveBeenCalledWith('blob:report');
    expect(notificationService.success).toHaveBeenCalledWith('Report downloaded');

    createObjectUrlSpy.mockRestore();
    revokeObjectUrlSpy.mockRestore();
    createElementSpy.mockRestore();
  });
});

