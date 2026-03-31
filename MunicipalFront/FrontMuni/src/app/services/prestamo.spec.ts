import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { PrestamoService } from './prestamo';

describe('PrestamoService', () => {
  let service: PrestamoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PrestamoService]
    });
    service = TestBed.inject(PrestamoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
