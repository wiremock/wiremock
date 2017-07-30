import {Component, OnInit} from '@angular/core';
import {CookieService} from './services/cookie.service';

@Component({
  selector: 'wm-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{

  isDarkTheme: boolean = false;

  constructor(private cookieService: CookieService) { }

  ngOnInit(): void {
    const isDarkTheme = this.cookieService.getCookie('darkTheme');
    this.isDarkTheme = (isDarkTheme == 'true');
  }

  changeTheme(isDarkTheme: boolean): void{
    this.isDarkTheme = isDarkTheme;
  }
}
