import {StubMapping} from './wiremock/stub-mapping';
import {dia} from 'jointjs';
import Link = dia.Link;

export class StateLink {
  private readonly _source: string;
  private readonly _target: string;
  private readonly _mapping: StubMapping;

  private _link: Link;

  constructor(source: string, target: string, mapping: StubMapping) {
    this._source = source;
    this._target = target;
    this._mapping = mapping;
  }

  get source(): string {
    return this._source;
  }

  get target(): string {
    return this._target;
  }

  get mapping(): StubMapping {
    return this._mapping;
  }


  get link(): Link {
    return this._link;
  }

  set link(value: Link) {
    this._link = value;
  }
}
