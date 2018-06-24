import {Component, HostBinding, OnInit} from '@angular/core';
import {WiremockService} from '../../services/wiremock.service';
import {WebSocketService} from '../../services/web-socket.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {UtilService} from '../../services/util.service';
import {GetServeEventsResult} from '../../model/wiremock/get-serve-events-result';
import {ServeEvent} from '../../model/wiremock/serve-event';

@Component({
  selector: 'wm-matched',
  templateUrl: './matched.component.html',
  styleUrls: ['./matched.component.scss']
})
export class MatchedComponent implements OnInit {

  @HostBinding('class') classes = 'wmHolyGrailBody';

  serveEventResult: GetServeEventsResult;

  constructor(private wiremockService: WiremockService, private webSocketService: WebSocketService,
              private messageService: MessageService) {
  }

  ngOnInit() {
    this.loadMappings();
  }

  private loadMappings() {
    this.wiremockService.getRequests().subscribe(data => {
        this.serveEventResult = new GetServeEventsResult().deserialize(data, true);
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

  copyCurl(request: ServeEvent) {
    if (UtilService.copyToClipboard(UtilService.copyCurl(request))) {
      this.messageService.setMessage(new Message('Curl copied to clipboard', MessageType.INFO, 3000));
    } else {
      this.messageService.setMessage(new Message('Was not able to copy. Details in log', MessageType.ERROR, 10000));
    }
  }

  resetJournal() {
    this.wiremockService.resetJournal().subscribe(() => {
      // do nothing
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }

}
