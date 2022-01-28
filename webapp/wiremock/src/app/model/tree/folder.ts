import {Item} from '../wiremock/item';

export class Folder implements Item {

  constructor(private id: string, private name: string) {
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
    return this.name;
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
