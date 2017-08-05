import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UnmatchedViewComponent } from './unmatched-view.component';

describe('UnmatchedViewComponent', () => {
  let component: UnmatchedViewComponent;
  let fixture: ComponentFixture<UnmatchedViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UnmatchedViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UnmatchedViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
