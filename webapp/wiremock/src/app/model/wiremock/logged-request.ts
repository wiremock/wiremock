import {Item} from './item';
import {UtilService} from '../../services/util.service';
import {Proxy} from './proxy';
import * as moment from 'moment';

export class LoggedRequest extends Proxy implements Item {

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
  date: string;

  constructor() {
    super();
  }

  getTitle(): string {
    return this.url;
  }

  getSubtitle(): string {
    let soap;
    if (UtilService.isDefined(this.body) &&
      UtilService.isDefined(soap = UtilService.getSoapMethodRegex().exec(this.body))) {
      return soap[2];
    }
    return 'method=' + this.method;
  }

  getId(): string {
    // value exists in transient layer. This way we skip typescripts type safety.
    return (this as any).id;
  }

  getCode(): string {
    return UtilService.itemModelStringify(this);
  }

  hasGroup(): boolean {
    return false;
  }

  getGroup(): string | undefined {
    return undefined;
  }

  deserialize(unchecked: LoggedRequest): LoggedRequest {
    // We hide a generated id in a "transient" layer
    UtilService.transient(this, 'id', UtilService.generateUUID());
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
    this.date = moment(this.loggedDate).format();

    return this;
  }
}
