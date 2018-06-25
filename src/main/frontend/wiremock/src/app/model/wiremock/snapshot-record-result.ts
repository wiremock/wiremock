import {StubMapping} from './stub-mapping';
import {UtilService} from '../../services/util.service';

export class SnapshotRecordResult {
  mappings: StubMapping[];
  ids: string[];

  deserialize(unchecked: SnapshotRecordResult): SnapshotRecordResult {
    if (UtilService.isDefined(unchecked.mappings)) {
      this.mappings = [];
      for (const mapping of unchecked.mappings) {
        this.mappings.push(new StubMapping().deserialize(mapping, null));
      }
    }

    if (UtilService.isDefined(unchecked.ids)) {
      this.ids = unchecked.ids;
    }
    return this;
  }


  getIds(): string[] {
    if (UtilService.isDefined(this.ids)) {
      return this.ids;
    }

    const result: string[] = [];

    if (UtilService.isDefined(this.mappings)) {
      for (const mapping of this.mappings) {
        result.push(mapping.uuid);
      }
    }

    return result;
  }

}
