import {Component, HostBinding, OnInit} from '@angular/core';
import {UtilService} from '../../services/util.service';
import {WiremockService} from '../../services/wiremock.service';
import {WebSocketService} from '../../services/web-socket.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {FindRequestResult} from '../../model/wiremock/find-request-result';
import {LoggedRequest} from '../../model/wiremock/logged-request';

@Component({
  selector: 'wm-unmatched',
  templateUrl: './unmatched.component.html',
  styleUrls: ['./unmatched.component.scss']
})
export class UnmatchedComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  requestResult: FindRequestResult;

  constructor(private wiremockService: WiremockService, private webSocketService: WebSocketService,
              private messageService: MessageService) {
  }

  ngOnInit() {
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
    if (UtilService.copyToClipboard(UtilService.copyCurl(request))) {
      this.messageService.setMessage(new Message('Curl copied to clipboard', MessageType.INFO, 3000));
    } else {
      this.messageService.setMessage(new Message('Was not able to copy. Details in log', MessageType.ERROR, 10000));
    }
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
}
