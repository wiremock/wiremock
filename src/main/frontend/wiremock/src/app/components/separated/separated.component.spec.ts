import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SeparatedComponent } from './separated.component';

describe('SeparatedComponent', () => {
  let component: SeparatedComponent;
  let fixture: ComponentFixture<SeparatedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SeparatedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SeparatedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
