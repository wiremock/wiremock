import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {PagerService} from '../services/pager.service';
import {UtilService} from '../services/util.service';
import {Item} from '../wiremock/model/item';

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
  selectedItemId: string;

  pager: any = {};
  pagedItems: Item[];
  currentPage: number;

  constructor(private pagerService: PagerService) {
  }

  ngOnInit() {
    this.setPage(1);
  }

  isSelected(item: Item): boolean{
    return item.getId() === this.selectedItemId;
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
      this.pager = this.pagerService.getPager(this.items.length, page, 200);
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
        if (item.getId() === this.selectedItemId) {
          this.selectEmitter.emit(item);
          found = true;
        }
      }
      if (!found) {
        this.itemSelected(this.pagedItems[0]);
      }
    }
  }

  itemSelected(item: Item): void {
    this.selectedItemId = item.getId();
    this.selectEmitter.emit(item);
  }

}
