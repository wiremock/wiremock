import {Component, OnInit} from '@angular/core';
import {Message, MessageService} from './message.service';
import {UtilService} from '../../services/util.service';

@Component({
  // selector: 'wm-message',
  selector: 'wm-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss']
})
export class MessageComponent implements OnInit {

  // @HostBinding('class') classes = 'wmAlert';

  message: Message;

  constructor(private messageService: MessageService) {
    this.message = null;

    this.messageService.getSubject().subscribe(next => {
      this.message = next;

      if (UtilService.isDefined(next) && UtilService.isDefined(next.duration)) {
        setTimeout(() => {
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
