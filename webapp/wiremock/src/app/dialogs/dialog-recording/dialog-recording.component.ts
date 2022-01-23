import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'wm-dialog-recording',
  templateUrl: './dialog-recording.component.html',
  styleUrls: [ './dialog-recording.component.scss' ]
})
export class DialogRecordingComponent implements OnInit {

  constructor(public activeModal: NgbActiveModal) {
  }

  ngOnInit() {
  }

}
