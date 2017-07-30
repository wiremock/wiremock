import {RequestPattern} from './request-pattern';
import {ResponseDefinition} from './response-definition';
import {Item} from '../../list/list.component';

export class StubMapping extends Item{
  uuid: string;
  name: string;
  persistent: Boolean;
  request: RequestPattern;
  response: ResponseDefinition;
  priority: number;
  scenarioName: string;
  requiredScenarioState: string;
  newScenarioState: string;

  deserialize(unchecked: StubMapping): StubMapping{
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
    return "method=" + this.request.method + ", status=" + this.response.status;
  }

}
