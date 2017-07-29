import { Component, OnInit } from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {ListStubMappingsResult} from '../wiremock/model/list-stub-mappings-result';
import {StubMapping} from '../wiremock/model/stub-mapping';
import {DataEntries, Entry} from '../code-entry-list/code-entry-list.component';

@Component({
  selector: 'wm-mapping',
  templateUrl: './mapping.component.html',
  styleUrls: ['./mapping.component.scss']
})
export class MappingComponent implements OnInit {

  mappingResult: ListStubMappingsResult;

  constructor(private wiremockService: WiremockService) { }

  ngOnInit() {
    this.refreshMappings();
  }

  toJson(value: any): string{
    if(value == null || typeof value === 'undefined'){
      return "";
    }else{
      return JSON.stringify(value);
    }
  }

  getGeneral(mapping: StubMapping){
    const dataEntries = new DataEntries();
    dataEntries.addEntry({
        key: "uuid",
        value: mapping.uuid,
        language: ""
    });

    dataEntries.addEntry({
      key: "name",
      value: mapping.name,
      language: ""
    });

    return dataEntries;
  }

  getRequest(mapping: StubMapping){
    const dataEntries = new DataEntries();
    dataEntries.addEntry({
      key: "url",
      value: mapping.request.url,
      language: ""
    });

    dataEntries.addEntry({
      key: "urlPattern",
      value: mapping.request.urlPattern,
      language: ""
    });

    dataEntries.addEntry({
      key: "urlPath",
      value: mapping.request.urlPath,
      language: ""
    });

    dataEntries.addEntry({
      key: "urlPathPattern",
      value: mapping.request.urlPathPattern,
      language: ""
    });

    dataEntries.addEntry({
      key: "method",
      value: mapping.request.method,
      language: ""
    });

    dataEntries.addEntry({
      key: "headers",
      value: this.toJson(mapping.request.headers),
      language: "json"
    });

    dataEntries.addEntry({
      key: "queryParameters",
      value: mapping.request.queryParameters,
      language: ""
    });

    dataEntries.addEntry({
      key: "cookies",
      value: this.toJson(mapping.request.cookies),
      language: "json"
    });

    dataEntries.addEntry({
      key: "basicAuth",
      value: this.toJson(mapping.request.basicAuth),
      language: "json"
    });

    dataEntries.addEntry({
      key: "bodyPatterns",
      value: this.toJson(mapping.request.bodyPatterns),
      language: "json"
    });

    dataEntries.addEntry({
      key: "customMatcher",
      value: this.toJson(mapping.request.customMatcher),
      language: "json"
    });

    return dataEntries;
  }

  getResponseDefinition(mapping: StubMapping){
    const dataEntries = new DataEntries();
    dataEntries.addEntry({
      key: "status",
      value: mapping.response.status,
      language: ""
    });

    dataEntries.addEntry({
      key: "name",
      value: mapping.response.statusMessage,
      language: ""
    });

    dataEntries.addEntry({
      key: "body",
      value: mapping.response.body,
      language: ""
    });

    dataEntries.addEntry({
      key: "jsonBody",
      value: mapping.response.jsonBody,
      language: ""
    });

    dataEntries.addEntry({
      key: "base64Body",
      value: mapping.response.base64Body,
      language: ""
    });

    dataEntries.addEntry({
      key: "bodyFileName",
      value: mapping.response.bodyFileName,
      language: ""
    });

    dataEntries.addEntry({
      key: "headers",
      value: this.toJson(mapping.response.headers),
      language: "json"
    });

    dataEntries.addEntry({
      key: "additionalProxyRequestHeaders",
      value: this.toJson(mapping.response.additionalProxyRequestHeaders),
      language: ""
    });

    dataEntries.addEntry({
      key: "fixedDelayMilliseconds",
      value: mapping.response.fixedDelayMilliseconds,
      language: ""
    });

    dataEntries.addEntry({
      key: "delayDistribution",
      value: mapping.response.delayDistribution,
      language: ""
    });

    dataEntries.addEntry({
      key: "proxyBaseUrl",
      value: mapping.response.proxyBaseUrl,
      language: ""
    });

    dataEntries.addEntry({
      key: "fault",
      value: mapping.response.fault,
      language: ""
    });

    dataEntries.addEntry({
      key: "transformers",
      value: this.toJson(mapping.response.transformers),
      language: "json"
    });

    dataEntries.addEntry({
      key: "transformerParameters",
      value: this.toJson(mapping.response.transformerParameters),
      language: "json"
    });

    dataEntries.addEntry({
      key: "fromConfiguredStub",
      value: mapping.response.fromConfiguredStub,
      language: ""
    });

    return dataEntries;
  }


  private refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
      this.mappingResult = data.json();
    },
    err => {
      console.log("failed!", err);
    });
  }



}
