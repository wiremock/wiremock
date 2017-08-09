import { Component, OnInit } from '@angular/core';
import {MdDialogRef} from '@angular/material';

@Component({
  selector: 'wm-dialog-recording',
  templateUrl: './dialog-recording.component.html',
  styleUrls: ['./dialog-recording.component.scss']
})
export class DialogRecordingComponent implements OnInit {

  constructor(public dialogRef: MdDialogRef<DialogRecordingComponent>) { }

  ngOnInit() {
  }

}
