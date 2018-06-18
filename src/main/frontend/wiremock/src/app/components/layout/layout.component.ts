import {Component, EventEmitter, HostBinding, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {UtilService} from '../../services/util.service';
import {SearchEvent} from '../../model/wiremock/search-event';
import {FormControl} from '@angular/forms';
import {debounceTime} from 'rxjs/operators';

@Component({
  selector: 'wm-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit, OnChanges {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  @Input()
  items: Item[];

  filteredItems: Item[];

  @Input()
  activeItem: Item;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  search = new FormControl();

  caseSensitiveSearchEnabled = false;

  searchClearVisible = false;

  constructor() {
  }

  ngOnInit() {
    this.search.valueChanges.pipe(debounceTime(100)).subscribe(next => {
      // this.lastSearchText = next;
      this.onSearchChanged(new SearchEvent(next, this.caseSensitiveSearchEnabled));
    });

    this.search.valueChanges.pipe().subscribe(next => {
      // this.lastSearchText = next;
      this.searchClearVisible = Boolean(next);
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    this.onSearchChanged(null);
  }

  onSearchChanged(search: SearchEvent) {
    // this.lastSearch = search;
    if (UtilService.isDefined(search)) {
      this.filteredItems = UtilService.deepSearch(this.items, search.text, search.caseSensitive);
    } else {
      this.filteredItems = UtilService.deepSearch(this.items, '', false);
    }

    // We deselect an item when there are no results.
    if (UtilService.isUndefined(this.filteredItems) || this.filteredItems.length === 0) {
      // this.onItemSelected(null);
    }
  }

  onActiveItemChange(item: Item) {
    this.activeItem = item;
    this.activeItemChange.emit(this.activeItem);
  }

  onCaseSensitiveChanged() {
    this.caseSensitiveSearchEnabled = !this.caseSensitiveSearchEnabled;
    this.onSearchChanged(new SearchEvent(this.search.value, this.caseSensitiveSearchEnabled));
  }

  clearSearch() {
    this.search.setValue('');
  }
}
