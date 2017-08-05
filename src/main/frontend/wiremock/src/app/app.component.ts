import {Component, OnInit} from '@angular/core';
import {CookieService} from './services/cookie.service';
import {SettingsService} from './services/settings.service';

@Component({
  selector: 'wm-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{

  isDarkTheme: boolean = false;

  constructor(private settingsService: SettingsService) { }

  ngOnInit(): void {
    this.isDarkTheme = this.settingsService.isDarkTheme();
  }

  changeTheme(isDarkTheme: boolean): void{
    this.isDarkTheme = isDarkTheme;
  }
}
