import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';

@Component({
  selector: 'wm-code-entry',
  templateUrl: './code-entry.component.html',
  styleUrls: ['./code-entry.component.scss']
})
export class CodeEntryComponent implements OnInit, OnChanges {
  ngOnChanges(changes: SimpleChanges): void {
    console.log("change");
  }
  @Input('code')
  code: any;

  @Input('language')
  language: string;

  @Input('type')
  type: string;

  constructor() { }

  ngOnInit() {
  }
}
