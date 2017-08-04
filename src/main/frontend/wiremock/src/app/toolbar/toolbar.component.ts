import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CookieService} from '../services/cookie.service';
import {WiremockService} from '../services/wiremock.service';

@Component({
  selector: 'wm-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit {

  isDarkTheme: boolean;
  isTabSlide: boolean;

  @Output('themeChanged')
  eventEmitter = new EventEmitter();

  constructor(private cookieService: CookieService, private wiremockService: WiremockService) { }

  changeTheme(): void{
    this.isDarkTheme = !this.isDarkTheme;
    this.cookieService.setCookie('darkTheme', String(this.isDarkTheme), 7300);
    this.eventEmitter.emit(this.isDarkTheme);
  }

  ngOnInit() {
    this.isDarkTheme = this.cookieService.getCookie('darkTheme') == 'true';
    this.isTabSlide = this.cookieService.getCookie("tabSlide") == 'true';
  }

  changeTabSlide(): void{
    this.isTabSlide = !this.isTabSlide;
    this.cookieService.setCookie("tabSlide",  String(this.isTabSlide), 7300);
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
