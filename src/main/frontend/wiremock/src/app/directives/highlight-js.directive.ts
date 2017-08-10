import {AfterViewChecked, Directive, ElementRef, Input, NgZone} from '@angular/core';
import {UtilService} from '../services/util.service';

declare const hljs: any;


@Directive({
  selector: '[wmHighlightJs]'
})
export class HighlightJsDirective implements AfterViewChecked {

  @Input()
  wmHighlightJs: string;
  prevWmHighlightJs: string;

  @Input()
  language: string;

  constructor(private elementRef: ElementRef, private zone: NgZone) {
  }

  isLangAvailable() {
    return typeof this.language !== 'undefined' && this.language != null && this.language.length > 0;
  }

  ngAfterViewChecked(): void {
    this.highlight();
  }

  private highlight(): void {
    // We compare the old with new value to prevent loop
    if (this.wmHighlightJs === this.prevWmHighlightJs) {
      return;
    }

    if (this.language === 'plain') {
      const code = this.elementRef.nativeElement;
      code.classList.add('hljs');
      this.prevWmHighlightJs = this.wmHighlightJs;
      code.innerHTML = this.wmHighlightJs;
      return;
    }

    if (UtilService.isBlank(this.wmHighlightJs)) {
      const code = this.elementRef.nativeElement;
      code.classList.add('hljs');
      this.prevWmHighlightJs = this.wmHighlightJs;
      return;
    }

    this.prevWmHighlightJs = this.wmHighlightJs;

    this.zone.runOutsideAngular(() => {
      const code = this.elementRef.nativeElement;

      // const prettyCode = this.prettify(this.code);
      const prettyCode = UtilService.prettify(this.wmHighlightJs);
      code.classList.add('hljs');

      try {

        const highlighted = hljs.highlightAuto(prettyCode, this.isLangAvailable() ? [this.language] : ['html', 'json', 'xml', 'http']);

        if (highlighted.language === 'json' || highlighted.language === 'xml' ||
            highlighted.language === 'http' || highlighted.language === 'html') {
          code.innerHTML = highlighted.value;
        } else {
          code.innerHTML = prettyCode;
        }
      } catch (error) {
        // we do nothing
        code.innerHTML = prettyCode;
      }
    });
  }
}
