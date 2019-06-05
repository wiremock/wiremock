import {LoggedRequest} from '../model/wiremock/logged-request';
import {ServeEvent} from '../model/wiremock/serve-event';

export class CurlExtractor {

  static copyCurl(request: LoggedRequest | ServeEvent): string {
    if (request instanceof LoggedRequest) {
      return CurlExtractor.extractCurl(request);
    } else {
      return CurlExtractor.extractCurl(request.request);
    }
  }

  private static extractCurl(request: LoggedRequest): string {
    let curlString = 'curl ';
    curlString += CurlExtractor.extractHttpMethod(request) + ' ';
    curlString += CurlExtractor.extractURL(request) + ' ';
    curlString += this.extractBody(request) + ' ';
    curlString += this.addVerbose() + ' ';
    curlString += this.extractHeaders(request) + ' ';

    return curlString;
  }

  private static extractHttpMethod(request: LoggedRequest): string {
    return '-X ' + request.method;
  }

  private static extractURL(request: LoggedRequest): string {
    return '\'' + request.absoluteUrl + '\'';
  }

  private static extractBody(request: LoggedRequest): string {
    return '-d \'' + request.body.replace(/\n/g, '') + '\'';
  }

  private static addVerbose(): string {
    return '-v';
  }

  private static extractHeaders(request: LoggedRequest): string {
    let headerString = '';
    const headers = request.headers;
    for (const property in headers) {
      if (headers.hasOwnProperty(property) && this.checkProperty(property)) {
        const value = headers[property];
        headerString += '-H "' + property + ': ' + value + '" ';
      }
    }
    return headerString;
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
