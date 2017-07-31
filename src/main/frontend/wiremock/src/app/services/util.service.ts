import {Injectable} from '@angular/core';
import {Item} from '../wiremock/model/item';

@Injectable()
export class UtilService {

  private static SOAP_REGEX = null;
  private static SOAP_PATH_REGEX = null;

  constructor() {
  }

  public static getSoapRegex(): RegExp {
    if (UtilService.SOAP_REGEX == null) {
      UtilService.SOAP_REGEX = new RegExp('.*?<([^<>: ]+?:)?Envelope( xmlns:([^<>: ]+?)=([^<> ]+?))?><([^<>: ]+?:)?Body( xmlns:([^<>: ]+?)=([^<> ]+?))?><([^<>: ]+?:)?([^<>: ]+?)( xmlns:([^<>: ]+?)=([^<> ]+))([^>]*)?>');
    }
    return UtilService.SOAP_REGEX;
  }

  public static getSoapXPathRegex(): RegExp {
    if (UtilService.SOAP_PATH_REGEX == null) {
      UtilService.SOAP_PATH_REGEX = new RegExp('/.*?Envelope/.*?Body/([^: ]+:)?(.+)');
    }
    return UtilService.SOAP_PATH_REGEX;
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
    let func:any = UtilService.eachRecursiveRegex;

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

    for(let item of items){
      if(func(item, toSearch)){
        result.push(item);
      }
    }

    return result;
  }

  public static isFunction(obj: any):boolean{
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

}
