import {RequestPattern} from './request-pattern';
import {ResponseDefinition} from './response-definition';
import {Item} from './item';
import {UtilService} from '../../services/util.service';
import {Proxy} from './proxy';
import {ProxyConfig} from './proxy-config';

export class StubMapping extends Proxy implements Item {

  uuid: string;
  name: string;
  persistent: boolean;
  request: RequestPattern;
  response: ResponseDefinition;
  priority: number;
  scenarioName: string;
  requiredScenarioState: string;
  newScenarioState: string;

  postServeActions: Map<String, any>;

  metadata: any;

  constructor() {
    super();
  }

  static createEmpty(): StubMapping {
    const mapping: StubMapping = new StubMapping();

    mapping.request = new RequestPattern();
    mapping.request.method = 'POST';
    mapping.request.url = '';

    mapping.response = new ResponseDefinition();
    mapping.response.status = 200;
    mapping.response.body = '';
    mapping.response.headers = {
      'Content-Type': 'text/plain'
    };

    return mapping;
  }

  deserialize(unchecked: StubMapping, proxyConfig: ProxyConfig): StubMapping {
    this.uuid = unchecked.uuid;
    this.name = unchecked.name;
    this.persistent = unchecked.persistent;
    this.request = new RequestPattern().deserialize(unchecked.request);
    this.response = new ResponseDefinition().deserialize(unchecked.response);
    this.priority = unchecked.priority;
    this.scenarioName = unchecked.scenarioName;
    this.requiredScenarioState = unchecked.requiredScenarioState;
    this.newScenarioState = unchecked.newScenarioState;
    this.metadata = unchecked.metadata;
    this.postServeActions = unchecked.postServeActions;

    if (UtilService.isDefined(proxyConfig) && (UtilService.isDefined(this.response.proxyBaseUrl) || proxyConfig.proxyConfig.has(this.uuid))) {
      this.setProxy(true);
      this.setProxyEnabled(!proxyConfig.proxyConfig.has(this.uuid));
    }

    return this;
  }

  getTitle(): string {
    return this.request.url || this.request.urlPattern || this.request.urlPath || this.request.urlPathPattern;
  }

  getSubtitle(): string {
    let soap;
    let soapResult = '';
    if (UtilService.isDefined(this.request) && UtilService.isDefined(this.request.bodyPatterns) &&
      UtilService.isDefined(this.request.bodyPatterns)) {


      for (const bodyPattern of this.request.bodyPatterns) {
        if (UtilService.isDefined(bodyPattern.matchesXPath) &&
          UtilService.isDefined(soap = UtilService.getSoapXPathRegex().exec(bodyPattern.matchesXPath))) {
          if (soapResult.length !== 0) {
            soapResult += ', ';
          }
          soapResult += soap[2];
        }
      }
    }
    let result = '';
    if (soapResult.length > 0) {
      result = soapResult;
    } else {
      result = 'method=' + this.request.method;
    }
    return result + ', status=' + this.response.status;
  }

  getId(): string {
    return this.uuid;
  }

  getCode(): string {
    return UtilService.itemModelStringify(this);
  }

  hasFolderDefinition(): boolean {
    return UtilService.isDefined(this.getFolderName());
  }

  getFolderName(): string | undefined {
    if (UtilService.isFolderDefined(this)) {
      return this.metadata[UtilService.WIREMOCK_GUI_KEY][UtilService.DIR_KEY];
    }
    return undefined;
  }
}
