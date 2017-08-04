export class LoggedResponse {

  status: number;
  headers:any;
  body: string;
  fault: any;

  deserialize(unchecked: LoggedResponse): LoggedResponse{
    return unchecked;
  }
}
