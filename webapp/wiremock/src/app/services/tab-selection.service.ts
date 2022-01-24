import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/internal/Subject';

@Injectable({
  providedIn: 'root'
})
export class TabSelectionService {

  private tab: Subject<Tab> = new Subject();
  tab$ = this.tab.asObservable();

  constructor() {
  }

  public selectTab(value: Tab): void {
    this.tab.next(value);
  }
}

export enum Tab {
  RAW, SEPARATED, TEST
}
