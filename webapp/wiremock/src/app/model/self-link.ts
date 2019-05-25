import {StateLink} from "./state-link";
import {StubMapping} from "./wiremock/stub-mapping";

export class SelfLink extends StateLink{

  constructor(source: string, target: string, mapping: StubMapping) {
    super(source, target, mapping);
  }

}
