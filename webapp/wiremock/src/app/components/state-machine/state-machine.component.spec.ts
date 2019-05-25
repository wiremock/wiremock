import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StateMachineComponent } from './state-machine.component';

describe('StateMachineComponent', () => {
  let component: StateMachineComponent;
  let fixture: ComponentFixture<StateMachineComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StateMachineComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateMachineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
