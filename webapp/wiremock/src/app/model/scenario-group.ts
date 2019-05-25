import {Item} from "./wiremock/item";
import {StubMapping} from "./wiremock/stub-mapping";
import {UtilService} from "../services/util.service";

export class ScenarioGroup implements Item {

  private readonly _stateNames;

  constructor(private _mappings: StubMapping[]) {
    this._stateNames = new Set();

    this._mappings.forEach(mapping => {
      if (UtilService.isDefined(mapping.requiredScenarioState)) {
        this._stateNames.add(mapping.requiredScenarioState);
      }
      if (UtilService.isDefined(mapping.newScenarioState)) {
        this._stateNames.add(mapping.newScenarioState);
      }
    });
  }


  get stateNames() {
    return this._stateNames;
  }

  get mappings(): StubMapping[] {
    return this._mappings;
  }

  getCode(): string {
    return this._mappings[0].getCode();
  }

  getId(): string {
    return this._mappings[0].getId();
  }

  getSubtitle(): string {
    return this._mappings.length + ' mappings, ' + this._stateNames.size + ' states';
  }

  getTitle(): string {
    return this._mappings[0].scenarioName;
  }

  isProxy(): boolean {
    return false;
  }

  isProxyEnabled(): boolean {
    return false;
  }

}
