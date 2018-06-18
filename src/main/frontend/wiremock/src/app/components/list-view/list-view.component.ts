import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Item} from '../../model/wiremock/item';

@Component({
  selector: 'wm-list-view',
  templateUrl: './list-view.component.html',
  styleUrls: ['./list-view.component.scss']
})
export class ListViewComponent implements OnInit {

  @Input()
  items: Item[];

  @Input()
  activeItem: Item;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  constructor() {

  }


  ngOnInit() {
  }

  selectActiveItem(item: Item) {
    this.activeItem = item;
    this.activeItemChange.emit(this.activeItem);
  }
}
