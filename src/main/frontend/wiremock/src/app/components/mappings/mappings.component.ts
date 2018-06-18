import {Component, HostBinding, OnInit} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {WiremockService} from '../../services/wiremock.service';
import {ListStubMappingsResult} from '../../model/wiremock/list-stub-mappings-result';
import {UtilService} from '../../services/util.service';

@Component({
  selector: 'wm-mappings',
  templateUrl: './mappings.component.html',
  styleUrls: ['./mappings.component.scss']
})
export class MappingsComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  result: ListStubMappingsResult;
  activeItem: Item;

  editMappingText: string;

  editMode: State;
  State = State;


  constructor(private wiremockService: WiremockService) {
  }

  ngOnInit() {
    this.editMode = State.NORMAL;
    this.loadMappings();
  }

  private loadMappings() {
    this.wiremockService.getMappings().subscribe(data => {
        this.result = new ListStubMappingsResult().deserialize(data);
        if (this.result != null && this.result.mappings != null && this.result.mappings.length > 0) {
          this.activeItem = this.result.mappings[0];
        }
      },
      err => {
        // UtilService.showErrorMessage(this.messageService, err);
      });
  }

  newMapping() {

  }

  enableEditMode() {
    this.editMappingText = UtilService.prettify(this.activeItem.getCode());
    this.editMode = State.EDIT;
  }

  onActiveItemChange(item: Item) {
    this.activeItem = item;
    this.editMode = State.NORMAL;
  }
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}
