import {Item} from '../wiremock/item';
import {UtilService} from '../../services/util.service';

export class Root implements Item {

  constructor(private id = UtilService.generateUUID()) {

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

  getFolderName(): string | undefined {
    return undefined;
  }

  hasFolderDefinition(): boolean {
    return false;
  }
}
