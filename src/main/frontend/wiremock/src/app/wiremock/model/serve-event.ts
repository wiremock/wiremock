import {StubMapping} from './stub-mapping';
import {ResponseDefinition} from './response-definition';
import {Item} from './item';
import {LoggedRequest} from './logged-request';
import {LoggedResponse} from './logged-response';

export class ServeEvent  implements Item{
  getTitle(): string {
    return this.request.url;
  }

  getSubtitle(): string {
    return "method=" + this.request.method + ", status=" + this.response.status;
  }

  getId(): string {
    return this.id;
  }


  id: string;
  request: LoggedRequest;
  mapping: StubMapping;
  responseDefinition: ResponseDefinition;
  response: LoggedResponse;
  wasMatched: boolean;

  deserialize(unchecked: ServeEvent): ServeEvent{
    this.id = unchecked.id;
    this.request = unchecked.request;
    this.mapping = unchecked.mapping;
    this.responseDefinition = unchecked.responseDefinition;
    this.response = unchecked.response;
    this.wasMatched = unchecked.wasMatched;

    return this;
  }
}
