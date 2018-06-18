import {Directive, HostBinding} from '@angular/core';

@Directive({
  selector: 'wm-raw-separated-separated'
})
export class SeparatedDirective {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  constructor() {
  }

}
