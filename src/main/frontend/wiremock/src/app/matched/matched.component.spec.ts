import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchedComponent } from './matched.component';

describe('MatchedComponent', () => {
  let component: MatchedComponent;
  let fixture: ComponentFixture<MatchedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
