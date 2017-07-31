import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {PagerService} from '../services/pager.service';
import {UtilService} from '../services/util.service';

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

  pager: any = {};
  pagedItems: Item[];
  currentPage: number;

  constructor(private pagerService: PagerService) {
  }

  ngOnInit() {
    this.setPage(1);
  }

  setPage(page: number): void {
    if (page < 1) {
      page = 1;
    }

    if (page > this.pager.totalPages) {
      page = this.pager.totalPages;
    }

    if (UtilService.isUndefined(this.items)) {
      this.pagedItems = [];
      this.currentPage = 1;
    } else {
      this.currentPage = page;
      this.pager = this.pagerService.getPager(this.items.length, page, 100);
      this.pagedItems = this.items.slice(this.pager.startIndex, this.pager.endIndex + 1);

      this.selectPrev();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.setPage(this.currentPage);
    this.selectPrev();
  }

  private selectPrev(): void {
    if (UtilService.isDefined(this.pagedItems) && this.pagedItems.length > 0) {
      let found = false;
      for (const item of this.pagedItems) {
        if (item.selected) {
          this.previousSelectedItem = item;
          found = true;
        }
      }
      if (!found) {
        this.itemSelected(this.pagedItems[0]);
      }
    }
  }

  itemSelected(item: Item): void {
    if (UtilService.isDefined(this.previousSelectedItem)) {
      this.previousSelectedItem.setSelected(false);
    }
    item.setSelected(true);

    this.selectEmitter.emit(item);
    this.previousSelectedItem = item;
  }

}

export abstract class Item {

  selected: boolean | false;

  abstract getTitle(): string;

  abstract getSubtitle(): string;

  setSelected(value: boolean) {
    this.selected = value;
  }
}
