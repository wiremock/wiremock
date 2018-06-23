import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {UtilService} from './util.service';
import {Observable} from 'rxjs/internal/Observable';
import {Observer} from 'rxjs/internal/types';
import {Subject} from 'rxjs/internal/Subject';
import {catchError, filter} from 'rxjs/operators';
import {throwError} from 'rxjs/internal/observable/throwError';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private socket: Subject<Object>;

  constructor() {
    this.socket = this.createWebSocket();
  }

  public getSocket(): Subject<any> {
    return this.socket;
  }

  public observe(key: string): Observable<any> {
    return this.getSocket().pipe(catchError(this.handleError), filter(next => next.data === key));
  }


  private handleError(error: any) {
    console.error('An error occurred:', error);
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  }

  private createWebSocket(): Subject<any> {
    const socket = environment.getWebSocket();

    const observable = Observable.create((messageEventObserver: Observer<MessageEvent>) => {
      socket.onmessage = messageEventObserver.next.bind(messageEventObserver);
      socket.onerror = messageEventObserver.error.bind(messageEventObserver);
      socket.onclose = messageEventObserver.complete.bind(messageEventObserver);
      // return socket.close.bind(socket); // return defines what happens when subscriber unsubscribe.
    });

    const observer = {
      next: (data: any) => {
        if (socket.readyState === WebSocket.OPEN) {
          if (typeof data === 'object') {
            socket.send(UtilService.toJson(data));
          } else {
            socket.send(data);
          }
        }
      }
    };
    return Subject.create(observer, observable);
  }

}
