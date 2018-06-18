import {Injectable} from '@angular/core';
import * as vkbeautify from 'vkbeautify';
// import {Message, MessageService, MessageType} from '../message/message.service';
import {Item} from '../model/wiremock/item';

@Injectable()
export class UtilService {

  public static copyToClipboard(text: string): boolean {
    // https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript

    const textArea = document.createElement('textarea');

    // Place in top-left corner of screen regardless of scroll position.
    textArea.style.position = 'fixed';
    textArea.style.top = '0';
    textArea.style.left = '0';

    // Ensure it has a small width and height. Setting to 1px / 1em
    // doesn't work as this gives a negative w/h on some browsers.
    textArea.style.width = '2em';
    textArea.style.height = '2em';

    // We don't need padding, reducing the size if it does flash render.
    textArea.style.padding = '0';

    // Clean up any borders.
    textArea.style.border = 'none';
    textArea.style.outline = 'none';
    textArea.style.boxShadow = 'none';

    // Avoid flash of white box if rendered for any reason.
    textArea.style.background = 'transparent';


    textArea.value = text;

    document.body.appendChild(textArea);

    textArea.select();

    try {
      const successful = document.execCommand('copy');
      if (successful) {
        return true;
      } else {
        console.error('Was not able to copy. No exception was thrown. Result=' + successful);
      }
    } catch (err) {
      console.error(err);
    }
    document.body.removeChild(textArea);

    return false;
  }

  // public static showErrorMessage(messageService: MessageService, err: any): void {
  //
  //   if (UtilService.isDefined(err._body) && err._body instanceof ProgressEvent) {
  //     if (err.status === 0) {
  //       messageService.setMessage(new Message('status=' + err.status + ': Is Wiremock started?', MessageType.ERROR, 10000));
  //     } else {
  //       messageService.setMessage(new Message(err.statusText + ': status=' + err.status, MessageType.ERROR, 10000));
  //     }
  //   } else {
  //     messageService.setMessage(new Message(err.statusText + ': status=' + err.status + ', message=',
  // MessageType.ERROR, 10000, err._body));
  //   }
  // }

  public static getSoapRecognizeRegex(): RegExp {
    return /<([^/][^<> ]*?):Envelope[^<>]*?>\s*?<([^/][^<> ]*?):Body[^<>]*?>/;
  }

  public static getSoapNamespaceRegex(): RegExp {
    return /xmlns:([^<> ]+?)="([^<> ]+?)"/g;
  }

  public static getSoapMethodRegex(): RegExp {
    return /<[^/][^<> ]*?:Body[^<> ]*?>\s*?<([^/][^<> ]*?):([^<> ]+)[^<>]*?>/;
  }

  public static getSoapXPathRegex(): RegExp {
    return /\/.*?Envelope\/.*?Body\/([^: ]+:)?(.+)/;
  }

  public static isDefined(value: any): boolean {
    return !(value === null || typeof value === 'undefined');
  }

  public static isUndefined(value: any): boolean {
    return !UtilService.isDefined(value);
  }

  public static isBlank(value: string): boolean {
    return (UtilService.isUndefined(value) || value.length === 0);
  }

  public static isNotBlank(value: string): boolean {
    return !this.isBlank(value);
  }

  public static itemModelStringify(item: any): string {
    if (item._code === null || typeof item._code === 'undefined') {
      // we store the code string in the model without stringify it into JSON
      item._code = JSON.stringify(item, function (key, value) {
        if (key === '_code') {
          return undefined;
        } else {
          return value;
        }
      });
    }
    return item._code;
  }

  public static getParametersOfUrl(url: string) {
    if (UtilService.isUndefined(url)) {
      return '';
    }

    const uri_dec = decodeURIComponent(url);

    const paramStart = uri_dec.indexOf('?');

    if (paramStart < 0) {
      return '';
    }

    return UtilService.extractQueryParams(uri_dec.substring(paramStart + 1));
  }

  private static extractQueryParams(queryParams) {
    if (UtilService.isUndefined(queryParams)) {
      return [];
    }

    const decodeQueryParams = decodeURIComponent(queryParams);

    const result = [];

    // This works but typescript is not aware of entries function yet
    // if(isDefined(URLSearchParams)){
    //   const params = new URLSearchParams(decodeQueryParams);
    //
    //   for(const pair of params.entries()){
    //     result.push({key: pair[0], value: pair[1]})
    //   }
    //
    //   return result;
    // }

    const array = decodeQueryParams.split('&');
    let splitKeyValue;
    for (let i = 0; i < array.length; i++) {
      splitKeyValue = array[i].split('=');
      result.push({key: splitKeyValue[0], value: splitKeyValue[1]});
    }

    return result;
  }

