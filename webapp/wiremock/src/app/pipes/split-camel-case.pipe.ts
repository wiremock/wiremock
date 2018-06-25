import {Pipe, PipeTransform} from '@angular/core';
import {UtilService} from '../services/util.service';

@Pipe({
  name: 'splitCamelCase'
})
export class SplitCamelCasePipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if (UtilService.isUndefined(value)) {
      return '';
    }
    // return value.split(/(?=[A-Z])/).join(' ');
    return value.replace(/([a-z](?=[A-Z]))/g, '$1 ');
  }

}
