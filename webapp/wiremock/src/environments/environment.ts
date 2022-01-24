// This file can be replaced during build by using the `fileReplacements` array.
// `ng build ---prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: true,
  getWebSocket: function (): WebSocket {
    return new WebSocket('ws://localhost:8089/__admin/events');
  },
  wiremockUrl: '/',
  url: '/__admin/',
  resourcesUrl: '/__admin/webapp/'
};


// export const environment = {
//   production: false,
//   getWebSocket: function (): WebSocket {
//     return new WebSocket('ws://localhost:8089/__admin/events');
//   },
//   wiremockUrl: 'http://localhost:8089/',
//   url: 'http://localhost:8089/__admin/',
//   resourcesUrl: '/'
// };

/*
 * In development mode, to ignore zone related error stack frames such as
 * `zone.run`, `zoneDelegate.invokeTask` for easier debugging, you can
 * import the following file, but please comment it out in production mode
 * because it will have performance impact when throw error
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
