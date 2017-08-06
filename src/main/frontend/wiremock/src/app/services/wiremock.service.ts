import { Injectable } from '@angular/core';
import {Http, RequestMethod, RequestOptions, Request, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {environment} from '../../environments/environment';

@Injectable()
export class WiremockService {

  constructor(private http: Http) { }

  resetAll(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'reset');
  }

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

  saveMapping(id: string, mapping: string){
    return this.createRequest(RequestMethod.Put, 'mappings/' + id, mapping);
  }

  saveNewMapping(mapping: string){
    return this.createRequest(RequestMethod.Post, 'mappings', mapping);
  }

  deleteMapping(id: string): Observable<Response>{
    return this.createRequest(RequestMethod.Delete, 'mappings/' + id);
  }

  resetJournal(): Observable<Response>{
    return this.createRequest(RequestMethod.Delete, 'requests');
  }

  resetScenarios(): Observable<Response>{
    return this.createRequest(RequestMethod.Post, 'scenarios/reset');
  }

  getMatched(): Observable<Response>{
    return this.createRequest(RequestMethod.Get, 'requests');
  }

  getUnmatched(): Observable<Response>{
    return this.createRequest(RequestMethod.Get, 'requests/unmatched');
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

  private createRequest(method: RequestMethod, query: string, body?: any): Observable<Response>{
    return this.http.request(new Request(WiremockService.options(method, query, body)));
  }

  private static options(method: RequestMethod, query: string, body: any){
    return new RequestOptions({
      method: method,
      url: environment.url + query,
      body: typeof body === 'string' ? body : JSON.stringify(body)
    });
  }
}
