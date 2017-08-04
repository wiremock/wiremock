export class LoggedRequest {

  url: string;
  absoluteUrl: string;
  clientIp: string;
  method: string;
  headers: any;
  cookies: any;
  queryParams: any;
  body: string;
  isBrowserProxyRequest: boolean;
  loggedDate: any;

  deserialize(unchecked: LoggedRequest): LoggedRequest{
    return unchecked;
  }
}
