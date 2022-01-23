import {Component, HostBinding, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wm-code-entry',
  templateUrl: './code-entry.component.html',
  styleUrls: [ './code-entry.component.scss' ]
})
export class CodeEntryComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  @Input()
  code: string;

  @Input()
  language: string;

  constructor() {
  }

  ngOnInit() {
  }
}
