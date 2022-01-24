import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MappingTestComponent } from './mapping-test.component';

describe('MappingTestComponent', () => {
  let component: MappingTestComponent;
  let fixture: ComponentFixture<MappingTestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MappingTestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MappingTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
