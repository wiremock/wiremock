export class LoggedResponse {

  status: number;
  headers: any;
  body: string;
  bodyAsBase64: string;
  fault: any;

  deserialize(unchecked: LoggedResponse): LoggedResponse {
    return unchecked;
  }
}
