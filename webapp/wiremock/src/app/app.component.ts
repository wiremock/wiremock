import {Component, HostBinding} from '@angular/core';

@Component({
  selector: 'wm-root',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.scss' ]
})
export class AppComponent {
  @HostBinding('class') classes = 'wmHolyGrailBody';

  title = 'app';
}
