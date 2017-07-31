import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Item} from '../list/list.component';

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

  @Output('onSelect')
  selectEmitter = new EventEmitter();

  constructor() { }

  selectItem(): void{
    this.selectEmitter.emit(this.item);
  }

  ngOnInit() {
  }

}
