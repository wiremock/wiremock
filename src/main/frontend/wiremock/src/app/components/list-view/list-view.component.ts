import {Component, Input, OnInit} from '@angular/core';
import {ListItem} from '../../model/list-item';

@Component({
  selector: 'app-list-view',
  templateUrl: './list-view.component.html',
  styleUrls: ['./list-view.component.scss']
})
export class ListViewComponent implements OnInit {

  @Input()
  items: ListItem[];

  @Input()
  activeItem: ListItem;

  constructor() {
  }

  ngOnInit() {
  }

}
