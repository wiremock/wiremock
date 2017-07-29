import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {DataSource} from '@angular/cdk';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';

@Component({
  selector: 'wm-code-entry-list',
  templateUrl: './code-entry-list.component.html',
  styleUrls: ['./code-entry-list.component.scss']
})
export class CodeEntryListComponent implements OnInit {

  @Input('entries')
  entries: DataEntries;
  dataSource: EntryDataSource | null;

  displayedColumns = ['key', 'value'];

  constructor(private changeDetector: ChangeDetectorRef) { }

  ngOnInit() {
    this.dataSource = new EntryDataSource(this.entries);
    this.changeDetector.detectChanges();
  }

}

export interface Entry{
  key: string;
  value: any;
  language: string;
}

export class DataEntries{
  dataChange: BehaviorSubject<Entry[]> = new BehaviorSubject<Entry[]>([]);
  get data(): Entry[]{
    return this.dataChange.value;
  };

  constructor() { }

  addEntry(entry: Entry){
    if(entry.value == null || typeof entry.value === 'undefined'){
      entry.value = "";
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