  public static deepSearch(items: Item[], search: string, caseSensitive: boolean): Item[] {
    if (UtilService.isBlank(search)) {
      return items;
    }

    let toSearch: any = null;
    let func: any = UtilService.eachRecursiveRegex;

    try {
      if (caseSensitive) {
        toSearch = new RegExp(search);
      } else {
        toSearch = new RegExp(search, 'i');
      }
    } catch (err) {
      toSearch = search;
      func = UtilService.eachRecursive;
    }

    const result: Item[] = [];

    for (const item of items) {
      if (func(item, toSearch)) {
        result.push(item);
      }
    }

    return result;
  }

  public static isFunction(obj: any): boolean {
    return typeof obj === 'function';
  }

  public static eachRecursiveRegex(obj, regex): boolean {
    for (const k of Object.keys(obj)) {
      // hasOwnProperty check not needed. We are iterating over properties of object
      if (typeof obj[k] === 'object' && UtilService.isDefined(obj[k])) {
        if (UtilService.eachRecursiveRegex(obj[k], regex)) {
          return true;
        }
      } else {
        if (!UtilService.isFunction(obj[k])) {
          const toCheck = obj[k] ? '' + obj[k] : '';
          if (toCheck.search(regex) > -1) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static eachRecursive(obj, text): boolean {
    for (const k of Object.keys(obj)) {
      // hasOwnProperty check not needed. We are iterating over properties of object
      if (typeof obj[k] === 'object' && UtilService.isDefined(obj[k])) {
        if (UtilService.eachRecursive(obj[k], text)) {
          return true;
        }
      } else {
        if (!UtilService.isFunction(obj[k])) {
          const toCheck = obj[k] ? '' + obj[k] : '';
          if (toCheck.includes(text)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static prettify(code: string): string {
    if (UtilService.isUndefined(code)) {
      return '';
    }

    try {
      return vkbeautify.json(code);
    } catch (err) {
      try {
        return vkbeautify.xml(code);
      } catch (err2) {
        return code;
      }
    }
  }

  public static toJson(value: any): string {
    if (UtilService.isUndefined(value)) {
      return '';
    } else {
      return JSON.stringify(value);
    }
  }

  public static transient(obj: any, key: string, value: any) {
    if (obj.hasOwnProperty(key)) {
      // move key to transient layer
      delete obj[key];
    }
    if (!obj.__proto__.__transient__) {
      // create transient layer
      obj.__proto__ = {
        '__proto__': obj.__proto__,
        '__tansient__': true
      };
    }
    obj.__proto__[key] = value;
  }

  public static generateUUID(): string { // Public Domain/MIT
    return new UUID().generate();
  }

  constructor() {
  }
}

/* tslint:disable */
export class UUID {
  lut = [];

  constructor() {
    for (let i = 0; i < 256; i++) {
      this.lut[i] = (i < 16 ? '0' : '') + (i).toString(16);
    }
  }

  generate(): string {
    const d0 = Math.random() * 0xffffffff | 0;
    const d1 = Math.random() * 0xffffffff | 0;
    const d2 = Math.random() * 0xffffffff | 0;
    const d3 = Math.random() * 0xffffffff | 0;
    return this.lut[d0 & 0xff] + this.lut[d0 >> 8 & 0xff] + this.lut[d0 >> 16 & 0xff] + this.lut[d0 >> 24 & 0xff] + '-' +
      this.lut[d1 & 0xff] + this.lut[d1 >> 8 & 0xff] + '-' + this.lut[d1 >> 16 & 0x0f | 0x40] + this.lut[d1 >> 24 & 0xff] + '-' +
      this.lut[d2 & 0x3f | 0x80] + this.lut[d2 >> 8 & 0xff] + '-' + this.lut[d2 >> 16 & 0xff] + this.lut[d2 >> 24 & 0xff] +
      this.lut[d3 & 0xff] + this.lut[d3 >> 8 & 0xff] + this.lut[d3 >> 16 & 0xff] + this.lut[d3 >> 24 & 0xff];
  }
}

/* tslint:enable */
