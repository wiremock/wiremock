import { TestBed, inject } from '@angular/core/testing';

import { MappingHelperService } from './mapping-helper.service';

describe('MappingHelperService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MappingHelperService]
    });
  });

  it('should be created', inject([MappingHelperService], (service: MappingHelperService) => {
    expect(service).toBeTruthy();
  }));
});
