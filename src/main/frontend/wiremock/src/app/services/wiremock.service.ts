import { Injectable } from '@angular/core';
import {Http, RequestMethod, RequestOptions, Request, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class WiremockService {

  constructor(private http: Http) { }

  getMappings(): Observable<Response>{
    const options = new RequestOptions({
      method: RequestMethod.Get,
      url: '/__admin/mappings'
    });

    return this.http.request(new Request(options));
  }
}
