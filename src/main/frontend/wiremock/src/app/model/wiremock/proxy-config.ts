export class ProxyConfig {
  proxyConfig: Map<string, string>;

  deserialze(proxyData: ProxyConfig): ProxyConfig {
    this.proxyConfig = new Map<string, string>();
    for (const key in proxyData.proxyConfig) {
      if (proxyData.proxyConfig.hasOwnProperty(key)) {
        this.proxyConfig.set(key, proxyData.proxyConfig[key]);
      }
    }
    return this;
  }
}
