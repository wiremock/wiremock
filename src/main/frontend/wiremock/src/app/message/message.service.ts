import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class MessageService {

  private message = new BehaviorSubject<Message>(null);
  message$ = this.message.asObservable();

  constructor() {
  }

  setMessage(message: Message) {
    this.message.next(message);
  }

  getMessage(): Message {
    return this.message.getValue();
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
  INFO,
  WARN,
  ERROR
}


