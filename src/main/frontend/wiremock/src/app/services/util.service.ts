import {Injectable} from '@angular/core';
import {Item} from '../wiremock/model/item';
import * as vkbeautify from 'vkbeautify';
import {Message, MessageService, MessageType} from '../message/message.service';

@Injectable()
export class UtilService {

  private static SOAP_REGEX = null;
  private static SOAP_PATH_REGEX = null;
  private static SOAP_RECOGNIZE_REGEX = null;
  private static SOAP_NAMESPACE_REGEX = null;
  private static SOAP_METHOD_REGEX = null;

  constructor() {
  }

  public static showErrorMessage(messageService: MessageService, err: any): void{
    messageService.setMessage(new Message(err.statusText + ": status=" + err.status + ", message=", MessageType.ERROR, 10000, err._body));
  }

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
    return new RegExp('/\s*?Envelope/\s*?Body/([^: ]+:)?(\s+)');
  }

  public static isDefined(value: any): boolean {
    return !(value === null || typeof value === 'undefined');
  }

  public static isUndefined(value: any): boolean {
    return !UtilService.isDefined(value);
  }

  public static isBlank(value: string): boolean {
    return (value === null || typeof value === 'undefined' || value.length == 0);
  }

  public static isNotBlank(value: string): boolean {
    return !this.isBlank(value);
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

    //This works but typescript is not aware of entries function yet
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
      result.push({key: splitKeyValue[0], value: splitKeyValue[1]})
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

    for (let item of items) {
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
    for (let k in obj) {
      if (typeof obj[k] === 'object' && obj[k] !== null) {
        if (UtilService.eachRecursiveRegex(obj[k], regex)) {
          return true;
        }
      }
      else {
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
    for (let k in obj) {
      if (typeof obj[k] === 'object' && obj[k] !== null) {
        if (UtilService.eachRecursive(obj[k], text)) {
          return true;
        }
      }
      else {
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
    if (code === null || typeof code === 'undefined') {
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


  public static generateUUID(): string { // Public Domain/MIT
    // let d = new Date().getTime();
    // if (typeof performance !== 'undefined' && typeof performance.now === 'function'){
    //   d += performance.now(); //use high-precision timer if available
    // }
    // return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    //   const r = (d + Math.random() * 16) % 16 | 0;
    //   d = Math.floor(d / 16);
    //   return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    // });
    return new UUID().generate();
  }

}


  export class UUID{
    lut = [];

    constructor(){
      for (let i=0; i<256; i++) {
        this.lut[i] = (i<16?'0':'')+(i).toString(16);
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
