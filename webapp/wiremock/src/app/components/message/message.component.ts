import {Component, OnInit} from '@angular/core';
import {Message, MessageService, MessageType} from './message.service';
import {UtilService} from '../../services/util.service';

@Component({
  // selector: 'wm-message',
  selector: 'wm-message',
  templateUrl: './message.component.html',
  styleUrls: [ './message.component.scss' ]
})
export class MessageComponent implements OnInit {

  // @HostBinding('class') classes = 'wmAlert';

  message: Message;

  timeout: number;

  constructor(private messageService: MessageService) {
    this.message = null;

    this.messageService.getSubject().subscribe(next => {
      this.message = next;

      if (UtilService.isDefined(next) && UtilService.isDefined(next.duration)) {
        if (this.timeout) {
          clearTimeout(this.timeout);
        }
        this.timeout = setTimeout(() => {
          this.closeAlert();
        }, next.duration);
      }
    });
  }

  ngOnInit() {
  }

  closeAlert() {
    this.message = null;
  }
}
