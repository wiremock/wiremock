import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Item} from '../wiremock/model/item';

@Component({
  selector: 'wm-list-entry',
  templateUrl: './list-entry.component.html',
  styleUrls: ['./list-entry.component.scss']
})
export class ListEntryComponent implements OnInit {

  @Input()
  item: Item;

  @Input()
  selected: boolean;

  @Input()
  first: boolean;

  @Output()
  onSelect = new EventEmitter();

  constructor() {
  }

  selectItem(): void {
    this.onSelect.emit(this.item);
  }

  ngOnInit() {
  }

}
