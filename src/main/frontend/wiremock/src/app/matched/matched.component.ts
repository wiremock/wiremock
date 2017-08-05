import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ServeEvent} from '../wiremock/model/serve-event';
import {UtilService} from '../services/util.service';
import {DataEntries, Entry} from '../code-entry-list/code-entry-list.component';

@Component({
  selector: 'wm-matched',
  templateUrl: './matched.component.html',
  styleUrls: ['./matched.component.scss']
})
export class MatchedComponent implements OnInit, OnChanges {

  @Input()
  selectedMatched: ServeEvent;
  code: string;

  general: DataEntries;
  request: DataEntries;
  responseDefinition: DataEntries;

  constructor() { }


  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges): void {
    this.code = UtilService.toJson(this.selectedMatched);
    this.general = this.getGeneral();
    this.request = this.getRequest();
    this.responseDefinition = this.getResponseDefinition();
  }

  isVisible(): boolean{
    return UtilService.isDefined(this.selectedMatched);
  }

  getGeneral(){
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('uuid', this.selectedMatched.id, ''));

    return dataEntries;
  }

  getRequest(){
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('url', this.selectedMatched.request.url, ''));
    dataEntries.addEntry(new Entry('urlQueryParams', UtilService.getParametersOfUrl(this.selectedMatched.request.url),'', 'params'));
    dataEntries.addEntry(new Entry('method', this.selectedMatched.request.method, ''));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMatched.request.headers), 'json'));
    dataEntries.addEntry(new Entry('queryParameters', UtilService.toJson(this.selectedMatched.request.queryParams), 'json'));
    dataEntries.addEntry(new Entry('cookies', UtilService.toJson(this.selectedMatched.request.cookies), 'json'));

    return dataEntries;
  }

  getResponseDefinition(){
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('status', this.selectedMatched.response.status, ''));
    dataEntries.addEntry(new Entry('body', this.selectedMatched.response.body, ''));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMatched.response.headers), 'json'));
    dataEntries.addEntry(new Entry('fault', this.selectedMatched.response.fault, ''));

    return dataEntries;
  }

}
