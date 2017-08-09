import {Component, EventEmitter, OnInit, Output, ElementRef} from '@angular/core';
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
  onChange = new EventEmitter();

  lastSearchText: string;
  lastCaseSensitive: boolean;

  search = new FormControl();

  constructor(private elementRef: ElementRef, private searchService: SearchService) { }

  ngOnInit() {
    this.search.valueChanges.debounceTime(100).subscribe(next => {
      this.lastSearchText = next;
      this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
      setTimeout(() =>{
        //This is a workaround for trigger a change. We just need this for the search
        this.elementRef.nativeElement.click();
      });
    });

    this.searchService.searchValue$.subscribe(value =>{
      this.setSearchValue(value);
    });

  }

  onCaseSensitiveChanged(event: MdCheckboxChange){
    this.lastCaseSensitive = event.checked;
    this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
  }

  setSearchValue(value: string){
    this.search.setValue(value);
  }
}
