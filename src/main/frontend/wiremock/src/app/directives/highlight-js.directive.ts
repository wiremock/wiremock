import {AfterViewChecked, Directive, ElementRef, Input, NgZone} from '@angular/core';
import * as vkbeautify from 'vkbeautify';

declare const hljs: any;


@Directive({
  selector: '[wm-highlight-js]'
})
export class HighlightJsDirective implements  AfterViewChecked{

  @Input('wm-highlight-js')
  private code: string;
  private previousCode: string;

  constructor(private elementRef: ElementRef, private zone: NgZone) { }

  ngAfterViewChecked(): void {
    //We compare the old with new value to prevent loop
    if(this.previousCode != null && this.previousCode === this.code){
      return;
    }
    this.previousCode = this.code;

    this.zone.runOutsideAngular(() => {

      const code = this.elementRef.nativeElement;

      const prettyCode = this.prettify(code.innerHTML);
      code.classList.add('hljs');

      try {
        const highlighted = hljs.highlightAuto(prettyCode);

        if (highlighted.language === 'json' || highlighted.language === 'xml' || highlighted.language === 'http') {
          code.innerHTML = highlighted.value;
        } else {
          code.innerHTML = prettyCode;
        }
      } catch (error) {
        //we do nothing
        code.innerHTML = prettyCode;
      }
    });
  }

  prettify(code: string): string{
    if (code === null || typeof code === 'undefined') {
      return '';
    }
    try {
      return vkbeautify.json(code);
    } catch (err) {
      try{
        return vkbeautify.xml(code);
      }catch(err2){
        return code;
      }
    }
  }

}
