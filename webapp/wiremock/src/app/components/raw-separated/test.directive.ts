import {Directive, HostBinding} from '@angular/core';

@Directive({
  // tslint:disable-next-line:directive-selector
  selector: 'wm-raw-separated-test'
})
export class TestDirective {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  constructor() {
  }

}
