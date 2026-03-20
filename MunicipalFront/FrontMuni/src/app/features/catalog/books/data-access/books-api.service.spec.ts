import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { BooksApiService } from './books-api.service';

describe('BooksApiService', () => {
  let service: BooksApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), BooksApiService],
    });

    service = TestBed.inject(BooksApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests paginated books list', () => {
    service.findAll({ title: 'harry', page: 2, size: 10 }).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/catalog/books?title=harry&page=2&size=10');
    expect(req.request.method).toBe('GET');
    req.flush({
      content: [],
      page: 2,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      first: false,
      last: true,
      sortBy: 'title',
      sortDirection: 'asc',
    });
  });

  it('deletes a book by id', () => {
    service.delete(5).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/catalog/books/5');
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
