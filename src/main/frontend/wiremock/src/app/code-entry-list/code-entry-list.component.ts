import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {DataSource} from '@angular/cdk';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {UtilService} from '../services/util.service';
import {CookieService} from '../services/cookie.service';
import {SettingsService} from '../services/settings.service';

@Component({
  selector: 'wm-code-entry-list',
  templateUrl: './code-entry-list.component.html',
  styleUrls: ['./code-entry-list.component.scss']
})
export class CodeEntryListComponent implements OnInit, OnChanges {

  @Input('entries')
  entries: DataEntries;
  dataSource: EntryDataSource | null;

  displayedColumns = ['key', 'value'];

  areEmptyCodeEntriesHidden: boolean;

  constructor(private settingsService: SettingsService) { }

  ngOnInit() {
    this.settingsService.codeEntriesHidden$.subscribe(next=>{
      this.areEmptyCodeEntriesHidden = next;
    })
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.dataSource = new EntryDataSource(this.entries);
  }

  getEmptyCodeEntriesHidden(value: any): boolean{
    return this.areEmptyCodeEntriesHidden && UtilService.isBlank(value);
  }
}

export class Entry{
  type: string;
  key: string;
  value: any;
  language: string;

  constructor(key: string, value: any, language: string, type?: string){
    this.key = key;
    this.value = value;
    this.language = language;
    if(UtilService.isDefined(type)){
      this.type = type;
    }else{
      this.type = 'code';
    }
  }
}

export class DataEntries{
  dataChange: BehaviorSubject<Entry[]> = new BehaviorSubject<Entry[]>([]);
  get data(): Entry[]{
    return this.dataChange.value;
  };

  constructor() { }

  addEntry(entry: Entry){
    if(entry.value == null || typeof entry.value === 'undefined'){
      entry.value = '';
    }
    const copiedData = this.data.slice();
    copiedData.push(entry);
    this.dataChange.next(copiedData);
  }
}

export class EntryDataSource extends DataSource<any> {

  constructor(private _entries: DataEntries) {
    super();
  }

  /** Connect function called by the table to retrieve one stream containing the data to render. */
  connect(): Observable<Entry[]> {
    const displayDataChanges = [
      this._entries.dataChange
    ];

    return Observable.merge(displayDataChanges).map(() => {
      return this._entries.data;
    });
  }

  disconnect() {}
}
