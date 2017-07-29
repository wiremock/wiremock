import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UnmatchedComponent } from './unmatched.component';

describe('UnmatchedComponent', () => {
  let component: UnmatchedComponent;
  let fixture: ComponentFixture<UnmatchedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UnmatchedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UnmatchedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
