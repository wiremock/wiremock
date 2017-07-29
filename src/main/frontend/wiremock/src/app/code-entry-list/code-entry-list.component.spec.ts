import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeEntryListComponent } from './code-entry-list.component';

describe('CodeEntryListComponent', () => {
  let component: CodeEntryListComponent;
  let fixture: ComponentFixture<CodeEntryListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CodeEntryListComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeEntryListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
