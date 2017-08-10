import {Component, ElementRef, EventEmitter, OnInit, Output} from '@angular/core';
import {MdCheckboxChange} from '@angular/material';
import {SearchEvent} from '../wiremock/model/search-event';
import {FormControl} from '@angular/forms';
import {SearchService} from '../services/search.service';

@Component({
  selector: 'wm-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  @Output('onSearchChanged')
  onSearchChanged = new EventEmitter();

  lastSearchText: string;
  lastCaseSensitive: boolean;

  search = new FormControl();

  // Normally it should worked with local var and focused property of material.
  // For some reason it did not. Therefore, via blur and focus and this var.
  focused = false;

  constructor(private elementRef: ElementRef, private searchService: SearchService) {
  }

  ngOnInit() {
    this.search.valueChanges.debounceTime(100).subscribe(next => {
      this.lastSearchText = next;
      this.onSearchChanged.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
      setTimeout(() => {
        // This is a workaround for trigger a change. We just need this for the search
        this.elementRef.nativeElement.click();
      });
    });

    this.searchService.searchValue$.subscribe(value => {
      this.setSearchValue(value);
    });
  }

  onCaseSensitiveChanged(event: MdCheckboxChange) {
    this.lastCaseSensitive = event.checked;
    this.onSearchChanged.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
  }

  clearSearch(event: MouseEvent): void {
    this.setSearchValue('');
    event.stopPropagation();
  }

  setSearchValue(value: string) {
    this.search.setValue(value);
  }

  onBlur(): void {
    this.focused = false;
  }

  onFocus(): void {
    this.focused = true;
  }
}
