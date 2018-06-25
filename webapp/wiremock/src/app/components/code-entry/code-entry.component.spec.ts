import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeEntryComponent } from './code-entry.component';

describe('CodeEntryComponent', () => {
  let component: CodeEntryComponent;
  let fixture: ComponentFixture<CodeEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CodeEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CodeEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
