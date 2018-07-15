import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs/internal/BehaviorSubject';

@Injectable({
  providedIn: 'root'
})
export class TabSelectionService {

  private tab: BehaviorSubject<Tab> = new BehaviorSubject(null);
  tab$ = this.tab.asObservable();

  constructor() {
  }

  public selectTab(value: Tab): void {
    this.tab.next(value);
  }
}

export enum Tab {
  RAW, SEPARATED
}
