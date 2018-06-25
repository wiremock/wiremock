import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {UtilService} from './util.service';
import {Observable} from 'rxjs/internal/Observable';
import {Subject} from 'rxjs/internal/Subject';
import {filter} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private socket: ReconnectWebSocket;

  constructor() {
    this.socket = new ReconnectWebSocket();
    this.socket.socketReconnect();
  }


  public observe(key: string): Observable<MessageEvent> {
    return this.socket.subject.pipe(filter(next => {
      return next.data === key;
    }));
  }

  // This was the initial approach. For some reason it did not work with multiple subscribers. Therefore, replaced with simple solution
  // ReconnectWebSocket actually still provides the method which could be overwritten.

  // private createSubject(): Subject<any> {
  //   try {
  //     const observable = Observable.create((messageEventObserver: Observer<MessageEvent>) => {
  //       this.socket.onmessage = messageEventObserver.next.bind(messageEventObserver);
  //
  //       // Although it is cool we do not want to close the subscriptions. We try to reconnect to the webSocket.
  //
  //       // this.socket.onerror = messageEventObserver.error.bind(messageEventObserver);
  //       // this.socket.onclose = messageEventObserver.complete.bind(messageEventObserver);
  //       // return socket.close.bind(socket); // return defines what happens when subscriber unsubscribe.
  //     });
  //
  //     const observer = {
  //       next: (data: any) => {
  //         if (this.socket.readyState() === WebSocket.OPEN) {
  //           if (typeof data === 'object') {
  //             this.socket.send(UtilService.toJson(data));
  //           } else {
  //             this.socket.send(data);
  //           }
  //         }
  //       }
  //     };
  //     return Subject.create(observer, observable);
  //   } catch (err) {
  //     console.log(err);
  //   }
  //   // return new Subject<any>();
  // }
}


let reconnectWebSocket: ReconnectWebSocket;

export class ReconnectWebSocket {
  subject: Subject<MessageEvent>;

  private webSocket: WebSocket;


  constructor() {
    // We need the var because of overwritten method this would be the actual WebSocket
    reconnectWebSocket = this;

    this.subject = new Subject<MessageEvent>();
  }

  socketReconnect() {
    this.webSocket = environment.getWebSocket();

    this.webSocket.onmessage = this.privateOnmessage;
    this.webSocket.onerror = this.privateOnerror;
    this.webSocket.onclose = this.privateOnclose;
  }

  private privateOnmessage(ev: MessageEvent): void {
    if (UtilService.isDefined(reconnectWebSocket.onmessage)) {
      reconnectWebSocket.onmessage(ev);
    }
  }

  private privateOnerror(ev: Event): void {
    if (UtilService.isDefined(reconnectWebSocket.onerror)) {
      reconnectWebSocket.onerror(ev);
    }
  }

  private privateOnclose(ev: CloseEvent): void {
    if (UtilService.isDefined(reconnectWebSocket.onclose)) {
      reconnectWebSocket.onclose(ev);
    }

    setTimeout(() => reconnectWebSocket.socketReconnect(), 5000);
  }

  onmessage(ev: MessageEvent): void {
    reconnectWebSocket.subject.next(ev);
  }

  onerror(ev: Event): void {
  }

  onclose(ev: CloseEvent): void {
  }

  readyState(): number {
    return this.webSocket.readyState;
  }

  send(data: any) {
    this.webSocket.send(data);
  }
}
