import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RawSeparatedComponent } from './raw-separated.component';

describe('RawSeparatedComponent', () => {
  let component: RawSeparatedComponent;
  let fixture: ComponentFixture<RawSeparatedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RawSeparatedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RawSeparatedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
