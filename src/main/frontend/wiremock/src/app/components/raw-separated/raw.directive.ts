import {Directive, HostBinding} from '@angular/core';

@Directive({
  selector: 'wm-raw-separated-raw'
})
export class RawDirective {

  @HostBinding('class') classes = 'wmHolyGrailScroll';

  constructor() {
  }

}
