import { WiremockPage } from './app.po';

describe('wiremock App', () => {
  let page: WiremockPage;

  beforeEach(() => {
    page = new WiremockPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('wm works!');
  });
});
