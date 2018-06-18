export class ListItem {

  private _toggleActive = false;
  private _active = false;

  constructor(private _title: string, private _subtitle: string,
              private _hasToggle: boolean) {
  }


  get title(): string {
    return this._title;
  }

  set title(value: string) {
    this._title = value;
  }

  get subtitle(): string {
    return this._subtitle;
  }

  set subtitle(value: string) {
    this._subtitle = value;
  }

  get hasToggle(): boolean {
    return this._hasToggle;
  }

  set hasToggle(value: boolean) {
    this._hasToggle = value;
  }

  get toggleActive(): boolean {
    return this._toggleActive;
  }

  set toggleActive(value: boolean) {
    this._toggleActive = value;
  }

  get active(): boolean {
    return this._active;
  }

  set active(value: boolean) {
    this._active = value;
  }
}
