import { Component, OnInit } from '@angular/core';
import {CookieService} from '../services/cookie.service';
import {SettingsService} from '../services/settings.service';

@Component({
  selector: 'wm-tab-view',
  templateUrl: './tab-view.component.html',
  styleUrls: ['./tab-view.component.scss']
})
export class TabViewComponent implements OnInit {

  constructor(private settingsService: SettingsService) { }

  ngOnInit() {
  }

  tabSlide(): boolean{
    return this.settingsService.isTabSlide();
  }

}
