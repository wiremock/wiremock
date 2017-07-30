import { Injectable } from '@angular/core';

@Injectable()
export class UtilService {

  private static SOAP_REGEX = null;
  private static SOAP_PATH_REGEX = null;

  constructor() { }

  public static getSoapRegex(): RegExp{
    if(UtilService.SOAP_REGEX == null){
      UtilService.SOAP_REGEX = new RegExp('.*?<([^<>: ]+?:)?Envelope( xmlns:([^<>: ]+?)=([^<> ]+?))?><([^<>: ]+?:)?Body( xmlns:([^<>: ]+?)=([^<> ]+?))?><([^<>: ]+?:)?([^<>: ]+?)( xmlns:([^<>: ]+?)=([^<> ]+))([^>]*)?>');
    }
    return UtilService.SOAP_REGEX;
  }

  public static getSoapXPathRegex(): RegExp{
    if(UtilService.SOAP_PATH_REGEX == null){
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

  public static getParametersOfUrl(url: string){
    if(UtilService.isUndefined(url)){
      return "";
    }

    const uri_dec = decodeURIComponent(url);

    const paramStart = uri_dec.indexOf('?');

    if(paramStart < 0){
      return "";
    }

    return UtilService.extractQueryParams(uri_dec.substring(paramStart + 1));
  }

  private static extractQueryParams(queryParams){
    if(UtilService.isUndefined(queryParams)){
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

    const array = decodeQueryParams.split("&");
    let splitKeyValue;
    for(let i = 0; i < array.length; i++){
      splitKeyValue = array[i].split("=");
      result.push({key: splitKeyValue[0], value: splitKeyValue[1]})
    }

    return result;
  }

}
