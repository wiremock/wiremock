export const environment = {
  production: true,
  getWebSocket: function (): WebSocket {
    const loc = window.location;
    let new_uri;
    if (loc.protocol === 'https:') {
      new_uri = 'wss:';
    } else {
      new_uri = 'ws:';
    }
    new_uri += '//' + loc.host;
    new_uri += '/__admin/events';

    return new WebSocket(new_uri);
  },
  wiremockUrl: '/',
  url: '/__admin/',
  resourcesUrl: '/__admin/webapp/'
};
