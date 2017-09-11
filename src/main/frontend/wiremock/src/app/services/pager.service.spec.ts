import { TestBed, inject } from '@angular/core/testing';

import { PagerService } from './pager.service';

describe('PagerService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PagerService]
    });
  });

  it('should ...', inject([PagerService], (service: PagerService) => {
    expect(service).toBeTruthy();
  }));
});
