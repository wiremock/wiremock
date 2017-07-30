import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';

@Component({
  selector: 'wm-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit, OnChanges {
  @Input('items')
  items: Item[];

  @Output('onSelect')
  selectEmitter = new EventEmitter();

  previousSelectedItem: Item;

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.items != null && typeof this.items !== 'undefined'){
      for(const item of this.items){
        if(item.selected){
          this.previousSelectedItem = item;
        }
      }
    }
  }

  itemSelected(item :Item): void{
    if(typeof this.previousSelectedItem !== 'undefined'){
      this.previousSelectedItem.setSelected(false);
    }
    item.setSelected(true);

    this.selectEmitter.emit(item);
    this.previousSelectedItem = item;
  }

}

export abstract class Item{

  selected: boolean | false;

  abstract getTitle(): string;
  abstract getSubtitle(): string;

  setSelected(value: boolean){
    this.selected = value;
  }
}
