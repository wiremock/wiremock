import { IsNoObjectPipe } from './is-no-object.pipe';

describe('IsNoObjectPipe', () => {
  it('create an instance', () => {
    const pipe = new IsNoObjectPipe();
    expect(pipe).toBeTruthy();
  });
});
