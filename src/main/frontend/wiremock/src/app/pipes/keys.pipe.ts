import {Pipe, PipeTransform} from '@angular/core';
import {UtilService} from '../services/util.service';

@Pipe({
  name: 'keys'
})
export class KeysPipe implements PipeTransform {

  transform(value: any, args?: any): any {

    const result: KeyValue[] = [];

    if (UtilService.isUndefined(value)) {
      return result;
    }

    for (const key in value) {
      if (value.hasOwnProperty(key) && '_code' !== key) {

        result.push(new KeyValue(key, value[key]));
        // if (args === null || typeof args === 'undefined' || this.checkArgs(args, value[key])) {
        // }
      }
    }
    return result;

    // return Object.keys(value); // .map(key => value[key]);
  }

  // private checkArgs(args: any, value: any) {
  //   return args ? typeof value === 'object' : typeof value !== 'object';
  // }

}

export class KeyValue {
  key: string;
  value: any;


  constructor(key: string, value: any) {
    this.key = key;
    this.value = value;
  }
}
