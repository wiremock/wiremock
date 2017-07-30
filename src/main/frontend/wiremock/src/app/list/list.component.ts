import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wm-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  @Input('items')
  items: Item[];

  constructor() { }

  ngOnInit() {
  }

}

export interface Item{
  getTitle(): string;
  getSubtitle(): string;
}
