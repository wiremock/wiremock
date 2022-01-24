import {Component, HostBinding, Input, OnChanges, OnInit, ViewChild} from '@angular/core';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import mimeDb from 'mime-db';
import {WiremockService} from '../../services/wiremock.service';
import {HttpErrorResponse, HttpEventType, HttpHeaders, HttpProgressEvent, HttpResponse} from '@angular/common/http';
import {Item} from '../../model/wiremock/item';
import {ServeEvent} from '../../model/wiremock/serve-event';
import {BehaviorSubject, Subject} from 'rxjs';
import {CodeEditorComponent} from '../code-editor/code-editor.component';

@Component({
  selector: 'wm-mapping-test',
  templateUrl: './mapping-test.component.html',
  styleUrls: [ './mapping-test.component.scss' ]
})
export class MappingTestComponent implements OnInit, OnChanges {


  @HostBinding('class') classes = 'wmHolyGrailBody';

  codeOptions = {
    selectionStyle: 'text',
    highlightActiveLine: true,
    highlightSelectedWord: true,
    readOnly: false,
    cursorStyle: 'ace',
    mergeUndoDeltas: 'true',
    behavioursEnabled: true,
    wrapBehavioursEnabled: true,
    copyWithEmptySelection: true,
    autoScrollEditorIntoView: true, // we need that
    useSoftTabs: true,
    // ...
    highlightGutterLine: false,
    showPrintMargin: false,
    printMarginColumn: false,
    printMargin: false,
    showGutter: true,
    displayIndentGuides: true,
    fontSize: 14,
    fontFamily: 'SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
    showLineNumbers: true,
    // ..
    wrap: true,
    enableMultiselect: true
  };

  codeReadOnlyOptions = {
    selectionStyle: 'text',
    highlightActiveLine: true, // readOnly
    highlightSelectedWord: true,
    readOnly: true, // readOnly
    cursorStyle: 'ace',
    mergeUndoDeltas: 'true',
    behavioursEnabled: true,
    wrapBehavioursEnabled: true,
    copyWithEmptySelection: true,
    autoScrollEditorIntoView: true, // we need that
    useSoftTabs: true,
    // ...
    highlightGutterLine: false,
    showPrintMargin: false,
    printMarginColumn: false,
    printMargin: false,
    showGutter: true,
    displayIndentGuides: true,
    fontSize: 14,
    fontFamily: 'SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
    showLineNumbers: true,
    // ..
    wrap: true,
    enableMultiselect: true
  };

  @Input()
  mapping: StubMapping;

  @ViewChild('headerContent') headerEditor: CodeEditorComponent;
  @ViewChild('bodyContent') bodyEditor: CodeEditorComponent;

  headerCode: string;

  url = new FormControl();
  language = 'json';

  response: any = '';
  responseBody: any = '';
  responseLanguage = 'json';
  responseIsMapping = false;
  responseMappingId = '';

  $headerValueChanges = new BehaviorSubject('');


  private static contentTypeToLanguage(cT: string) {
    // text/plain;charset=UTF-8

    let result = 'text';

    if (cT.includes('json')) {
      result = 'json';
    } else if (cT.includes('xml')) {
      result = 'xml';
    }

    return result;
  }

  constructor(/*public activeModal: NgbActiveModal,*/
              private wiremockService: WiremockService) {
  }

  ngOnInit(): void {

  }

  ngOnChanges(): void {
    if (this.mapping == null) {
      return;
    }

    this.url.setValue(this.getUrl());

    const cT = this.guessRequestContentType();
    this.responseBody = '';
    this.response = '';
    this.responseLanguage = 'json';
    this.responseIsMapping = false;

    this.$headerValueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(nextCT => {
        this.language = MappingTestComponent.contentTypeToLanguage(nextCT);
      });

    this.setContentType(cT);
  }

  private getUrl() {
    if (this.mapping.request) {
      return this.mapping.request.url ?? this.mapping.request.urlPath ??
        this.mapping.request.urlPattern ?? this.mapping.request.urlPathPattern;
    } else {
      return '';
    }
  }

  private guessRequestContentType() {
    let result = 'application/json';

    let cT = {'default': 'application/json'};
    if (this.mapping.request && this.mapping.request.headers) {
      cT = this.mapping.request.headers['Content-Type'] ?? this.mapping.request.headers['content-type'] ?? cT;
    }

    for (const value of Object.values(cT)) {
      if (typeof value === 'string') {
        if (value in mimeDb) {
          // we stop when we find the first valid one.
          result = value;
          break;
        } else {
          // check what every string we read if we can extract something.
          if (value.includes('json')) {
            result = 'application/json';
          } else if (value.includes('xml')) {
            result = 'application/xml';
          }
        }
      }
    }
    return result;
  }

  run() {
    let body = this.bodyEditor.getCode();
    if (body.trim().length === 0) {
      body = null;
    }

    const headers: { [header: string]: string | string[] } = {} = this.getHeaders();

    this.wiremockService.test(this.url.value, this.mapping.request.method.toUpperCase(), body, headers).subscribe(resp => {
      this.handleResponse(resp);
    }, (error: Error) => {
      this.handleErrorResponse(error);
    });
  }

  private handleResponse(resp) {
    if (!resp) {
      this.responseBody = '';
      this.response = '';
    }

    const data: any = {};
    data.type = HttpEventType[resp.type];
    switch (resp.type) {
      case HttpEventType.Response:
        data.status = resp.status;
        data.statusText = resp.statusText;
        data.headers = this.parseHeaders(resp.headers);
        this.checkIfIsMapping(data.headers);

        this.responseBody = resp.body ?? '';
        break;
      case HttpEventType.DownloadProgress:
      case HttpEventType.UploadProgress:
        data.loaded = resp.loaded;
        data.total = resp.total;

        this.responseBody = '';
        break;
      default:
        this.responseBody = '';
    }

    this.response = JSON.stringify(data);
  }

  private handleErrorResponse(error: Error | HttpErrorResponse) {
    const data: any = {};
    data.type = error.name;
    data.message = error.message;

    if (error instanceof HttpErrorResponse) {
      data.status = error.status;
      data.statusText = error.statusText;
      data.headers = this.parseHeaders(error.headers);

      this.responseLanguage = MappingTestComponent.contentTypeToLanguage(data.headers['Content-Type'] ?? data.headers['content-type'] ?? 'json');
      this.checkIfIsMapping(data.headers);

      this.responseBody = error.error ?? '';
    }

    this.response = JSON.stringify(data);
  }

  private checkIfIsMapping(headers: any) {
    const matchingMapping = headers['matched-stub-id'];
    this.responseIsMapping = matchingMapping === this.mapping.getId();
    this.responseMappingId = matchingMapping;
  }

  private parseHeaders(headers: any) {
    const result = {};
    if (headers) {
      const keys: string[] = headers.keys();

      keys.forEach(key => {
        result[key] = headers.get(key);
      });

      // result = keys.map(key =>
      //   `${key}: ${headers.get(key)}`);
    }
    return result;
  }

  asServeEvent(item: Item): ServeEvent {
    return <ServeEvent>item;
  }

  headerValueChange(text: string) {
    this.$headerValueChanges.next(text);
  }

  private getHeaders() {
    try {
      return JSON.parse(this.headerEditor.getCode());
    } catch (error) {
      return {};
    }
  }

  private setContentType(cT: string) {
    const headers = this.getHeaders();
    headers['content-type'] = cT;

    try {
      this.headerCode = JSON.stringify(headers);
    } catch (error) {
      // Do nothing
    }
  }
}
