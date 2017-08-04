
export class RequestPattern {

  url: string;
  urlPattern: string;
  urlPath: string;
  urlPathPattern: string;
  method: string;
  headers: any;
  queryParameters: any;
  cookies: any;
  basicAuth: any;
  bodyPatterns: any;
  customMatcher: any;

  deserialize(unchecked: RequestPattern): RequestPattern{
    return unchecked;
    // this.url = unchecked.url;
    // this.urlPattern = unchecked.urlPattern;
    // this.urlPath = unchecked.urlPath;
    // this.urlPathPattern = unchecked.urlPathPattern;
    // this.method = unchecked.method;
    // this.queryParameters = unchecked.queryParameters;
    // this.cookies = unchecked.cookies;
    // this.basicAuth = unchecked.basicAuth;
    // this.bodyPatterns = unchecked.bodyPatterns;
    // this.customMatcher = unchecked.customMatcher;
    //
    // return this;
  }
}
