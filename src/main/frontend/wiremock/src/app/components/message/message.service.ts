import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs/internal/BehaviorSubject';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  private message = new BehaviorSubject<Message>(null);

  constructor() {
  }

  getSubject(): BehaviorSubject<Message> {
    return this.message;
  }

  setMessage(message: Message) {
    this.message.next(message);
  }
}

export class Message {
  message: string;
  html: string;
  type: MessageType;
  duration: number;

  constructor(message: string, type: MessageType, duration: number, html?: string) {
    this.message = message;
    this.html = html;
    this.type = type;
    this.duration = duration;
  }
}

export enum MessageType {
  INFO = 'info',
  WARN = 'warning',
  ERROR = 'danger'
}
