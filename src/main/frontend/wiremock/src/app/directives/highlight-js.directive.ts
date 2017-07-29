import {AfterViewChecked, Directive, ElementRef, Input, NgZone, OnInit} from '@angular/core';
import * as vkbeautify from 'vkbeautify';

declare const hljs: any;

@Directive({
  selector: '[wm-highlight-js]'
})
export class HighlightJsDirective implements OnInit, AfterViewChecked{

  @Input('wm-highlight-js')
  selector: string;

  constructor(private elementRef: ElementRef, private zone: NgZone) { }

  ngOnInit(): void {
  }

  ngAfterViewChecked(): void {
    const selector = this.selector || 'code';

    if(this.elementRef.nativeElement.innerHTML && this.elementRef.nativeElement.querySelector){
      const codes = this.elementRef.nativeElement.querySelectorAll(selector);
      this.zone.runOutsideAngular(() =>{
        for(const code of codes ){
          const prettyCode = this.prettify(code.innerHTML);
          code.classList.add("hljs");

          try{
            const highlighted = hljs.highlightAuto(prettyCode);

            if(highlighted.language === 'json' || highlighted.language === 'xml' || highlighted.language === 'http'){
              code.innerHTML = highlighted.value;
            }else{
              code.innerHTML = prettyCode;
            }
          }catch(error){
            //we do nothing
            code.innerHTML = prettyCode;
          }
        }
      })
    }
  }

  prettify(code: string): string{
    if (code === null || typeof code === 'undefined') {
      return "";
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
