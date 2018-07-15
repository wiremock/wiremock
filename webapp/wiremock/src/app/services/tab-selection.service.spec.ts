import { TestBed, inject } from '@angular/core/testing';

import { TabSelectionService } from './tab-selection.service';

describe('TabSelectionService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TabSelectionService]
    });
  });

  it('should be created', inject([TabSelectionService], (service: TabSelectionService) => {
    expect(service).toBeTruthy();
  }));
});
