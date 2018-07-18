import { TestBed, inject } from '@angular/core/testing';

import { AutoRefreshService } from './auto-refresh.service';

describe('AutoRefreshService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AutoRefreshService]
    });
  });

  it('should be created', inject([AutoRefreshService], (service: AutoRefreshService) => {
    expect(service).toBeTruthy();
  }));
});
