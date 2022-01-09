export class ResponseDefinition {
  status: number;
  statusMessage: string;
  body: string;
  jsonBody: string;
  base64Body: string;
  bodyFileName: string;
  headers: any;
  additionalProxyRequestHeaders: any;
  fixedDelayMilliseconds: number;
  delayDistribution: any;
  chunkedDribbleDelay: any;
  proxyBaseUrl: string;
  proxyUrlPrefixToRemove: string;
  fault: any;
  transformers: any;
  transformerParameters: any;
  fromConfiguredStub: Boolean;

  deserialize(unchecked: ResponseDefinition): ResponseDefinition {
    return unchecked;
    // this.status = unchecked.status;
    // this.statusMessage = unchecked.statusMessage;
    // this.body = unchecked.body;
    // this.jsonBody = unchecked.jsonBody;
    // this.base64Body = unchecked.base64Body;
    // this.bodyFileName = unchecked.bodyFileName;
    // this.headers = unchecked.headers;
    // this.additionalProxyRequestHeaders = unchecked.additionalProxyRequestHeaders;
    // this.fixedDelayMilliseconds = unchecked.fixedDelayMilliseconds;
    // this.delayDistribution = unchecked.delayDistribution;
    // this.proxyBaseUrl = unchecked.proxyBaseUrl;
    // this.fault = unchecked.fault;
    // this.transformers = unchecked.transformers;
    // this.transformerParameters = unchecked.transformerParameters;
    // this.fromConfiguredStub = unchecked.fromConfiguredStub;
    //
    // return this;
  }

}
