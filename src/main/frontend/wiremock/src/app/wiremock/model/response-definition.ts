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
  proxyBaseUrl: string;
  fault: any;
  transformers: any;
  transformerParameters: any;
  fromConfiguredStub: Boolean;

}
