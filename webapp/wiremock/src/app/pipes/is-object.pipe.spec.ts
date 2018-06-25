import { IsObjectPipe } from './is-object.pipe';

describe('IsObjectPipe', () => {
  it('create an instance', () => {
    const pipe = new IsObjectPipe();
    expect(pipe).toBeTruthy();
  });
});
