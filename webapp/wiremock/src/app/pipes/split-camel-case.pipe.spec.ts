import { SplitCamelCasePipe } from './split-camel-case.pipe';

describe('SplitCamelCasePipe', () => {
  it('create an instance', () => {
    const pipe = new SplitCamelCasePipe();
    expect(pipe).toBeTruthy();
  });
});
