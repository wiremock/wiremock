import {
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  HostBinding,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef
} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {UtilService} from '../../services/util.service';
import {SearchEvent} from '../../model/wiremock/search-event';
import {FormControl} from '@angular/forms';
import {debounceTime, takeUntil} from 'rxjs/operators';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {SearchService} from '../../services/search.service';
import {Subject} from 'rxjs/internal/Subject';

@Component({
  selector: 'wm-layout',
  templateUrl: './layout.component.html',
  styleUrls: [ './layout.component.scss' ]
})
export class LayoutComponent implements OnInit, OnChanges, OnDestroy {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  private ngUnsubscribe: Subject<any> = new Subject();

  @ContentChild('content') content: TemplateRef<ElementRef>;
  @ContentChild('actions') actions: TemplateRef<ElementRef>;

  @Input()
  items: Item[];

  filteredItems: Item[];

  activeItem: Item;

  @Input()
  activeItemId: string;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  search = new FormControl();

  caseSensitiveSearchEnabled = false;
  searchClearVisible = false;

  lastSearch: string;

  constructor(private searchService: SearchService,
              private activatedRoute: ActivatedRoute, private router: Router) {
  }

  ngOnInit() {
    this.search.valueChanges.pipe(takeUntil(this.ngUnsubscribe), debounceTime(200)).subscribe(next => {
      this.lastSearch = next;
      this.onSearchChanged(new SearchEvent(next, this.caseSensitiveSearchEnabled));
    });

    this.search.valueChanges.pipe(takeUntil(this.ngUnsubscribe)).subscribe(next => {
      this.searchClearVisible = Boolean(next);
    });

    this.activatedRoute.queryParams.pipe(takeUntil(this.ngUnsubscribe)).subscribe((params: Params) => {
      this.activeItemId = params['active'];
      this.setActiveItemById(this.activeItemId, UtilService.isDefined(this.activeItemId));
    });

    this.searchService.search$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(newSearch => {
      if (UtilService.isDefined(newSearch)) {
        this.lastSearch = newSearch;
        this.search.setValue(newSearch);
      }
    });
  }

  private setActiveItemById(itemId: string, noClear = false) {
    if (UtilService.isDefined(this.filteredItems)) {
      this.setActiveItem(UtilService.getActiveItem(this.filteredItems, itemId));
    } else if (!noClear) {
      this.setActiveItem(null);
    }
  }

  private setActiveItem(item: Item) {
    this.activeItem = item;
    if (UtilService.isDefined(this.activeItem)) {
      this.activeItemId = this.activeItem.getId();
      const currentUrl = this.router.url;
      const newPath = this.router.url.split('?')[0];

      if (currentUrl.indexOf(newPath) === -1 || (currentUrl.indexOf(newPath) > -1 && currentUrl.indexOf(this.activeItemId) === -1)) {
        this.router.navigate([ this.router.url.split('?')[0] ], {queryParams: {active: this.activeItemId}});
      }
    } else {
      this.activeItemId = null;
      const currentUrl = this.router.url;
      const newUrl = this.router.url.split('?')[0];
      if (currentUrl !== newUrl) {
        this.router.navigate([ newUrl ]);
      }
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (UtilService.isUndefined(changes)) {
      return;
    }
    if (UtilService.isDefined(changes.items) && (UtilService.isDefined(changes.items.currentValue)
      || UtilService.isDefined(changes.items.previousValue))) {
      // We only update filteredItems when actual items changed. activeItemId can be set but it is only a suggestion. This component
      // is responsible for selecting items
      this.onSearchChanged(new SearchEvent(this.lastSearch, this.caseSensitiveSearchEnabled));
    }
  }

  onSearchChanged(search: SearchEvent) {
    // this.lastSearch = search;
    if (UtilService.isDefined(search) && UtilService.isDefined(search.text)) {
      this.filteredItems = UtilService.deepSearch(this.items, search.text, search.caseSensitive);
    } else {
      this.filteredItems = UtilService.deepSearch(this.items, '', false);
    }

    if (UtilService.isDefined(this.filteredItems) && this.filteredItems.length > 0) {

      const foundIndex = this.filteredItems.findIndex((item: Item, index: number) => {
        return UtilService.isDefined(this.activeItemId) && item.getId() === this.activeItemId;
      });

      if (foundIndex > -1) {
        // We need to set it although it is found because the actual object may be replaced
        this.activeItem = this.filteredItems[foundIndex];
      } else {
        this.onActiveItemChange(this.filteredItems[0]);
      }
    } else {
      this.onActiveItemChange(null);
    }
  }

  onActiveItemChange(item: Item) {
    this.setActiveItem(item);
    this.activeItemChange.emit(this.activeItem);
  }

  onCaseSensitiveChanged() {
    this.caseSensitiveSearchEnabled = !this.caseSensitiveSearchEnabled;
    this.onSearchChanged(new SearchEvent(this.search.value, this.caseSensitiveSearchEnabled));
  }

  clearSearch() {
    this.search.setValue('');
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }
}
