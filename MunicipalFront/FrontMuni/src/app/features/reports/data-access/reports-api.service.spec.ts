import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ReportsApiService } from './reports-api.service';

describe('ReportsApiService', () => {
  let service: ReportsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ReportsApiService],
    });

    service = TestBed.inject(ReportsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads report preview by author id card', () => {
    service.getBooksByAuthorIdCard('0102').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/reports/books/by-author-id-card/0102');
    expect(req.request.method).toBe('GET');
    req.flush({ authorIdCard: '0102', authorName: 'Author', generatedAt: '2026-03-20T00:00:00Z', totalBooks: 0, books: [] });
  });

  it('requests report PDF as blob', () => {
    service.downloadBooksByAuthorIdCardPdf('0102').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/reports/books/by-author-id-card/0102/pdf');
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['test'], { type: 'application/pdf' }));
  });
});
