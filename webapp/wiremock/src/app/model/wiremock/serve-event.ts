import {StubMapping} from './stub-mapping';
import {ResponseDefinition} from './response-definition';
import {Item} from './item';
import {LoggedRequest} from './logged-request';
import {LoggedResponse} from './logged-response';
import {UtilService} from '../../services/util.service';
import {Proxy} from './proxy';

export class ServeEvent extends Proxy implements Item {
  id: string;
  request: LoggedRequest;
  stubMapping: StubMapping;
  responseDefinition: ResponseDefinition;
  response: LoggedResponse;
  wasMatched: boolean;

  constructor() {
    super();
  }

  getTitle(): string {
    return this.request.url;
  }

  getSubtitle(): string {
    return this.request.getSubtitle() + ', status=' + this.response.status;
  }

  getId(): string {
    return this.id;
  }

  getCode(): string {
    return UtilService.itemModelStringify(this);
  }

  hasFolderDefinition(): boolean {
    return false;
  }

  getFolderName(): string | undefined {
    return undefined;
  }

  deserialize(unchecked: ServeEvent): ServeEvent {
    this.id = unchecked.id;
    this.request = new LoggedRequest().deserialize(unchecked.request);
    this.stubMapping = new StubMapping().deserialize(unchecked.stubMapping, null);
    this.responseDefinition = unchecked.responseDefinition;
    this.response = unchecked.response;
    this.wasMatched = unchecked.wasMatched;

    // We do not want proxy feature for served events
    // if (UtilService.isDefined(this.responseDefinition.proxyBaseUrl)) {
    //   this.setProxy(true);
    // }

    return this;
  }
}
