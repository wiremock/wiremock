import {ChangeDetectorRef, Component, NgZone, OnInit} from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {ListStubMappingsResult} from '../wiremock/model/list-stub-mappings-result';
import {StubMapping} from '../wiremock/model/stub-mapping';
import {UtilService} from '../services/util.service';
import {SseService} from '../services/sse.service';
import {Observer} from 'rxjs/Observer';
import {Observable} from 'rxjs/Rx';
import {Message, MessageService, MessageType} from '../message/message.service';
import {ActivatedRoute} from '@angular/router';

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

  constructor(private wiremockService: WiremockService, private sseService: SseService, private cdr: ChangeDetectorRef,
              private messageService: MessageService, private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe((params) =>{
      const query = decodeURI(params['mapping'] || '');
      this.selectByItemId = query;
    });

    this.newMappingText = UtilService.prettify('{"request": {"method": "POST","url": ""},"response": {"status": 200,"body": "","headers": {"Content-Type": "text/plain"}}}');
    this.editMode = State.NORMAL;

    //soft update of mappings can  be triggered via observer
    Observable.create(observer =>{
      this.refreshMappingsObserver = observer;
    }).debounceTime(100).subscribe(() =>{
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
    this.selectedMapping = mapping;
    this.selectedMappingText = this.getSelectedMappingText();

    this.cdr.detectChanges();
  }

  saveMapping(): void{
    switch(this.editMode){
      case State.EDIT:
        this.saveEditedMapping();
        break;
      case State.NEW:
        this.saveNewMapping();
        break;
    }
  }

  saveEditedMapping(): void{
    this.wiremockService.saveMapping(this.selectedMapping.getId(), this.selectedMappingText).subscribe(response =>{
      this.setEditMode(State.NORMAL);
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  saveNewMapping(): void{
    this.wiremockService.saveNewMapping(this.newMappingText).subscribe(response =>{
      this.setEditMode(State.NORMAL);
      const newMapping = new StubMapping().deserialize(response.json());
      this.selectByItemId = newMapping.getId();
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  saveMappings(): void{
    this.wiremockService.saveMappings().subscribe(data =>{
      this.messageService.setMessage(new Message("Mappings saved", MessageType.INFO,3000));
    }, err =>{
      UtilService.showErrorMessage(this.messageService, err);
    })
  }

  resetMappings(): void{
    this.wiremockService.resetMappings().subscribe(data =>{
      //No feedback necessary
    }, err =>{
      UtilService.showErrorMessage(this.messageService, err);
    })
  }

  removeMapping(): void{
    this.wiremockService.deleteMapping(this.selectedMapping.getId()).subscribe(data =>{
      //No feedback necessary
    }, err =>{
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  deleteAllMappings(): void{
    this.wiremockService.deleteAllMappings().subscribe(data =>{
      //No feedback necessary
    }, err =>{
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  resetScenarios(): void{
    this.wiremockService.resetScenarios().subscribe(data =>{
      this.messageService.setMessage(new Message("Scenarios has been reset", MessageType.INFO,3000));
    }, err =>{
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  //################ HELPER ###################
  getMappingForHelper(): StubMapping{
    switch (this.editMode){
      case State.NEW:
        return JSON.parse(this.newMappingText);
      case State.EDIT:
        return JSON.parse(this.selectedMappingText);
    }
  }

  setMappingForHelper(mapping:StubMapping): void{
    switch (this.editMode){
      case State.NEW:
        this.newMappingText = UtilService.prettify(JSON.stringify(mapping));
      case State.EDIT:
        this.selectedMappingText = UtilService.prettify(JSON.stringify(mapping));
    }
  }

  helperAddDelay(): void{
    try{
      const mapping:StubMapping = this.getMappingForHelper();
      if(UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.fixedDelayMilliseconds)){
        mapping.response.fixedDelayMilliseconds = 2000;
        this.setMappingForHelper(mapping);
      }
    }catch(err){
      this.showHelperErrorMessage(err);
    }
  }

  helperAddPriority(): void{
    try{
      const mapping:StubMapping = this.getMappingForHelper();
      if(UtilService.isDefined(mapping) && UtilService.isUndefined(mapping.priority)){
        mapping.priority = 1;
        this.setMappingForHelper(mapping);
      }
    }catch(err){
      this.showHelperErrorMessage(err);
    }
  }

  helperAddHeaderRequest(): void{
    try{
      const mapping:StubMapping = this.getMappingForHelper();
      if(UtilService.isDefined(mapping) && UtilService.isDefined(mapping.request) && UtilService.isUndefined(mapping.request.headers)){
        mapping.request.headers = {"Content-Type": {
          "matches": ".*/xml"
        }};
        this.setMappingForHelper(mapping);
      }
    }catch(err){
      this.showHelperErrorMessage(err);
    }
  }

  helperAddHeaderResponse(): void{
    try{
      const mapping:StubMapping = this.getMappingForHelper();
      if(UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.headers)){
        mapping.response.headers = {"Content-Type": "application/json"};
        this.setMappingForHelper(mapping);
      }
    }catch(err){
      this.showHelperErrorMessage(err);
    }
  }

  private showHelperErrorMessage(err: any){
    this.messageService.setMessage(new Message(err.name + ": message=" + err.message + ", lineNumber=" + err.lineNumber + ", columnNumber=" + err.columnNumber,
      MessageType.ERROR,10000));
  }

  refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
      this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
    },
    err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}


