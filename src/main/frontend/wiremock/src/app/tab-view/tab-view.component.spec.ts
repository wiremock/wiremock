import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TabViewComponent } from './tab-view.component';

describe('TabViewComponent', () => {
  let component: TabViewComponent;
  let fixture: ComponentFixture<TabViewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TabViewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TabViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
