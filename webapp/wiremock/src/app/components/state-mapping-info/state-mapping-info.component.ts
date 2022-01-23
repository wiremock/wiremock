import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {StubMapping} from '../../model/wiremock/stub-mapping';

@Component({
  selector: 'wm-state-mapping-info',
  templateUrl: './state-mapping-info.component.html',
  styleUrls: [ './state-mapping-info.component.scss' ]
})
export class StateMappingInfoComponent implements OnInit {

  @Input()
  mapping: StubMapping;

  constructor(public activeModal: NgbActiveModal) {
  }

  ngOnInit() {
  }

}
