import {Item} from '../wiremock/item';
import {v4 as uuidv4} from 'uuid';

export class Root implements Item {

  constructor(private id = uuidv4()) {

  }

  getCode(): string {
    return '';
  }

  getId(): string {
    return this.id;
  }

  getSubtitle(): string {
    return '';
  }

  getTitle(): string {
    return '<Root>';
  }

  isProxy(): boolean {
    return false;
  }

  isProxyEnabled(): boolean {
    return false;
  }

  getGroup(): string | undefined {
    return undefined;
  }

  hasGroup(): boolean {
    return false;
  }
}
