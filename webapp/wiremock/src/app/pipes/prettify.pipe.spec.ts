import { PrettifyPipe } from './prettify.pipe';

describe('PrettifyPipe', () => {
  it('create an instance', () => {
    const pipe = new PrettifyPipe();
    expect(pipe).toBeTruthy();
  });
});
