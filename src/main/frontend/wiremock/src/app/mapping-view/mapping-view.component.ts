import {ChangeDetectorRef, Component, NgZone, OnInit} from '@angular/core';
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
  selectedMappingText: string;
  newMappingText: string;


  selectByItemId: string;

  editMode: State;
  State = State;

  private refreshMappingsObserver: Observer<string>;

  constructor(private wiremockService: WiremockService, private sseService: SseService, private cdr: ChangeDetectorRef, private zone: NgZone) {
  }

  ngOnInit() {
    this.newMappingText = UtilService.prettify('{"request": {"method": "POST","url": ""},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
    this.editMode = State.NORMAL;

    //soft update of mappings can  be triggered via observer
    Observable.create(observer =>{
      this.refreshMappingsObserver = observer;
    }).debounceTime(100).subscribe(next =>{
      this.refreshMappings();
    });

    //SSE registration for mappings updates
    this.sseService.register("message",data => {
      if(data.data === 'mappings'){
        this.refreshMappingsObserver.next(data.data);
      }
    });

    //initial mapping fetch
    this.refreshMappings();
  }

  setEditMode(value: State){
    this.editMode = value;
    this.selectedMappingText = this.getSelectedMappingText();
    this.newMappingText = this.getNewMappingsText();
  }

  private getSelectedMappingText(): string{
    return UtilService.prettify(JSON.stringify(this.selectedMapping));
  }

  private getNewMappingsText(): string{
    return UtilService.prettify('{"request": {"method": "POST","url": ""},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
  }

  setSelectedMapping(mapping: StubMapping){
    // const checkNeeded: boolean = UtilService.isUndefined(this.selectedMapping) && this.selectedMapping != mapping;
    this.selectedMapping = mapping;
    this.selectedMappingText = this.getSelectedMappingText();

    this.cdr.detectChanges();
  }

  saveEditedMapping(): void{
    this.wiremockService.saveMapping(this.selectedMapping.getId(), this.selectedMappingText).subscribe(response =>{
      this.setEditMode(State.NORMAL);
    }, err => {
      //TODO: popup
      console.log(err);
    });
  }

  saveNewMapping(): void{
    this.wiremockService.saveNewMapping(this.newMappingText).subscribe(response =>{
      this.setEditMode(State.NORMAL);
      const newMapping = new StubMapping().deserialize(response.json());
      this.selectByItemId = newMapping.getId();

      //TODO: select saved mapping
    }, err => {
      //TODO: popup
      console.log(err);
    });
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
      //No feedback necessary
    }, err =>{
      //TODO: show popup with error
    })
  }

  removeMapping(): void{
    this.wiremockService.deleteMapping(this.selectedMapping.getId()).subscribe(data =>{
      //No feedback necessary
    }, err =>{
      //TODO: show popup with error
    });
  }

  deleteAllMappings(): void{
    this.wiremockService.deleteAllMappings().subscribe(data =>{
      //No feedback necessary
    }, err =>{
      //TODO: show popup with error
    });
  }

  resetScenarios(): void{
    this.wiremockService.resetScenarios().subscribe(data =>{
      //TODO: show popup with rest ok.
    }, err =>{
      //TODO: show popup with error
    });
  }



  refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
        this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
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


