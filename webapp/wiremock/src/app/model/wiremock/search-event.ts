export class SearchEvent {
  text: string;
  caseSensitive: boolean;

  constructor(text: string, caseSensitive: boolean) {
    this.text = text;
    this.caseSensitive = caseSensitive;
  }
}
