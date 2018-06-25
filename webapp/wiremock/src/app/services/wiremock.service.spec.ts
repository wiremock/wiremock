import { TestBed, inject } from '@angular/core/testing';

import { WiremockService } from './wiremock.service';

describe('WiremockService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WiremockService]
    });
  });

  it('should ...', inject([WiremockService], (service: WiremockService) => {
    expect(service).toBeTruthy();
  }));
});
