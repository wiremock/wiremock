import {Component, Input, OnInit} from '@angular/core';
import {ListItem} from '../../model/list-item';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {

  @Input()
  items: ListItem[];

  @Input()
  activeItem: ListItem;

  JSON: JSON;

  constructor() {
    this.JSON = JSON;
  }

  ngOnInit() {
  }

}
