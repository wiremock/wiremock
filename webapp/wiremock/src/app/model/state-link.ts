import {StubMapping} from "./wiremock/stub-mapping";

export class StateLink {
  private readonly _source: string;
  private readonly _target: string;
  private readonly _mapping: StubMapping;

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
}
