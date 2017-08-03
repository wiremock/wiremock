import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {MdCheckboxChange} from '@angular/material';
import {SearchEvent} from '../wiremock/model/search-event';
import {FormControl} from '@angular/forms';

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

  constructor() { }

  ngOnInit() {
    this.search.valueChanges.debounceTime(500).subscribe(next => {
      this.lastSearchText = next;
      this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive))
    });
  }

  onCaseSensitiveChanged(event: MdCheckboxChange){
    this.lastCaseSensitive = event.checked;
    this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
  }
}
