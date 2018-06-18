import {Component, HostBinding, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Item} from '../../model/wiremock/item';

@Component({
  selector: 'wm-separated',
  templateUrl: './separated.component.html',
  styleUrls: ['./separated.component.scss']
})
export class SeparatedComponent implements OnInit, OnChanges {

  @HostBinding('class') classes = 'wmHolyGrailScroll';

  @Input()
  activeItem: Item;

  @Input()
  specialTypes: SeparatedType[];

  color: string[] = ['bg-warning', 'bg-info', 'bg-danger'];
  colorIndex = 0;

  constructor() {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.colorIndex = 0;
  }

  isObject(property: any): boolean {
    return typeof property === 'object';
  }

  getNextColor(): string {
    this.colorIndex++;
    return this.color[this.colorIndex - 1];
  }
}

export enum SeparatedType {
  LINK,
  MAP,
  TEXT,
}
