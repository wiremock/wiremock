import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CookieService} from '../services/cookie.service';
import {WiremockService} from '../services/wiremock.service';
import {SettingsService} from '../services/settings.service';

@Component({
  selector: 'wm-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {

  isDarkTheme: boolean;
  isTabSlide: boolean;
  areEmptyCodeEntriesHidden: boolean;


  @Output('themeChanged')
  eventEmitter = new EventEmitter();

  constructor(private cookieService: CookieService, private wiremockService: WiremockService, private settingsService: SettingsService) { }

  changeHideEmptyCodeEntries(): void{
    this.areEmptyCodeEntriesHidden = !this.areEmptyCodeEntriesHidden;
    this.settingsService.setEmptyCodeEntriesHidden(this.areEmptyCodeEntriesHidden);
  }

  changeTheme(): void{
    this.isDarkTheme = !this.isDarkTheme;
    this.settingsService.setDarkTheme(this.isDarkTheme);
    this.eventEmitter.emit(this.isDarkTheme);
  }

  ngOnInit() {
    this.isDarkTheme = this.settingsService.isDarkTheme();
    this.isTabSlide = this.settingsService.isTabSlide();
    this.areEmptyCodeEntriesHidden = this.settingsService.areEmptyCodeEntriesHidden();
  }

  changeTabSlide(): void{
    this.isTabSlide = !this.isTabSlide;
    this.settingsService.setTabSlide(this.isTabSlide);
  }

  shutdown(): void{
    this.wiremockService.shutdown().subscribe();
  }

  startRecording(): void{
    this.wiremockService.startRecording().subscribe();
  }

  stopRecording(): void{
    this.wiremockService.stopRecording().subscribe();
  }

}
