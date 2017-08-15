import {RequestPattern} from './request-pattern';
import {ResponseDefinition} from './response-definition';
import {Item} from './item';
import {UtilService} from '../../services/util.service';

export class StubMapping implements Item {

  uuid: string;
  name: string;
  persistent: Boolean;
  request: RequestPattern;
  response: ResponseDefinition;
  priority: number;
  scenarioName: string;
  requiredScenarioState: string;
  newScenarioState: string;

  deserialize(unchecked: StubMapping): StubMapping {
    this.uuid = unchecked.uuid;
    this.name = unchecked.name;
    this.persistent = unchecked.persistent;
    this.request = new RequestPattern().deserialize(unchecked.request);
    this.response = new ResponseDefinition().deserialize(unchecked.response);
    this.priority = unchecked.priority;
    this.scenarioName = unchecked.scenarioName;
    this.requiredScenarioState = unchecked.requiredScenarioState;
    this.newScenarioState = unchecked.newScenarioState;

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
    if(soapResult.length > 0){
      result = soapResult;
    }else{
      result = 'method=' + this.request.method;
    }
    return result + ', status=' + this.response.status;
  }

  getId(): string {
    return this.uuid;
  }
}
