import {Component, OnInit} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {WiremockService} from '../../services/wiremock.service';
import {ListStubMappingsResult} from '../../model/wiremock/list-stub-mappings-result';

@Component({
  selector: 'wm-mappings',
  templateUrl: './mappings.component.html',
  styleUrls: ['./mappings.component.scss']
})
export class MappingsComponent implements OnInit {

  result: ListStubMappingsResult;
  activeItem: Item;

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
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}
