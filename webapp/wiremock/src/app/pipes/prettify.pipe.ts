import {Pipe, PipeTransform} from '@angular/core';
import {UtilService} from '../services/util.service';

@Pipe({
  name: 'prettify'
})
export class PrettifyPipe implements PipeTransform {

  transform(value: string, args?: any): any {
    return UtilService.prettify(value) + '';
  }

}
