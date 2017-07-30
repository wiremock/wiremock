import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ListEntryComponent } from './list-entry.component';

describe('ListEntryComponent', () => {
  let component: ListEntryComponent;
  let fixture: ComponentFixture<ListEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ListEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
