import {Component, EventEmitter, HostBinding, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {UtilService} from '../../services/util.service';

@Component({
  selector: 'wm-list-view',
  templateUrl: './list-view.component.html',
  styleUrls: ['./list-view.component.scss']
})
export class ListViewComponent implements OnInit, OnChanges {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  @Input()
  items: Item[];

  filteredItems: Item[];

  @Input()
  activeItem: Item;

  pageSize = 20;

  page: number;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  constructor() {

  }


  ngOnInit() {
    this.page = 1;
  }

  selectActiveItem(item: Item) {
    if (this.activeItem === item) {
      return;
    }
    this.activeItem = item;
    this.activeItemChange.emit(this.activeItem);
  }

  ngOnChanges(changes: SimpleChanges): void {

    if (UtilService.isDefined(changes.items) && UtilService.isDefined(this.items)) {
      const maxPages = this.items.length / this.pageSize;
      if (maxPages < this.page) {
        this.page = maxPages;
      }
      this.setFilteredItems();
    }
  }

  private setFilteredItems() {
    this.filteredItems = this.items.slice((this.page - 1) * this.pageSize, this.page * this.pageSize);
  }

  onPageChange(page: number) {
    this.page = page;
    this.setFilteredItems();
    this.selectActiveItem(this.filteredItems[0]);
  }
}
