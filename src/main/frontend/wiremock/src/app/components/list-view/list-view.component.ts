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

  page = 1;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  constructor() {

  }


  ngOnInit() {
  }

  selectActiveItem(item: Item) {
    if (this.activeItem === item) {
      return;
    }
    this.activeItem = item;
    this.activeItemChange.emit(this.activeItem);
  }

  ngOnChanges(changes: SimpleChanges): void {

    let changed = false;

    if (UtilService.isDefined(changes.items) && UtilService.isDefined(this.items)) {
      if (this.items.length > this.pageSize) {
        const maxPages = Math.ceil(this.items.length / this.pageSize);
        if (maxPages < this.page) {
          this.page = maxPages;
        }
      } else {
        this.page = 1;
      }
      // this.setFilteredItems();
      changed = true;
    }
    // http://localhost:4200/mappings?active=e7bc601e-a16b-431f-b69e-d405cbf0f353

    if (UtilService.isDefined(changes.activeItem) && UtilService.isDefined(this.activeItem)) {
      const index = this.items.indexOf(this.activeItem) + 1;
      this.page = Math.ceil(index / this.pageSize);

      changed = true;
    }

    if (changed) {
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
