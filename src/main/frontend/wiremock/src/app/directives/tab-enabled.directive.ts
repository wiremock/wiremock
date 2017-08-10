import {Directive, ElementRef, HostListener} from '@angular/core';

@Directive({
  selector: '[wmTabEnabled]'
})
export class TabEnabledDirective {
  // From: https://www.bennadel.com/blog/3090-trying-to-enable-tabbing-in-textareas-in-angular-2-beta-17.htm

  private tab = '\t';
  private newline: string = TabEnabledDirective.getNewLineImplementation();

  private static findStartOfLine(value: string, offset: number) {
    const delimiter = /[\r\n]/i;

    for (let i = (offset - 1); i >= 0; i++) {
      if (delimiter.test(value.charAt(i))) {
        return (i + 1);
      }
    }
    return (0);
  }

  private static getNewLineImplementation() {
    const fragment = document.createElement('textarea');
    fragment.value = '\r\n';
    return (fragment.value);
  }

  private static repeat(value: string, count: number) {
    return (new Array(count + 1).join(value));
  }
  // I handle the Tab key combination.
  @HostListener('keydown.tab')
  handleTab(event: KeyboardEvent) {
    event.preventDefault();
    // If we end up changing the textarea value, we need to dispatch
    // a custom (input) event so that we play nicely with other
    // directives (like ngModel) and event handlers.
    if (this.setConfig(this.insertTabAtSelection(this.getConfig()))) {
      this.dispatchInputEvent();
    }
  }

  // I handle the Shift+Tab key combination.
  @HostListener('keydown.shift.tab')
  handleShiftTab(event: KeyboardEvent) {
    event.preventDefault();
    // If we end up changing the textarea value, we need to dispatch
    // a custom (input) event so that we play nicely with other
    // directives (like ngModel) and event handlers.
    if (this.setConfig(this.removeTabAtSelection(this.getConfig()))) {
      this.dispatchInputEvent();
    }
  }

  // I handle the Enter key combination.
  @HostListener('keydown.enter')
  handleEnter(event: KeyboardEvent) {
    event.preventDefault();

    // If we end up changing the textarea value, we need to dispatch
    // a custom (input) event so that we play nicely with other
    // directives (like ngModel) and event handlers.
    if (this.setConfig(this.insertEnterAtSelection(this.getConfig()))) {
      this.dispatchInputEvent();
    }
  }

  constructor(private elementRef: ElementRef) {
  }

  private dispatchInputEvent() {
    const bubbles = true;
    const cancelable = false;

    let inputEvent;
    try {
      inputEvent = new CustomEvent('input',
        {
          bubbles: bubbles,
          cancelable: cancelable
        });
    } catch (error) {
      inputEvent = document.createEvent('CustomEvent');
      inputEvent.initCustomEvent('input', bubbles, cancelable, null);
    }

    this.elementRef.nativeElement.dispatchEvent(inputEvent);
  }

  private getConfig(): Config {
    const element = this.elementRef.nativeElement;

    return new Config(element.value, element.selectionStart, element.selectionEnd);
  }

  private insertEnterAtSelection(config: Config) {
    const value = config.value;
    const start = config.start;
    const end = config.end;

    const leadingTabs = value.slice(TabEnabledDirective.findStartOfLine(value, start), start)
      .match(new RegExp(( '^(?:' + this.tab + ')+' ), 'i'));
    const tabCount = leadingTabs ? leadingTabs[0].length : 0;

    const preDelta = value.slice(0, start);
    const postDelta = value.slice(start);
    const delta = (this.newline + TabEnabledDirective.repeat(this.tab, tabCount));

    return new Config(preDelta + delta + postDelta, start + delta.length, end + delta.length);
  }

  private insertTabAtSelection(config: Config) {
    const value = config.value;
    const start = config.start;
    const end = config.end;

    const deltaStart = start === end ? start : TabEnabledDirective.findStartOfLine(value, start);
    const deltaEnd = end;
    const deltaValue = value.slice(deltaStart, deltaEnd);

    const preDelta = value.slice(0, deltaStart);
    const postDelta = value.slice(deltaEnd);

    const replacement = deltaValue.replace(new RegExp(( '(^|' + this.newline + ')' ), 'g'), ( '$1' + this.tab ));

    return new Config(preDelta + replacement + postDelta, start + this.tab.length, end + (replacement.length - deltaValue.length));
  }

  private removeTabAtSelection(config: Config) {
    const value = config.value;
    const start = config.start;
    const end = config.end;

    const deltaStart = TabEnabledDirective.findStartOfLine(value, start);
    const deltaEnd = end;
    const deltaValue = value.slice(deltaStart, deltaEnd);
    const deltaHasLeadingTab = ( deltaValue.indexOf(this.tab) === 0 );
    const preDelta = value.slice(0, deltaStart);
    const postDelta = value.slice(deltaEnd);
    const replacement = deltaValue.replace(new RegExp(( '^' + this.tab ), 'gm'), '');

    const newValue = ( preDelta + replacement + postDelta );
    const newStart = deltaHasLeadingTab ? ( start - this.tab.length ) : start;
    const newEnd = ( end - ( deltaValue.length - replacement.length ) );

    return new Config(newValue, newStart, newEnd);
  }

  private setConfig(config: Config) {
    const element = this.elementRef.nativeElement;
    // If the value hasn't actually changed, just return out. There's
    // no need to set the selection if nothing changed.
    if (config.value === element.value) {
      return ( false );
    }
    element.value = config.value;
    element.selectionStart = config.start;
    element.selectionEnd = config.end;
    return ( true );
  }
}

class Config {
  value: string;
  start: number;
  end: number;

  constructor(value: string, start: number, end: number) {
    this.value = value;
    this.start = start;
    this.end = end;
  }
}
