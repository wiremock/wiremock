import {Component, HostBinding, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {ServeEvent} from '../../model/wiremock/serve-event';
import {isDefined} from "@ng-bootstrap/ng-bootstrap/util/util";
import {UtilService} from "../../services/util.service";
import {StubMapping} from "../../model/wiremock/stub-mapping";
import {ResponseDefinition} from "../../model/wiremock/response-definition";
import {WiremockService} from "../../services/wiremock.service";
import {debounceTime} from "rxjs/operators";
import {LoggedRequest} from "../../model/wiremock/logged-request";

import * as moment from 'moment';

@Component({
  selector: 'wm-separated',
  templateUrl: './separated.component.html',
  styleUrls: ['./separated.component.scss']
})
export class SeparatedComponent implements OnInit, OnChanges {

  @HostBinding('class') classes = 'wmHolyGrailScroll';

  @Input()
  activeItem: Item;

  color: string[] = ['bg-warning', 'bg-info', 'bg-danger', 'bg-primary', 'bg-secondary', 'bg-dark'];
  colorIndex = 0;

  bodyFileData: string;
  bodyGroupKey: string;

  constructor(private wiremockService: WiremockService) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.colorIndex = 0;
    if (UtilService.isDefined(this.activeItem)) {
      let responseDefinition: ResponseDefinition;
      if (this.activeItem instanceof StubMapping) {
        responseDefinition = (this.activeItem as StubMapping).response;
        this.bodyGroupKey = 'response';
      }else if (this.activeItem instanceof ServeEvent) {
        responseDefinition = (this.activeItem as ServeEvent).responseDefinition;
        this.bodyGroupKey = 'responseDefinition';
      }else{
        responseDefinition = null;
      }

      if (UtilService.isDefined(responseDefinition) && UtilService.isDefined(responseDefinition.bodyFileName)) {
        this.wiremockService.getFileBody(responseDefinition.bodyFileName).pipe(debounceTime(500)).subscribe(body => this.bodyFileData = body);
      }else{
        this.bodyFileData = null;
        this.bodyGroupKey = null;
      }
    }
  }

  isObject(property: any): boolean {
    return typeof property === 'object';
  }

  getNextColor(): string {
    this.colorIndex++;
    return this.color[this.colorIndex - 1];
  }

  isServeEvent(item: Item) {
    return item instanceof ServeEvent;
  }

  asServeEvent(item: Item): ServeEvent {
    return <ServeEvent>item;
  }

}
