import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Item} from '../wiremock/model/item';
import {UtilService} from '../services/util.service';
import {SearchEvent} from 'app/wiremock/model/search-event';

@Component({
  selector: 'wm-list-view',
  templateUrl: './list-view.component.html',
  styleUrls: ['./list-view.component.scss']
})
export class ListViewComponent implements OnInit, OnChanges {
  @Input('items')
  items: Item[];

  @Input('selectByItemId')
  selectByItemId: string;

  filteredItem : Item[];

  @Output('onSelect')
  selectEmitter = new EventEmitter();

  lastSearch: SearchEvent;

  constructor() { }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges): void {
    if(UtilService.isDefined(changes.selectByItemId)){
      this.selectByItemId = changes.selectByItemId.currentValue;
    }else{
      this.onSearchChanged(this.lastSearch);
    }
  }

  onSearchChanged(search: SearchEvent){
    this.lastSearch = search;
    if(UtilService.isDefined(search)){
      this.filteredItem = UtilService.deepSearch(this.items, search.text, search.caseSensitive);
    }else{
      this.filteredItem = UtilService.deepSearch(this.items, '', false);
    }

    //We deselect an item when there are no results.
    if(UtilService.isUndefined(this.filteredItem) || this.filteredItem.length == 0){
      this.onSelect(null);
    }
  }

  onSelect(item: Item){
    this.selectEmitter.emit(item);
  }
}
