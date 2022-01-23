import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs/internal/BehaviorSubject';

@Injectable({
  providedIn: 'root'
})
export class AutoRefreshService {

  private autoRefresh: BehaviorSubject<boolean> = new BehaviorSubject(true);
  autoRefresh$ = this.autoRefresh.asObservable();

  constructor() {
  }

  public setAutoRefresh(value: boolean): void {
    this.autoRefresh.next(value);
  }

  public isAutoRefreshEnabled(): boolean {
    return this.autoRefresh.getValue();
  }
}
