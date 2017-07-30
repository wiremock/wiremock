import {StubMapping} from './stub-mapping';

export class ListStubMappingsResult {
  meta: any;
  mappings: StubMapping[];

  deserialize(unchecked: ListStubMappingsResult): ListStubMappingsResult{
    this.meta = unchecked.meta;
    this.mappings = [];
    unchecked.mappings.forEach(mapping => {
      this.mappings.push(new StubMapping().deserialize(mapping));
    });

    return this;
  }
}
