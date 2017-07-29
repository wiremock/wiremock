import {RequestPattern} from './request-pattern';
import {ResponseDefinition} from './response-definition';

export class StubMapping {
  uuid: string;
  name: string;
  persistent: Boolean;
  request: RequestPattern;
  response: ResponseDefinition;


}
