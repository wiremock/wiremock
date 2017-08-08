import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {PagerService} from '../services/pager.service';
import {UtilService} from '../services/util.service';
import {Item} from '../wiremock/model/item';
import {MdSelectChange} from '@angular/material';
import {SettingsService} from '../services/settings.service';

@Component({
  selector: 'wm-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit, OnChanges {
  @Input('items')
  items: Item[];
  itemsWereNull: boolean;

  @Input('selectByItemId')
  selectByItemId: string;

  @Output('onSelect')
  selectEmitter = new EventEmitter();

  selectedItem: Item;
  selectedItemId: string;
  selectedItemPageIndex: number;

  pager: any = {};
  pagedItems: Item[];
  currentPage: number;

  pagerMaxItemsPerPage: number;
  pagerMaxItemsPerPageOptions:number[] = [10,20,40,60,80,100,150,200];

  constructor(private pagerService: PagerService, private settingsService: SettingsService) {
  }

  ngOnInit() {
    this.pagerMaxItemsPerPage = this.settingsService.getPagerMaxItemsPerPage();
    this.setPage(-1);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(UtilService.isDefined(changes.selectByItemId)){
      this.selectedItemId = changes.selectByItemId.currentValue;
    }else{
      this.setPage(-1);
      // this.setPage(this.currentPage);
    }
    // this.selectPrev();
  }

  setPagerMaxItemsPerPage(value: MdSelectChange){
    this.pagerMaxItemsPerPage = value.value;
    this.settingsService.setPagerMaxItemsPerPage(this.pagerMaxItemsPerPage);
    this.setPage(this.currentPage);
  }

  isSelected(item: Item): boolean{
    return item.getId() === this.selectedItemId;
  }

  setPage(page: number): void {
    if (UtilService.isUndefined(this.items)) {
      //We have no items. This is default config
      this.pagedItems = [];
      this.currentPage = 1;
      return;
    }

    let item: Item;
    let foundIndex = -1;

    for(let index = 0; index < this.items.length; index++){
      item = this.items[index];

      if (item.getId() === this.selectedItemId) {
        this.itemSelected(item, index);
        foundIndex = index;
        break;
      }
    }

    this.itemsWereNull = UtilService.isUndefined(this.pagedItems) || this.pagedItems.length == 0;





    if(foundIndex > -1 && page == -1){
      //We found the item which was selected earlier.
      page = Math.floor(foundIndex / this.pagerMaxItemsPerPage) + 1;

    }else if(page > -1){
      //We cound not find the item which was selected earlier but we have a page which is specified
    }else{
      //We have no selected item and no page. We stay on the current page
      page = this.currentPage;
    }

    //We have some items. Initialize the pager and set the pagedItems
    this.pager = this.pagerService.getPager(this.items.length, page, this.pagerMaxItemsPerPage, 8);
    this.pagedItems = this.items.slice(this.pager.startIndex, this.pager.endIndex + 1);
    this.currentPage = this.pager.currentPage;

    this.selectPrev();
  }

  private selectPrev(): void {
    if (UtilService.isDefined(this.pagedItems) && this.pagedItems.length > 0) {
      let found = false;
      //Search for item to select
      let item: Item;
      for (let index = 0; index < this.pagedItems.length; index++) {
        item = this.pagedItems[index];
        if (item.getId() === this.selectedItemId) {
          this.itemSelected(item, index);
          found = true;
        }
      }
      if (!found) {
        if(this.selectedItemPageIndex > 0 && this.selectedItemPageIndex < this.pagedItems.length){
          this.itemSelected(this.pagedItems[this.selectedItemPageIndex], this.selectedItemPageIndex);
        }else{
          this.itemSelected(this.pagedItems[0], 0);
        }
      }
    }
  }

  // setPage(page: number): void {
  //   this.itemsWereNull = UtilService.isUndefined(this.pagedItems) || this.pagedItems.length == 0;
  //   if (UtilService.isUndefined(this.items)) {
  //     //We have no items. This is default config
  //     this.pagedItems = [];
  //     this.currentPage = 1;
  //   } else {
  //     //We have some items. Initialize the pager and set the pagedItems
  //     this.pager = this.pagerService.getPager(this.items.length, page, this.pagerMaxItemsPerPage, 8);
  //     this.pagedItems = this.items.slice(this.pager.startIndex, this.pager.endIndex + 1);
  //     this.currentPage = this.pager.currentPage;
  //   }
  //   this.selectPrev();
  // }
  //
  // private selectPrev(): void {
  //   if (UtilService.isDefined(this.pagedItems) && this.pagedItems.length > 0) {
  //     let found = false;
  //     //Search for item to select
  //     let item: Item;
  //     for (let index = 0; index < this.pagedItems.length; index++) {
  //       item = this.pagedItems[index];
  //       if (item.getId() === this.selectedItemId) {
  //         this.itemSelected(item, index);
  //         found = true;
  //       }
  //     }
  //     if (!found) {
  //       if(this.selectedItemPageIndex > 0 && this.selectedItemPageIndex < this.pagedItems.length){
  //         this.itemSelected(this.pagedItems[this.selectedItemPageIndex], this.selectedItemPageIndex);
  //       }else{
  //         this.itemSelected(this.pagedItems[0], 0);
  //       }
  //     }
  //   }
  // }

  itemSelected(item: Item, index: number): void {
    // if(this.selectedItemId == item.getId() && ! this.itemsWereNull){
    //   this.itemsWereNull = UtilService.isUndefined(this.pagedItems) || this.pagedItems.length == 0;
    //   return;
    // }
    if(this.selectedItem == item && ! this.itemsWereNull){
      this.itemsWereNull = UtilService.isUndefined(this.pagedItems) || this.pagedItems.length == 0;
      return;
    }
    this.itemsWereNull = UtilService.isUndefined(this.pagedItems) || this.pagedItems.length == 0;
    this.selectedItem = item;
    this.selectedItemId = item.getId();
    this.selectedItemPageIndex = index;
    this.selectEmitter.emit(item);
  }

}
