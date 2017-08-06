import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {GetServeEventsResult} from '../wiremock/model/get-serve-events-result';
import {Observer} from 'rxjs/Observer';
import {WiremockService} from '../services/wiremock.service';
import {SseService} from '../services/sse.service';
import {Observable} from 'rxjs/Observable';
import {FindRequestResult} from '../wiremock/model/find-request-result';
import {UtilService} from '../services/util.service';
import {isUndefined} from 'util';

@Component({
  selector: 'wm-unmatched-view',
  templateUrl: './unmatched-view.component.html',
  styleUrls: ['./unmatched-view.component.scss']
})
export class UnmatchedViewComponent implements OnInit {

  matchedResult: FindRequestResult;
  selectedMatched: any;

  private refreshMatchedObserver: Observer<string>;

  constructor(private wiremockService: WiremockService, private cdr: ChangeDetectorRef, private sseService: SseService) { }

  ngOnInit() {
    //soft update of mappings can  be triggered via observer
    Observable.create(observer =>{
      this.refreshMatchedObserver = observer;
    }).debounceTime(200).subscribe(next =>{
      this.refreshMatched();
    });

    //SSE registration for mappings updates
    this.sseService.register("message",data => {
      if(data.data === 'unmatched'){
        this.refreshMatchedObserver.next(data.data);
      }
    });

    //initial matched fetch
    this.refreshMatched();
  }

  setSelectedMatched(data: any): void{
    this.selectedMatched = data;
    this.cdr.detectChanges();
  }

  isSoapRequest(){
    if(UtilService.isUndefined(this.selectedMatched) || UtilService.isUndefined(this.selectedMatched.body)){
      return false;
    }
    return UtilService.isDefined(this.selectedMatched.body.match(UtilService.getSoapRecognizeRegex()));
  }

  createMapping(): void{

  }

  createSoapMapping():void{
    this._createSoapMapping();
  }

  private _createSoapMapping(){
    const request = this.selectedMatched;

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
    let protect = 0;
    const regex = UtilService.getSoapNamespaceRegex();
    while((match = regex.exec(body)) !== null){
      nameSpaces[match[1]] = match[2];
    }

    const soapMethod = body.match(UtilService.getSoapMethodRegex());

    const xPath = "/" + String(result[1]) + ":Envelope/" + String(result[2]) + ":Body/" + String(soapMethod[1]) + ":" + String(soapMethod[2]);

    let printedNamespaces:string = "";

    let first: boolean = true;
    for(let key in nameSpaces){
      if(!first){
        printedNamespaces += ",";
      }else{
        first = false;
      }
      printedNamespaces += "\"" + key + "\": \"" + nameSpaces[key] + "\"";
    }

    let message = '{"request": {"method": "' + method + '","url": "' + url + '","bodyPatterns": [{"matchesXPath": "' + xPath + '","xPathNamespaces": {' + printedNamespaces + '}}]},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/xml"}}}';
    message = UtilService.prettify(message);

    console.log(message);



  }

  resetJournal(): void{
    this.wiremockService.resetJournal().subscribe(next =>{
      //nothing to show
    }, err=>{
      //TODO: show message
    });
  }

  refreshMatched(): void{
    this.wiremockService.getUnmatched().subscribe(data => {
        this.matchedResult = new FindRequestResult().deserialize(data.json());

        // if(UtilService.isUndefined(this.matchedResult) || UtilService.isUndefined(this.matchedResult.requests)
        //   || this.matchedResult.requests.length == 0){
        //   this.selectedMatched = null;
        // }
      },
      err => {
        console.log("failed!", err);
      });
  }

}
