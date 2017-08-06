import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {Observer} from 'rxjs/Observer';
import {WiremockService} from '../services/wiremock.service';
import {SseService} from '../services/sse.service';
import {Observable} from 'rxjs/Observable';
import {FindRequestResult} from '../wiremock/model/find-request-result';
import {UtilService} from '../services/util.service';
import {isUndefined} from 'util';
import {LoggedRequest} from '../wiremock/model/logged-request';
import {MessageService, Message, MessageType} from 'app/message/message.service';

@Component({
  selector: 'wm-unmatched-view',
  templateUrl: './unmatched-view.component.html',
  styleUrls: ['./unmatched-view.component.scss']
})
export class UnmatchedViewComponent implements OnInit {

  unmatchedResult: FindRequestResult;
  selectedUnmatched: any;

  private refreshUnmatchedObserver: Observer<string>;

  constructor(private wiremockService: WiremockService, private cdr: ChangeDetectorRef, private sseService: SseService, private messageService: MessageService) { }

  ngOnInit() {
    //soft update of mappings can  be triggered via observer
    Observable.create(observer =>{
      this.refreshUnmatchedObserver = observer;
    }).debounceTime(200).subscribe(next =>{
      this.refreshMatched();
    });

    //SSE registration for mappings updates
    this.sseService.register('message',data => {
      if(data.data === 'unmatched'){
        this.refreshUnmatchedObserver.next(data.data);
      }
    });

    //initial matched fetch
    this.refreshMatched();
  }

  setSelectedMatched(data: any): void{
    this.selectedUnmatched = data;
    this.cdr.detectChanges();
  }

  isSoapRequest(){
    if(UtilService.isUndefined(this.selectedUnmatched) || UtilService.isUndefined(this.selectedUnmatched.body)){
      return false;
    }
    return UtilService.isDefined(this.selectedUnmatched.body.match(UtilService.getSoapRecognizeRegex()));
  }

  createMapping(): void{
    const message = this._createMapping(this.selectedUnmatched);

    this.messageService.setMessage(new Message(message, MessageType.INFO, 20000));
  }

  createSoapMapping():void{
    this._createSoapMapping();
  }

  private _createSoapMapping(): string{
    const request = this.selectedUnmatched;

    const method:string = request.method;
    const url:string = request.url;
    const body:string = request.body;
    const result = body.match(UtilService.getSoapRecognizeRegex());

    if(isUndefined(result)){
      return;
    }

    const nameSpaces: {[key: string]: string} = {};

    // const nameSpaces = [];

    let match;
    const regex = UtilService.getSoapNamespaceRegex();
    while((match = regex.exec(body)) !== null){
      nameSpaces[match[1]] = match[2];
    }

    const soapMethod = body.match(UtilService.getSoapMethodRegex());

    const xPath = '/' + String(result[1]) + ':Envelope/' + String(result[2]) + ':Body/' + String(soapMethod[1]) + ':' + String(soapMethod[2]);

    let printedNamespaces:string = '';

    let first: boolean = true;
    for(let key in nameSpaces){
      if(!first){
        printedNamespaces += ',';
      }else{
        first = false;
      }
      printedNamespaces += '"' + key + '": "' + nameSpaces[key] + '"';
    }

    let message = '{"request": {"method": "' + method + '","url": "' + url + '","bodyPatterns": [{"matchesXPath": "' + xPath + '","xPathNamespaces": {' + printedNamespaces + '}}]},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/xml"}}}';
    message = UtilService.prettify(message);

    return message;
  }

  private _createMapping(request: LoggedRequest): string{
    return UtilService.prettify('{"request": {"method": "' + request.method + '","url": "' + request.url + '"},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
  }

  resetJournal(): void{
    this.wiremockService.resetJournal().subscribe(next =>{
      //nothing to show
    }, err=>{
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  refreshMatched(): void{
    this.wiremockService.getUnmatched().subscribe(data => {
        this.unmatchedResult = new FindRequestResult().deserialize(data.json());
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

}
