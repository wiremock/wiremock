export interface Item {
  getTitle(): string;

  getSubtitle(): string;

  getId(): string;

  getCode(): string;

  isProxy(): boolean;

  isProxyEnabled(): boolean;

  hasFolderDefinition(): boolean;

  getFolderName(): string | undefined;
}
