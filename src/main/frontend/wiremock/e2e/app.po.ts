import { browser, element, by } from 'protractor';

export class WiremockPage {
  navigateTo() {
    return browser.get('/');
  }

  getParagraphText() {
    return element(by.css('wm-root h1')).getText();
  }
}
