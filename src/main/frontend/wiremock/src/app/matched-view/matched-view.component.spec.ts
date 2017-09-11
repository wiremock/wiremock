import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchedViewComponent } from './matched-view.component';

describe('MatchedViewComponent', () => {
  let component: MatchedViewComponent;
  let fixture: ComponentFixture<MatchedViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchedViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchedViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
