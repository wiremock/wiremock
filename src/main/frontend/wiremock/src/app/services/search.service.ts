import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class SearchService {

  private searchValue = new BehaviorSubject<string>("");
  searchValue$ = this.searchValue.asObservable();

  constructor() { }

  public setValue(value: string): void{
    this.searchValue.next(value);
  }

}
