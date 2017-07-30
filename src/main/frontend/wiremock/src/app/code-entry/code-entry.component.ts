import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'wm-code-entry',
  templateUrl: './code-entry.component.html',
  styleUrls: ['./code-entry.component.scss']
})
export class CodeEntryComponent implements OnInit {
  @Input('code')
  code: string;

  @Input('language')
  language: string;

  constructor() { }

  ngOnInit() {
  }
}
