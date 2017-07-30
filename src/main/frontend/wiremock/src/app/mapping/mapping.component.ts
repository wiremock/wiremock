import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {StubMapping} from '../wiremock/model/stub-mapping';
import {DataEntries, Entry} from '../code-entry-list/code-entry-list.component';

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
    this.code = this.toJson(this.selectedMapping);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.code = this.toJson(this.selectedMapping);
  }

  isVisible(): boolean{
    return !(this.selectedMapping == null || typeof this.selectedMapping === 'undefined');
  }

  toJson(value: any): string{
    if(value == null || typeof value === 'undefined'){
      return '';
    }else{
      return JSON.stringify(value);
    }
  }

  getRaw(): string{
    const temp = this.toJson(this.selectedMapping);
    if(temp === this.code){
      return this.code;
    }else{
      this.code = temp;
    }

    return this.code;
  }

  getGeneral(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry({
        key: 'uuid',
        value: this.selectedMapping.uuid,
        language: ''
    });

    dataEntries.addEntry({
      key: 'name',
      value: this.selectedMapping.name,
      language: ''
    });

    return dataEntries;
  }

  getRequest(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry({
      key: 'url',
      value: this.selectedMapping.request.url,
      language: ''
    });

    dataEntries.addEntry({
      key: 'urlPattern',
      value: this.selectedMapping.request.urlPattern,
      language: ''
    });

    dataEntries.addEntry({
      key: 'urlPath',
      value: this.selectedMapping.request.urlPath,
      language: ''
    });

    dataEntries.addEntry({
      key: 'urlPathPattern',
      value: this.selectedMapping.request.urlPathPattern,
      language: ''
    });

    dataEntries.addEntry({
      key: 'method',
      value: this.selectedMapping.request.method,
      language: ''
    });

    dataEntries.addEntry({
      key: 'headers',
      value: this.toJson(this.selectedMapping.request.headers),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'queryParameters',
      value: this.selectedMapping.request.queryParameters,
      language: ''
    });

    dataEntries.addEntry({
      key: 'cookies',
      value: this.toJson(this.selectedMapping.request.cookies),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'basicAuth',
      value: this.toJson(this.selectedMapping.request.basicAuth),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'bodyPatterns',
      value: this.toJson(this.selectedMapping.request.bodyPatterns),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'customMatcher',
      value: this.toJson(this.selectedMapping.request.customMatcher),
      language: 'json'
    });

    return dataEntries;
  }

  getResponseDefinition(){
    const dataEntries = new DataEntries();
    if(this.selectedMapping == null || typeof this.selectedMapping === 'undefined'){
      return dataEntries;
    }
    dataEntries.addEntry({
      key: 'status',
      value: this.selectedMapping.response.status,
      language: ''
    });

    dataEntries.addEntry({
      key: 'name',
      value: this.selectedMapping.response.statusMessage,
      language: ''
    });

    dataEntries.addEntry({
      key: 'body',
      value: this.selectedMapping.response.body,
      language: ''
    });

    dataEntries.addEntry({
      key: 'jsonBody',
      value: this.selectedMapping.response.jsonBody,
      language: ''
    });

    dataEntries.addEntry({
      key: 'base64Body',
      value: this.selectedMapping.response.base64Body,
      language: ''
    });

    dataEntries.addEntry({
      key: 'bodyFileName',
      value: this.selectedMapping.response.bodyFileName,
      language: ''
    });

    dataEntries.addEntry({
      key: 'headers',
      value: this.toJson(this.selectedMapping.response.headers),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'additionalProxyRequestHeaders',
      value: this.toJson(this.selectedMapping.response.additionalProxyRequestHeaders),
      language: ''
    });

    dataEntries.addEntry({
      key: 'fixedDelayMilliseconds',
      value: this.selectedMapping.response.fixedDelayMilliseconds,
      language: ''
    });

    dataEntries.addEntry({
      key: 'delayDistribution',
      value: this.selectedMapping.response.delayDistribution,
      language: ''
    });

    dataEntries.addEntry({
      key: 'proxyBaseUrl',
      value: this.selectedMapping.response.proxyBaseUrl,
      language: ''
    });

    dataEntries.addEntry({
      key: 'fault',
      value: this.selectedMapping.response.fault,
      language: ''
    });

    dataEntries.addEntry({
      key: 'transformers',
      value: this.toJson(this.selectedMapping.response.transformers),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'transformerParameters',
      value: this.toJson(this.selectedMapping.response.transformerParameters),
      language: 'json'
    });

    dataEntries.addEntry({
      key: 'fromConfiguredStub',
      value: this.selectedMapping.response.fromConfiguredStub,
      language: ''
    });

    return dataEntries;
  }
}
