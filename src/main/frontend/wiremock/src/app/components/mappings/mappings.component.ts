import {Component, HostBinding, OnInit} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {WiremockService} from '../../services/wiremock.service';
import {ListStubMappingsResult} from '../../model/wiremock/list-stub-mappings-result';
import {UtilService} from '../../services/util.service';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {ActivatedRoute, Params, Router} from '@angular/router';

@Component({
  selector: 'wm-mappings',
  templateUrl: './mappings.component.html',
  styleUrls: ['./mappings.component.scss']
})
export class MappingsComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  result: ListStubMappingsResult;
  activeItem: Item;
  activeItemId: string;

  editMappingText: string;
  newMappingText: string;

  editMode: State;
  State = State;


  constructor(private wiremockService: WiremockService, private activatedRoute: ActivatedRoute, private router: Router) {
  }

  ngOnInit() {
    this.editMode = State.NORMAL;

    this.loadMappings();

    this.activatedRoute.queryParams.subscribe((params: Params) => {
      this.activeItemId = params['active'];
    });
  }

  private setActiveItem(item: Item) {
    this.activeItem = item;
    if (UtilService.isDefined(this.activeItem)) {
      this.activeItemId = this.activeItem.getId();
      this.router.navigate(['mappings'], {queryParams: {active: this.activeItemId}});
    } else {
      this.activeItemId = null;
      this.router.navigate(['mappings']);
    }

    this.editMode = State.NORMAL;
  }

  private loadMappings() {
    this.wiremockService.getMappings().subscribe(data => {
        this.result = new ListStubMappingsResult().deserialize(data);
        this.setActiveItem(UtilService.getActiveItem(this.result.mappings, this.activeItemId));
      },
      err => {
        // UtilService.showErrorMessage(this.messageService, err);
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
      // UtilService.showErrorMessage(this.messageService, err);
    });
    this.editMode = State.NORMAL;
  }

  editMapping() {
    this.editMappingText = UtilService.prettify(this.activeItem.getCode());
    this.editMode = State.EDIT;
  }

  onActiveItemChange(item: Item) {
    this.setActiveItem(item);
  }

  removeMapping() {
    this.wiremockService.deleteMapping(this.activeItem.getId()).subscribe(() => {
      // do nothing
    }, err => {
      // UtilService.showErrorMessage(this.messageService, err);
    });
  }

  saveMappings() {
    this.wiremockService.saveMappings().subscribe(() => {
      // do nothing
    }, err => {
      // UtilService.showErrorMessage(this.messageService, err);
    });
  }

  resetMappings() {
    this.wiremockService.resetMappings().subscribe(() => {
      // do nothing
    }, err => {
      // UtilService.showErrorMessage(this.messageService, err);
    });
  }

  removeAllMappings() {
    this.wiremockService.deleteAllMappings().subscribe(() => {
      // do nothing
    }, err => {
      // UtilService.showErrorMessage(this.messageService, err);
    });
  }
}

export enum State {
  NORMAL,
  EDIT,
  NEW,
}
