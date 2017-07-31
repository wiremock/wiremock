import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {MdCheckboxChange} from '@angular/material';
import {SearchEvent} from '../wiremock/model/search-event';

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


  constructor() { }

  ngOnInit() {
  }

  onCaseSensitiveChanged(event: MdCheckboxChange){
    this.lastCaseSensitive = event.checked;
    this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
  }

  onSearchChanged(searchText: string){
    this.lastSearchText = searchText;
    this.onChange.emit(new SearchEvent(this.lastSearchText, this.lastCaseSensitive));
  }
}
