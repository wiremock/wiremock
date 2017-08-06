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
  response: DataEntries;
  responseDefinition: DataEntries;

  constructor() { }


  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges): void {
    this.code = UtilService.toJson(this.selectedMatched);
    this.general = this.getGeneral();
    this.request = this.getRequest();
    this.response = this.getResponse();
    this.responseDefinition = this.getResponseDefinition();
  }

  isVisible(): boolean{
    return UtilService.isDefined(this.selectedMatched);
  }

  getGeneral(): DataEntries{
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('uuid', this.selectedMatched.id, 'plain'));
    if(UtilService.isDefined(this.selectedMatched.mapping)){
      dataEntries.addEntry(new Entry('mapping', this.selectedMatched.mapping.getId(), 'plain'));
    }

    return dataEntries;
  }

  getRequest(): DataEntries{
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('url', this.selectedMatched.request.url, 'plain'));
    dataEntries.addEntry(new Entry('absoluteUrl', this.selectedMatched.request.url, 'plain'));
    dataEntries.addEntry(new Entry('urlQueryParams', UtilService.getParametersOfUrl(this.selectedMatched.request.url),'', 'params'));
    dataEntries.addEntry(new Entry('method', this.selectedMatched.request.method, 'plain'));
    dataEntries.addEntry(new Entry('clientIp', this.selectedMatched.request.clientIp, 'plain'));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMatched.request.headers), 'json'));
    dataEntries.addEntry(new Entry('cookies', UtilService.toJson(this.selectedMatched.request.cookies), 'json'));
    dataEntries.addEntry(new Entry('browserProxyRequest', UtilService.toJson(this.selectedMatched.request.isBrowserProxyRequest), 'plain'));
    dataEntries.addEntry(new Entry('loggedDate', this.selectedMatched.request.loggedDate, 'plain'));
    dataEntries.addEntry(new Entry('Date', new Date(this.selectedMatched.request.loggedDate), 'plain'));
    dataEntries.addEntry(new Entry('body', this.selectedMatched.request.body, ''));
    dataEntries.addEntry(new Entry('bodyAsBase64', UtilService.toJson(this.selectedMatched.request.bodyAsBase64), 'plain'));

    return dataEntries;
  }

  getResponse(): DataEntries{
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('status', this.selectedMatched.response.status, 'plain'));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMatched.response.headers), 'json'));
    dataEntries.addEntry(new Entry('body', this.selectedMatched.response.body, ''));
    dataEntries.addEntry(new Entry('bodyAsBase64', this.selectedMatched.response.bodyAsBase64, 'plain'));
    dataEntries.addEntry(new Entry('fault', this.selectedMatched.response.fault, ''));

    return dataEntries;
  }

  getResponseDefinition(): DataEntries{
    const dataEntries = new DataEntries();
    if(this.selectedMatched == null || typeof this.selectedMatched === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('status', this.selectedMatched.responseDefinition.status, 'plain'));
    dataEntries.addEntry(new Entry('statusMessage', this.selectedMatched.responseDefinition.statusMessage, 'plain'));
    dataEntries.addEntry(new Entry('body', this.selectedMatched.responseDefinition.body, ''));
    dataEntries.addEntry(new Entry('jsonBody', this.selectedMatched.responseDefinition.jsonBody, ''));
    dataEntries.addEntry(new Entry('base64Body', this.selectedMatched.responseDefinition.base64Body, 'plain'));
    dataEntries.addEntry(new Entry('bodyFileName', this.selectedMatched.responseDefinition.bodyFileName, 'plain'));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMatched.responseDefinition.headers), 'json'));
    dataEntries.addEntry(new Entry('additionalProxy RequestHeaders', UtilService.toJson(this.selectedMatched.responseDefinition.additionalProxyRequestHeaders), ''));
    dataEntries.addEntry(new Entry('fixedDelayMilliseconds', this.selectedMatched.responseDefinition.fixedDelayMilliseconds, 'plain'));
    dataEntries.addEntry(new Entry('delayDistribution', this.selectedMatched.responseDefinition.delayDistribution, ''));
    dataEntries.addEntry(new Entry('proxyBaseUrl', this.selectedMatched.responseDefinition.proxyBaseUrl, 'plain'));
    dataEntries.addEntry(new Entry('fault', this.selectedMatched.responseDefinition.fault, ''));
    dataEntries.addEntry(new Entry('transformers', UtilService.toJson(this.selectedMatched.responseDefinition.transformers), 'json'));
    dataEntries.addEntry(new Entry('transformerParameters', UtilService.toJson(this.selectedMatched.responseDefinition.transformerParameters), 'json'));
    dataEntries.addEntry(new Entry('fromConfiguredStub', this.selectedMatched.responseDefinition.fromConfiguredStub, 'plain'));
    // dataEntries.addEntry(new Entry('isProxyingEnabled', this.selectedMatched.responseDefinition.fromConfiguredStub, 'plain'));

    return dataEntries;
  }

}
