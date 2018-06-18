import {Component, OnInit} from '@angular/core';
import {ListItem} from '../../model/list-item';

@Component({
  selector: 'app-mappings',
  templateUrl: './mappings.component.html',
  styleUrls: ['./mappings.component.scss']
})
export class MappingsComponent implements OnInit {

  items: ListItem[];
  activeItem: ListItem;

  constructor() {
    this.items = [];
    this.items.push(new ListItem('/test/url', 'POST 200', false));
    this.items.push(new ListItem('/test/url2', 'POST 400', false));
    this.items.push(new ListItem('/test/url3', 'GET 500', true));

    this.activeItem = this.items[1];
  }

  ngOnInit() {
  }

}
