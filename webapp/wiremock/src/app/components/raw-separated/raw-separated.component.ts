import {Component, HostBinding, Input, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'wm-raw-separated',
  templateUrl: './raw-separated.component.html',
  styleUrls: ['./raw-separated.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class RawSeparatedComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  @Input()
  separatedDisabled = false;

  @Input()
  rawDisabled = false;

  constructor() {
  }

  ngOnInit() {
  }

}
