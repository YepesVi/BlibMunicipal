import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { UsersApiService } from './users-api.service';

describe('UsersApiService', () => {
  let service: UsersApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), UsersApiService],
    });

    service = TestBed.inject(UsersApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests users list with query params', () => {
    service.findAll({ username: 'admin', page: 1, size: 5 }).subscribe();

    const req = httpMock.expectOne(
      'http://localhost:8080/api/users?username=admin&page=1&size=5'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], page: 1, size: 5, totalElements: 0, totalPages: 0, first: false, last: true, sortBy: 'id', sortDirection: 'asc' });
  });

  it('creates user', () => {
    service.create({ username: 'new.user', password: 'password123', role: 'EMPLOYEE' }).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/users');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, username: 'new.user', role: 'EMPLOYEE' });
  });
});
