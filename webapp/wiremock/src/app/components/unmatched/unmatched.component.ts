import {Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {UtilService} from '../../services/util.service';
import {WiremockService} from '../../services/wiremock.service';
import {WebSocketService} from '../../services/web-socket.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {FindRequestResult} from '../../model/wiremock/find-request-result';
import {LoggedRequest} from '../../model/wiremock/logged-request';
import {debounceTime, filter, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs/internal/Subject';
import {CurlExtractor} from '../../services/curl-extractor';
import {AutoRefreshService} from '../../services/auto-refresh.service';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {StateMappingInfoComponent} from '../state-mapping-info/state-mapping-info.component';
import {CurlPreviewComponent} from '../curl-preview/curl-preview.component';

@Component({
  selector: 'wm-unmatched',
  templateUrl: './unmatched.component.html',
  styleUrls: [ './unmatched.component.scss' ]
})
export class UnmatchedComponent implements OnInit, OnDestroy {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  private ngUnsubscribe: Subject<any> = new Subject();

  requestResult: FindRequestResult;

  constructor(private wiremockService: WiremockService, private webSocketService: WebSocketService,
              private messageService: MessageService, private autoRefreshService: AutoRefreshService,
              private modalService: NgbModal) {
  }

  ngOnInit() {
    this.webSocketService.observe('unmatched').pipe(
      filter(() => this.autoRefreshService.isAutoRefreshEnabled()),
      takeUntil(this.ngUnsubscribe), debounceTime(100))
      .subscribe(() => {
        this.loadMappings();
      });

    this.loadMappings();
  }

  private loadMappings() {
    this.wiremockService.getUnmatched().subscribe(data => {
        this.requestResult = new FindRequestResult().deserialize(data);
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

  isSoapRequest(request: LoggedRequest): boolean {
    if (UtilService.isUndefined(request) || UtilService.isUndefined(request.body)) {
      return false;
    }
    return UtilService.isDefined(request.body.match(UtilService.getSoapRecognizeRegex()));
  }

  copyRequest(request: LoggedRequest) {
    const message = this.createMapping(request);
    this.copyMappingTemplateToClipboard(message);
  }

  copySoap(request: LoggedRequest) {
    const message = this.createSoapMapping(request);
    this.copyMappingTemplateToClipboard(message);
  }

  copyCurl(request: LoggedRequest) {
    if (UtilService.copyToClipboard(CurlExtractor.copyCurl(request))) {
      this.messageService.setMessage(new Message('Curl copied to clipboard', MessageType.INFO, 3000));
    } else {
      this.messageService.setMessage(new Message('Was not able to copy. Details in log', MessageType.ERROR, 10000));
    }
  }

  editCurl(request: LoggedRequest) {
    const curl = CurlExtractor.extractCurl(request);
    const modalRef = this.modalService.open(CurlPreviewComponent, {
      size: 'lg',
      windowClass: 'modal-h70'
    });
    modalRef.componentInstance.curl = curl;
  }

  private copyMappingTemplateToClipboard(message: string) {
    if (UtilService.copyToClipboard(message)) {
      this.messageService.setMessage(new Message('Mapping template copied to clipboard', MessageType.INFO, 3000));
    } else {
      this.messageService.setMessage(new Message('Was not able to copy. Details in log', MessageType.ERROR, 10000));
    }
  }

  private createMapping(request: LoggedRequest): string {
    return UtilService.prettify('{"request": {"method": "' + request.method + '","url": "' + request.url
      + '"},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
  }

  private createSoapMapping(request: LoggedRequest): string {
    const method: string = request.method;
    const url: string = request.url;
    const body: string = request.body;
    const result = body.match(UtilService.getSoapRecognizeRegex());

    if (UtilService.isUndefined(result)) {
      return;
    }

    const nameSpaces: { [key: string]: string } = {};

    let match;
    const regex = UtilService.getSoapNamespaceRegex();
    while ((match = regex.exec(body)) !== null) {
      nameSpaces[match[1]] = match[2];
    }

    const soapMethod = body.match(UtilService.getSoapMethodRegex());

    const xPath = '/' + String(result[1]) + ':Envelope/' + String(result[2]) + ':Body/'
      + String(soapMethod[1]) + ':' + String(soapMethod[2]);

    let printedNamespaces = '';

    let first = true;
    for (const key of Object.keys(nameSpaces)) {
      if (!first) {
        printedNamespaces += ',';
      } else {
        first = false;
      }
      printedNamespaces += '"' + key + '": "' + nameSpaces[key] + '"';
    }

    let message = '{"request": {"method": "' + method + '","url": "' + url + '","bodyPatterns": [{"matchesXPath": "'
      + xPath + '","xPathNamespaces": {' + printedNamespaces
      + '}}]},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/xml"}}}';
    message = UtilService.prettify(message);

    return message;
  }


  resetJournal() {
    this.wiremockService.resetJournal().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }

}
