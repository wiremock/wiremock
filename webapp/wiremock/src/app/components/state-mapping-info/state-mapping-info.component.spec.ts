import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StateMappingInfoComponent } from './state-mapping-info.component';

describe('StateMappingInfoComponent', () => {
  let component: StateMappingInfoComponent;
  let fixture: ComponentFixture<StateMappingInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StateMappingInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateMappingInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
