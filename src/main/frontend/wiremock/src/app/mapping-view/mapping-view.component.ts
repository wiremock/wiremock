import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {ListStubMappingsResult} from '../wiremock/model/list-stub-mappings-result';
import {StubMapping} from '../wiremock/model/stub-mapping';
import {UtilService} from '../services/util.service';
import {SseService} from '../services/sse.service';
import {Observer} from 'rxjs/Observer';
import {Observable} from 'rxjs/Rx';

@Component({
  selector: 'wm-mapping-view',
  templateUrl: './mapping-view.component.html',
  styleUrls: ['./mapping-view.component.scss']
})
export class MappingViewComponent implements OnInit {


  mappingResult: ListStubMappingsResult;
  selectedMapping: StubMapping;

  editMode: State;
  State = State;

  private refreshMappingsObserver: Observer<string>;

  constructor(private wiremockService: WiremockService, private cdr: ChangeDetectorRef, private sseService: SseService) {
  }

  ngOnInit() {
    this.editMode = State.NORMAL;

    Observable.create(observer =>{
      this.refreshMappingsObserver = observer;
    }).debounceTime(200).subscribe(next =>{
      this.refreshMappings();
    });

    this.sseService.register("message",data => {
      if(data.data === 'mappings'){
        // this.refreshMappings();
        this.refreshMappingsObserver.next(data.data);
      }
    });

    this.refreshMappings();
  }

  setEditMode(value: State){
    this.editMode = value;
  }

  saveEditedMapping(): void{
    //TODO: save mapping
    this.editMode = State.NORMAL;
  }

  saveNewMapping(): void{
    //TODO: save mapping
    this.editMode = State.NORMAL;
  }

  handleTabKey(event: any):void{
    //TODO: not working properly.
    if(event.which === 9){
      event.preventDefault();

      const elem = event.target;

      // get caret position/selection
      const start = elem.selectionStart;
      const end = elem.selectionEnd;

      const text = elem.innerHTML;

      elem.innerHTML = text.substring(0, start) + "\t" + text.substring(end);

      event.target.selectionStart = event.target.selectionEnd = start + 1;
    }
  }

  getSelectedMappingText(): string{
    return UtilService.prettify(JSON.stringify(this.selectedMapping));
  }

  getNewMappingText(): string{
    return UtilService.prettify('{"request": {"method": "POST","url": ""},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
  }

  setSelectedMapping(mapping: StubMapping){
    this.selectedMapping = mapping;
    this.cdr.detectChanges();
  }

  saveMappings(): void{
    this.wiremockService.saveMappings().subscribe(data =>{
      //TODO: show popup with save ok.
    }, err =>{
      //TODO: show popup with error
    })
  }

  resetMappings(): void{
    this.wiremockService.resetMappings().subscribe(data =>{
      this.refreshMappings();
    }, err =>{
      //TODO: show popup with error
    })
  }

  removeMapping(): void{
    this.wiremockService.deleteMapping(this.selectedMapping.uuid).subscribe(data =>{
      //it worked fine
      this.refreshMappings();
    }, err =>{
      //TODO: show popup with error
    });
  }

  deleteAllMappings(): void{
    this.wiremockService.deleteAllMappings().subscribe(data =>{
      //it worked fine
      this.refreshMappings();
    }, err =>{
      //TODO: show popup with error
    });
  }

  resetScenarios(): void{
    this.wiremockService.resetScenarios().subscribe(data =>{
      //it worked fine
      //TODO: feedback?
    }, err =>{
      //TODO: show popup with error
    });
  }

  private refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
        this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
        if(UtilService.isUndefined(this.mappingResult) || UtilService.isUndefined(this.mappingResult.mappings)
          || this.mappingResult.mappings.length == 0){
          this.selectedMapping = null;
        }
      },
      err => {
        console.log("failed!", err);
      });
  }
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}

