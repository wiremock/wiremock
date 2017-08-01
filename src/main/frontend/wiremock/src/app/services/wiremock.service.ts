import { Injectable } from '@angular/core';
import {Http, RequestMethod, RequestOptions, Request, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {environment} from '../../environments/environment';

@Injectable()
export class WiremockService {

  constructor(private http: Http) { }

  getMappings(): Observable<Response>{
    return this.createRequest(RequestMethod.Get, 'mappings');
  }

  saveMappings(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'mappings/save');
  }

  resetMappings(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'mappings/reset');
  }

  deleteAllMappings(): Observable<Response>{
    return this.createRequest(RequestMethod.Delete, 'mappings');
  }

  deleteMapping(id: string): Observable<Response>{
    return this.createRequest(RequestMethod.Delete, 'mappings/' + id);
  }

  startRecording(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'recordings/start');
  }

  stopRecording(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'recordings/stop');
  }

  getRecordingStatus(): Observable<Response>{
    return this.createRequest(RequestMethod.Get, 'recordings/status');
  }

  shutdown(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'shutdown');
  }



  private createRequest(method: RequestMethod, query: string): Observable<Response>{
    return this.http.request(new Request(WiremockService.options(method, query)));
  }

  private static options(method: RequestMethod, query: string){
    return new RequestOptions({
      method: method,
      url: environment.url + query
    });
  }
}
