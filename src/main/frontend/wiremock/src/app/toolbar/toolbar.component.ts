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

  @Output('themeChanged')
  eventEmitter = new EventEmitter();

  constructor(private cookieService: CookieService, private wiremockService: WiremockService) { }

  changeTheme(): void{
    this.isDarkTheme = !this.isDarkTheme;
    this.cookieService.setCookie('darkTheme', String(this.isDarkTheme), 7300);
    this.eventEmitter.emit(this.isDarkTheme);
  }

  ngOnInit() {
    const isDarkTheme = this.cookieService.getCookie('darkTheme');
    this.isDarkTheme = (isDarkTheme == 'true');
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
