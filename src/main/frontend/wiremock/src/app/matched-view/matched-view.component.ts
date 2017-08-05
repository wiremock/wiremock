import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {Observer} from 'rxjs/Observer';
import {Observable} from 'rxjs/Observable';
import {SseService} from '../services/sse.service';
import {UtilService} from '../services/util.service';
import {GetServeEventsResult} from '../wiremock/model/get-serve-events-result';

@Component({
  selector: 'wm-matched-view',
  templateUrl: './matched-view.component.html',
  styleUrls: ['./matched-view.component.scss']
})
export class MatchedViewComponent implements OnInit {

  matchedResult: GetServeEventsResult;
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
      if(data.data === 'matched'){
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

  refreshMatched(): void{
    this.wiremockService.getMatched().subscribe(data => {
        this.matchedResult = new GetServeEventsResult().deserialize(data.json(), true);
        if(UtilService.isUndefined(this.matchedResult) || UtilService.isUndefined(this.matchedResult.requests)
          || this.matchedResult.requests.length == 0){
          this.selectedMatched = null;
        }
      },
      err => {
        console.log("failed!", err);
      });
  }

}
