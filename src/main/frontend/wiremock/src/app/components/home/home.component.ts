import {Component, HostBinding, OnInit} from '@angular/core';

@Component({
  selector: 'wm-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  isCollapsed = false;

  constructor() {
  }

  ngOnInit() {
  }

}
