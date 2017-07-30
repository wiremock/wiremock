import {Component, OnInit} from '@angular/core';
import {CookieService} from './services/cookie.service';

@Component({
  selector: 'wm-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  title = 'wm works!';

  isDarkTheme: boolean = false;

  constructor(private cookieService: CookieService) { }


  changeTheme(): void{
   this.isDarkTheme = !this.isDarkTheme;
   this.cookieService.setCookie('darkTheme', String(this.isDarkTheme), 7300);
  }

  ngOnInit(): void {
    const isDarkTheme = this.cookieService.getCookie('darkTheme');
    this.isDarkTheme = (isDarkTheme == 'true');
  }
}
