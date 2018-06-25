export class Proxy {
  private _isProxy = false;
  private _isProxyEnabled = true;

  public constructor() {
  }

  isProxy(): boolean {
    return this._isProxy;
  }

  setProxy(value: boolean) {
    this._isProxy = value;
  }

  isProxyEnabled(): boolean {
    return this._isProxyEnabled;
  }

  setProxyEnabled(value: boolean) {
    this._isProxyEnabled = value;
  }
}
