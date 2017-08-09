import { TestBed, inject } from '@angular/core/testing';

import { MappingViewHelperService } from './mapping-view-helper.service';

describe('MappingViewHelperService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingViewHelperService]
    });
  });

  it('should be created', inject([MappingViewHelperService], (service: MappingViewHelperService) => {
    expect(service).toBeTruthy();
  }));
});
