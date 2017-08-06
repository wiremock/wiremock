import {Component, OnDestroy, OnInit} from '@angular/core';
import {Message, MessageService, MessageType} from './message.service';
import {SettingsService} from '../services/settings.service';
import {MdSnackBar} from '@angular/material';

@Component({
  selector: 'wm-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss']
})
export class MessageComponent implements OnInit, OnDestroy {
  message: Message;
  isDarkTheme: boolean;

  constructor(private messageService: MessageService, private settingsService: SettingsService, private snackBar: MdSnackBar) {
    // this.message = message;
  }

  ngOnInit() {
    this.message = this.messageService.getMessage();
    this.isDarkTheme = this.settingsService.isDarkTheme();
  }

  ngOnDestroy(): void {
  }

  closeMessage(): void{
    this.snackBar.dismiss();
  }

  getTypeClass(type: MessageType):string{
    switch (type){
      case MessageType.INFO:
        return "Info";
      case MessageType.WARN:
        return "Warn";
      case MessageType.ERROR:
        return "Error";
    }
  }

  getCloseColor(type: MessageType): string{
    switch (type){
      case MessageType.INFO:
        return "accent";
      case MessageType.WARN:
        return "primary";
      case MessageType.ERROR:
        return "warn";
    }
  }

}
