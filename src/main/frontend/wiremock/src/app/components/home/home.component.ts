import {Component, HostBinding, OnInit} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'wm-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  isCollapsed = false;

  constructor(private router: Router) {
  }

  ngOnInit() {
  }

  isActive(url: string): boolean {
    return this.router.isActive(url, false);
  }

}
