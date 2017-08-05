import {AfterViewChecked, Directive, ElementRef, Input, NgZone} from '@angular/core';
import * as vkbeautify from 'vkbeautify';
import {UtilService} from '../services/util.service';

declare const hljs: any;


@Directive({
  selector: '[wm-highlight-js]'
})
export class HighlightJsDirective implements  AfterViewChecked{


  @Input('wm-highlight-js')
  code: string;

  @Input('language')
  language: string;

  prevCode: string;


  constructor(private elementRef: ElementRef, private zone: NgZone) { }

  isLangAvailable() {
    return typeof this.language !== 'undefined' && this.language != null && this.language.length > 0;
  }

  ngAfterViewChecked(): void {
    this.highlight();
  }

  private highlight():void{
    //We compare the old with new value to prevent loop
    if(this.code == this.prevCode){
      return;
    }

    if(this.language === 'plain'){
      const code = this.elementRef.nativeElement;
      code.classList.add('hljs');
      this.prevCode = this.code;
      code.innerHTML = this.code;
      return;
    }

    if(UtilService.isBlank(this.code)){
      const code = this.elementRef.nativeElement;
      code.classList.add('hljs');
      this.prevCode = this.code;
      return;
    }

    this.prevCode = this.code;

    this.zone.runOutsideAngular(() => {
      const code = this.elementRef.nativeElement;

      const prettyCode = this.prettify(this.code);
      code.classList.add('hljs');

      try {

        const highlighted = hljs.highlightAuto(prettyCode, this.isLangAvailable() ? [this.language]:['html', 'json', 'xml', 'http']);

        if (highlighted.language === 'json' || highlighted.language === 'xml' || highlighted.language === 'http' || highlighted.language === 'html') {
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
