import { Injectable } from '@angular/core';
import {environment} from '../../environments/environment';

@Injectable()
export class SseService {

  private eventSource: any;

  constructor() {
    if(typeof(EventSource) !== 'undefined'){
      this.eventSource = new EventSource(environment.url + "sse");
    }else{
      console.log("SSE not supported by browser.");
      this.eventSource = null;
    }
  }

  register(event, callback: Callback): void{
    if(this.eventSource === null){
      return;
    }else{
      this.eventSource.addEventListener(event, callback);
    }
  }

}

export interface Callback{ (data:any): void}

declare class EventSource{
  onmessage: Callback;
  addEventListener(event: string, callback: Callback): void;
  constructor(name: string);
}
