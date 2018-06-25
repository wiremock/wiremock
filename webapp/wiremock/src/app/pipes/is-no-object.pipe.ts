import {Pipe, PipeTransform} from '@angular/core';
import {KeyValue} from './keys.pipe';
import {UtilService} from '../services/util.service';

@Pipe({
  name: 'isNoObject'
})
export class IsNoObjectPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    const result: KeyValue[] = [];

    if (UtilService.isUndefined(value)) {
      return result;
    }

    for (let i = 0; i < value.length; i++) {
      if (typeof value[i].value !== 'object') {
        result.push(value[i]);
      }
    }
    return result;
  }

}
