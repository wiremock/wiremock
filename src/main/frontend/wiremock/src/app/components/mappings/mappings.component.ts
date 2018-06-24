import {Component, HostBinding, OnInit} from '@angular/core';
import {WiremockService} from '../../services/wiremock.service';
import {ListStubMappingsResult} from '../../model/wiremock/list-stub-mappings-result';
import {UtilService} from '../../services/util.service';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {WebSocketService} from '../../services/web-socket.service';
import {WebSocketListener} from '../../interfaces/web-socket-listener';
import {debounceTime} from 'rxjs/operators';
import {MappingHelperService} from './mapping-helper.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {Item} from '../../model/wiremock/item';

@Component({
  selector: 'wm-mappings',
  templateUrl: './mappings.component.html',
  styleUrls: ['./mappings.component.scss']
})
export class MappingsComponent implements OnInit, WebSocketListener {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  result: ListStubMappingsResult;

  activeItemId: string;

  editMappingText: string;
  newMappingText: string;

  editMode: State;
  State = State;


  constructor(private wiremockService: WiremockService, private webSocketService: WebSocketService,
              private messageService: MessageService) {
  }

  ngOnInit() {

    this.webSocketService.observe('mappings').pipe(debounceTime(100)).subscribe(() => {
      this.loadMappings();
    });

    this.editMode = State.NORMAL;
    this.loadMappings();
  }


  private loadMappings() {
    this.wiremockService.getMappings().subscribe(data => {
        this.result = new ListStubMappingsResult().deserialize(data);
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

  newMapping() {
    this.newMappingText = UtilService.prettify(UtilService.toJson(StubMapping.createEmpty()));
    this.editMode = State.NEW;
  }

  saveNewMapping() {
    this.wiremockService.saveNewMapping(this.newMappingText).subscribe(data => {
      console.log(data.getId());
      this.activeItemId = data.getId();
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
    this.editMode = State.NORMAL;
  }

  editMapping(item: Item) {
    this.editMappingText = UtilService.prettify(item.getCode());
    this.editMode = State.EDIT;
  }

  saveEditMapping(item: Item) {
    this.wiremockService.saveMapping(item.getId(), this.editMappingText).subscribe(data => {
      // console.log(data.getId());
      this.activeItemId = data.getId();
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
    this.editMode = State.NORMAL;
  }

  onActiveItemChange() {
    this.editMode = State.NORMAL;
  }

  removeMapping(item: Item) {
    this.wiremockService.deleteMapping(item.getId()).subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  saveMappings() {
    this.wiremockService.saveMappings().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  resetMappings() {
    this.wiremockService.resetMappings().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  removeAllMappings() {
    this.wiremockService.deleteAllMappings().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  resetAllScenarios() {
    this.wiremockService.resetScenarios().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

  onMessage(): void {
    this.loadMappings();
  }

  // ##### HELPER #####
  private showHelperErrorMessage(err: any) {
    this.messageService.setMessage(new Message(err.name + ': message=' + err.message +
      ', lineNumber=' + err.lineNumber + ', columnNumber=' + err.columnNumber,
      MessageType.ERROR, 10000));
  }

  getMappingForHelper(): StubMapping {
    try {
      switch (this.editMode) {
        case State.NEW:
          return JSON.parse(this.newMappingText);
        case State.EDIT:
          return JSON.parse(this.editMappingText);
      }
    } catch (err) {
      this.showHelperErrorMessage(err);
    }
  }

  private setMappingForHelper(mapping: StubMapping): void {
    if (UtilService.isUndefined(mapping)) {
      return;
    }
    try {
      switch (this.editMode) {
        case State.NEW:
          this.newMappingText = UtilService.prettify(JSON.stringify(mapping));
          break;
        case State.EDIT:
          this.editMappingText = UtilService.prettify(JSON.stringify(mapping));
          break;
      }
    } catch (err) {
      this.showHelperErrorMessage(err);
    }
  }

  helpersAddDelay(): void {
    this.setMappingForHelper(MappingHelperService.helperAddDelay(this.getMappingForHelper()));
  }

  helpersAddPriority(): void {
    this.setMappingForHelper(MappingHelperService.helperAddPriority(this.getMappingForHelper()));
  }

  helpersAddHeaderRequest(): void {
    this.setMappingForHelper(MappingHelperService.helperAddHeaderRequest(this.getMappingForHelper()));
  }

  helpersAddHeaderResponse(): void {
    this.setMappingForHelper(MappingHelperService.helperAddHeaderResponse(this.getMappingForHelper()));
  }

  helpersAddScenario() {
    this.setMappingForHelper(MappingHelperService.helperAddScenario(this.getMappingForHelper()));
  }

  helpersAddProxyUrl() {
    this.setMappingForHelper(MappingHelperService.helperAddProxyBaseUrl(this.getMappingForHelper()));
  }

  helpersAddProxyHeader() {
    this.setMappingForHelper(MappingHelperService.helperAddAdditionalProxyRequestHeaders(this.getMappingForHelper()));
  }

  helpersAddTransformer() {
    this.setMappingForHelper(MappingHelperService.helperAddResponseTemplatingTransformer(this.getMappingForHelper()));
  }
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}
