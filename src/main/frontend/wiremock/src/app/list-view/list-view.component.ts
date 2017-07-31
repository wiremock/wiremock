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

  filteredItem : Item[];

  @Output('onSelect')
  selectEmitter = new EventEmitter();

  lastSearch: SearchEvent;

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    // this.filteredItem = UtilService.deepSearch(this.items, '', false);
    this.onSearchChanged(this.lastSearch);
    // this.filteredItem = this.items;
  }

  onSearchChanged(search: SearchEvent){
    this.lastSearch = search;
    if(UtilService.isDefined(search)){
      this.filteredItem = UtilService.deepSearch(this.items, search.text, search.caseSensitive);
      // this.filteredItem = UtilService.deepSearch(this.items, '', false);
    }else{
      this.filteredItem = UtilService.deepSearch(this.items, '', false);
    }
  }

  onSelect(item: Item){
    this.selectEmitter.emit(item);
  }

}
