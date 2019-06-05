import {LoggedRequest} from '../model/wiremock/logged-request';
import {ServeEvent} from '../model/wiremock/serve-event';

export class CurlExtractor {

  public static copyCurl(request: LoggedRequest | ServeEvent): string {
    return CurlExtractor.extractCurl(request).toString();
  }

  public static extractCurl(request: LoggedRequest | ServeEvent): Curl {
    if (request instanceof ServeEvent) {
      return this.extractCurl(request.request);
    }
    const curl: Curl = new Curl();
    curl.httpMethod = request.method;
    curl.url = request.absoluteUrl;
    curl.body = request.body;
    curl.verbose = true;
    curl.headers = this.extractHeaders(request);

    return curl;
  }

  private static extractHeaders(request: LoggedRequest): Header[] {

    const headers = [];
    const keys = Object.keys(request.headers);

    keys.forEach(key => {
      const header = new Header();
      header.key = key;
      header.value = request.headers[key];
      headers.push(header);
    });
    return headers;
  }


  private static checkProperty(property): boolean {
    switch (property) {
      case 'Postman-Token':
      case 'User-Agent':
      case 'Connection':
      case 'Cookie':
      case 'Referer':
      case 'Accept-Encoding':
      case 'Accept-Language':
      case 'Cache-Control':
      case 'Host':
        return false;
    }
    return true;
  }
}

export class Curl {
  private _httpMethod: string;
  private _url: string;
  private _body: string;
  private _verbose: boolean;
  private _headers: Header[];

  get httpMethod(): string {
    return this._httpMethod;
  }

  set httpMethod(value: string) {
    this._httpMethod = value;
  }

  get url(): string {
    return this._url;
  }

  set url(value: string) {
    this._url = value;
  }

  get body(): string {
    return this._body;
  }

  set body(value: string) {
    this._body = value;
  }

  get verbose(): boolean {
    return this._verbose;
  }

  set verbose(value: boolean) {
    this._verbose = value;
  }

  get headers(): Header[] {
    return this._headers;
  }

  set headers(value: Header[]) {
    this._headers = value;
  }

  public toString(): string {
    let curlString = 'curl ';
    curlString += this.httpMethodToString() + ' \\\n';
    curlString += this.urlToString() + ' \\\n';
    curlString += this.bodyToString() + ' \\\n';
    if (this.verbose) {
      curlString += this.verboseToString() + ' \\\n';
    }
    curlString += this.headersToString();

    return curlString;
  }

  private httpMethodToString(): string {
    return '-X ' + this.httpMethod;
  }

  private urlToString(): string {
    return '\'' + this.url + '\'';
  }

  private bodyToString(): string {
    return '-d \'' + this.body + '\'';
  }

  private verboseToString(): string {
    return '-v';
  }

  private headersToString(): string {
    let headerString = '';
    const headers = this.headers;
    headers.forEach((header, i) => {
      headerString += '-H \'' + header.key + ': ' + header.value + '\'';
      if (i !== this.headers.length - 1) {
        headerString += ' \\\n';
      }
    });
    return headerString;
  }
}

export class Header {
  private _key: string;
  private _value: string;


  get key(): string {
    return this._key;
  }

  set key(value: string) {
    this._key = value;
  }

  get value(): string {
    return this._value;
  }

  set value(value: string) {
    this._value = value;
  }
}
