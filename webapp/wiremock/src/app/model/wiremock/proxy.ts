export class Proxy {
  private _isProxy = false;
  private _isProxyEnabled = true;

  public constructor() {
    Object.defineProperty(this, '_isProxy', {
      enumerable: false,
      writable: true
    });
    Object.defineProperty(this, '_isProxyEnabled', {
      enumerable: false,
      writable: true
    });
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
