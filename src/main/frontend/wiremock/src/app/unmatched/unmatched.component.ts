import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {DataEntries, Entry} from '../code-entry-list/code-entry-list.component';
import {UtilService} from 'app/services/util.service';
import {LoggedRequest} from '../wiremock/model/logged-request';

@Component({
  selector: 'wm-unmatched',
  templateUrl: './unmatched.component.html',
  styleUrls: ['./unmatched.component.scss']
})
export class UnmatchedComponent implements OnInit, OnChanges {

  @Input()
  selectedUnmatched: LoggedRequest;
  code: string;

  general: DataEntries;
  request: DataEntries;

  constructor() {
  }


  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.code = UtilService.toJson(this.selectedUnmatched);
    this.general = this.getGeneral();
    this.request = this.getRequest();
  }

  isVisible(): boolean {
    return UtilService.isDefined(this.selectedUnmatched);
  }

  getGeneral(): DataEntries {
    const dataEntries = new DataEntries();
    if (this.selectedUnmatched == null || typeof this.selectedUnmatched === 'undefined') {
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('uuid', this.selectedUnmatched.getId(), 'plain'));

    return dataEntries;
  }

  getRequest(): DataEntries {
    const dataEntries = new DataEntries();
    if (this.selectedUnmatched == null || typeof this.selectedUnmatched === 'undefined') {
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('url', this.selectedUnmatched.url, 'plain'));
    dataEntries.addEntry(new Entry('absoluteUrl', this.selectedUnmatched.url, 'plain'));
    dataEntries.addEntry(new Entry('urlQueryParams', UtilService.getParametersOfUrl(this.selectedUnmatched.url), '', 'params'));
    dataEntries.addEntry(new Entry('method', this.selectedUnmatched.method, 'plain'));
    dataEntries.addEntry(new Entry('clientIp', this.selectedUnmatched.clientIp, 'plain'));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedUnmatched.headers), 'json'));
    dataEntries.addEntry(new Entry('cookies', UtilService.toJson(this.selectedUnmatched.cookies), 'json'));
    dataEntries.addEntry(new Entry('browserProxyRequest', UtilService.toJson(this.selectedUnmatched.isBrowserProxyRequest), 'plain'));
    dataEntries.addEntry(new Entry('loggedDate', this.selectedUnmatched.loggedDate, 'plain'));
    dataEntries.addEntry(new Entry('Date', new Date(this.selectedUnmatched.loggedDate), 'plain'));
    dataEntries.addEntry(new Entry('body', this.selectedUnmatched.body, ''));
    dataEntries.addEntry(new Entry('bodyAsBase64', UtilService.toJson(this.selectedUnmatched.bodyAsBase64), 'plain'));

    return dataEntries;
  }

}
