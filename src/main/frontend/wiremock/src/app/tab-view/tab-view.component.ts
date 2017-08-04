import { Component, OnInit } from '@angular/core';
import {CookieService} from '../services/cookie.service';

@Component({
  selector: 'wm-tab-view',
  templateUrl: './tab-view.component.html',
  styleUrls: ['./tab-view.component.scss']
})
export class TabViewComponent implements OnInit {

  constructor(private cookieService: CookieService) { }

  ngOnInit() {
  }

  tabSlideDisabled(): boolean{
    return this.cookieService.getCookie("tabSlide") !== 'true';
  }

}
