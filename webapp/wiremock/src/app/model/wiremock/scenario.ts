import {StubMapping} from './stub-mapping';
import {Item} from './item';
import {ProxyConfig} from './proxy-config';

export class Scenario implements Item {
  private _id: string;
  private _name: string;
  private _state: string;
  private _mappings: StubMapping[];
  private _possibleStates: string[];

  get id(): string {
    return this._id;
  }

  get name(): string {
    return this._name;
  }

  get state(): string {
    return this._state;
  }

  get mappings(): StubMapping[] {
    return this._mappings;
  }

  get possibleStates(): string[] {
    return this._possibleStates;
  }

  hasGroup(): boolean {
    return false;
  }

  getGroup(): string | undefined {
    return undefined;
  }

  public deserialize(unchecked: Scenario, proxyConfig: ProxyConfig): Scenario {
    this._id = unchecked.id;
    this._name = unchecked.name;
    this._state = unchecked.state;
    this._possibleStates = unchecked.possibleStates;

    this._mappings = [];
    unchecked.mappings.forEach(uncheckedMapping => {
      this._mappings.push(new StubMapping().deserialize(uncheckedMapping, proxyConfig));
    });

    return this;
  }

  getCode(): string {
    return this._mappings[0].getCode();
  }

  getId(): string {
    return this.mappings[0].getId();
  }

  getSubtitle(): string {
    return this._mappings.length + ' mappings, ' + this._possibleStates.length + 'states';
  }

  getTitle(): string {
    return this.name;
  }

  isProxy(): boolean {
    return false;
  }

  isProxyEnabled(): boolean {
    return false;
  }
}
