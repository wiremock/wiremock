import {Item} from './item';
import {UtilService} from '../../services/util.service';

export class LoggedRequest implements Item{

  constructor(){
  }

  getTitle(): string {
    return this.url;
  }

  getSubtitle(): string {
    let soap;
    if(UtilService.isDefined(this.body) &&
      UtilService.isDefined(soap = UtilService.getSoapMethodRegex().exec(this.body))){
      return soap[2];
    }
    return "method=" + this.method;
  }

  getId(): string {
    return this.id;
  }

  id: string;

  url: string;
  absoluteUrl: string;
  clientIp: string;
  method: string;
  headers: any;
  cookies: any;
  queryParams: any;
  body: string;
  bodyAsBase64: string;
  isBrowserProxyRequest: boolean;
  loggedDate: any;

  deserialize(unchecked: LoggedRequest): LoggedRequest{
    this.id = UtilService.generateUUID();
    this.url = unchecked.url;
    this.absoluteUrl = unchecked.absoluteUrl;
    this.clientIp = unchecked.clientIp;
    this.method = unchecked.method;
    this.headers = unchecked.headers;
    this.cookies = unchecked.cookies;
    this.queryParams = unchecked.queryParams;
    this.body = unchecked.body;
    this.bodyAsBase64 = unchecked.bodyAsBase64;
    this.isBrowserProxyRequest = unchecked.isBrowserProxyRequest;
    this.loggedDate = unchecked.loggedDate;


    return this;
  }
}
