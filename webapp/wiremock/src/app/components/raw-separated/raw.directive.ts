import {Directive, HostBinding} from '@angular/core';

@Directive({
  // tslint:disable-next-line:directive-selector
  selector: 'wm-raw-separated-raw'
})
export class RawDirective {

  @HostBinding('class') classes = 'wmHolyGrailScroll';

  constructor() {
  }

}
