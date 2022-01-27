import {Scenario} from './scenario';
import {ProxyConfig} from './proxy-config';

export class ScenarioResult {
  private _scenarios: Scenario[];

  get scenarios(): Scenario[] {
    return this._scenarios;
  }

  deserialize(unchecked: ScenarioResult, proxyConfig: ProxyConfig): ScenarioResult {
    this._scenarios = [];
    unchecked.scenarios.forEach(uncheckedScenario => {
      this._scenarios.push(new Scenario().deserialize(uncheckedScenario, proxyConfig));
    });

    return this;
  }
}
