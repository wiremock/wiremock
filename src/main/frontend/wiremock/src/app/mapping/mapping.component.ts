import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {StubMapping} from '../wiremock/model/stub-mapping';
import {DataEntries, Entry} from '../code-entry-list/code-entry-list.component';
import {UtilService} from 'app/services/util.service';

@Component({
  selector: 'wm-mapping',
  templateUrl: './mapping.component.html',
  styleUrls: ['./mapping.component.scss']
})
export class MappingComponent implements OnInit, OnChanges {

  @Input('selectedMapping')
  selectedMapping: StubMapping | null;
  code: string;

  constructor() { }

  ngOnInit() {
    this.code = UtilService.toJson(this.selectedMapping);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.code = UtilService.toJson(this.selectedMapping);
  }

  isVisible(): boolean{
    return UtilService.isDefined(this.selectedMapping);
  }

  getGeneral(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry(new Entry('uuid', this.selectedMapping.uuid, ''));
    dataEntries.addEntry(new Entry('name', this.selectedMapping.name, ''));
    dataEntries.addEntry(new Entry('priority', this.selectedMapping.priority, ''));
    dataEntries.addEntry(new Entry('scenarioName', this.selectedMapping.scenarioName, ''));
    dataEntries.addEntry(new Entry('requiredScenarioState', this.selectedMapping.requiredScenarioState, ''));
    dataEntries.addEntry(new Entry('newScenarioState', this.selectedMapping.newScenarioState, ''));

    return dataEntries;
  }

  getRequest(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('url', this.selectedMapping.request.url, ''));
    dataEntries.addEntry(new Entry('urlPattern', this.selectedMapping.request.urlPattern, ''));
    dataEntries.addEntry(new Entry('urlPath', this.selectedMapping.request.urlPath, ''));
    dataEntries.addEntry(new Entry('urlPathPattern', this.selectedMapping.request.urlPathPattern, ''));
    dataEntries.addEntry(new Entry('urlQueryParams', UtilService.getParametersOfUrl(this.selectedMapping.request.url),'', 'params'));
    dataEntries.addEntry(new Entry('method', this.selectedMapping.request.method, ''));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMapping.request.headers), 'json'));
    dataEntries.addEntry(new Entry('queryParameters', this.selectedMapping.request.queryParameters, ''));
    dataEntries.addEntry(new Entry('cookies', UtilService.toJson(this.selectedMapping.request.cookies), 'json'));
    dataEntries.addEntry(new Entry('basicAuth', UtilService.toJson(this.selectedMapping.request.basicAuth), 'json'));
    dataEntries.addEntry(new Entry('bodyPatterns', UtilService.toJson(this.selectedMapping.request.bodyPatterns), 'json'));
    dataEntries.addEntry(new Entry('customMatcher', UtilService.toJson(this.selectedMapping.request.customMatcher), 'json'));

    return dataEntries;
  }

  getResponseDefinition(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }

    dataEntries.addEntry(new Entry('status', this.selectedMapping.response.status, ''));
    dataEntries.addEntry(new Entry('name', this.selectedMapping.response.statusMessage, ''));
    dataEntries.addEntry(new Entry('body', this.selectedMapping.response.body, ''));
    dataEntries.addEntry(new Entry('jsonBody', this.selectedMapping.response.jsonBody, ''));
    dataEntries.addEntry(new Entry('base64Body', this.selectedMapping.response.base64Body, ''));
    dataEntries.addEntry(new Entry('bodyFileName', this.selectedMapping.response.bodyFileName, ''));
    dataEntries.addEntry(new Entry('headers', UtilService.toJson(this.selectedMapping.response.headers), 'json'));
    dataEntries.addEntry(new Entry('additionalProxy RequestHeaders', UtilService.toJson(this.selectedMapping.response.additionalProxyRequestHeaders), ''));
    dataEntries.addEntry(new Entry('fixedDelayMilliseconds', this.selectedMapping.response.fixedDelayMilliseconds, ''));
    dataEntries.addEntry(new Entry('delayDistribution', this.selectedMapping.response.delayDistribution, ''));
    dataEntries.addEntry(new Entry('proxyBaseUrl', this.selectedMapping.response.proxyBaseUrl, ''));
    dataEntries.addEntry(new Entry('fault', this.selectedMapping.response.fault, ''));
    dataEntries.addEntry(new Entry('transformers', UtilService.toJson(this.selectedMapping.response.transformers), 'json'));
    dataEntries.addEntry(new Entry('transformerParameters', UtilService.toJson(this.selectedMapping.response.transformerParameters), 'json'));
    dataEntries.addEntry(new Entry('fromConfiguredStub', this.selectedMapping.response.fromConfiguredStub, ''));

    return dataEntries;
  }
}
